package com.proyecto1.semantico.ast.z;

/**
 * Operación unaria, prefija o postfija: !, - (negación aritmética), ++, --. Cubre
 * #unaryNegacionDef, #unaryMenosDef, #unaryIncrementoPrefijoDef, #unaryDecrementoPrefijoDef
 * (prefijo=true) y la parte opcional de #postfixExpressionDef (prefijo=false).
 */
public final class Unaria extends NodoZ implements ExpresionZ {

    private final String operador;
    private final ExpresionZ operando;
    private final boolean prefijo;

    public Unaria(String operador, ExpresionZ operando, boolean prefijo, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.operando = operando;
        this.prefijo = prefijo;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionZ getOperando() {
        return operando;
    }

    public boolean isPrefijo() {
        return prefijo;
    }
}
