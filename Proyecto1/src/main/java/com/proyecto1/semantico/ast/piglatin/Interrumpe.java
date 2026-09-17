package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code INTERRUMPE ;} (#sentenciaInterrumpeDef). Equivale a {@code Romper} de Y. Sin datos propios más que la posición. */
public final class Interrumpe extends NodoPigLatin implements InstruccionPigLatin {
    public Interrumpe(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        if (!(ambito instanceof AmbitoBloque ab) || !ab.dentroDeAlgunCiclo())
            errores.reportar(linea, columna,
                    "'interrumpe' solo puede usarse dentro de un ciclo");
        return TipoPrimitivo.VOID;
    }
}
