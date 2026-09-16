package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * {@code NEW tipoBase (CORIZQ expression CORDER)+} (#primarioArregloConTamano):
 * "new int[5]" o "new int[3][3]". A diferencia de Y? (donde el tamaño de un arreglo
 * SIEMPRE es una constante entera literal, fija en la propia declaración de la
 * estructura), aquí cada tamaño es una {@link ExpresionZ} genérica (p. ej.
 * "new int[n]" con n una variable), porque en Z el arreglo se crea en tiempo de
 * ejecución con "new". Un elemento de {@code tamanos} por cada dimensión.
 */
public final class NuevoArregloConTamano extends NodoZ implements ExpresionZ {

    private final NodoTipoRef tipoElemento; // tipo escalar base, sin corchetes
    private final List<ExpresionZ> tamanos;

    public NuevoArregloConTamano(NodoTipoRef tipoElemento, List<ExpresionZ> tamanos, int linea, int columna) {
        super(linea, columna);
        this.tipoElemento = tipoElemento;
        this.tamanos = tamanos;
    }

    public NodoTipoRef getTipoElemento() {
        return tipoElemento;
    }

    public List<ExpresionZ> getTamanos() {
        return tamanos;
    }

    public int getDimensiones() {
        return tamanos.size();
    }
}
