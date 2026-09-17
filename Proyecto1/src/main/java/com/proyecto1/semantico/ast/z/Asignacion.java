package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code assignmentExpression} (#assignmentExpressionDef) cuando trae operador:
 * "objetivo op= valor". OJO: implementa {@link ExpresionZ}, NO {@link InstruccionZ} —
 * a diferencia de Y?, en la gramática de Z la asignación vive DENTRO de la jerarquía
 * de expresiones (así "a = (b = 5)" es válido). Una asignación usada como sentencia
 * suelta ("x = 5;") queda envuelta en {@link ExpresionStmt}, igual que cualquier otra
 * expresión.
 *
 * El objetivo se guarda ya armado como {@link ExpresionZ} (una cadena de
 * {@link Identificador} envuelto en {@link AccesoCampo}/{@link Indice}), exactamente
 * como sale de visitar "primaryExpression" normalmente — no hace falta lógica
 * distinta de encadenado para el lado izquierdo.
 */
public final class Asignacion extends NodoZ implements ExpresionZ {

    private final ExpresionZ objetivo;
    private final String operador; // "=", "+=", "-=", "*=", "/=", "%="
    private final ExpresionZ valor;

    public Asignacion(ExpresionZ objetivo, String operador, ExpresionZ valor, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.operador = operador;
        this.valor = valor;
    }

    public ExpresionZ getObjetivo() {
        return objetivo;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionZ getValor() {
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
        return tIzq;
    }
}
