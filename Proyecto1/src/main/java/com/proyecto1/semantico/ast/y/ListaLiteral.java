package com.proyecto1.semantico.ast.y;

import java.util.List;

/** {@code LLAVEIZQ (expresion (COMA expresion)*)? LLAVEDER} (#primariaListaLiteral): "{1, 2, 3}". */
public final class ListaLiteral extends NodoY implements ExpresionY {

    private final List<ExpresionY> elementos;

    public ListaLiteral(List<ExpresionY> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<ExpresionY> getElementos() {
        return elementos;
    }
}
