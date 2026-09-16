package com.proyecto1.semantico.ast.z;

/** {@code PRINTLN LPAREN expression RPAREN} (#primarioPrintln): imprime con salto de línea. */
public final class Println extends NodoZ implements ExpresionZ {

    private final ExpresionZ argumento;

    public Println(ExpresionZ argumento, int linea, int columna) {
        super(linea, columna);
        this.argumento = argumento;
    }

    public ExpresionZ getArgumento() {
        return argumento;
    }
}
