package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code expresionAsignacion} (#expresionAsignacionDef), cuando trae operador:
 * {@code expresionCondicional op= expresionAsignacion}. A diferencia de la asignación
 * de Y (que es su propia INSTRUCCIÓN), en PigLatin la asignación es una EXPRESIÓN —
 * la regla es recursiva a la derecha ({@code a = b = 5}) y solo se vuelve instrucción
 * cuando queda envuelta en {@link ExpresionStmt} (vía {@code expresionSentencia}).
 * Por eso implementa {@link ExpresionPigLatin} y no {@link InstruccionPigLatin}.
 */
public final class Asignacion extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objetivo;
    private final String operador; // "=", "+=", "-=", "*=", "/=", "%="
    private final ExpresionPigLatin valor;

    public Asignacion(ExpresionPigLatin objetivo, String operador, ExpresionPigLatin valor,
                       int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.operador = operador;
        this.valor = valor;
    }

    public ExpresionPigLatin getObjetivo() {
        return objetivo;
    }
    public String getOperador() {
        return operador;
    }

    public ExpresionPigLatin getValor() {
        return valor;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tIzq = objetivo.verificar(ambito, errores);
        Tipo tDer = valor.verificar(ambito, errores);

        if (!operador.equals("=")) {
            Tipo r = Tipos.resultadoAritmetico(tIzq, tDer, operador.equals("+="));
            if (r == null)
                errores.reportar(linea, columna, "Operador '" + operador + "' no válido");
        } else if (!Tipos.esAsignable(tIzq, tDer)) {
            errores.reportar(linea, columna,
                    "No se puede asignar " + tDer.nombre() + " a " + tIzq.nombre());
        }

        if (objetivo instanceof Identificador id) {
            Simbolo s = ambito.resolver(id.getNombre());
            if (s != null) s.marcarInicializado();
        }
        return tIzq;
    }
}
