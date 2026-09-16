package com.proyecto1.semantico.ast.y;

import java.util.List;

/** Nodo raíz del AST de Y?: corresponde a {@code programa} (#programaDef). */
public final class Programa extends NodoY {

    private final List<Estructura> estructuras;
    private final List<Funcion> funciones;

    public Programa(List<Estructura> estructuras, List<Funcion> funciones, int linea, int columna) {
        super(linea, columna);
        this.estructuras = estructuras;
        this.funciones = funciones;
    }

    public List<Estructura> getEstructuras() {
        return estructuras;
    }

    public List<Funcion> getFunciones() {
        return funciones;
    }
}
