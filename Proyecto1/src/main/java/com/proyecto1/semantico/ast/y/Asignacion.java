package com.proyecto1.semantico.ast.y;

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
}
