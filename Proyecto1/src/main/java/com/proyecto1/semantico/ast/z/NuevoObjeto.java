package com.proyecto1.semantico.ast.z;

import java.util.List;

/** {@code NEW ID LPAREN argumentList? RPAREN} (#primarioInstanciaClase): "new Persona(args)". */
public final class NuevoObjeto extends NodoZ implements ExpresionZ {

    private final String nombreClase;
    private final List<ExpresionZ> argumentos;

    public NuevoObjeto(String nombreClase, List<ExpresionZ> argumentos, int linea, int columna) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.argumentos = argumentos;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    public List<ExpresionZ> getArgumentos() {
        return argumentos;
    }
}
