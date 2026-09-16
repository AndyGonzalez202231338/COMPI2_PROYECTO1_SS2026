package com.proyecto1.semantico.tipos;

import java.util.Objects;

/**
 * Tipo arreglo: envuelve un tipo base. Para Y siempre se usa con un solo nivel
 * (los arreglos se "aplanan", según la especificación); para Zetariano puede anidarse
 * (TipoArreglo(TipoArreglo(ENTERO)) para representar "int[][]").
 *
 * Es un tipo compuesto en el sentido de "tiene estructura interna", pero
 * esCompuesto() se deja en false (reservado para estructuras/clases con miembros
 * accesibles por nombre) un arreglo se accede por índice, no por nombre de campo.
 */
public final class TipoArreglo implements Tipo {

    private final Tipo base;

    public TipoArreglo(Tipo base) {
        this.base = base;
    }

    public Tipo getBase() { return base; }

    /** Para "int[][][]" devuelve el tipo escalar final (ENTERO), atravesando todos los niveles. */
    public Tipo baseEscalar() {
        Tipo actual = base;
        while (actual instanceof TipoArreglo ta) actual = ta.getBase();
        return actual;
    }

    /** Cuántos pares "[]" tiene este arreglo (1 para "int[]", 2 para "int[][]", etc.). */
    public int dimensiones() {
        int contador = 1;
        Tipo actual = base;
        while (actual instanceof TipoArreglo ta) {
            contador++;
            actual = ta.getBase();
        }
        return contador;
    }

    @Override
    public String nombre() {
        return base.nombre() + "[]";
    }

    @Override
    public boolean esArreglo() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TipoArreglo otro)) return false;
        return Objects.equals(this.base, otro.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TipoArreglo.class, base);
    }

    @Override
    public String toString() {
        return nombre();
    }
}