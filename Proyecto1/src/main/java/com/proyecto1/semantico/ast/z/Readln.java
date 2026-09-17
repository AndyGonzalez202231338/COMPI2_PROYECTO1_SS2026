package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code READLN LPAREN RPAREN} (#primarioReadln): lee una línea de entrada (devuelve String). */
public final class Readln extends NodoZ implements ExpresionZ {
    public Readln(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return TipoPrimitivo.CADENA;
    }
}
