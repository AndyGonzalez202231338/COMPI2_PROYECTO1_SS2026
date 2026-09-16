package com.proyecto1.semantico.ast.y;

/** {@code instruccionHacerMientras} (#cicloHacerMientrasDef): "hacer: cuerpo mientras(cond)". */
public final class HacerMientras extends NodoY implements InstruccionY {

    private final Bloque cuerpo;
    private final ExpresionY condicion;

    public HacerMientras(Bloque cuerpo, ExpresionY condicion, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = cuerpo;
        this.condicion = condicion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }

    public ExpresionY getCondicion() {
        return condicion;
    }
}
