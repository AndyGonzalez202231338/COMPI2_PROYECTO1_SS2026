package com.proyecto1.semantico.ast.piglatin;

/**
 * Representa la regla {@code tipo} de la gramática ({@code #tipoNumerus, #tipoDecimalis,
 * #tipoTextum, #tipoLittera, #tipoFalsus, #tipoImportado}). Es una referencia
 * SINTÁCTICA nada más: guarda el nombre tal cual aparece en el código ("numerus",
 * "decimalis", "textum", "littera", "falsus", o el {@code ID} de un tipo importado) y
 * si es primitivo o no; todavía NO se resuelve contra la tabla de símbolos (eso es
 * parte de las validaciones semánticas).
 *
 * <p>No implementa {@link ExpresionPigLatin} ni {@link InstruccionPigLatin} porque una
 * referencia de tipo no es ni una expresión (no produce un valor en tiempo de
 * ejecución) ni una instrucción; solo aparece "colgada" de otros nodos
 * (declaraciones de variable, declaraciones de arreglo).
 */
public final class NodoTipoRef extends NodoPigLatin {

    private final String nombre;      // "numerus" | "decimalis" | "textum" | "littera" | "falsus" | <ID importado>
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
