package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

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

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        for (RamaSi rama : ramas) {
            Tipo tc = rama.getCondicion().verificar(ambito, errores);
            if (!Tipos.esBooleano(tc))
                errores.reportar(rama.getCondicion().getLinea(), rama.getCondicion().getColumna(),
                        "Condición del 'si' debe ser bool");
            AmbitoBloque amb = new AmbitoBloque(ambito, false);
            rama.getCuerpo().verificar(amb, errores);
        }
        if (contrario != null) {
            AmbitoBloque amb = new AmbitoBloque(ambito, false);
            contrario.verificar(amb, errores);
        }
        return TipoPrimitivo.VOID;
    }
}
