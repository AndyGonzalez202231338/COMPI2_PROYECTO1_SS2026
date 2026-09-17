package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoFuncion;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/** {@code RETORNAR expresion? NEWLINE} (#instRetorno). */
public final class Retorno extends NodoY implements InstruccionY {

    private final ExpresionY valor; // null == "retornar" sin valor

    public Retorno(ExpresionY valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public ExpresionY getValor() {
        return valor;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        AmbitoFuncion af = ambito.ambitoFuncionMasCercano();
        if (af == null) {
            errores.reportar(linea, columna, "'retornar' fuera de una función");
            return TipoPrimitivo.VOID;
        }
        af.marcarTuvoRetorno();

        if (valor == null) {
            if (!af.esVoid())
                errores.reportar(linea, columna, "Se esperaba un valor de retorno de tipo " + af.getTipoRetorno().nombre());
            return TipoPrimitivo.VOID;
        }
        Tipo tv = valor.verificar(ambito, errores);
        if (af.esVoid())
            errores.reportar(linea, columna, "La función no debe retornar valor");
        else if (!Tipos.esAsignable(af.getTipoRetorno(), tv))
            errores.reportar(linea, columna,
                    "Tipo de retorno incompatible: se esperaba " + af.getTipoRetorno().nombre() +
                            ", se recibió " + tv.nombre());
        return TipoPrimitivo.VOID;
    }
}
