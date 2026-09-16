package com.proyecto1.semantico.ast.z;

/** {@code PRINT LPAREN expression RPAREN} (#primarioPrint): imprime sin salto de línea. */
public final class Print extends NodoZ implements ExpresionZ {

    private final ExpresionZ argumento;

    public Print(ExpresionZ argumento, int linea, int columna) {
        super(linea, columna);
        this.argumento = argumento;
    }

    public ExpresionZ getArgumento() {
        return argumento;
    }
}
