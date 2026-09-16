package com.proyecto1.semantico.ast.y;

/** {@code CONTINUAR} (#instContinuar). Sin datos propios más que la posición. */
public final class Continuar extends NodoY implements InstruccionY {
    public Continuar(int linea, int columna) {
        super(linea, columna);
    }
}
