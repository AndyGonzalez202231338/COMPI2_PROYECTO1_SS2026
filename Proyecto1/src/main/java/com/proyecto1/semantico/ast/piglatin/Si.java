package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * {@code sentenciaSi} (#sentenciaSiDef): {@code si (cond) bloque (aliter (cond) bloque)*
 * (aliter bloque)? finis;}. La rama "si" más cero o más "aliter" con su propia
 * condición, y opcionalmente un "aliter" final sin condición (equivalente al
 * "contrario" de Y).
 */
public final class Si extends NodoPigLatin implements InstruccionPigLatin {

    private final List<RamaSi> ramas;   // ramas.get(0) es el "si"; el resto son los "aliter (cond)"
    private final Bloque contrario;     // null si no hay "aliter" final sin condición

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
