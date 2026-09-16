package com.proyecto1.semantico.ast.y;

import java.util.List;

/** Una {@code definicionEstructura} (#estructuraDef): "estructura Nombre: campo*". */
public final class Estructura extends NodoY {

    private final String nombre;
    private final List<CampoEstructura> campos;

    public Estructura(String nombre, List<CampoEstructura> campos, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.campos = campos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<CampoEstructura> getCampos() {
        return campos;
    }
}
