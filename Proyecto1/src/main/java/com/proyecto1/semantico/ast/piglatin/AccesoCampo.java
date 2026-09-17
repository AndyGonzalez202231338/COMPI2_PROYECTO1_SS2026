package com.proyecto1.semantico.ast.piglatin;

/** {@code primaria . ID} (#primariaCampo): "objeto.campo". */
public final class AccesoCampo extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objeto;
    private final String campo;

    public AccesoCampo(ExpresionPigLatin objeto, String campo, int linea, int columna) {
        super(linea, columna);
        this.objeto = objeto;
        this.campo = campo;
    }

    public ExpresionPigLatin getObjeto() {
        return objeto;
    }
    public String getCampo() {
        return campo;
    }
}
