package com.proyecto1.semantico.ast.z;

/**
 * {@code conditionalExpression} (#conditionalExpressionDef) cuando trae el '?:':
 * "condicion ? siVerdadero : siFalso". No existe equivalente en Y? — es exclusivo
 * de Zetariano.
 */
public final class Ternario extends NodoZ implements ExpresionZ {

    private final ExpresionZ condicion;
    private final ExpresionZ siVerdadero;
    private final ExpresionZ siFalso;

    public Ternario(ExpresionZ condicion, ExpresionZ siVerdadero, ExpresionZ siFalso, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.siVerdadero = siVerdadero;
        this.siFalso = siFalso;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }

    public ExpresionZ getSiVerdadero() {
        return siVerdadero;
    }

    public ExpresionZ getSiFalso() {
        return siFalso;
    }
}
