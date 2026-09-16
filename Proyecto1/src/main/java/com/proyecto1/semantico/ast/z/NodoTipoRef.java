package com.proyecto1.semantico.ast.z;

/**
 * Representa la regla {@code tipo} de Zetariano ({@code tipoBase (CORIZQ CORDER)*}).
 * A diferencia del {@code NodoTipoRef} de Y? (donde el arreglo se declara aparte, en
 * el sitio de la declaración, con tamaños constantes), en Z los corchetes son parte
 * del TIPO mismo ("int[]", "int[][]", sin tamaño: el tamaño real se decide en tiempo
 * de ejecución con "new"), así que aquí sí se guarda {@code dimensiones}.
 *
 * Sigue siendo una referencia puramente SINTÁCTICA: no se resuelve todavía contra la
 * tabla de símbolos si un ID de clase en verdad existe (eso es semántico, Parte 2).
 */
public final class NodoTipoRef {

    private final String nombreBase;   // "int" | "double" | "char" | "boolean" | "String" | <ID de clase>
    private final boolean esPrimitivo;
    private final int dimensiones;     // 0 = escalar, 1 = "tipo[]", 2 = "tipo[][]", ...

    public NodoTipoRef(String nombreBase, boolean esPrimitivo, int dimensiones) {
        this.nombreBase = nombreBase;
        this.esPrimitivo = esPrimitivo;
        this.dimensiones = dimensiones;
    }

    public String getNombreBase() {
        return nombreBase;
    }

    public boolean isEsPrimitivo() {
        return esPrimitivo;
    }

    public int getDimensiones() {
        return dimensiones;
    }

    public boolean esArreglo() {
        return dimensiones > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(nombreBase);
        for (int i = 0; i < dimensiones; i++) sb.append("[]");
        return sb.toString();
    }
}
