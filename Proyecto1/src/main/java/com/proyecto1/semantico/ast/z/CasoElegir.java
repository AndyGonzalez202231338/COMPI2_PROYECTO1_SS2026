package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * Un {@code switchCase} (#switchCaseDef): "case expresion: statement* break?", dentro
 * de un {@link Elegir}. A diferencia del {@code CasoElegir} de Y? (que exige un
 * {@code literal} y envuelve el cuerpo en un {@link Bloque}), aquí "valor" es una
 * {@link ExpresionZ} genérica (así lo permite la gramática de Z) y las instrucciones
 * van sueltas en una lista, sin "{}" propio (el propio switch ya las delimita). No es
 * un {@link com.proyecto1.semantico.ast.NodoAST} independiente, igual que su
 * equivalente de Y?: solo tiene sentido colgado de un {@link Elegir}.
 */
public final class CasoElegir {

    private final ExpresionZ valor;
    private final List<InstruccionZ> instrucciones;
    private final boolean tieneRomper; // false == cae al siguiente caso (fall-through)

    public CasoElegir(ExpresionZ valor, List<InstruccionZ> instrucciones, boolean tieneRomper) {
        this.valor = valor;
        this.instrucciones = instrucciones;
        this.tieneRomper = tieneRomper;
    }

    public ExpresionZ getValor() {
        return valor;
    }

    public List<InstruccionZ> getInstrucciones() {
        return instrucciones;
    }

    public boolean isTieneRomper() {
        return tieneRomper;
    }
}
