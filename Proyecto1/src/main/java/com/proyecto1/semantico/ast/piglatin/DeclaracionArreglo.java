package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * {@code series ID [ tamaño ] : tipo (= { expr, ... })? ;} (#declaracionArregloDef).
 * {@code tamano} ya viene parseado a {@code int} (no como texto crudo) para que los
 * nodos de más arriba no tengan que volver a parsear el literal entero.
 */
public final class DeclaracionArreglo extends NodoPigLatin implements InstruccionPigLatin {

    private final String nombre;
    private final int tamano;
    private final NodoTipoRef tipo;
    private final InicializadorArreglo inicializador; // null si no hay "= { ... }"

    public DeclaracionArreglo(String nombre, int tamano, NodoTipoRef tipo, InicializadorArreglo inicializador,
                               int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.tamano = tamano;
        this.tipo = tipo;
        this.inicializador = inicializador;
    }

    public String getNombre() {
        return nombre;
    }

    public int getTamano() {
        return tamano;
    }

    public NodoTipoRef getTipo() {
        return tipo;
    }

    public InicializadorArreglo getInicializador() {
        return inicializador;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo base = tipo.resolver(ambito, errores);
        Tipo tArr = new TipoArreglo(base);

        Simbolo s = new Simbolo(nombre, CategoriaSimbolo.VARIABLE, tArr, linea, columna);
        s.getTamanosArreglo().add(tamano);
        if (!ambito.declarar(s)) {
            errores.reportar(linea, columna, "Arreglo ya declarado: '" + nombre + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }
        if (inicializador != null)
            inicializador.verificar(ambito, errores);
        s.marcarInicializado();
        return TipoPrimitivo.VOID;
    }
}
