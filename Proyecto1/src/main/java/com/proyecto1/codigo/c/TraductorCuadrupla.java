package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.cuadruplas.*;

import java.util.Map;

/**
 * Traduce UNA cuádrupla a su línea de C equivalente (Fase 4: C3D -> C).
 *
 * Antes esto iba a ser un switch sobre {@code Cuadrupla.getOperador()} (un String).
 * Con Cuadrupla como interfaz sellada + Visitor, cada tipo de instrucción tiene su
 * propio método {@code visitar(...)}: si mañana se agrega un tipo de cuádrupla
 * nuevo, {@link VisitanteCuadrupla} obliga a agregar el método aquí también (no
 * compila si falta) — separación de responsabilidades real, no solo organizativa.
 *
 * <p><b>Convención de salida</b>: cada línea se emite CON {@code ;} final (o con
 * {@code :;} en el caso de las etiquetas — el {@code ;} extra evita el error de C
 * "label at end of compound statement" cuando la etiqueta queda como última línea
 * de un bloque). Es decir, el texto devuelto ya viene listo para concatenarse tal
 * cual, una línea por cuádrupla: quien ensambla el archivo (OrquestadorC3DaC, fase
 * posterior) solo necesita añadir indentación y saltos de línea.
 *
 * <p>Esta clase NO maneja indentación ni estructura de bloques. Tampoco agrupa
 * cuádruplas relacionadas ({@code param} + {@code call}, {@code begin_func} +
 * cuerpo + {@code end_func}); esas agrupaciones requieren contexto y son
 * responsabilidad del orquestador, no de un traductor cuádrupla-a-cuádrupla.
 */
public final class TraductorCuadrupla implements VisitanteCuadrupla<String> {

    /** Traduce una cuádrupla a su línea de C. Punto de entrada único de esta clase. */
    public String traducir(Cuadrupla c) {
        return c.aceptar(this);
    }

    /** Mapa lugar -> tipo C (de InferenciaTiposC). Puede ser vacío. */
    private final Map<String, String> tipos;

    /** Constructor sin tipos: print/read quedan pendientes (comportamiento actual). */
    public TraductorCuadrupla() {
        this(Map.of());
    }

    /** Constructor con tipos: print/read traducen usando el tipo del valor/destino. */
    public TraductorCuadrupla(Map<String, String> tipos) {
        this.tipos = (tipos != null) ? tipos : Map.of();
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
        // ":;" en vez de ":" evita el error de C
        //   "label at end of compound statement"
        // que el compilador emite cuando una etiqueta es la última línea de un bloque
        // (p. ej. L_fin: justo antes de la llave de cierre de una función).
        // El ";" convierte la línea en "etiqueta + sentencia vacía", lo cual es válido.
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
        String tipo = tipos.getOrDefault(c.valor(), "int");
        return "printf(\"" + formatoPara(tipo) + "\\n\", " + c.valor() + ");";
    }

    @Override
    public String visitar(CuadruplaRead c) {
        String tipo = tipos.getOrDefault(c.destino(), "char*");
        if (tipo.equals("char*")) {
            // Los strings necesitan un runtime: se asume que el orquestador inyecta
            // rt_read_string() al principio del archivo.
            return c.destino() + " = rt_read_string();";
        }
        // Numéricos y char: scanf directo. Se pasa la dirección con "&".
        return "scanf(\"" + formatoPara(tipo) + "\", &" + c.destino() + ");";
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

    /** Formato printf/scanf según tipo C. */
    private static String formatoPara(String tipoC) {
        if (tipoC == null) return "%d";
        if (tipoC.equals("double")) return "%lf";
        if (tipoC.equals("char*")) return "%s";
        if (tipoC.equals("char")) return "%c";
        return "%d";  // int por defecto
    }

    private static UnsupportedOperationException pendiente(String queNoEstaHecho) {
        return new UnsupportedOperationException(
                "TraductorCuadrupla: '" + queNoEstaHecho + "' todavía no está implementado (fase posterior)");
    }
}