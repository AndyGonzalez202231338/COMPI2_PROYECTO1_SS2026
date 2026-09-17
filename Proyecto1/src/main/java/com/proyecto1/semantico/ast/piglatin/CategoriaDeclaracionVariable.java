package com.proyecto1.semantico.ast.piglatin;

/**
 * Categoría de una {@link DeclaracionVariable}, según cuál de las 3 alternativas de
 * la regla {@code declaracionVariableSinPuntoYComa} se usó:
 * <ul>
 *   <li>{@code #declaracionVarConTipo}: {@code esto ID : tipo (= expresion)?} — el
 *       tipo viene explícito.</li>
 *   <li>{@code #declaracionVarEstructura}: {@code esto ID : ID inicializadorArreglo} —
 *       el "tipo" es el nombre de una estructura/clase importada y SIEMPRE trae un
 *       inicializador ({@code { ... }}).</li>
 *   <li>{@code #declaracionVarSoloValor}: {@code esto ID : expresion} — sin tipo
 *       explícito; se infiere del valor.</li>
 * </ul>
 */
public enum CategoriaDeclaracionVariable {
    CON_TIPO,
    ESTRUCTURA,
    SOLO_VALOR
}
