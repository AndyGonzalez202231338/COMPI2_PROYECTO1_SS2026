package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * {@code primaryExpression LPAREN argumentList? RPAREN} (#primarioLlamada): llamada a
 * método/función. Cubre tanto "metodo(args)" sueltas (objetivo = {@link Identificador})
 * como encadenadas "obj.metodo(args)" (objetivo = {@link AccesoCampo}) — misma unificación
 * que en Y?.
 */
public final class Llamada extends NodoZ implements ExpresionZ {

    private final ExpresionZ objetivo;
    private final List<ExpresionZ> argumentos;

    public Llamada(ExpresionZ objetivo, List<ExpresionZ> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionZ getObjetivo() {
        return objetivo;
    }

    public List<ExpresionZ> getArgumentos() {
        return argumentos;
    }
}
