package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
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

    /**
     * Emite UNA cuádrupla {@code (read, null, null, destino)}.
     * <ul>
     *   <li>Con variable: {@code destino = variable}. Se escribe directo en la
     *       variable ya declarada y validada por verificar(). Sin temporal intermedio:
     *       el valor leído es exactamente lo que va en esa variable.</li>
     *   <li>Sin variable: {@code destino = t} con un temporal nuevo. El valor se lee
     *       igual pero se descarta (nadie lo consume después); el temporal es basura
     *       que Fase 5 puede eliminar.</li>
     * </ul>
     * Devuelve {@code ResultadoC3D.vacio()}: es una sentencia, no produce valor
     * reutilizable por el llamador.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        if (variable != null) {
            generador.emitirRead(variable);
        } else {
            String t = generador.nuevoTemporal();
            generador.emitirRead(t);
        }
        return ResultadoC3D.vacio();
    }
}