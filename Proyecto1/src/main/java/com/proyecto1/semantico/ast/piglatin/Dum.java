package com.proyecto1.semantico.ast.piglatin;

/**
 * {@code sentenciaDum} (#sentenciaDumDef): {@code dum (cond) bloque finis;}. Equivale
 * al {@code Mientras} de Y: evalúa la condición ANTES de cada iteración.
 */
public final class Dum extends NodoPigLatin implements InstruccionPigLatin {

    private final ExpresionPigLatin condicion;
    private final Bloque cuerpo;

    public Dum(ExpresionPigLatin condicion, Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public ExpresionPigLatin getCondicion() {
        return condicion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
