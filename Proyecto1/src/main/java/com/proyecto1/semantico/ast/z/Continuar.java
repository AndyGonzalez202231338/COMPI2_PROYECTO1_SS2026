package com.proyecto1.semantico.ast.z;

/** {@code continueStatement} (#continueStatementDef). Sin datos propios más que la posición. */
public final class Continuar extends NodoZ implements InstruccionZ {
    public Continuar(int linea, int columna) {
        super(linea, columna);
    }
}
