package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code conditionalExpression} (#conditionalExpressionDef) cuando trae el '?:':
 * "condicion ? siVerdadero : siFalso". No existe equivalente en Y? — es exclusivo
 * de Zetariano.
 */
public final class Ternario extends NodoZ implements ExpresionZ {

    private final ExpresionZ condicion;
    private final ExpresionZ siVerdadero;
    private final ExpresionZ siFalso;

    public Ternario(ExpresionZ condicion, ExpresionZ siVerdadero, ExpresionZ siFalso, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.siVerdadero = siVerdadero;
        this.siFalso = siFalso;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }

    public ExpresionZ getSiVerdadero() {
        return siVerdadero;
    }

    public ExpresionZ getSiFalso() {
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
