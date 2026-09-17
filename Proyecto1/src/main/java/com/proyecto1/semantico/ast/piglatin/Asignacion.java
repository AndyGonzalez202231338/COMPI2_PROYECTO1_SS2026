package com.proyecto1.semantico.ast.piglatin;

/**
 * {@code expresionAsignacion} (#expresionAsignacionDef), cuando trae operador:
 * {@code expresionCondicional op= expresionAsignacion}. A diferencia de la asignación
 * de Y (que es su propia INSTRUCCIÓN), en PigLatin la asignación es una EXPRESIÓN —
 * la regla es recursiva a la derecha ({@code a = b = 5}) y solo se vuelve instrucción
 * cuando queda envuelta en {@link ExpresionStmt} (vía {@code expresionSentencia}).
 * Por eso implementa {@link ExpresionPigLatin} y no {@link InstruccionPigLatin}.
 */
public final class Asignacion extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objetivo;
    private final String operador; // "=", "+=", "-=", "*=", "/=", "%="
    private final ExpresionPigLatin valor;

    public Asignacion(ExpresionPigLatin objetivo, String operador, ExpresionPigLatin valor,
                       int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.operador = operador;
        this.valor = valor;
    }

    public ExpresionPigLatin getObjetivo() {
        return objetivo;
    }
    public String getOperador() {
        return operador;
    }

    public ExpresionPigLatin getValor() {
        return valor;
    }
}
