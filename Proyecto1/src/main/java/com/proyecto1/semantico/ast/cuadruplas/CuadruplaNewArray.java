package com.proyecto1.semantico.ast.cuadruplas;

/**
 * {@code destino = new tipoElemento[tamano]}. "tamano" queda como String (no int)
 * a propósito: puede ser un literal o el lugar de una variable/temporal calculado
 * en tiempo de ejecución (p. ej. "new entero[n]").
 */
public record CuadruplaNewArray(String tipoElemento, String tamano, String destino) implements Cuadrupla {
    @Override
    public String toStringLegible() { return destino + " = new " + tipoElemento + "[" + tamano + "]"; }

    @Override
    public <T> T aceptar(VisitanteCuadrupla<T> visitante) { return visitante.visitar(this); }
}
