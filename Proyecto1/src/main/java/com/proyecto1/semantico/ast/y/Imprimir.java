package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/** {@code IMPRIMIR(expresion (, expresion)*)} (#instImprimir). */
public final class Imprimir extends NodoY implements InstruccionY {

    private final List<ExpresionY> argumentos;

    public Imprimir(List<ExpresionY> argumentos, int linea, int columna) {
        super(linea, columna);
        this.argumentos = argumentos;
    }

    public List<ExpresionY> getArgumentos() {
        return argumentos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        for (ExpresionY a : argumentos) a.verificar(ambito, errores);
        return TipoPrimitivo.VOID;
    }
}
