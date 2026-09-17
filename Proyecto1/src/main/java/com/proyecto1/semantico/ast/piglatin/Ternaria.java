package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code expresionCondicional} (#expresionCondicionalDef), cuando trae el operador
 * ternario: {@code expresionOr ? expresion : expresionCondicional}. Sin equivalente
 * en Y (esa gramática no tiene ternario); se agrega porque PigLatin sí lo admite.
 */
public final class Ternaria extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin condicion;
    private final ExpresionPigLatin siVerdadero;
    private final ExpresionPigLatin siFalso;

    public Ternaria(ExpresionPigLatin condicion, ExpresionPigLatin siVerdadero, ExpresionPigLatin siFalso,
                     int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.siVerdadero = siVerdadero;
        this.siFalso = siFalso;
    }

    public ExpresionPigLatin getCondicion() {
        return condicion;
    }

    public ExpresionPigLatin getSiVerdadero() {
        return siVerdadero;
    }

    public ExpresionPigLatin getSiFalso() {
        return siFalso;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tc = condicion.verificar(ambito, errores);
        if (!Tipos.esBooleano(tc))
            errores.reportar(condicion.getLinea(), condicion.getColumna(),
                    "La condición del ternario debe ser bool");
        Tipo tv = siVerdadero.verificar(ambito, errores);
        Tipo tf = siFalso.verificar(ambito, errores);

        if (Tipos.esAsignable(tv, tf)) return tv;
        if (Tipos.esAsignable(tf, tv)) return tf;
        if (!tv.esDesconocido() && !tf.esDesconocido())
            errores.reportar(linea, columna,
                    "Ramas del ternario incompatibles: " + tv.nombre() + " vs " + tf.nombre());
        return TipoPrimitivo.DESCONOCIDO;
    }
}
