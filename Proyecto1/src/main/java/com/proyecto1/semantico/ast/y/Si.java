package com.proyecto1.semantico.ast.y;

import java.util.List;

/**
 * {@code instruccionSi} (#condicionSiDef): la rama "si" más cero o más "sino" con su
 * propia condición, y opcionalmente un "contrario" final sin condición.
 */
public final class Si extends NodoY implements InstruccionY {

    private final List<RamaSi> ramas;    // ramas.get(0) es el "si"; el resto son los "sino"
    private final Bloque contrario;       // null si no hay "contrario"

    public Si(List<RamaSi> ramas, Bloque contrario, int linea, int columna) {
        super(linea, columna);
        this.ramas = ramas;
        this.contrario = contrario;
    }

    public List<RamaSi> getRamas() {
        return ramas;
    }

    public Bloque getContrario() {
        return contrario;
    }
}
