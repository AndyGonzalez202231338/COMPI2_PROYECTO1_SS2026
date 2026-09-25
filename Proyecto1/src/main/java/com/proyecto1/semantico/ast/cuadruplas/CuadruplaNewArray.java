package com.proyecto1.semantico.ast.cuadruplas;

import java.util.List;

/**
 * {@code destino = new tipoElemento[t1][t2]...[tn]}. Los tamaños son Strings (no
 * int) a propósito: pueden ser literales ("5", "3") o el lugar de una
 * variable/temporal calculado en tiempo de ejecución (p. ej. "new int[n]").
 *
 * <p>La lista "tamanos" contiene:
 * <ul>
 *   <li>En FLAT: TODAS las dimensiones, en orden del externo al interno.</li>
 *   <li>En JAGGED: solo el tamaño del nivel externo. Los niveles internos se
 *       construyen con cuádruplas adicionales emitidas por el nodo Z
 *       NuevoArregloConTamano con bucles.</li>
 * </ul>
 * Esta cuádrupla es agnóstica a la estrategia: el nodo del AST ya decidió cuál
 * usar antes de emitirla.
 */
public record CuadruplaNewArray(String tipoElemento, List<String> tamanos, String destino) implements Cuadrupla {

    @Override
    public String toStringLegible() {
        StringBuilder sb = new StringBuilder(destino).append(" = new ").append(tipoElemento);
        for (String t : tamanos) sb.append("[").append(t).append("]");
        return sb.toString();
    }

    @Override
    public <T> T aceptar(VisitanteCuadrupla<T> visitante) { return visitante.visitar(this); }
}