package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;

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

    public List<Importacion> getImportaciones() { return importaciones; }
    public List<InstruccionPigLatin> getVariablesGlobales() { return variablesGlobales; }
    public FuncionPrincipal getPrincipal() { return principal; }

    /**
     * Punto de entrada de la generación de C3D (es la raíz del AST). Delega todo en
     * {@link FuncionPrincipal#generarC3D(GeneradorC3D, List)}, pasándole las variables
     * globales para que las declare dentro del {@code main} — mismo patrón que
     * {@code Clase(Z)} pasando los atributos a cada constructor.
     *
     * <p>Las importaciones NO generan C3D propio: son metadata de compilación, ya
     * resueltas por {@code CargadorImports} antes de esta fase. Aquí se ignoran.
     *
     * <p>Devuelve {@code ResultadoC3D.vacio()}.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        principal.generarC3D(generador, variablesGlobales);
        return ResultadoC3D.vacio();
    }
}