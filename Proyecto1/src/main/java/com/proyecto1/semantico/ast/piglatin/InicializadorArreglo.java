package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * {@code inicializadorArreglo} (#inicializadorArregloDef): {@code { expresion (, expresion)* }}.
 * Cumple dos roles, igual que {@code ListaLiteral} en Y:
 * <ul>
 *   <li>Estructural: como inicializador de {@link DeclaracionArreglo} y de la
 *       variante ESTRUCTURA de {@link DeclaracionVariable}.</li>
 *   <li>Como expresión: {@code primaria} también la referencia directamente
 *       ({@code #primariaListaLiteral}), por lo que implementa {@link ExpresionPigLatin}
 *       para poder aparecer en cualquier lugar donde se espera una expresión.</li>
 * </ul>
 */
public final class InicializadorArreglo extends NodoPigLatin implements ExpresionPigLatin {

    private final List<ExpresionPigLatin> elementos;

    public InicializadorArreglo(List<ExpresionPigLatin> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<ExpresionPigLatin> getElementos() {
        return elementos;
    }
}
