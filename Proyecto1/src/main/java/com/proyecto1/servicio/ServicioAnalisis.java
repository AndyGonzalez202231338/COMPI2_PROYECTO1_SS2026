package com.proyecto1.servicio;

import com.proyecto1.GramaticaPigLatin;
import com.proyecto1.GramaticaY;
import com.proyecto1.GramaticaZ;
import com.proyecto1.IndentTokenStream;
import com.proyecto1.LenguajeLexer;
import com.proyecto1.semantico.AnalizadorSemanticoPigLatin;
import com.proyecto1.semantico.AnalizadorSemanticoY;
import com.proyecto1.semantico.AnalizadorSemanticoZ;
import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.errores.ErrorSemantico;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.piglatin.ASTBuilderPigLatin;
import com.proyecto1.semantico.tabla.AmbitoGlobal;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.y.ASTBuilderY;
import com.proyecto1.semantico.z.ASTBuilderZ;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyecto1.codigo.c.OrquestadorC3DaC;
import com.proyecto1.semantico.ast.cuadruplas.Cuadrupla;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Orquesta el pipeline completo (lexer -> parser -> AST -> semántico -> C3D) para
 * UN archivo, sin duplicar nada de esa lógica: solo instancia y encadena las piezas
 * que ya existen (patrón Strategy por extensión: un método privado por lenguaje,
 * todos con la misma forma).
 *
 * <p>Si el lexer o el parser ya reportaron errores (vía {@link ListenerErroresANTLR}),
 * NO se construye el AST ni se corre el análisis semántico sobre ese archivo.
 *
 * <p><b>Fase 4:</b> si el análisis semántico terminó SIN errores, se genera también
 * el C3D del AST verificado y se guarda en {@link ResultadoAnalisis#getGeneradorC3D()}.
 * Además se escribe el archivo .c al lado del fuente y se imprimen las cuádruplas.
 *
 * <p><b>Prototipos de funciones importadas:</b> cuando un .pig importa funciones de
 * .y o métodos/clases de .z, el orquestador necesita los PROTOTIPOS de esas funciones
 * para no fallar con "implicit declaration" al compilar. Esos prototipos se
 * construyen desde el ámbito de imports como un {@code Map<String, Firma>}: la
 * clave es la etiqueta MANGLADA y el valor la {@link GeneradorC3D.Firma} ya armada.
 * No se pasan {@link Simbolo}s directos porque el símbolo de un método/constructor
 * de Z guarda el nombre plano, no la etiqueta manglada que usa el C3D.
 */
public class ServicioAnalisis {

    private final File raizProyecto;

    public ServicioAnalisis() {
        this(null);
    }

    public ServicioAnalisis(File raizProyecto) {
        this.raizProyecto = raizProyecto;
    }

    public ResultadoAnalisis analizar(File archivo, String texto) {
        String extension = extensionDe(archivo);
        try {
            return switch (extension) {
                case "y" -> analizarY(texto, archivo);
                case "z" -> analizarZ(texto, archivo);
                case "pig" -> analizarPigLatin(texto, archivo);
                default -> ResultadoAnalisis.extensionNoSoportada(archivo.getName());
            };
        } catch (Exception ex) {
            String detalle = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            return ResultadoAnalisis.errorInterno(etiquetaLenguaje(extension), detalle);
        }
    }

    private ResultadoAnalisis analizarY(String texto, File archivo) {
        ListenerErroresANTLR listener = new ListenerErroresANTLR();

        LenguajeLexer lexer = new LenguajeLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        IndentTokenStream tokens = new IndentTokenStream(lexer);

        GramaticaY parser = new GramaticaY(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        GramaticaY.ProgramaContext arbol = parser.programa();

        List<ErrorSemantico> erroresSemanticos = List.of();
        AmbitoGlobal ambitoGlobal = null;
        GeneradorC3D generadorC3D = null;
        if (!listener.tieneErrores()) {
            com.proyecto1.semantico.ast.y.Programa programa = new ASTBuilderY().construir(arbol);
            ambitoGlobal = new AmbitoGlobal();
            ManejadorErrores errores = new AnalizadorSemanticoY().analizar(programa, ambitoGlobal);
            erroresSemanticos = errores.obtenerErrores();

            if (erroresSemanticos.isEmpty()) {
                try {
                    generadorC3D = new GeneradorC3D(ambitoGlobal);
                    programa.generarC3D(generadorC3D);
                    System.out.println("[C3D] Generado correctamente (Y?): "
                            + generadorC3D.getCuadruplas().size() + " cuádruplas, "
                            + generadorC3D.getFirmas().size() + " función(es).");

                    imprimirCuadruplas(generadorC3D, "Y?");
                    generarArchivoC(generadorC3D, archivo, "_", null, ambitoGlobal, Map.of());

                } catch (Exception exC3D) {
                    String detalle = exC3D.getMessage() != null ? exC3D.getMessage() : exC3D.getClass().getSimpleName();
                    System.out.println("[C3D] Error generando código: " + detalle);
                    generadorC3D = null;
                }
            }
        }

        ResultadoAnalisis base = ResultadoAnalisis.conErrores("Y?", listener.getErroresLexicos(),
                listener.getErroresSintacticos(), erroresSemanticos, List.of(), contarLineas(texto), ambitoGlobal);

        return (generadorC3D != null) ? ResultadoAnalisis.conC3D(base, generadorC3D) : base;
    }

    private ResultadoAnalisis analizarZ(String texto, File archivo) {
        String nombreArchivo = archivo.getName();
        ListenerErroresANTLR listener = new ListenerErroresANTLR();

        LenguajeLexer lexer = new LenguajeLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GramaticaZ parser = new GramaticaZ(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        GramaticaZ.CompilationUnitContext arbol = parser.compilationUnit();

        List<ErrorSemantico> erroresSemanticos = List.of();
        List<ErrorSemantico> advertencias = List.of();
        AmbitoGlobal ambitoGlobalExportado = null;
        GeneradorC3D generadorC3D = null;
        if (!listener.tieneErrores()) {
            com.proyecto1.semantico.ast.z.Clase clase = new ASTBuilderZ().construir(arbol);
            AmbitoGlobal ambitoInterno = new CargadorClasesZ().cargar(archivo, raizProyecto);
            ManejadorErrores errores = new AnalizadorSemanticoZ().analizar(clase, ambitoInterno);
            erroresSemanticos = errores.obtenerErrores();

            Simbolo sClase = ambitoInterno.resolverLocal(clase.getNombre());
            if (sClase != null) {
                ambitoGlobalExportado = new AmbitoGlobal();
                ambitoGlobalExportado.declarar(sClase);
            }

            String nombreEsperado = clase.getNombre() + ".z";
            if (!nombreEsperado.equals(nombreArchivo)) {
                advertencias = List.of(new ErrorSemantico(clase.getLinea(), clase.getColumna(),
                        "El nombre del archivo ('" + nombreArchivo + "') no coincide con el de la clase pública ('"
                                + nombreEsperado + "')."));
            }

            if (erroresSemanticos.isEmpty()) {
                try {
                    generadorC3D = new GeneradorC3D(ambitoInterno);
                    clase.generarC3D(generadorC3D);
                    System.out.println("[C3D] Generado correctamente (Zetariano, clase '"
                            + clase.getNombre() + "'): " + generadorC3D.getCuadruplas().size()
                            + " cuádruplas, " + generadorC3D.getFirmas().size() + " método(s)/constructor(es).");

                    imprimirCuadruplas(generadorC3D, "Zetariano");
                    generarArchivoC(generadorC3D, archivo, "_", null, ambitoInterno, Map.of());

                } catch (Exception exC3D) {
                    String detalle = exC3D.getMessage() != null ? exC3D.getMessage() : exC3D.getClass().getSimpleName();
                    System.out.println("[C3D] Error generando código: " + detalle);
                    generadorC3D = null;
                }
            }
        }

        ResultadoAnalisis base = ResultadoAnalisis.conErrores("Zetariano", listener.getErroresLexicos(),
                listener.getErroresSintacticos(), erroresSemanticos, advertencias, contarLineas(texto), ambitoGlobalExportado);

        return (generadorC3D != null) ? ResultadoAnalisis.conC3D(base, generadorC3D) : base;
    }

    private ResultadoAnalisis analizarPigLatin(String texto, File archivo) {
        ListenerErroresANTLR listener = new ListenerErroresANTLR();

        LenguajeLexer lexer = new LenguajeLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GramaticaPigLatin parser = new GramaticaPigLatin(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        GramaticaPigLatin.ProgramaContext arbol = parser.programa();

        List<ErrorSemantico> erroresSemanticos = List.of();
        List<ErrorSemantico> advertencias = List.of();
        GeneradorC3D generadorC3D = null;
        if (!listener.tieneErrores()) {
            com.proyecto1.semantico.ast.piglatin.Programa programa = new ASTBuilderPigLatin().construir(arbol);
            CargadorImports.Resultado imports =
                    new CargadorImports(this, raizProyecto).cargar(programa.getImportaciones(), archivo);
            ManejadorErrores errores = new AnalizadorSemanticoPigLatin().analizar(programa, imports.getAmbitoGlobal());

            List<ErrorSemantico> todos = new ArrayList<>(imports.getErrores());
            todos.addAll(errores.obtenerErrores());
            todos.sort(Comparator.comparingInt(ErrorSemantico::getLinea).thenComparingInt(ErrorSemantico::getColumna));
            erroresSemanticos = todos;
            advertencias = imports.getAdvertencias();

            if (todos.isEmpty()) {
                try {
                    generadorC3D = new GeneradorC3D(imports.getAmbitoGlobal());
                    programa.generarC3D(generadorC3D);
                    System.out.println("[C3D] Generado correctamente (PigLatin): "
                            + generadorC3D.getCuadruplas().size() + " cuádruplas, "
                            + generadorC3D.getFirmas().size() + " función(es).");

                    // Firmas de las funciones/métodos/constructores IMPORTADOS,
                    // para emitir sus prototipos en main.c.
                    Map<String, GeneradorC3D.Firma> firmasExternas =
                            recolectarFirmasImportadas(imports.getAmbitoGlobal());

                    imprimirCuadruplas(generadorC3D, "PigLatin");
                    generarArchivoC(generadorC3D, archivo, "_", "main",
                            imports.getAmbitoGlobal(), firmasExternas);

                } catch (Exception exC3D) {
                    String detalle = exC3D.getMessage() != null ? exC3D.getMessage() : exC3D.getClass().getSimpleName();
                    System.out.println("[C3D] Error generando código: " + detalle);
                    generadorC3D = null;
                }
            }
        }

        ResultadoAnalisis base = ResultadoAnalisis.conErrores("PigLatin", listener.getErroresLexicos(),
                listener.getErroresSintacticos(), erroresSemanticos, advertencias, contarLineas(texto));

        return (generadorC3D != null) ? ResultadoAnalisis.conC3D(base, generadorC3D) : base;
    }

    private String extensionDe(File archivo) {
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');
        return punto >= 0 ? nombre.substring(punto + 1).toLowerCase() : "";
    }

    private String etiquetaLenguaje(String extension) {
        return switch (extension) {
            case "y" -> "Y?";
            case "z" -> "Zetariano";
            case "pig" -> "PigLatin";
            default -> "Desconocido";
        };
    }

    private int contarLineas(String texto) {
        if (texto.isEmpty()) return 0;
        int lineas = 1;
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) == '\n') lineas++;
        }
        return lineas;
    }

    /** Imprime la tabla de cuádruplas en stdout (para depurar). */
    private static void imprimirCuadruplas(GeneradorC3D generadorC3D, String lenguaje) {
        System.out.println("=== Cuádruplas (" + lenguaje + ") ===");
        int k = 0;
        for (Cuadrupla c : generadorC3D.getCuadruplas()) {
            System.out.printf("%3d: %s%n", k++, c.toStringLegible());
        }
    }

    /**
     * Genera el archivo .c al lado del fuente.
     *
     * @param prefijoLenguaje       prefijo que se aplica a los nombres de función.
     * @param nombreFuncionEntrada  nombre del entry point en el C3D ("main" en PigLatin,
     *                              null en Y/Z que no tienen main propio).
     * @param ambitoGlobalUsado     ámbito del que se sacan los typedefs (structs/clases).
     * @param firmasExternas        firmas de funciones importadas (para prototipos).
     */
    private static void generarArchivoC(GeneradorC3D generadorC3D, File archivo,
                                        String prefijoLenguaje, String nombreFuncionEntrada,
                                        AmbitoGlobal ambitoGlobalUsado,
                                        Map<String, GeneradorC3D.Firma> firmasExternas) {
        try {
            List<Simbolo> tipos = (ambitoGlobalUsado != null)
                    ? ambitoGlobalUsado.getTiposDefinidos()
                    : List.of();
            Map<String, GeneradorC3D.Firma> externas = (firmasExternas != null)
                    ? firmasExternas
                    : Map.of();

            OrquestadorC3DaC orch = new OrquestadorC3DaC(
                    generadorC3D.getCuadruplas(),
                    generadorC3D.getFirmas(),
                    prefijoLenguaje,
                    nombreFuncionEntrada,
                    tipos,
                    externas
            );
            String codigoC = orch.generarArchivoCompleto();

            String ruta = archivo.getAbsolutePath();
            int punto = ruta.lastIndexOf('.');
            String salida = (punto >= 0 ? ruta.substring(0, punto) : ruta) + ".c";

            Files.writeString(Path.of(salida), codigoC);
            System.out.println("[C] Archivo C generado: " + salida);
        } catch (Exception exC) {
            String detalle = exC.getMessage() != null ? exC.getMessage() : exC.getClass().getSimpleName();
            System.out.println("[C] Error generando C: " + detalle);
        }
    }

    // ---------- Recolección de firmas importadas (PigLatin) ----------

    /**
     * Construye el mapa {@code etiqueta -> Firma} de todas las funciones, métodos y
     * constructores IMPORTADOS (de .y y .z) que un .pig puede invocar.
     *
     * <p>Cada firma trae la etiqueta YA MANGLADA porque el símbolo individual guarda
     * el nombre plano, no la etiqueta que usa el C3D:
     * <ul>
     *   <li>Función de Y: la etiqueta es {@code s.getNombre()} (sin sufijos {@code #}).</li>
     *   <li>Método de Z: {@code Clase_metodo}.</li>
     *   <li>Constructor de Z: {@code Clase_init_aN} (usa {@link GeneradorC3D#etiquetaConstructor}).</li>
     * </ul>
     */
    private static Map<String, GeneradorC3D.Firma> recolectarFirmasImportadas(AmbitoGlobal ambito) {
        Map<String, GeneradorC3D.Firma> out = new HashMap<>();
        if (ambito == null) return out;

        for (Simbolo s : ambito.simbolosLocales()) {
            if (s.getCategoria() == CategoriaSimbolo.FUNCION) {
                String etiqueta = s.getNombre().split("#")[0];
                out.put(etiqueta, firmaDeFuncion(etiqueta, s));
            } else if (s.getCategoria() == CategoriaSimbolo.CLASE) {
                String nombreClase = s.getNombre();
                Tipo tipoThis = new com.proyecto1.semantico.tipos.TipoClase(s);

                // OJO: se recorre getMiembros().valores() y NO getMiembrosEnOrden():
                // los métodos y constructores de Z se registran con
                // agregarMiembroConClave("Nombre#aridad#tipos", ...), que NO alimenta
                // la lista ordenada (esa solo la pueblan los atributos).
                for (Simbolo m : s.getMiembros().valores()) {
                    if (m.getCategoria() == CategoriaSimbolo.METODO) {
                        String etiquetaMetodo = nombreClase + "_" + m.getNombre().split("#")[0];
                        out.put(etiquetaMetodo,
                                firmaDeMetodoOConstructor(etiquetaMetodo, m, tipoThis, m.getTipo()));
                    } else if (m.getCategoria() == CategoriaSimbolo.CONSTRUCTOR) {
                        String etiquetaCtor = GeneradorC3D.etiquetaConstructor(nombreClase, m.getParametros().size());
                        out.put(etiquetaCtor,
                                firmaDeMetodoOConstructor(etiquetaCtor, m, tipoThis, TipoPrimitivo.VOID));
                    }
                }
            }
        }
        return out;
    }

    /** Firma de una función suelta de Y (sin "this"). */
    private static GeneradorC3D.Firma firmaDeFuncion(String etiqueta, Simbolo f) {
        List<GeneradorC3D.ParametroFirma> params = new ArrayList<>();
        for (Simbolo p : f.getParametros()) {
            params.add(new GeneradorC3D.ParametroFirma(p.getNombre(), p.getTipo()));
        }
        return new GeneradorC3D.Firma(etiqueta, params, f.getTipo(), false);
    }

    /** Firma de un método o constructor de Z (con "this" como primer parámetro). */
    private static GeneradorC3D.Firma firmaDeMetodoOConstructor(String etiqueta, Simbolo m,
                                                                Tipo tipoThis, Tipo tipoRetorno) {
        List<GeneradorC3D.ParametroFirma> params = new ArrayList<>();
        params.add(new GeneradorC3D.ParametroFirma("this", tipoThis));
        for (Simbolo p : m.getParametros()) {
            params.add(new GeneradorC3D.ParametroFirma(p.getNombre(), p.getTipo()));
        }
        return new GeneradorC3D.Firma(etiqueta, params, tipoRetorno, true);
    }
}