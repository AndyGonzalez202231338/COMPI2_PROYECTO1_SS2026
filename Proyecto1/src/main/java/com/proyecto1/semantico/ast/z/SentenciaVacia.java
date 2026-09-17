package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * {@code PUNTOYCOMA} solo (#stmtVacia): un ";" suelto. Y? no tiene equivalente (su
 * gramática no admite sentencias vacías); existe solo para Z.
 */
public final class SentenciaVacia extends NodoZ implements InstruccionZ {
    public SentenciaVacia(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return TipoPrimitivo.VOID;
    }
}
