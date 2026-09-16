package com.proyecto1.semantico.ast.z;

/**
 * {@code PUNTOYCOMA} solo (#stmtVacia): un ";" suelto. Y? no tiene equivalente (su
 * gramática no admite sentencias vacías); existe solo para Z.
 */
public final class SentenciaVacia extends NodoZ implements InstruccionZ {
    public SentenciaVacia(int linea, int columna) {
        super(linea, columna);
    }
}
