package com.proyecto1.semantico.ast.z;

import java.util.List;

/** Un {@code block} (#blockDef): "{ statement* }". */
public final class Bloque extends NodoZ {

    private final List<InstruccionZ> instrucciones;

    public Bloque(List<InstruccionZ> instrucciones, int linea, int columna) {
        super(linea, columna);
        this.instrucciones = instrucciones;
    }

    public List<InstruccionZ> getInstrucciones() {
        return instrucciones;
    }
}
