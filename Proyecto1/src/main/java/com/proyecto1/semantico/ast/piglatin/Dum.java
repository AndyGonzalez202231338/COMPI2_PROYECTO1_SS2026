package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code sentenciaDum} (#sentenciaDumDef): {@code dum (cond) bloque finis;}. Equivale
 * al {@code Mientras} de Y: evalúa la condición ANTES de cada iteración.
 */
public final class Dum extends NodoPigLatin implements InstruccionPigLatin {

    private final ExpresionPigLatin condicion;
    private final Bloque cuerpo;

    public Dum(ExpresionPigLatin condicion, Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public ExpresionPigLatin getCondicion() {
        return condicion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tc = condicion.verificar(ambito, errores);
        if (!Tipos.esBooleano(tc))
            errores.reportar(condicion.getLinea(), condicion.getColumna(),
                    "Condición del 'dum' debe ser bool");
        AmbitoBloque amb = new AmbitoBloque(ambito, true);
        cuerpo.verificar(amb, errores);
        return TipoPrimitivo.VOID;
    }
}
