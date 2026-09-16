package com.proyecto1.semantico.ast.y;

/** {@code instruccionMientras} (#cicloMientrasDef): "mientras(cond) hacer cuerpo". */
public final class Mientras extends NodoY implements InstruccionY {

    private final ExpresionY condicion;
    private final Bloque cuerpo;

    public Mientras(ExpresionY condicion, Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public ExpresionY getCondicion() {
        return condicion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
