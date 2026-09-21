package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

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

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // Cada rama (si / sino) tiene su condicion (bool) y su propio sub-ambito, igual que en Z.
        // El ambito de cada rama es hijo del actual, asi un 'continuar'/'romper' dentro de un si
        // que esta dentro de un ciclo sigue viendo el ciclo (dentroDeAlgunCiclo sube por los padres).
        for (RamaSi rama : ramas) {
            Tipo tc = rama.getCondicion().verificar(ambito, errores);
            if (!Tipos.esBooleano(tc))
                errores.reportar(rama.getCondicion().getLinea(), rama.getCondicion().getColumna(),
                        "La condición de 'si' debe ser bool, se recibió " + tc.nombre());
            rama.getCuerpo().verificar(new AmbitoBloque(ambito, false), errores);
        }
        if (contrario != null) {
            contrario.verificar(new AmbitoBloque(ambito, false), errores);
        }
        return TipoPrimitivo.VOID;
    }
}