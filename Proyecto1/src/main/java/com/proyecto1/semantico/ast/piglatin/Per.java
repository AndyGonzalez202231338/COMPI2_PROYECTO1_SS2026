package com.proyecto1.semantico.ast.piglatin;

/**
 * {@code sentenciaPer} (#sentenciaPerDef): {@code per (init; cond?; act?) bloque}.
 * Equivale al {@code Para} de Y, con dos diferencias fieles a la gramática:
 * <ul>
 *   <li>NO lleva {@code finis;} al final (a diferencia de {@link Si} y {@link Dum}).</li>
 *   <li>{@code init} viene de {@code inicializacionFor}, que puede ser una
 *       {@link DeclaracionVariable} (sin {@code ;}, alternativa
 *       {@code #initForDeclaracion}) o una {@link ListaExpresiones} (alternativa
 *       {@code #initForExpresiones}); {@code act} viene de {@code actualizacionFor},
 *       que siempre es una {@link ListaExpresiones} (#actualizacionForDef). Por eso
 *       ambas se guardan como {@code InstruccionPigLatin} genérico en vez de un tipo
 *       más estrecho — igual que "init"/"act" en el {@code Para} de Y.</li>
 * </ul>
 * Tanto {@code init} como {@code cond} y {@code act} pueden faltar (los "?" de la
 * gramática).
 */
public final class Per extends NodoPigLatin implements InstruccionPigLatin {

    private final InstruccionPigLatin inicializacion; // DeclaracionVariable | ListaExpresiones | null
    private final ExpresionPigLatin condicion;          // null si se omitió
    private final InstruccionPigLatin actualizacion;    // ListaExpresiones | null
    private final Bloque cuerpo;

    public Per(InstruccionPigLatin inicializacion, ExpresionPigLatin condicion,
               InstruccionPigLatin actualizacion, Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
        this.cuerpo = cuerpo;
    }

    public InstruccionPigLatin getInicializacion() {
        return inicializacion;
    }

    public ExpresionPigLatin getCondicion() {
        return condicion;
    }

    public InstruccionPigLatin getActualizacion() {
        return actualizacion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
