package com.proyecto1.semantico.ast.z;

/**
 * Una expresión usada como instrucción suelta (#expressionStatementDef -> #stmtExpresion),
 * p. ej. "x = 5;", "obj.metodo();" o "contador++;" — cualquier {@link ExpresionZ}
 * (incluida una {@link Asignacion}, que en Z es una expresión) terminada en ';'.
 */
public final class ExpresionStmt extends NodoZ implements InstruccionZ {

    private final ExpresionZ expresion;

    public ExpresionStmt(ExpresionZ expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public ExpresionZ getExpresion() {
        return expresion;
    }
}
