package com.proyecto1.servicio;

import com.proyecto1.semantico.errores.ErrorSemantico;

import java.util.Collections;
import java.util.List;

/**
 * Resultado de analizar un archivo (.y, .z o .pig): las tres listas de errores por
 * separado (léxicos, sintácticos, semánticos) más un mensaje de resumen listo para
 * mostrar. Es un DTO puro -- no sabe nada de JavaFX ni de la consola; quien la
 * imprima (Fase 3, en MainController) decide el formato exacto (colores, prefijos
 * "[OK]"/"[ERROR]", etc.).
 *
 * Reutiliza {@link ErrorSemantico} (línea, columna, mensaje) para las tres
 * categorías en vez de inventar ErrorLexico/ErrorSintactico casi idénticas: lo que
 * distingue a un error léxico de uno semántico aquí es en QUÉ LISTA cae, no la
 * forma de la clase que lo representa.
 */
public final class ResultadoAnalisis {

    private final String lenguaje;
    private final List<ErrorSemantico> erroresLexicos;
    private final List<ErrorSemantico> erroresSintacticos;
    private final List<ErrorSemantico> erroresSemanticos;
    private final List<ErrorSemantico> advertencias;
    private final int cantidadLineas;

    private ResultadoAnalisis(String lenguaje, List<ErrorSemantico> erroresLexicos,
                              List<ErrorSemantico> erroresSintacticos, List<ErrorSemantico> erroresSemanticos,
                              List<ErrorSemantico> advertencias, int cantidadLineas) {
        this.lenguaje = lenguaje;
        this.erroresLexicos = erroresLexicos;
        this.erroresSintacticos = erroresSintacticos;
        this.erroresSemanticos = erroresSemanticos;
        this.advertencias = advertencias;
        this.cantidadLineas = cantidadLineas;
    }

    public static ResultadoAnalisis conErrores(String lenguaje, List<ErrorSemantico> erroresLexicos,
                                               List<ErrorSemantico> erroresSintacticos,
                                               List<ErrorSemantico> erroresSemanticos, int cantidadLineas) {
        return conErrores(lenguaje, erroresLexicos, erroresSintacticos, erroresSemanticos,
                Collections.emptyList(), cantidadLineas);
    }

    /** Igual que {@link #conErrores(String, List, List, List, int)} pero con advertencias no bloqueantes (no cuentan para isExito()). */
    public static ResultadoAnalisis conErrores(String lenguaje, List<ErrorSemantico> erroresLexicos,
                                               List<ErrorSemantico> erroresSintacticos,
                                               List<ErrorSemantico> erroresSemanticos,
                                               List<ErrorSemantico> advertencias, int cantidadLineas) {
        return new ResultadoAnalisis(lenguaje, erroresLexicos, erroresSintacticos, erroresSemanticos,
                advertencias, cantidadLineas);
    }

    /** Para cuando el lexer/parser/analizador lanzó una excepción inesperada (no debería pasar, pero no debe tumbar la UI). */
    public static ResultadoAnalisis errorInterno(String lenguaje, String mensaje) {
        ErrorSemantico error = new ErrorSemantico(0, 0, "Error interno: " + mensaje);
        return new ResultadoAnalisis(lenguaje, Collections.emptyList(), Collections.emptyList(),
                List.of(error), Collections.emptyList(), 0);
    }

    public static ResultadoAnalisis extensionNoSoportada(String nombreArchivo) {
        ErrorSemantico error = new ErrorSemantico(0, 0,
                "No se reconoce el tipo de archivo de '" + nombreArchivo + "' (se esperaba .y, .z o .pig).");
        return new ResultadoAnalisis("Desconocido", Collections.emptyList(), Collections.emptyList(),
                List.of(error), Collections.emptyList(), 0);
    }

    public boolean isExito() { return getTotalErrores() == 0; }

    public int getTotalErrores() {
        return erroresLexicos.size() + erroresSintacticos.size() + erroresSemanticos.size();
    }

    public String getLenguaje() { return lenguaje; }
    public List<ErrorSemantico> getErroresLexicos() { return erroresLexicos; }
    public List<ErrorSemantico> getErroresSintacticos() { return erroresSintacticos; }
    public List<ErrorSemantico> getErroresSemanticos() { return erroresSemanticos; }
    public List<ErrorSemantico> getAdvertencias() { return advertencias; }
    public int getCantidadLineas() { return cantidadLineas; }

    /** Mensaje corto, sin prefijos de presentación (esos los agrega quien imprima en consola). */
    public String getMensajeResumen() {
        if (isExito()) {
            return "Análisis correcto: " + cantidadLineas + " línea" + (cantidadLineas == 1 ? "" : "s") + ".";
        }
        int total = getTotalErrores();
        return total + " error" + (total == 1 ? "" : "es") + " encontrado" + (total == 1 ? "" : "s") + ".";
    }
}