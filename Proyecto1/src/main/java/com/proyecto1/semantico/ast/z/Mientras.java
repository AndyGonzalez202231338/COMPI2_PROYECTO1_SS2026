package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/** {@code whileStatement} (#whileStatementDef): "while(cond) cuerpo". */
public final class Mientras extends NodoZ implements InstruccionZ {

    private final ExpresionZ condicion;
    private final InstruccionZ cuerpo; // no siempre Bloque: llaves opcionales en Z

    public Mientras(ExpresionZ condicion, InstruccionZ cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }

    public InstruccionZ getCuerpo() {
        return cuerpo;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tc = condicion.verificar(ambito, errores);
        if (!Tipos.esBooleano(tc))
            errores.reportar(condicion.getLinea(), condicion.getColumna(),
                    "Condición del 'while' debe ser bool");

        AmbitoBloque amb = new AmbitoBloque(ambito, true);
        cuerpo.verificar(amb, errores);
        return TipoPrimitivo.VOID;
    }
}
