package com.proyecto1.semantico.ast.y;

/**
 * Operación unaria, prefija o postfija: !, - (negación aritmética), ++, --. Cubre
 * #expUnariaPrefijaDef (prefijo=true) y la parte opcional de #expPostfijaDef
 * (prefijo=false, solo aplica a ++/--).
 */
public final class Unaria extends NodoY implements ExpresionY {

    private final String operador;
    private final ExpresionY operando;
    private final boolean prefijo;

    public Unaria(String operador, ExpresionY operando, boolean prefijo, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.operando = operando;
        this.prefijo = prefijo;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionY getOperando() {
        return operando;
    }

    public boolean isPrefijo() {
        return prefijo;
    }
}
