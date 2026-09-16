package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.ast.NodoAST;

/**
 * Marca los nodos que representan una EXPRESIÓN de Zetariano (producen un valor / un
 * tipo). Mismo rol que {@code ExpresionY}: no agrega métodos, solo permite escribir
 * {@code List<ExpresionZ>} en vez de {@code List<NodoAST>}.
 *
 * OJO: a diferencia de Y, aquí también implementa esta interfaz {@link Asignacion}
 * en la gramática de Z la asignación vive dentro de "assignmentExpression", no es una
 * instrucción aparte.
 */
public interface ExpresionZ extends NodoAST {
}
