package com.proyecto1.semantico.ast.z;

/**
 * Un {@code fieldDeclaration} (#fieldDeclarationDef): "tipo ID (= expresion)? ;".
 * A diferencia de {@code CampoEstructura} de Y?, no necesita una lista de tamaños de
 * arreglo aparte: en Z el arreglo ya viene incluido en {@code tipo} (ver
 * {@link NodoTipoRef#getDimensiones()}).
 */
public final class Atributo extends NodoZ {

    private final NodoTipoRef tipo;
    private final String nombre;
    private final ExpresionZ inicializador; // null si no hay "= expresion"

    public Atributo(NodoTipoRef tipo, String nombre, ExpresionZ inicializador, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.inicializador = inicializador;
    }

    public NodoTipoRef getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public ExpresionZ getInicializador() {
        return inicializador;
    }
}
