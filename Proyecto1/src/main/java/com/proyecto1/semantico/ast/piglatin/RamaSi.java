package com.proyecto1.semantico.ast.piglatin;

/**
 * Una rama "si (cond) bloque" o "aliter (cond) bloque" dentro de un {@link Si}. No
 * implementa {@link com.proyecto1.semantico.ast.NodoAST} por sí sola (no es una
 * instrucción ni una expresión independiente, solo tiene sentido colgada de un
 * {@link Si}); no necesita línea/columna propias porque el nodo {@link Si} que la
 * contiene ya sabe su posición.
 */
public final class RamaSi {

    private final ExpresionPigLatin condicion;
    private final Bloque cuerpo;

    public RamaSi(ExpresionPigLatin condicion, Bloque cuerpo) {
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public ExpresionPigLatin getCondicion() {
        return condicion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
