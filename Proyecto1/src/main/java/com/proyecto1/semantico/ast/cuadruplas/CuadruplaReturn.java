package com.proyecto1.semantico.ast.cuadruplas;

/** {@code return valor} o {@code return} sin valor (valor == null). */
public record CuadruplaReturn(String valor) implements Cuadrupla {
    @Override
    public String toStringLegible() { return valor != null ? "return " + valor : "return"; }

    @Override
    public <T> T aceptar(VisitanteCuadrupla<T> visitante) { return visitante.visitar(this); }
}
