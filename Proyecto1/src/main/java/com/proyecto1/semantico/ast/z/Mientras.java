package com.proyecto1.semantico.ast.z;

/** {@code whileStatement} (#whileStatementDef): "while(cond) cuerpo". */
public final class Mientras extends NodoZ implements InstruccionZ {

    private final ExpresionZ condicion;
    private final InstruccionZ cuerpo; // no siempre Bloque: llaves opcionales en Z

    public Mientras(ExpresionZ condicion, InstruccionZ cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }

    public InstruccionZ getCuerpo() {
        return cuerpo;
    }
}
