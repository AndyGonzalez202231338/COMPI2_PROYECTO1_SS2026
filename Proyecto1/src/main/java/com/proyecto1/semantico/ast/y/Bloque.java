package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/**
 * Un {@code bloque} o {@code bloqueSimple} (#bloqueDef / #bloqueSimpleDef). Se
 * unifican en una sola clase: la diferencia entre ambos en la gramática es puramente
 * de puntuación (si llevan ':' antes o no), no de contenido. las dos son, ya dentro
 * del AST, "una lista de instrucciones".
 */
public final class Bloque extends NodoY {

    private final List<InstruccionY> instrucciones;

    public Bloque(List<InstruccionY> instrucciones, int linea, int columna) {
        super(linea, columna);
        this.instrucciones = instrucciones;
    }

    public List<InstruccionY> getInstrucciones() {
        return instrucciones;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        for (InstruccionY i : instrucciones) i.verificar(ambito, errores);
        return TipoPrimitivo.VOID;
    }
}
