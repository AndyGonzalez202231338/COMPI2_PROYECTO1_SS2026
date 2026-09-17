package com.proyecto1.semantico.ast.piglatin;

/**
 * Cualquier operación binaria: {@code ||, &&, ==, !=, <, >, <=, >=, +, -, *, /, %}.
 * Cubre las etiquetas #expresionOrDef, #expresionAndDef, #expresionIgualdadDef,
 * #expresionRelacionalDef, #expresionAditivaDef y #expresionMultiplicativaDef (cuando
 * traen operador) — los seis niveles de precedencia de la gramática se colapsan en
 * esta única clase porque la precedencia ya quedó resuelta por la FORMA del árbol que
 * entrega ANTLR; una vez hecho ese trabajo, guardar "de qué nivel de precedencia
 * venía" no aporta nada.
 */
public final class Binaria extends NodoPigLatin implements ExpresionPigLatin {

    private final String operador; // texto exacto del operador: "||", "&&", "==", "+", ...
    private final ExpresionPigLatin izquierdo;
    private final ExpresionPigLatin derecho;

    public Binaria(String operador, ExpresionPigLatin izquierdo, ExpresionPigLatin derecho,
                    int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionPigLatin getIzquierdo() {
        return izquierdo;
    }

    public ExpresionPigLatin getDerecho() {
        return derecho;
    }
}
