package com.proyecto1.semantico.ast.z;

/**
 * Un literal (#primarioEntero, #primarioFlotante, #primarioCaracter, #primarioCadena,
 * #primarioTrue, #primarioFalse, #primarioNull). Igual que en Y?, el valor ya viene
 * parseado a su tipo Java (Long/Double/Character/String/Boolean/null), no como texto
 * crudo. Para NULO, {@code valor} es simplemente {@code null}.
 */
public final class Literal extends NodoZ implements ExpresionZ {

    private final Object valor;
    private final CategoriaLiteral categoria;

    public Literal(Object valor, CategoriaLiteral categoria, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.categoria = categoria;
    }

    public Object getValor() {
        return valor;
    }

    public CategoriaLiteral getCategoria() {
        return categoria;
    }
}
