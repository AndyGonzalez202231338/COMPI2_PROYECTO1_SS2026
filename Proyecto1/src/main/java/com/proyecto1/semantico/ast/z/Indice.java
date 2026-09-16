package com.proyecto1.semantico.ast.z;

/** {@code primaryExpression CORIZQ expression CORDER} (#primarioIndice): "arreglo[indice]". */
public final class Indice extends NodoZ implements ExpresionZ {

    private final ExpresionZ arreglo;
    private final ExpresionZ indice;

    public Indice(ExpresionZ arreglo, ExpresionZ indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public ExpresionZ getArreglo() {
        return arreglo;
    }

    public ExpresionZ getIndice() {
        return indice;
    }
}
