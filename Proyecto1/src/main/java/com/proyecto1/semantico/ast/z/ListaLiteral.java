package com.proyecto1.semantico.ast.z;

import java.util.List;

/** {@code LLAVEIZQ initializerList? LLAVEDER} (#primarioListaLiteral): "{1, 2, 3}". */
public final class ListaLiteral extends NodoZ implements ExpresionZ {

    private final List<ExpresionZ> elementos;

    public ListaLiteral(List<ExpresionZ> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<ExpresionZ> getElementos() {
        return elementos;
    }
}
