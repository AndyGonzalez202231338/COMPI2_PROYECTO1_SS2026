package com.proyecto1.semantico.ast.y;

import java.util.List;

/** {@code primaria LPAREN argumentos? RPAREN} (#primariaLlamada): llamada a función. */
public final class Llamada extends NodoY implements ExpresionY {

    private final ExpresionY objetivo; // normalmente un Identificador con el nombre de la función
    private final List<ExpresionY> argumentos;

    public Llamada(ExpresionY objetivo, List<ExpresionY> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionY getObjetivo() {
        return objetivo;
    }

    public List<ExpresionY> getArgumentos() {
        return argumentos;
    }
}
