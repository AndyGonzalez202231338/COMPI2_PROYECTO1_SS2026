package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;

/**
 * {@code expresion ;} (#expresionSentenciaDef): una expresión usada como instrucción
 * suelta. Cubre asignaciones ({@code x = 5;}), llamadas ({@code metodo();}) y llamadas
 * encadenadas ({@code obj.metodo();}), que en la gramática de PigLatin son todas la
 * misma regla {@code expresion} (ver {@link Asignacion} y {@link Llamada}).
 */
public final class ExpresionStmt extends NodoPigLatin implements InstruccionPigLatin {

    private final ExpresionPigLatin expresion;

    public ExpresionStmt(ExpresionPigLatin expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public ExpresionPigLatin getExpresion() {
        return expresion;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return expresion.verificar(ambito, errores);
    }
}
