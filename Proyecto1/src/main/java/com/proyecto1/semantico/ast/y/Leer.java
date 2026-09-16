package com.proyecto1.semantico.ast.y;

/** {@code LEER LPAREN RPAREN} (#primariaLeer): lectura de entrada estándar. */
public final class Leer extends NodoY implements ExpresionY {
    public Leer(int linea, int columna) {
        super(linea, columna);
    }
}
