package com.proyecto1.semantico.ast.y;

/**
 * Un {@code casoElegir} (#casoDef): "caso literal: bloque", dentro de un {@link Elegir}.
 * Igual que {@link RamaSi}, no es un {@link com.proyecto1.semantico.ast.NodoAST}
 * independiente: solo tiene sentido como pieza de un {@link Elegir}.
 */
public final class CasoElegir {

    private final Literal valor;
    private final Bloque cuerpo;

    public CasoElegir(Literal valor, Bloque cuerpo) {
        this.valor = valor;
        this.cuerpo = cuerpo;
    }

    public Literal getValor() {
        return valor;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
