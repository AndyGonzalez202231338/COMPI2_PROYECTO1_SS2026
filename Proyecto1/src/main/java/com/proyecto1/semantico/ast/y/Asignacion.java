package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code asignacion} (#asigDef): "ID (.campo | [indice])* op= expresion". El lado
 * izquierdo se guarda ya armado como una {@link ExpresionY} (una cadena de
 * {@link Identificador} envuelto en {@link AccesoCampo} / {@link Indice}, exactamente
 * igual que si fuera una "primaria" en medio de una expresión), para no duplicar esa
 * lógica de encadenado.
 */
public final class Asignacion extends NodoY implements InstruccionY {

    private final ExpresionY objetivo;
    private final String operador; // "=", "+=", "-=", "*=", "/="
    private final ExpresionY valor;

    public Asignacion(ExpresionY objetivo, String operador, ExpresionY valor, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.operador = operador;
        this.valor = valor;
    }

    public ExpresionY getObjetivo() {
        return objetivo;
    }
    public String getOperador() {
        return operador;
    }

    public ExpresionY getValor() {
        return valor;
    }

    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tIzq = objetivo.verificar(ambito, errores);
        Tipo tDer = valor.verificar(ambito, errores);

        if (!Tipos.esAsignable(tIzq, tDer)) {
            errores.reportar(linea, columna,
                    "No se puede asignar " + tDer.nombre() + " a " + tIzq.nombre());
        }

        // Marcar el símbolo como inicializado (si el objetivo es una variable simple)
        if (objetivo instanceof Identificador id) {
            Simbolo s = ambito.resolver(id.getNombre());
            if (s != null) s.marcarInicializado();
        }
        return tIzq;
    }
}
