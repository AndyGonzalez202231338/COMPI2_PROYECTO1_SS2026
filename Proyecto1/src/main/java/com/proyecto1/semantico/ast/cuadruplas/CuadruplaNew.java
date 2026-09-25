package com.proyecto1.semantico.ast.cuadruplas;

/** {@code destino = new clase} — Fase 4 lo traduce a malloc(sizeof(clase)). */
public record CuadruplaNew(String clase, String destino) implements Cuadrupla {
    @Override
    public String toStringLegible() { return destino + " = new " + clase; }

    @Override
    public <T> T aceptar(VisitanteCuadrupla<T> visitante) { return visitante.visitar(this); }
}
