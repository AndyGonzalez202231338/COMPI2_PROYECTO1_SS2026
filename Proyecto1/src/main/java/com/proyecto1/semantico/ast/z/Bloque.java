package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/** Un {@code block} (#blockDef): "{ statement* }". */
public final class Bloque extends NodoZ implements InstruccionZ {

    private final List<InstruccionZ> instrucciones;

    public Bloque(List<InstruccionZ> instrucciones, int linea, int columna) {
        super(linea, columna);
        this.instrucciones = instrucciones;
    }

    public List<InstruccionZ> getInstrucciones() {
        return instrucciones;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        for (InstruccionZ i : instrucciones) i.verificar(ambito, errores);
        return TipoPrimitivo.VOID;
    }
}