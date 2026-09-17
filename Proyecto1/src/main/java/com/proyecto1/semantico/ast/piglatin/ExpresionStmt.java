package com.proyecto1.semantico.ast.piglatin;

/**
 * {@code expresion ;} (#expresionSentenciaDef): una expresión usada como instrucción
 * suelta. Cubre asignaciones ({@code x = 5;}), llamadas ({@code metodo();}) y llamadas
 * encadenadas ({@code obj.metodo();}), que en la gramática de PigLatin son todas la
 * misma regla {@code expresion} (ver {@link Asignacion} y {@link Llamada}).
 */
public final class ExpresionStmt extends NodoPigLatin implements InstruccionPigLatin {

    private final ExpresionPigLatin expresion;

    public ExpresionStmt(ExpresionPigLatin expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public ExpresionPigLatin getExpresion() {
        return expresion;
    }
}
