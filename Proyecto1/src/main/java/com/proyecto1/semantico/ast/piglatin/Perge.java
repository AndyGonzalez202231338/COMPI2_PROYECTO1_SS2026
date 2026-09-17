package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code PERGE ;} (#sentenciaPergeDef). Equivale a {@code Continuar} de Y. Sin datos propios más que la posición. */
public final class Perge extends NodoPigLatin implements InstruccionPigLatin {
    public Perge(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        if (!(ambito instanceof AmbitoBloque ab) || !ab.dentroDeAlgunCiclo())
            errores.reportar(linea, columna,
                    "'perge' solo puede usarse dentro de un ciclo");
        return TipoPrimitivo.VOID;
    }
}
