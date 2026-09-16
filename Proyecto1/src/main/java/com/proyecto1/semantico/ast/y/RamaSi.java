package com.proyecto1.semantico.ast.y;

/**
 * Una rama "si (cond) entonces bloque" o "sino (cond) entonces bloque" dentro de un
 * {@link Si}. No implementa {@link com.proyecto1.semantico.ast.NodoAST} por sí sola
 * (no es una instrucción ni una expresión independiente, solo tiene sentido colgada de
 * un {@link Si}); no necesita línea/columna propias porque el nodo {@link Si} que la
 * contiene ya sabe su posición.
 */
public final class RamaSi {

    private final ExpresionY condicion;
    private final Bloque cuerpo;

    public RamaSi(ExpresionY condicion, Bloque cuerpo) {
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
