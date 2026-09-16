package com.proyecto1.semantico.ast.z;

/**
 * {@code ifStatement} (#ifStatementDef): "if (cond) entonces (else contrario)?".
 * A diferencia del {@code Si} de Y? (que junta todas las ramas "sino" en una lista
 * porque la gramática de Y? las repite con "*"), aquí NO hace falta: en Z un
 * "else if (...) ..." es sintácticamente solo "ELSE statement" donde ese statement
 * ES otro {@code Si} — el encadenado sale gratis anidando este mismo nodo.
 *
 * "entonces"/"contrario" son {@link InstruccionZ} (no {@link Bloque}) porque en Z las
 * llaves son opcionales para una sola sentencia ("if (x > 0) return x;" es válido).
 */
public final class Si extends NodoZ implements InstruccionZ {

    private final ExpresionZ condicion;
    private final InstruccionZ entonces;
    private final InstruccionZ contrario; // null si no hay "else"

    public Si(ExpresionZ condicion, InstruccionZ entonces, InstruccionZ contrario, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.entonces = entonces;
        this.contrario = contrario;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }
    public InstruccionZ getEntonces() {
        return entonces;
    }

    public InstruccionZ getContrario() {
        return contrario;
    }
}
