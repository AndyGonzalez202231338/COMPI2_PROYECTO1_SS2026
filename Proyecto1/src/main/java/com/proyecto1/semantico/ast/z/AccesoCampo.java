package com.proyecto1.semantico.ast.z;

/** {@code primaryExpression PUNTO ID} (#primarioCampo): "objeto.campo". */
public final class AccesoCampo extends NodoZ implements ExpresionZ {

    private final ExpresionZ objeto;
    private final String campo;

    public AccesoCampo(ExpresionZ objeto, String campo, int linea, int columna) {
        super(linea, columna);
        this.objeto = objeto;
        this.campo = campo;
    }

    public ExpresionZ getObjeto() {
        return objeto;
    }
    public String getCampo() {
        return campo;
    }
}
