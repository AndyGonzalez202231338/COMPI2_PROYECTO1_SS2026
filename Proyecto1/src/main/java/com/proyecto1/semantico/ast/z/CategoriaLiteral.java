package com.proyecto1.semantico.ast.z;

/**
 * Qué tipo de literal es un {@link Literal} de Zetariano. Igual que la de Y? pero con
 * un caso más: NULO, para el literal {@code null} (#primarioNull), que Y? no tiene.
 */
public enum CategoriaLiteral {
    ENTERO,
    FLOTANTE,
    CARACTER,
    CADENA,
    BOOLEANO,
    NULO
}
