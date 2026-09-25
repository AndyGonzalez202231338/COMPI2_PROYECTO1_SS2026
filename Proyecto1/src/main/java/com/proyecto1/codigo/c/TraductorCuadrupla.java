package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.cuadruplas.*;

/**
 * Traduce UNA cuádrupla a su línea de C equivalente (Fase 4: C3D -> C).
 *
 * Antes esto iba a ser un switch sobre {@code Cuadrupla.getOperador()} (un String).
 * Con Cuadrupla como interfaz sellada + Visitor, cada tipo de instrucción tiene su
 * propio método {@code visitar(...)}: si mañana se agrega un tipo de cuádrupla
 * nuevo, {@link VisitanteCuadrupla} obliga a agregar el método aquí también (no
 * compila si falta) — separación de responsabilidades real, no solo organizativa.
 *
 * Cada línea NO lleva ";" al final salvo que ya lo necesite por sintaxis de C
 * (si/no/goto no lo llevan como sentencia simple, pero aquí SIEMPRE se agrega el
 * ";" final porque OrquestadorC3DaC (fase posterior) las concatena tal cual, una
 * por línea. La indentación también la pone OrquestadorC3DaC, no esta clase.
 */
public final class TraductorCuadrupla implements VisitanteCuadrupla<String> {

    /** Traduce una cuádrupla a su línea de C. Punto de entrada único de esta clase. */
    public String traducir(Cuadrupla c) {
        return c.aceptar(this);
    }

    // ---------- Aritmética / lógica / relacionales ----------
    // El operador (+, -, ==, &&, ...) es el mismo símbolo en C, así que se copia tal
    // cual. La única excepción es la unaria "not": en C3D el operador es la palabra
    // "not", pero en C es "!".

    @Override
    public String visitar(CuadruplaBinaria c) {
        return c.t() + " = " + c.a() + " " + c.operador() + " " + c.b() + ";";
    }

    @Override
    public String visitar(CuadruplaUnaria c) {
        String op = "not".equals(c.operador()) ? "!" : c.operador();
        return c.t() + " = " + op + c.a() + ";";
    }

    @Override
    public String visitar(CuadruplaAsignacion c) {
        return c.destino() + " = " + c.valor() + ";";
    }

    // ---------- Control de flujo ----------

    @Override
    public String visitar(CuadruplaGoto c) {
        return "goto " + c.etiqueta() + ";";
    }

    @Override
    public String visitar(CuadruplaIfFalse c) {
        return "if (!" + c.condicion() + ") goto " + c.etiqueta() + ";";
    }

    @Override
    public String visitar(CuadruplaIfTrue c) {
        return "if (" + c.condicion() + ") goto " + c.etiqueta() + ";";
    }

    @Override
    public String visitar(CuadruplaEtiqueta c) {
        // ";" extra: evita el error de C "a label can only be part of a statement"
        // cuando la etiqueta es la última línea de un bloque (p. ej. L_fin: al final
        // de una función, justo antes de la llave de cierre).
        return c.etiqueta() + ":;";
    }

    @Override
    public String visitar(CuadruplaReturn c) {
        return c.valor() != null ? "return " + c.valor() + ";" : "return;";
    }

    // ---------- Funciones: begin_func / end_func / call / param se dejan para la
    // fase que arme las cabeceras de función completas (necesitan la Firma
    // registrada en GeneradorC3D, no solo esta cuádrupla suelta) ----------

    @Override
    public String visitar(CuadruplaBeginFunc c) {
        throw pendiente("begin_func");
    }

    @Override
    public String visitar(CuadruplaEndFunc c) {
        throw pendiente("end_func");
    }

    @Override
    public String visitar(CuadruplaCall c) {
        throw pendiente("call");
    }

    @Override
    public String visitar(CuadruplaParam c) {
        throw pendiente("param");
    }

    // ---------- I/O, arreglos, objetos: fases posteriores ----------

    @Override
    public String visitar(CuadruplaPrint c) {
        throw pendiente("print");
    }

    @Override
    public String visitar(CuadruplaRead c) {
        throw pendiente("read");
    }

    @Override
    public String visitar(CuadruplaIndiceCarga c) {
        throw pendiente("índice (carga)");
    }

    @Override
    public String visitar(CuadruplaIndiceGuarda c) {
        throw pendiente("índice (guarda)");
    }

    @Override
    public String visitar(CuadruplaCampoCarga c) {
        throw pendiente("campo (carga)");
    }

    @Override
    public String visitar(CuadruplaCampoGuarda c) {
        throw pendiente("campo (guarda)");
    }

    @Override
    public String visitar(CuadruplaNew c) {
        throw pendiente("new");
    }

    @Override
    public String visitar(CuadruplaNewArray c) {
        throw pendiente("newarr");
    }

    private static UnsupportedOperationException pendiente(String queNoEstaHecho) {
        return new UnsupportedOperationException(
                "TraductorCuadrupla: '" + queNoEstaHecho + "' todavía no está implementado (fase posterior)");
    }
}