package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
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

    /**
     * Emite (después de generar el C3D del valor):
     * <ul>
     *   <li>{@code x = e}: una cuádrupla {@code (=, v, null, x)}.</li>
     *   <li>{@code x op= e} (+=, -=, *=, /=): {@code t = x op v} y luego {@code x = t}.</li>
     * </ul>
     * Devuelve {@code ResultadoC3D.vacio()}: en Y? la asignación es una instrucción.
     * Solo se admite como objetivo un {@link Identificador} (variable simple); campos
     * y elementos de arreglo quedan para una fase posterior y lanzan
     * {@link UnsupportedOperationException}.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        // si el nodo es de tipo identicador
        if (!(objetivo instanceof Identificador id)) {
            throw new UnsupportedOperationException(
                    "Asignación a campos o arreglos: pendiente en C3D");
        }
        //guardar el nombre de la variable
        String variable = id.getNombre();
        ResultadoC3D v = valor.generarC3D(generador);

        //Si es una asignacion normla x = 1
        if (operador.equals("=")) {
            generador.emitirAsignacion(v.getLugar(), variable);
        } else {
            // si la asignacion es x += 1
            //t0 = x + 1
            //x = t0
            // "+=" -> "+", "-=" -> "-", "*=" -> "*", "/=" -> "/" (operador binario)
            String opBinario = operador.substring(0, operador.length() - 1);
            //Crear un nuevo temporal t0
            String t = generador.nuevoTemporal();
            //Crear su cuarteta (+, x, 1, t0)
            generador.emitirBinaria(opBinario, variable, v.getLugar(), t);
            // vuelve a asignar (t0,x)
            generador.emitirAsignacion(t, variable);
        }
        return ResultadoC3D.vacio();
    }
}