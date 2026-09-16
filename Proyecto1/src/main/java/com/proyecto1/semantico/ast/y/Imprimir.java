package com.proyecto1.semantico.ast.y;

import java.util.List;

/** {@code IMPRIMIR(expresion (, expresion)*)} (#instImprimir). */
public final class Imprimir extends NodoY implements InstruccionY {

    private final List<ExpresionY> argumentos;

    public Imprimir(List<ExpresionY> argumentos, int linea, int columna) {
        super(linea, columna);
        this.argumentos = argumentos;
    }

    public List<ExpresionY> getArgumentos() {
        return argumentos;
    }
}
