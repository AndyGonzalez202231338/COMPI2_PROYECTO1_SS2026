package com.proyecto1.semantico.ast.z;

/** {@code READLN LPAREN RPAREN} (#primarioReadln): lee una línea de entrada (devuelve String). */
public final class Readln extends NodoZ implements ExpresionZ {
    public Readln(int linea, int columna) {
        super(linea, columna);
    }
}
