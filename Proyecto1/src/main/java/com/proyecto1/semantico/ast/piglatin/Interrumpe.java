package com.proyecto1.semantico.ast.piglatin;

/** {@code INTERRUMPE ;} (#sentenciaInterrumpeDef). Equivale a {@code Romper} de Y. Sin datos propios más que la posición. */
public final class Interrumpe extends NodoPigLatin implements InstruccionPigLatin {
    public Interrumpe(int linea, int columna) {
        super(linea, columna);
    }
}
