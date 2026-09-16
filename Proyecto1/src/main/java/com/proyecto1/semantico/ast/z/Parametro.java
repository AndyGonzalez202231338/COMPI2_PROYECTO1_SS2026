package com.proyecto1.semantico.ast.z;

/**
 * Un {@code formalParameter} (#formalParameterDef): "tipo ID". Más simple que el
 * {@code Parametro} de Y?: Z no distingue sintácticamente paso por valor/referencia
 * con marcadores especiales ("[]"/"{}") — el arreglo ya viene incluido en el propio
 * {@code tipo}, y cualquier ID de clase es implícitamente por referencia, así que no
 * hace falta una {@code CategoriaParametro} aparte.
 */
public final class Parametro extends NodoZ {

    private final NodoTipoRef tipo;
    private final String nombre;

    public Parametro(NodoTipoRef tipo, String nombre, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
    }

    public NodoTipoRef getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }
}
