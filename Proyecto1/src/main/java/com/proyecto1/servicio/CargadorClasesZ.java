package com.proyecto1.servicio;

import com.proyecto1.GramaticaZ;
import com.proyecto1.LenguajeLexer;
import com.proyecto1.semantico.AnalizadorSemanticoZ;
import com.proyecto1.semantico.ast.z.Clase;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.AmbitoGlobal;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.z.ASTBuilderZ;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pre-carga en un {@link AmbitoGlobal} compartido las clases HERMANAS de un .z: los demás
 * archivos .z del mismo proyecto. A diferencia de Pig Latin, Zetariano NO tiene {@code import}
 * -- dos clases del mismo proyecto se ven entre sí automáticamente, igual que dos clases Java
 * del mismo paquete -- así que esta carga es AUTOMÁTICA (se dispara siempre que hay un
 * {@code raizProyecto}), no depende de que el .z declare nada.
 *
 * <h3>Por qué en DOS rondas</h3>
 * Sobre TODAS las hermanas encontradas:
 * <ol>
 *   <li><b>Ronda 1</b> ({@link AnalizadorSemanticoZ#registrarFirma}): se registra el NOMBRE
 *       (bare, sin miembros) de CADA clase hermana en el {@link AmbitoGlobal} compartido.</li>
 *   <li><b>Ronda 2</b> ({@link AnalizadorSemanticoZ#registrarMiembros}): con TODOS los nombres
 *       ya presentes, se registran los atributos/métodos/constructores (solo sus FIRMAS) de
 *       cada una.</li>
 * </ol>
 * Hacerlo en dos rondas SEPARADAS (no una sola pasada por archivo) es justo lo que permite
 * referencias MUTUAS/circulares entre hermanas -- el caso típico de una lista enlazada, donde
 * {@code Nodo} tiene un campo {@code Nodo siguiente} (se referencia a SÍ MISMA) y {@code Pila}
 * tiene un campo {@code Nodo tope} (referencia cruzada): si se resolviera el tipo de un campo
 * en la misma pasada en que se registra su clase, el orden en que se escanearon los archivos
 * importaría y una referencia "hacia adelante" fallaría.
 *
 * <p>El resultado de este cargador se usa como el {@code global} que recibe
 * {@link AnalizadorSemanticoZ#analizar(Clase, AmbitoGlobal)} para la clase que sí se está
 * compilando: sigue siendo UN SOLO {@link AmbitoGlobal} plano (no un padre/hijo en capas como
 * el de Pig Latin) precisamente para que esa clase pueda auto-referenciarse ("Nodo siguiente;"
 * dentro del propio Nodo.z) -- {@code NodoTipoRef.resolver} busca con
 * {@code ambito.ambitoGlobal().resolverLocal(...)}, que sube hasta la RAÍZ de la cadena de
 * ámbitos; con una capa extra encima, la propia clase (declarada en la capa hija) quedaría
 * invisible para sí misma.
 *
 * <h3>Qué NO hace</h3>
 * NO verifica los CUERPOS de las clases hermanas (esos errores, si los hay, le pertenecen al
 * archivo de esa hermana, no al que se está analizando ahora) y NO reporta nada si una hermana
 * tiene errores de sintaxis: simplemente no aporta símbolos (esa hermana tendrá sus propios
 * errores cuando se analice directamente).
 */
public final class CargadorClasesZ {

    private static final Set<String> CARPETAS_IGNORADAS = Set.of("target", "node_modules", "build", "out");
    private static final int PROFUNDIDAD_MAXIMA = 8;

    /**
     * @param archivoActual el .z que se va a analizar después con el resultado de este método
     *                      (se excluye de la búsqueda: su propia clase la registra
     *                      {@link AnalizadorSemanticoZ#analizar} normalmente)
     * @param raizProyecto  carpeta raíz del proyecto donde buscar hermanas, o {@code null} si
     *                      el archivo no pertenece a ningún proyecto abierto (en ese caso se
     *                      devuelve un {@link AmbitoGlobal} vacío: sin proyecto no hay dónde
     *                      buscar hermanas, ni tiene sentido intentarlo)
     */
    public AmbitoGlobal cargar(File archivoActual, File raizProyecto) {
        AmbitoGlobal global = new AmbitoGlobal();
        if (raizProyecto == null) {
            return global;
        }

        List<Clase> hermanas = new ArrayList<>();
        for (File archivo : buscarArchivosZ(raizProyecto)) {
            if (mismoArchivo(archivo, archivoActual)) continue;
            Clase clase = intentarParsear(archivo);
            if (clase != null) hermanas.add(clase);
        }

        AnalizadorSemanticoZ analizador = new AnalizadorSemanticoZ();
        // Los problemas al registrar una hermana (p. ej. un tipo que ni así se encuentra)
        // no son del archivo actual: se descartan aquí a propósito, sin reportarse.
        ManejadorErrores descartable = new ManejadorErrores();

        // Ronda 1: SOLO nombres (ver Javadoc de la clase). Un LinkedHashMap conserva el
        // emparejamiento clase->símbolo sin depender de índices paralelos, y ordena la ronda 2
        // en el mismo orden en que se registraron los nombres (determinista, fácil de depurar).
        Map<Clase, Simbolo> registradas = new LinkedHashMap<>();
        for (Clase hermana : hermanas) {
            Simbolo simbolo = analizador.registrarFirma(hermana, global, descartable);
            // simbolo == null: dos hermanas con el mismo nombre de clase (o coincide con el
            // nombre de la clase actual). Es un problema real del proyecto, pero -otra vez- no
            // le corresponde al archivo que se está analizando ahora mismo reportarlo.
            if (simbolo != null) registradas.put(hermana, simbolo);
        }

        // Ronda 2: miembros, ya con TODOS los nombres de clase visibles.
        for (Map.Entry<Clase, Simbolo> entrada : registradas.entrySet()) {
            analizador.registrarMiembros(entrada.getKey(), entrada.getValue(), global, descartable);
        }

        return global;
    }

    /** Construye SOLO el AST (Clase) de un .z; null si tiene errores léxicos/sintácticos o no se pudo leer. */
    private Clase intentarParsear(File archivo) {
        String texto;
        try {
            texto = new String(Files.readAllBytes(archivo.toPath()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return null;
        }

        ListenerErroresANTLR listener = new ListenerErroresANTLR();

        LenguajeLexer lexer = new LenguajeLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GramaticaZ parser = new GramaticaZ(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        GramaticaZ.CompilationUnitContext arbol = parser.compilationUnit();
        if (listener.tieneErrores()) {
            return null;
        }
        try {
            return new ASTBuilderZ().construir(arbol);
        } catch (Exception ex) {
            return null; // un .z sintácticamente válido pero que el builder no logra armar: se ignora igual
        }
    }

    private boolean mismoArchivo(File a, File b) {
        return a.getAbsoluteFile().equals(b == null ? null : b.getAbsoluteFile());
    }

    // ------------------------------------------------------------------
    // Búsqueda de archivos .z bajo la raíz del proyecto
    // ------------------------------------------------------------------

    private List<File> buscarArchivosZ(File raiz) {
        Path inicio = raiz.toPath();
        List<File> encontrados = new ArrayList<>();
        try {
            Files.walkFileTree(inicio, EnumSet.noneOf(FileVisitOption.class), PROFUNDIDAD_MAXIMA,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            if (!dir.equals(inicio)) {
                                String nombre = dir.getFileName().toString();
                                if (nombre.startsWith(".") || CARPETAS_IGNORADAS.contains(nombre)) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path archivo, BasicFileAttributes attrs) {
                            if (archivo.getFileName().toString().endsWith(".z")) {
                                encontrados.add(archivo.toFile());
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path archivo, IOException ex) {
                            return FileVisitResult.CONTINUE; // sin permisos, etc.: se ignora
                        }
                    });
        } catch (IOException ex) {
            return List.of();
        }
        return encontrados;
    }
}