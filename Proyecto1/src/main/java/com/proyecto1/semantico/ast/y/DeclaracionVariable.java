package com.proyecto1.semantico.ast.y;

import java.util.List;

/** {@code declaracionVariable} (#declVarDef) usada como instrucción (#instDeclaracion). */
public final class DeclaracionVariable extends NodoY implements InstruccionY {

    private final NodoTipoRef tipo;
    private final String nombre;
    private final List<Integer> tamanosArreglo; // vacío si no es arreglo
    private final ExpresionY inicializador;      // null si no hay "= expresion"

    public DeclaracionVariable(NodoTipoRef tipo, String nombre, List<Integer> tamanosArreglo,
                                ExpresionY inicializador, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.tamanosArreglo = tamanosArreglo;
        this.inicializador = inicializador;
    }

    public NodoTipoRef getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }
    public List<Integer> getTamanosArreglo() {
        return tamanosArreglo;
    }

    public ExpresionY getInicializador() {
        return inicializador;
    }
}
