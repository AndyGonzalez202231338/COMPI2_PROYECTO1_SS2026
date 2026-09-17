package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * {@code primaria ( listaArgumentos? )} (#primariaLlamada): llamada a función o
 * método. {@code objetivo} es normalmente un {@link Identificador} (llamada simple,
 * {@code funcion()}) o un {@link AccesoCampo} (llamada encadenada,
 * {@code obj.metodo()}), reutilizando el encadenado que ya arma {@code primaria} de
 * forma recursiva a la izquierda.
 */
public final class Llamada extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objetivo;
    private final List<ExpresionPigLatin> argumentos;

    public Llamada(ExpresionPigLatin objetivo, List<ExpresionPigLatin> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionPigLatin getObjetivo() {
        return objetivo;
    }

    public List<ExpresionPigLatin> getArgumentos() {
        return argumentos;
    }
}
