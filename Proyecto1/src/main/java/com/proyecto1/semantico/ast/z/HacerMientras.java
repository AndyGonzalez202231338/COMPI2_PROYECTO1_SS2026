package com.proyecto1.semantico.ast.z;

/** {@code doWhileStatement} (#doWhileStatementDef): "do cuerpo while(cond);". */
public final class HacerMientras extends NodoZ implements InstruccionZ {

    private final InstruccionZ cuerpo;
    private final ExpresionZ condicion;

    public HacerMientras(InstruccionZ cuerpo, ExpresionZ condicion, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = cuerpo;
        this.condicion = condicion;
    }

    public InstruccionZ getCuerpo() {
        return cuerpo;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }
}
