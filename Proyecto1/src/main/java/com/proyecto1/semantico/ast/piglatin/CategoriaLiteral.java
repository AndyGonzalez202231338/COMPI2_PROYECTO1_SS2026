package com.proyecto1.semantico.ast.piglatin;

/**
 * Qué tipo de literal es un {@link Literal}, para no tener que volver a mirar el
 * texto original. Incluye {@code NULO} (para {@code NULL}), que no existe en la
 * gramática de Y.
 */
public enum CategoriaLiteral {
    ENTERO,
    FLOTANTE,
    CARACTER,
    CADENA,
    BOOLEANO,
    NULO
}
