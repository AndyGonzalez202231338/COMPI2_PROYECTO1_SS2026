package com.proyecto1.semantico.ast.cuadruplas;

/**
 * Subconjunto de {@link Cuadrupla} que representa un salto cuyo destino puede no
 * conocerse todavía en el momento de emitirlo (backpatching): {@code goto},
 * {@code if_false}, {@code if_true}. Es la única familia de instrucciones sobre la
 * que tiene sentido "reemplazar la etiqueta destino después de emitida" — por eso
 * {@link #conResultado(String)} vive aquí y no en {@link Cuadrupla}: para cualquier
 * otra instrucción (un {@code print}, un {@code call}) "cambiarle el resultado"
 * no tiene un significado sensato, y con esta interfaz separada el compilador ya no
 * deja intentarlo por error.
 *
 * Los 4 sitios del proyecto que hacen backpatching (Si de Y, Si de PigLatin, Elegir
 * de Y, Elegir de Z) obtienen la cuádrupla por índice como {@code Cuadrupla} y
 * necesitan castear a {@code CuadruplaSalto} antes de llamar
 * {@code conResultado(...)} — es el único ajuste que este cambio les pide, ya que
 * antes {@code conResultado} vivía en la interfaz genérica.
 */
public sealed interface CuadruplaSalto extends Cuadrupla
        permits CuadruplaGoto, CuadruplaIfFalse, CuadruplaIfTrue {

    /** La etiqueta destino actual del salto (para leerla sin castear a la subclase concreta). */
    String getEtiquetaDestino();

    /** Devuelve una copia de esta misma instrucción con otra etiqueta destino. */
    CuadruplaSalto conResultado(String nuevaEtiquetaDestino);
}
