package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.cuadruplas.*;

import java.util.List;
import java.util.Map;

/**
 * Traduce UNA cuádrupla del C3D a su línea equivalente en C.
 *
 * <p>El despacho es por Visitor sobre la jerarquía sellada de {@link Cuadrupla}.
 * Si mañana se agrega un tipo de cuádrupla nuevo, {@link VisitanteCuadrupla} obliga
 * a agregar el método aquí también (no compila si falta).
 *
 * <p><b>Convención de salida:</b> cada línea se emite CON {@code ;} final (o con
 * {@code :;} en las etiquetas). Sin indentación: eso lo añade el orquestador.
 *
 * <h2>Fase 4.4 — el modelo de heap</h2>
 * <p>Ya documentado en las versiones anteriores (new→malloc, campo→{@code ->},
 * {@code this} como string, sin {@code free}).
 *
 * <h2>Fase 4.6 — strings y E/S tipada</h2>
 * <p>El traductor recibe un mapa {@code lugar -> tipoC} (típicamente provisto por
 * {@code InferenciaTiposC}). Con él:
 * <ul>
 *   <li>{@code a + b} donde alguno es {@code char*} se traduce a
 *       {@code t = rt_concat(a, b);} — en C puro {@code +} no concatena strings.</li>
 *   <li>{@code a == b} / {@code a != b} donde ambos son {@code char*} se traduce a
 *       {@code rt_strcmp(a, b) == 0} / {@code != 0} — en C puro {@code ==} compara
 *       punteros, no contenidos.</li>
 *   <li>{@code print v} / {@code read v} eligen {@code printf}/{@code scanf} con
 *       formato según el tipo del operando.</li>
 * </ul>
 * Sin el mapa (constructor vacío), todas estas operaciones caen a sus fallbacks:
 * {@code int} para formato, {@code +} y {@code ==} se traducen tal cual (asumiendo
 * que el usuario no está usando strings en ese contexto).
 */
public final class TraductorCuadrupla implements VisitanteCuadrupla<String> {

    /** Mapa lugar -> tipo C ("int", "double", "char*", ...). Puede estar vacío. */
    private final Map<String, String> tipos;

    /** Constructor sin tipos: print/read/binarias sobre strings usan fallback int. */
    public TraductorCuadrupla() {
        this(Map.of());
    }

    /** Constructor con tipos (típicamente desde InferenciaTiposC). */
    public TraductorCuadrupla(Map<String, String> tipos) {
        this.tipos = (tipos != null) ? tipos : Map.of();
    }

    public String traducir(Cuadrupla c) {
        return c.aceptar(this);
    }

    // ---------- Aritmética / lógica / relacionales ----------

    /**
     * Binaria genérica. Además del caso normal {@code t = a op b}, cubre dos
     * excepciones específicas de strings que C no hace por sí solo:
     *
     * <ul>
     *   <li>{@code +} donde alguno de los operandos es {@code char*} →
     *       {@code t = rt_concat(a, b);} (concatenación, no suma aritmética).</li>
     *   <li>{@code ==} / {@code !=} donde ambos son {@code char*} →
     *       {@code t = (rt_strcmp(a, b) == 0);} / {@code != 0} (comparación de
     *       contenido, no de punteros).</li>
     * </ul>
     *
     * <p>El resto de los operadores se emiten tal cual (aritméticos, relacionales,
     * lógicos). El C3D ya validó los tipos; este método solo decide la forma en C.
     */
    @Override
    public String visitar(CuadruplaBinaria c) {
        String op = c.operador();
        String aTipo = tipoEfectivo(c.a());
        String bTipo = tipoEfectivo(c.b());
        boolean aEsString = "char*".equals(aTipo);
        boolean bEsString = "char*".equals(bTipo);

        // Concatenación si alguno de los operandos es string.
        if ("+".equals(op) && (aEsString || bEsString)) {
            String aLugar = aEsString ? c.a() : convertirAString(c.a(), aTipo);
            String bLugar = bEsString ? c.b() : convertirAString(c.b(), bTipo);
            return c.t() + " = rt_concat(" + aLugar + ", " + bLugar + ");";
        }

        // Comparación de strings por contenido.
        if (("==".equals(op) || "!=".equals(op)) && aEsString && bEsString) {
            String cmp = "==".equals(op) ? "== 0" : "!= 0";
            return c.t() + " = (rt_strcmp(" + c.a() + ", " + c.b() + ") " + cmp + ");";
        }

        // Caso general.
        return c.t() + " = " + c.a() + " " + op + " " + c.b() + ";";
    }

    /**
     * Tipo C efectivo de un operando, con detección de literales por forma.
     * Los literales string/char NO están en el mapa de tipos (nunca son destino de
     * una cuádrupla), así que hay que reconocerlos por su sintaxis.
     */
    private String tipoEfectivo(String lugar) {
        if (lugar == null) return "int";
        if (lugar.length() >= 2 && lugar.startsWith("\"") && lugar.endsWith("\"")) return "char*";
        if (lugar.length() >= 2 && lugar.startsWith("'") && lugar.endsWith("'")) return "char";
        if ("true".equals(lugar) || "false".equals(lugar)) return "int";
        if ("null".equals(lugar)) return "void*";
        return tipos.getOrDefault(lugar, "int");
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
        return c.etiqueta() + ":;";
    }

    @Override
    public String visitar(CuadruplaReturn c) {
        return c.valor() != null ? "return " + c.valor() + ";" : "return;";
    }

    // ---------- Heap ----------

