package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/** {@code >> expresion (>> expresion)* ;} (#sentenciaImprimirDef). Uno o más valores impresos en secuencia. */
public final class Imprimir extends NodoPigLatin implements InstruccionPigLatin {

    private final List<ExpresionPigLatin> argumentos;

    public Imprimir(List<ExpresionPigLatin> argumentos, int linea, int columna) {
        super(linea, columna);
        this.argumentos = argumentos;
    }

    public List<ExpresionPigLatin> getArgumentos() {
        return argumentos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        for (ExpresionPigLatin a : argumentos) a.verificar(ambito, errores);
        return TipoPrimitivo.VOID;
    }
}
