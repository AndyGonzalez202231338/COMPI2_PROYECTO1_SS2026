package com.proyecto1.semantico.ast.piglatin;

/** {@code primaria [ expresion ]} (#primariaIndice): "arreglo[indice]". */
public final class Indice extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin arreglo;
    private final ExpresionPigLatin indice;

    public Indice(ExpresionPigLatin arreglo, ExpresionPigLatin indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public ExpresionPigLatin getArreglo() {
        return arreglo;
    }

    public ExpresionPigLatin getIndice() {
        return indice;
    }
}
