package com.proyecto1.semantico.ast.y;

/**
 * Representa la regla {@code tipo} de la gramática ({@code tipoEntero, tipoFlotante,
 * tipoCaracter, tipoCadena, tipoBool, tipoEstructura}). Es una referencia SINTÁCTICA
 * nada más: guarda el nombre tal cual aparece en el código ("entero", "flotante",
 * "Persona", ...) y si es primitivo o no; todavía NO se resuelve contra la tabla de
 * símbolos (comprobar que "Persona" en verdad exista como estructura ya
 * definida antes es parte de las validaciones semánticas ).
 *
 * No implementa {@link ExpresionY} ni {@link InstruccionY} porque una referencia de
 * tipo no es ni una expresión (no produce un valor en tiempo de ejecución) ni una
 * instrucción; solo aparece "colgada" de otros nodos (parámetros, campos,
 * declaraciones, tipo de retorno).
 */
public final class NodoTipoRef extends NodoY {

    private final String nombre;      // "entero" | "flotante" | "caracter" | "cadena" | "bool" | <ID de estructura>
    private final boolean esPrimitivo;

    public NodoTipoRef(String nombre, boolean esPrimitivo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.esPrimitivo = esPrimitivo;
    }

    public String getNombre() {
        return nombre;
    }
    public boolean isEsPrimitivo() {
        return esPrimitivo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
