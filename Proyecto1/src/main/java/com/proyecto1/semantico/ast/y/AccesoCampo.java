package com.proyecto1.semantico.ast.y;

/** {@code primaria PUNTO ID} (#primariaCampo): "objeto.campo". */
public final class AccesoCampo extends NodoY implements ExpresionY {

    private final ExpresionY objeto;
    private final String campo;

    public AccesoCampo(ExpresionY objeto, String campo, int linea, int columna) {
        super(linea, columna);
        this.objeto = objeto;
        this.campo = campo;
    }

    public ExpresionY getObjeto() {
        return objeto;
    }
    public String getCampo() {
        return campo;
    }
}
