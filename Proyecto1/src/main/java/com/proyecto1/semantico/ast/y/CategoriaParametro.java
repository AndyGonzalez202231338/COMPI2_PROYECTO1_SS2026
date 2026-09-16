package com.proyecto1.semantico.ast.y;

/**
 * Categoría de un {@link Parametro}, según cuál de las 3 alternativas de la regla
 * {@code parametro} se usó: {@code #parametroPrimitivo}, {@code #parametroArreglo} o
 * {@code #parametroEstructura}. Se guarda como campo dentro de {@link Parametro} en vez
 * de tener tres clases de parámetro distintas, porque las tres comparten la misma forma
 * final (un tipo + un nombre) y lo único que cambia es cómo se pasan (valor vs.
 * referencia), que es justo lo que esta categoría representa.
 */
public enum CategoriaParametro {
    PRIMITIVO,
    ARREGLO,
    ESTRUCTURA
}
