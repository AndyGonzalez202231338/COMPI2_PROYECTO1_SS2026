package com.proyecto1.semantico.ast.piglatin;

/**
 * {@code ;} (#sentenciaVaciaDef): una instrucción vacía (un {@code ;} suelto, sin
 * ningún contenido). No tiene equivalente en Y; se incluye porque la gramática de
 * PigLatin la admite explícitamente como alternativa de {@code sentencia}. Sin datos
 * propios más que la posición.
 */
public final class SentenciaVacia extends NodoPigLatin implements InstruccionPigLatin {
    public SentenciaVacia(int linea, int columna) {
        super(linea, columna);
    }
}
