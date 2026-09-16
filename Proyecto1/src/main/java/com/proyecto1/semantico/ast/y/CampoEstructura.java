package com.proyecto1.semantico.ast.y;

import java.util.List;

/**
 * Un {@code campoEstructura} (#campoDef): "tipo ID" o "tipo ID[5]" (o "[5][10]" si hay
 * varios pares de corchetes seguidos. La validación de que Y solo admite arreglos de
 * un nivel queda pendiente para la parte de reglas semánticas; aquí se guarda
 * fielmente lo que el programador escribió).
 */
public final class CampoEstructura extends NodoY {

    private final NodoTipoRef tipo;
    private final String nombre;
    private final List<Integer> tamanosArreglo; // vacío si no es arreglo

    public CampoEstructura(NodoTipoRef tipo, String nombre, List<Integer> tamanosArreglo, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.tamanosArreglo = tamanosArreglo;
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
}
