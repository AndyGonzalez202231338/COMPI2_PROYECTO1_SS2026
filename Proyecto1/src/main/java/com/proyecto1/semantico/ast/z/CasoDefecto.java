package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * Un {@code defaultCase} (#defaultCaseDef): "default: statement* break?", dentro de un
 * {@link Elegir}. Misma forma que {@link CasoElegir} pero sin "valor" (no compara
 * contra nada). Tampoco es un nodo independiente, igual que {@link CasoElegir}.
 */
public final class CasoDefecto {

    private final List<InstruccionZ> instrucciones;
    private final boolean tieneRomper;

    public CasoDefecto(List<InstruccionZ> instrucciones, boolean tieneRomper) {
        this.instrucciones = instrucciones;
        this.tieneRomper = tieneRomper;
    }

    public List<InstruccionZ> getInstrucciones() {
        return instrucciones;
    }

    public boolean isTieneRomper() {
        return tieneRomper;
    }
}
