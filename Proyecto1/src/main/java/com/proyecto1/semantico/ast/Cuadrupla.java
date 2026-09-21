package com.proyecto1.semantico.ast;

/**
 * Cuádrupla estructurada del Código de Tres Direcciones: (operador, arg1, arg2, resultado).
 *
 * Es un DTO INMUTABLE. Se eligió esta representación (en vez de un String libre como
 * "t1 = a + b") por tres razones:
 *   1. Backpatching: para rellenar la etiqueta destino de un salto se reemplaza la
 *      cuádrupla completa en la tabla (ver TablaCuadruplas.reemplazar), sin editar texto.
 *   2. Conversión a C (Fase 4): cada campo se lee directamente, sin re-parsear strings.
 *   3. Optimización (Fase 5): se puede inspeccionar operador y operandos con seguridad.
 *
 * Los campos que no aplican a una operación van en null.
 *
 * Convenciones:
 *   Binaria:      (op, a, b, t)        ->  t = a op b
 *   Asignación:   (=, v, null, x)      ->  x = v
 *   Unaria:       (op, v, null, t)     ->  t = op v
 *   Goto:         (goto, null, null, L)
 *   Condicional:  (if_false, c, null, L) / (if_true, c, null, L)
 *   Etiqueta:     (label, null, null, L)
 *   Print:        (print, v, null, null)
 *   Read:         (read, null, null, x)
 *   Call:         (call, f, nArgs, t)
 *   Return:       (return, v, null, null)
 *   Funciones:    (begin_func, nombre, nArgs, null) / (end_func, null, null, null)
 */
public final class Cuadrupla {

    public static final String OP_ASIGNACION = "=";
    public static final String OP_GOTO = "goto";
    public static final String OP_IF_FALSE = "if_false";
    public static final String OP_IF_TRUE = "if_true";
    public static final String OP_ETIQUETA = "label";
    public static final String OP_PRINT = "print";
    public static final String OP_READ = "read";
    public static final String OP_CALL = "call";
    public static final String OP_RETURN = "return";
    public static final String OP_BEGIN_FUNC = "begin_func";
    public static final String OP_END_FUNC = "end_func";

    private final String operador;
    private final String arg1;
    private final String arg2;
    private final String resultado;

    public Cuadrupla(String operador, String arg1, String arg2, String resultado) {
        this.operador = operador;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.resultado = resultado;
    }

    public String getOperador() { return operador; }
    public String getArg1() { return arg1; }
    public String getArg2() { return arg2; }
    public String getResultado() { return resultado; }

    /** Devuelve una copia con otro resultado; útil para backpatching de saltos. */
    public Cuadrupla conResultado(String nuevoResultado) {
        return new Cuadrupla(operador, arg1, arg2, nuevoResultado);
    }

    /** Formato crudo tipo tupla: (op, arg1, arg2, resultado). Los null se muestran como "-". */
    @Override
    public String toString() {
        return "(" + operador + ", " + guion(arg1) + ", " + guion(arg2) + ", " + guion(resultado) + ")";
    }

    /** Formato legible para humanos: "t0 = a + b", "goto L1", "if_false t0 goto L2", etc. */
    public String toStringLegible() {
        if (operador == null) {
            return toString();
        }
        switch (operador) {
            case OP_ASIGNACION:
                return resultado + " = " + arg1;
            case OP_GOTO:
                return "goto " + resultado;
            case OP_IF_FALSE:
                return "if_false " + arg1 + " goto " + resultado;
            case OP_IF_TRUE:
                return "if_true " + arg1 + " goto " + resultado;
            case OP_ETIQUETA:
                return resultado + ":";
            case OP_PRINT:
                return "print " + arg1;
            case OP_READ:
                return "read " + resultado;
            case OP_CALL:
                return "call " + arg1 + ", " + arg2 + (resultado != null ? " -> " + resultado : "");
            case OP_RETURN:
                return arg1 != null ? "return " + arg1 : "return";
            case OP_BEGIN_FUNC:
                return "begin_func " + arg1 + ", " + arg2;
            case OP_END_FUNC:
                return "end_func";
            default:
                if (arg1 != null && arg2 == null && resultado != null) {
                    return resultado + " = " + operador + " " + arg1;      // unaria
                }
                if (arg1 != null && arg2 != null && resultado != null) {
                    return resultado + " = " + arg1 + " " + operador + " " + arg2; // binaria
                }
                return toString();
        }
    }

    private static String guion(String s) {
        return s == null ? "-" : s;
    }
}