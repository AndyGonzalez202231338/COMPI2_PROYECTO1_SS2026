package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code breakStatement} (#breakStatementDef). Sin datos propios más que la posición. */
public final class Romper extends NodoZ implements InstruccionZ {
    public Romper(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // "break" es válido dentro de un ciclo O dentro de un caso/siempre de un switch.
        // Se consulta dentroDeAlgoRompible() y no dentroDeAlgunCiclo(): esta última solo
        // habilita "continue", que no tiene sentido dentro de un switch suelto.
        if (!(ambito instanceof AmbitoBloque ab) || !ab.dentroDeAlgoRompible())
            errores.reportar(linea, columna,
                    "'break' solo puede usarse dentro de un ciclo o switch");
        return TipoPrimitivo.VOID;
    }

    /**
     * Emite {@code (goto, null, null, L)} donde L es {@code generador.etiquetaFinCiclo()}:
     * el destino de "break" más cercano, ya sea el L_fin de un ciclo (empujado con
     * {@code entrarCiclo}) o el L_fin de un switch (empujado con
     * {@code entrarBloqueRompible} por {@link Elegir#generarC3D}).
     * Si la pila está vacía lanza {@link IllegalStateException}: verificar() ya reportó
     * el error, esto solo ocurre si se generó C3D sin análisis previo.
     * Devuelve {@code ResultadoC3D.vacio()}.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        String destino = generador.etiquetaFinCiclo();
        if (destino == null) {
            throw new IllegalStateException("'break' fuera de un ciclo o switch (línea "
                    + linea + ", columna " + columna + ")");
        }
        generador.emitirGoto(destino);
        return ResultadoC3D.vacio();
    }
}