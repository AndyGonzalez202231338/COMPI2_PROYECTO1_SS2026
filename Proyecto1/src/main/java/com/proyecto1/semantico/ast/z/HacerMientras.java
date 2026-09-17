package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/** {@code doWhileStatement} (#doWhileStatementDef): "do cuerpo while(cond);". */
public final class HacerMientras extends NodoZ implements InstruccionZ {

    private final InstruccionZ cuerpo;
    private final ExpresionZ condicion;

    public HacerMientras(InstruccionZ cuerpo, ExpresionZ condicion, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = cuerpo;
        this.condicion = condicion;
    }

    public InstruccionZ getCuerpo() {
        return cuerpo;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        AmbitoBloque amb = new AmbitoBloque(ambito, true);
        cuerpo.verificar(amb, errores);

        Tipo tc = condicion.verificar(ambito, errores);
        if (!Tipos.esBooleano(tc))
            errores.reportar(condicion.getLinea(), condicion.getColumna(),
                    "Condición del 'do-while' debe ser bool");
        return TipoPrimitivo.VOID;
    }
}
