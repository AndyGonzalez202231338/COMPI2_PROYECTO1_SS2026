package com.proyecto1.servicio;

import com.proyecto1.GramaticaPigLatin;
import com.proyecto1.GramaticaY;
import com.proyecto1.GramaticaZ;
import com.proyecto1.IndentTokenStream;
import com.proyecto1.LenguajeLexer;
import com.proyecto1.semantico.AnalizadorSemanticoPigLatin;
import com.proyecto1.semantico.AnalizadorSemanticoY;
import com.proyecto1.semantico.AnalizadorSemanticoZ;
import com.proyecto1.semantico.errores.ErrorSemantico;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.piglatin.ASTBuilderPigLatin;
import com.proyecto1.semantico.tabla.AmbitoGlobal;
import com.proyecto1.semantico.y.ASTBuilderY;
import com.proyecto1.semantico.z.ASTBuilderZ;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.util.List;

/**
 * Orquesta el pipeline completo (lexer -> parser -> AST -> semántico) para UN
 * archivo, sin duplicar nada de esa lógica: solo instancia y encadena las piezas
 * que ya existen (patrón Strategy por extensión: un método privado por lenguaje,
 * todos con la misma forma).
 *
 * Si el lexer o el parser ya reportaron errores (vía {@link ListenerErroresANTLR}),
 * NO se construye el AST ni se corre el análisis semántico sobre ese archivo: un
 * árbol de parseo con errores sintácticos puede estar incompleto o mal formado, y
 * el analizador semántico no está preparado para recorrer un árbol así (asumiría
 * que la sintaxis ya es válida). Esto es consistente con cómo funcionan casi todos
 * los compiladores: no tiene sentido revisar tipos de un programa que ni siquiera
 * parsea.
 *
 * IMPORTS de Pig Latin: por ahora se analiza cada .pig con un {@link AmbitoGlobal}
 * de imports VACÍO (ver la nota en {@link #analizarPigLatin}) -- cargar los .y/.z
 * importados de verdad queda pendiente, tal como se pidió.
 */
public class ServicioAnalisis{

    public ResultadoAnalisis analizar(File archivo, String texto) {
        String extension = extensionDe(archivo);
        try {
            return switch (extension) {
                case "y" -> analizarY(texto);
                case "z" -> analizarZ(texto, archivo.getName());
                case "pig" -> analizarPigLatin(texto);
                default -> ResultadoAnalisis.extensionNoSoportada(archivo.getName());
            };
        } catch (Exception ex) {
            String detalle = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            return ResultadoAnalisis.errorInterno(etiquetaLenguaje(extension), detalle);
        }
    }

    private ResultadoAnalisis analizarY(String texto) {
        ListenerErroresANTLR listener = new ListenerErroresANTLR();

        LenguajeLexer lexer = new LenguajeLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        // Y? SÍ es sensible a indentación: hace falta el filtro que sintetiza INDENT/DEDENT.
        IndentTokenStream tokens = new IndentTokenStream(lexer);

        GramaticaY parser = new GramaticaY(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        GramaticaY.ProgramaContext arbol = parser.programa();

        List<ErrorSemantico> erroresSemanticos = List.of();
        if (!listener.tieneErrores()) {
            com.proyecto1.semantico.ast.y.Programa programa = new ASTBuilderY().construir(arbol);
            ManejadorErrores errores = new AnalizadorSemanticoY().analizar(programa);
            erroresSemanticos = errores.obtenerErrores();
        }

        return ResultadoAnalisis.conErrores("Y?", listener.getErroresLexicos(),
                listener.getErroresSintacticos(), erroresSemanticos, contarLineas(texto));
    }

    private ResultadoAnalisis analizarZ(String texto, String nombreArchivo) {
        ListenerErroresANTLR listener = new ListenerErroresANTLR();

        LenguajeLexer lexer = new LenguajeLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        // Zetariano NO es sensible a indentación (usa '{' '}' ';'): CommonTokenStream normal.
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GramaticaZ parser = new GramaticaZ(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        GramaticaZ.CompilationUnitContext arbol = parser.compilationUnit();

        List<ErrorSemantico> erroresSemanticos = List.of();
        List<ErrorSemantico> advertencias = List.of();
        if (!listener.tieneErrores()) {
            com.proyecto1.semantico.ast.z.Clase clase = new ASTBuilderZ().construir(arbol);
            ManejadorErrores errores = new AnalizadorSemanticoZ().analizar(clase);
            erroresSemanticos = errores.obtenerErrores();

            // Advertencia semántica NO bloqueante: el nombre del archivo debería
            // coincidir con el de la clase pública que contiene (regla de Zetariano).
            String nombreEsperado = clase.getNombre() + ".z";
            if (!nombreEsperado.equals(nombreArchivo)) {
                advertencias = List.of(new ErrorSemantico(clase.getLinea(), clase.getColumna(),
                        "El nombre del archivo ('" + nombreArchivo + "') no coincide con el de la clase pública ('"
                                + nombreEsperado + "')."));
            }
        }

        return ResultadoAnalisis.conErrores("Zetariano", listener.getErroresLexicos(),
                listener.getErroresSintacticos(), erroresSemanticos, advertencias, contarLineas(texto));
    }

    private ResultadoAnalisis analizarPigLatin(String texto) {
        ListenerErroresANTLR listener = new ListenerErroresANTLR();

        LenguajeLexer lexer = new LenguajeLexer(CharStreams.fromString(texto));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        // Pig Latin tampoco es sensible a indentación (usa ';' / 'finis;' / '{}'
        // explícitos) -- ver la nota completa arriba de esta clase.
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GramaticaPigLatin parser = new GramaticaPigLatin(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        GramaticaPigLatin.ProgramaContext arbol = parser.programa();

        List<ErrorSemantico> erroresSemanticos = List.of();
        if (!listener.tieneErrores()) {
            com.proyecto1.semantico.ast.piglatin.Programa programa = new ASTBuilderPigLatin().construir(arbol);
            // Los .y/.z importados todavía no se cargan: se analiza con un ámbito
            // de imports vacío. Cuando se implemente la carga real de imports, este
            // AmbitoGlobal es el que hay que reemplazar por el de los archivos
            // realmente importados.
            AmbitoGlobal globalImportsVacio = new AmbitoGlobal();
            ManejadorErrores errores = new AnalizadorSemanticoPigLatin().analizar(programa, globalImportsVacio);
            erroresSemanticos = errores.obtenerErrores();
        }

        return ResultadoAnalisis.conErrores("PigLatin", listener.getErroresLexicos(),
                listener.getErroresSintacticos(), erroresSemanticos, contarLineas(texto));
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
}