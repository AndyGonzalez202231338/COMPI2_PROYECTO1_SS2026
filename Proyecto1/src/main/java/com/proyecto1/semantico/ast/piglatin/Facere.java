package com.proyecto1.semantico.ast.piglatin;

/**
 * {@code sentenciaFacere} (#sentenciaFacereDef): {@code facere bloque dum (cond);}.
 * Equivale al {@code HacerMientras} de Y: el cuerpo se ejecuta al menos una vez y la
 * condición se evalúa DESPUÉS de cada iteración. Nótese que, a diferencia de
 * {@link Si} y {@link Dum}, esta sentencia NO lleva {@code finis;} — solo el {@code ;}
 * final tras la condición.
 */
public final class Facere extends NodoPigLatin implements InstruccionPigLatin {

    private final Bloque cuerpo;
    private final ExpresionPigLatin condicion;

    public Facere(Bloque cuerpo, ExpresionPigLatin condicion, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = cuerpo;
        this.condicion = condicion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }

    public ExpresionPigLatin getCondicion() {
        return condicion;
    }
}
