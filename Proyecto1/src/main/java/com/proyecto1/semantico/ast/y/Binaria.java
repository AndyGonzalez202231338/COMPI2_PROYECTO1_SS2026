package com.proyecto1.semantico.ast.y;

/**
 * Cualquier operación binaria: ||, &&, ==, !=, =, ;=, +, -, *, /, %.
 * Cubre las etiquetas #expOrDef, #expAndDef, #expRelacionalDef (cuando trae operador),
 * #expAditivaDef y #expMultiplicativaDef los cinco niveles de precedencia de la
 * gramática se colapsan en esta única clase porque la precedencia ya quedó resuelta
 * por la FORMA del árbol que entrega ANTLR; una vez hecho ese trabajo, guardar "de qué
 * nivel de precedencia venía" no aporta nada.
 */
public final class Binaria extends NodoY implements ExpresionY {

    private final String operador; // texto exacto del operador: "||", "&&", "==", "+", ...
    private final ExpresionY izquierdo;
    private final ExpresionY derecho;

    public Binaria(String operador, ExpresionY izquierdo, ExpresionY derecho, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionY getIzquierdo() {
        return izquierdo;
    }

    public ExpresionY getDerecho() {
        return derecho;
    }
}
