package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * {@code novus ID ( listaArgumentos? )} (#primariaNuevoObjeto): instanciación de un
 * objeto/estructura ("new" de PigLatin). Sin equivalente en Y (esa gramática no tiene
 * instanciación de objetos); {@code nombreTipo} es el {@code ID} que nombra la
 * clase/estructura a instanciar.
 */
public final class NuevoObjeto extends NodoPigLatin implements ExpresionPigLatin {

    private final String nombreTipo;
    private final List<ExpresionPigLatin> argumentos;

    public NuevoObjeto(String nombreTipo, List<ExpresionPigLatin> argumentos, int linea, int columna) {
        super(linea, columna);
        this.nombreTipo = nombreTipo;
        this.argumentos = argumentos;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public List<ExpresionPigLatin> getArgumentos() {
        return argumentos;
    }
}
