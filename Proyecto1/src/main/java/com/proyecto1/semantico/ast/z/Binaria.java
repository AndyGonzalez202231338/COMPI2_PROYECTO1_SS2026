package com.proyecto1.semantico.ast.z;

/**
 * Cualquier operación binaria: ||, &&, ==, !=, <, >, <=, >=, +, -, *, /, %. Igual que
 * en Y?, los niveles de precedencia se colapsan en esta única clase (#logicalOrExpressionDef,
 * #logicalAndExpressionDef, #equalityExpressionDef, #relationalExpressionDef,
 * #additiveExpressionDef, #multiplicativeExpressionDef): la precedencia ya quedó
 * resuelta por la FORMA del árbol que entrega ANTLR.
 */
public final class Binaria extends NodoZ implements ExpresionZ {

    private final String operador;
    private final ExpresionZ izquierdo;
    private final ExpresionZ derecho;

    public Binaria(String operador, ExpresionZ izquierdo, ExpresionZ derecho, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionZ getIzquierdo() {
        return izquierdo;
    }

    public ExpresionZ getDerecho() {
        return derecho;
    }
}
