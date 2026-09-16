package com.proyecto1.semantico.ast.y;

/** {@code RETORNAR expresion? NEWLINE} (#instRetorno). */
public final class Retorno extends NodoY implements InstruccionY {

    private final ExpresionY valor; // null == "retornar" sin valor

    public Retorno(ExpresionY valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public ExpresionY getValor() {
        return valor;
    }
}
