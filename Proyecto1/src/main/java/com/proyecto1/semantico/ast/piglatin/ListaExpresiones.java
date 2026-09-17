package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * {@code listaExpresiones} (#listaExpresionesDef): {@code expresion (, expresion)*}.
 * Usada en dos lugares de {@code per (...)}: como {@code inicializacionFor}
 * (alternativa {@code #initForExpresiones}, cuando el "init" no es una declaración) y
 * como {@code actualizacionFor} (única alternativa, {@code #actualizacionForDef}). Se
 * modela como {@link InstruccionPigLatin} (no como expresión) porque en ambos casos
 * cuelga directamente de un {@link Per}, nunca de otra expresión.
 */
public final class ListaExpresiones extends NodoPigLatin implements InstruccionPigLatin {

    private final List<ExpresionPigLatin> expresiones;

    public ListaExpresiones(List<ExpresionPigLatin> expresiones, int linea, int columna) {
        super(linea, columna);
        this.expresiones = expresiones;
    }

    public List<ExpresionPigLatin> getExpresiones() {
        return expresiones;
    }
}
