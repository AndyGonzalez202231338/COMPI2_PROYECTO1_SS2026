package com.proyecto1.semantico.ast.y;

/**
 * Un {@code parametro}. Las tres alternativas de la gramática (#parametroPrimitivo,
 * #parametroArreglo, #parametroEstructura) se representan con esta única clase + su
 * {@link CategoriaParametro}, construida siempre a través de una de las tres fábricas
 * estáticas de abajo (así queda imposible construir, por ejemplo, un parámetro
 * ESTRUCTURA sin {@code nombreTipoEstructura}).
 */
public final class Parametro extends NodoY {

    private final CategoriaParametro categoria;
    private final NodoTipoRef tipo;             // tipo del valor (PRIMITIVO) o del elemento (ARREGLO)
    private final String nombreTipoEstructura;  // solo si categoria == ESTRUCTURA (el primer ID: "{} Persona p")
    private final String nombre;                // nombre del parámetro

    private Parametro(CategoriaParametro categoria, NodoTipoRef tipo, String nombreTipoEstructura,
                       String nombre, int linea, int columna) {
        super(linea, columna);
        this.categoria = categoria;
        this.tipo = tipo;
        this.nombreTipoEstructura = nombreTipoEstructura;
        this.nombre = nombre;
    }

    public static Parametro primitivo(NodoTipoRef tipo, String nombre, int linea, int columna) {
        return new Parametro(CategoriaParametro.PRIMITIVO, tipo, null, nombre, linea, columna);
    }

    public static Parametro arreglo(NodoTipoRef tipoElemento, String nombre, int linea, int columna) {
        return new Parametro(CategoriaParametro.ARREGLO, tipoElemento, null, nombre, linea, columna);
    }

    public static Parametro estructura(String nombreTipoEstructura, String nombre, int linea, int columna) {
        return new Parametro(CategoriaParametro.ESTRUCTURA, null, nombreTipoEstructura, nombre, linea, columna);
    }

    public CategoriaParametro getCategoria() { return categoria; }
    public NodoTipoRef getTipo() { return tipo; }
    public String getNombreTipoEstructura() { return nombreTipoEstructura; }
    public String getNombre() { return nombre; }
}
