package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

public final class Mientras extends NodoY implements InstruccionY {

    private final ExpresionY condicion;
    private final Bloque cuerpo;

    public Mientras(ExpresionY condicion, Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public ExpresionY getCondicion() { return condicion; }
    public Bloque getCuerpo() { return cuerpo; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tc = condicion.verificar(ambito, errores);
        if (!Tipos.esBooleano(tc))
            errores.reportar(condicion.getLinea(), condicion.getColumna(),
                    "La condición de 'mientras' debe ser bool, se recibió " + tc.nombre());

        AmbitoBloque amb = new AmbitoBloque(ambito, true); // esCiclo = true
        cuerpo.verificar(amb, errores);
        return TipoPrimitivo.VOID;
    }
}