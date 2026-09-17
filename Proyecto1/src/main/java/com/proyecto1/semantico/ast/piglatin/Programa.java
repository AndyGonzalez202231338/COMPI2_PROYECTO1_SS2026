package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * Nodo raíz del AST de PigLatin: {@code programa} ({@code importaciones? seccionVariables?
 * funcionPrincipal EOF}). {@code seccionVariables} no tiene su propio nodo (no aporta
 * nada por sí sola, es solo "una lista de declaraciones"): sus declaraciones
 * ({@link DeclaracionVariable} / {@link DeclaracionArreglo}) se guardan directamente
 * aquí en {@link #getVariablesGlobales()}, igual que {@code Programa} de Y guarda
 * directamente las listas de sus secciones.
 */
public final class Programa extends NodoPigLatin {

    private final List<Importacion> importaciones;
    private final List<InstruccionPigLatin> variablesGlobales; // DeclaracionVariable | DeclaracionArreglo
    private final FuncionPrincipal principal;

    public Programa(List<Importacion> importaciones, List<InstruccionPigLatin> variablesGlobales,
                     FuncionPrincipal principal, int linea, int columna) {
        super(linea, columna);
        this.importaciones = importaciones;
        this.variablesGlobales = variablesGlobales;
        this.principal = principal;
    }

    public List<Importacion> getImportaciones() {
        return importaciones;
    }

    public List<InstruccionPigLatin> getVariablesGlobales() {
        return variablesGlobales;
    }

    public FuncionPrincipal getPrincipal() {
        return principal;
    }
}
