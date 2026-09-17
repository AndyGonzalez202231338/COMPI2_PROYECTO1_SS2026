package com.proyecto1.semantico.ast.piglatin;

/** {@code PERGE ;} (#sentenciaPergeDef). Equivale a {@code Continuar} de Y. Sin datos propios más que la posición. */
public final class Perge extends NodoPigLatin implements InstruccionPigLatin {
    public Perge(int linea, int columna) {
        super(linea, columna);
    }
}
