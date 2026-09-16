package com.proyecto1.semantico.ast.y;

/** Una expresión usada como instrucción suelta (#instExpresion), p. ej. "leer();" o "contador++;". */
public final class ExpresionStmt extends NodoY implements InstruccionY {

    private final ExpresionY expresion;

    public ExpresionStmt(ExpresionY expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public ExpresionY getExpresion() {
        return expresion;
    }
}
