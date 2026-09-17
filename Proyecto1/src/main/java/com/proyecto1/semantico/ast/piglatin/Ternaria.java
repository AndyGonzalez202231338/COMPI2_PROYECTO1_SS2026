package com.proyecto1.semantico.ast.piglatin;

/**
 * {@code expresionCondicional} (#expresionCondicionalDef), cuando trae el operador
 * ternario: {@code expresionOr ? expresion : expresionCondicional}. Sin equivalente
 * en Y (esa gramática no tiene ternario); se agrega porque PigLatin sí lo admite.
 */
public final class Ternaria extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin condicion;
    private final ExpresionPigLatin siVerdadero;
    private final ExpresionPigLatin siFalso;

    public Ternaria(ExpresionPigLatin condicion, ExpresionPigLatin siVerdadero, ExpresionPigLatin siFalso,
                     int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.siVerdadero = siVerdadero;
        this.siFalso = siFalso;
    }

    public ExpresionPigLatin getCondicion() {
        return condicion;
    }

    public ExpresionPigLatin getSiVerdadero() {
        return siVerdadero;
    }

    public ExpresionPigLatin getSiFalso() {
        return siFalso;
    }
}
