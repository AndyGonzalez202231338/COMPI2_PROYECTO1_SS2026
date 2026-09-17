package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoFuncion;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/** {@code returnStatement} (#returnStatementDef): "return expresion? ;". */
public final class Retorno extends NodoZ implements InstruccionZ {

    private final ExpresionZ valor; // null == "return;" sin valor

    public Retorno(ExpresionZ valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public ExpresionZ getValor() {
        return valor;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        AmbitoFuncion af = ambito.ambitoFuncionMasCercano();
        if (af == null) {
            errores.reportar(linea, columna, "'return' fuera de método/constructor");
            return TipoPrimitivo.VOID;
        }
        af.marcarTuvoRetorno();

        if (valor == null) {
            if (!af.esVoid())
                errores.reportar(linea, columna, "Se esperaba valor de retorno de tipo " + af.getTipoRetorno().nombre());
            return TipoPrimitivo.VOID;
        }
        Tipo tv = valor.verificar(ambito, errores);
        if (af.esVoid())
            errores.reportar(linea, columna, "El método es void, no debe retornar valor");
        else if (!Tipos.esAsignable(af.getTipoRetorno(), tv))
            errores.reportar(linea, columna,
                    "Tipo de retorno incompatible: se esperaba " + af.getTipoRetorno().nombre() +
                            ", se recibió " + tv.nombre());
        return TipoPrimitivo.VOID;
    }
}