    @Override
    public String visitar(CuadruplaNew c) {
        return c.destino() + " = (" + c.clase() + "*) malloc(sizeof(" + c.clase() + "));";
    }

    @Override
    public String visitar(CuadruplaCampoCarga c) {
        return c.destino() + " = " + c.objeto() + "->" + c.campo() + ";";
    }

    @Override
    public String visitar(CuadruplaCampoGuarda c) {
        return c.objeto() + "->" + c.campo() + " = " + c.valor() + ";";
    }

    // ---------- I/O ----------

    /**
     * {@code print v} → {@code printf(formato, v);}. El formato se elige según el
     * tipo C de {@code v}:
     * <ul>
     *   <li>{@code int} → {@code "%d"}</li>
     *   <li>{@code double} → {@code "%lf"}</li>
     *   <li>{@code char} → {@code "%c"}</li>
     *   <li>{@code char*} → {@code "%s"}</li>
     * </ul>
     * Sin tipos, cae a {@code "%d"}.
     */
    @Override
    public String visitar(CuadruplaPrint c) {
        String valor = c.valor();
        String tipo;
        if (valor != null && valor.length() >= 2
                && valor.startsWith("\"") && valor.endsWith("\"")) {
            tipo = "char*";   // literal string
        } else if (valor != null && valor.length() >= 2
                && valor.startsWith("'") && valor.endsWith("'")) {
            tipo = "char";    // literal char
        } else {
            tipo = tipos.getOrDefault(valor, "int");
        }
        return "printf(\"" + formatoPrintf(tipo) + "\", " + valor + ");";
    }

    /**
     * {@code read destino} → depende del tipo del destino:
     * <ul>
     *   <li>{@code char*}: {@code destino = rt_read_string();} (lectura de línea completa).</li>
     *   <li>Numéricos o {@code char}: {@code scanf(formato, &destino);}.</li>
     * </ul>
     * Sin tipos, asume {@code int} y usa {@code scanf("%d", &destino)}.
     */
    @Override
    public String visitar(CuadruplaRead c) {
        String tipo = tipos.getOrDefault(c.destino(), "int");
        if ("char*".equals(tipo)) {
            return c.destino() + " = rt_read_string();";
        }
        return "scanf(\"" + formatoScanf(tipo) + "\", &" + c.destino() + ");";
    }

    // ---------- Funciones: pendientes en el orquestador ----------

    @Override public String visitar(CuadruplaBeginFunc c) { throw pendiente("begin_func"); }
    @Override public String visitar(CuadruplaEndFunc c)   { throw pendiente("end_func"); }

    /**
     * El traductor de cuádrupla-a-cuádrupla NO maneja {@code call} solo: la
     * información de los {@code param} previos vive fuera de la cuádrupla y la
     * agrupa el orquestador. Aquí solo se deja el método como pendiente.
     *
     * <p>Excepción: si la llamada es a {@code rt_print}/{@code rt_println}/{@code rt_readln}
     * (que Z emite como llamadas al runtime), el orquestador puede resolverla sin
     * necesitar el agrupamiento de params (son de aridad 1 o 0). Pero se maneja en
     * el orquestador, no aquí.
     */
    @Override public String visitar(CuadruplaCall c)  { throw pendiente("call"); }
    @Override public String visitar(CuadruplaParam c) { throw pendiente("param"); }

    // ---------- Arreglos ----------

    @Override
    public String visitar(CuadruplaIndiceCarga c) {
        return c.destino() + " = " + c.arreglo() + "[" + c.indice() + "];";
    }

    @Override
    public String visitar(CuadruplaIndiceGuarda c) {
        return c.arreglo() + "[" + c.indice() + "] = " + c.valor() + ";";
    }

    /**
     * {@code destino = new Tipo[t1][t2]...[tn]} → malloc del producto de tamaños.
     * Ver Fase 4.5b para el detalle (agnóstico a flat/jagged).
     */
    @Override
    public String visitar(CuadruplaNewArray c) {
        List<String> ts = c.tamanos();
        String producto = (ts.size() == 1) ? ts.get(0) : String.join(" * ", ts);
        return c.destino() + " = (" + c.tipoElemento() + "*) malloc(("
                + producto + ") * sizeof(" + c.tipoElemento() + "));";
    }

    // ---------- Helpers ----------

    /** Formato printf según tipo C. */
    private static String formatoPrintf(String tipoC) {
        if (tipoC == null) return "%d";
        return switch (tipoC) {
            case "double" -> "%lf";
            case "char"   -> "%c";
            case "char*"  -> "%s";
            default       -> "%d";  // int, bool, punteros (se imprimen como enteros)
        };
    }

    /** Formato scanf según tipo C. No se llama para {@code char*}. */
    private static String formatoScanf(String tipoC) {
        if (tipoC == null) return "%d";
        return switch (tipoC) {
            case "double" -> "%lf";
            case "char"   -> " %c";  // el espacio inicial evita el '\n' previo
            default       -> "%d";
        };
    }

    private static UnsupportedOperationException pendiente(String queNoEstaHecho) {
        return new UnsupportedOperationException(
                "TraductorCuadrupla: '" + queNoEstaHecho + "' todavía no está implementado (fase posterior)");
    }

    /** Envuelve un valor numérico en su conversión a string del runtime. */
    private static String convertirAString(String lugar, String tipoC) {
        if ("double".equals(tipoC)) return "rt_double_to_string(" + lugar + ")";
        return "rt_int_to_string(" + lugar + ")";
    }
}