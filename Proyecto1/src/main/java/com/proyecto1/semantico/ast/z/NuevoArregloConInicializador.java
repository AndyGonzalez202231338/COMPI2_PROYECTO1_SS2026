package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;

import java.util.List;

/**
 * {@code NEW tipoBase (CORIZQ CORDER)+ LLAVEIZQ initializerList? LLAVEDER}
 * (#primarioArregloConInicializador): "new int[]{1, 2, 3}" o "new int[][]{{1,2},{3,4}}"
 * (los elementos anidados salen naturalmente como {@link ListaLiteral} dentro de
 * {@code elementos}, sin necesidad de una regla aparte para arreglos multidimensionales).
 */
public final class NuevoArregloConInicializador extends NodoZ implements ExpresionZ {

    private final NodoTipoRef tipoElemento;
    private final int dimensiones; // cuántos pares "[]" vacíos hubo
    private final List<ExpresionZ> elementos;

    public NuevoArregloConInicializador(NodoTipoRef tipoElemento, int dimensiones,
                                         List<ExpresionZ> elementos, int linea, int columna) {
        super(linea, columna);
        this.tipoElemento = tipoElemento;
        this.dimensiones = dimensiones;
        this.elementos = elementos;
    }

    public NodoTipoRef getTipoElemento() {
        return tipoElemento;
    }

    public int getDimensiones() {
        return dimensiones;
    }

    public List<ExpresionZ> getElementos() {
        return elementos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo base = tipoElemento.resolver(ambito, errores);
        for (ExpresionZ e : elementos) e.verificar(ambito, errores);
        for (int i = 0; i < dimensiones; i++) base = new TipoArreglo(base);
        return base;
    }
}
