package com.proyecto1.semantico.ast.z;

/** {@code breakStatement} (#breakStatementDef). Sin datos propios más que la posición. */
public final class Romper extends NodoZ implements InstruccionZ {
    public Romper(int linea, int columna) {
        super(linea, columna);
    }
}
