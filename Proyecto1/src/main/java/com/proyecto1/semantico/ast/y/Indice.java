package com.proyecto1.semantico.ast.y;

/** {@code primaria CORIZQ expresion CORDER} (#primariaIndice): "arreglo[indice]". */
public final class Indice extends NodoY implements ExpresionY {

    private final ExpresionY arreglo;
    private final ExpresionY indice;

    public Indice(ExpresionY arreglo, ExpresionY indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public ExpresionY getArreglo() {
        return arreglo;
    }

    public ExpresionY getIndice() {
        return indice;
    }
}
