package com.proyecto1.semantico.ast.z;

/** {@code returnStatement} (#returnStatementDef): "return expresion? ;". */
public final class Retorno extends NodoZ implements InstruccionZ {

    private final ExpresionZ valor; // null == "return;" sin valor

    public Retorno(ExpresionZ valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public ExpresionZ getValor() {
        return valor;
    }
}
