package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * {@code ID? << } (#sentenciaLeerDef): lectura de entrada estándar. A diferencia del
 * {@code Leer} de Y (que es una EXPRESIÓN, {@code leer()}), en PigLatin es una
 * SENTENCIA completa que opcionalmente guarda el valor leído en una variable ya
 * declarada; por eso aquí implementa {@link InstruccionPigLatin} y no
 * {@link ExpresionPigLatin}. {@code variable} es {@code null} cuando el {@code ID} se
 * omite (se lee y se descarta el valor).
 */
public final class Leer extends NodoPigLatin implements InstruccionPigLatin {

    private final String variable; // null si no se especificó el ID

    public Leer(String variable, int linea, int columna) {
        super(linea, columna);
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        if (variable != null) {
            Simbolo s = ambito.resolver(variable);
            if (s == null)
                errores.reportar(linea, columna, "Variable no declarada: '" + variable + "'");
            else
                s.marcarInicializado();
        }
        return TipoPrimitivo.VOID;
    }
}
