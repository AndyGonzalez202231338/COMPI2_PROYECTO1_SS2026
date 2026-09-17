package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

public final class HacerMientras extends NodoY implements InstruccionY {

    private final Bloque cuerpo;
    private final ExpresionY condicion;

    public HacerMientras(Bloque cuerpo, ExpresionY condicion, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = cuerpo;
        this.condicion = condicion;
    }

    public Bloque getCuerpo() { return cuerpo; }
    public ExpresionY getCondicion() { return condicion; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        AmbitoBloque amb = new AmbitoBloque(ambito, true); // esCiclo = true
        cuerpo.verificar(amb, errores);

        Tipo tc = condicion.verificar(ambito, errores);
        if (!Tipos.esBooleano(tc))
            errores.reportar(condicion.getLinea(), condicion.getColumna(),
                    "La condición de 'hacer-mientras' debe ser bool, se recibió " + tc.nombre());

        return TipoPrimitivo.VOID;
    }
}