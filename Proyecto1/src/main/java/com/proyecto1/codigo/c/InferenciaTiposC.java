package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.cuadruplas.*;
import com.proyecto1.semantico.tipos.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Infiere el tipo C de cada variable local y temporal de UNA función a partir de
 * las cuádruplas de su cuerpo (entre su begin_func y su end_func, ambos excluidos)
 * más su {@link GeneradorC3D.Firma} (con los parámetros ya tipados).
 *
 * <p><b>Qué produce:</b> un mapa {@code lugar -> tipo C} de los lugares DECLARADOS
 * dentro de la función (variables locales + temporales), en el orden en que cada
 * uno se detectó por primera vez. Los parámetros de la firma NO aparecen ahí (van
 * en la cabecera de la función y eso lo arma el ensamblador, no esta clase), pero
 * SÍ se registran internamente para poder inferir el tipo de las operaciones que
 * los usan como operandos.
 *
 * <p><b>Cómo infiere:</b> recorre las cuádruplas UNA vez (single-pass) como
 * {@link VisitanteCuadrupla}{@code <Void>}. Cada cuádrupla que "declara" un lugar
 * nuevo (Binaria, Unaria, Asignación, Call, Read, New, NewArray, ÍndiceCarga,
 * CampoCarga) aporta un tipo; el resto (goto, if_false, if_true, label, print,
 * param, begin_func, end_func, return, índices/guarda, campo/guarda) no declaran
 * nada y se ignoran.
 *
 * <p><b>Límites de la inferencia:</b> cuando no se puede deducir un tipo (p. ej.
 * una asignación {@code x = "hola"} donde "hola" es un literal que no está en el
 * mapa), se usa {@code int} como fallback y se sigue, sin fallar — el objetivo es
 * producir C compilable, no un sistema de tipos preciso. La resolución fina de
 * tipos de campo/índice queda para Fase 4.3/4.4.
 *
 * <p><b>Limitación single-pass:</b> si un mismo lugar se usa primero en un contexto
 * {@code int} y después en uno {@code double}, se queda con {@code int} (el primero
 * que se detectó). Un análisis de flujo real (data-flow) lo resolvería; fuera del
 * alcance de esta fase.
 */
public final class InferenciaTiposC implements VisitanteCuadrupla<Void> {

    /** Firma de la función cuyo cuerpo estamos analizando. */
    private final GeneradorC3D.Firma firma;

    /** Todas las firmas del programa, indexadas por etiqueta. Necesario para tipar CuadruplaCall. */
    private final Map<String, GeneradorC3D.Firma> firmasPrograma;

    /**
     * Tipos conocidos (parámetros + locales declarados). Se usa para lookup cuando
     * una cuádrupla menciona un lugar como OPERANDO. No se expone directamente.
     */
    private final Map<String, String> tiposConocidos = new LinkedHashMap<>();

    /**
     * Solo los lugares declarados DENTRO de la función (no parámetros), en el orden
     * en que se detectaron por primera vez. Es lo que devuelve {@link #getDeclaraciones()}.
     */
    private final Map<String, String> declaracionesLocales = new LinkedHashMap<>();

    /**
     * Construye el inferidor, registra los parámetros de la firma y recorre el cuerpo
     * UNA vez. Al terminar el constructor, {@link #getDeclaraciones()} y
     * {@link #comoLineasDeC()} ya están listos.
     *
     * @param cuadruplas       cuádruplas del cuerpo de UNA función, sin su begin_func
     *                         ni su end_func. Puede ser vacía (función sin cuerpo).
     * @param firma            firma de esa función, con parámetros y tipo de retorno.
     * @param firmasPrograma   todas las firmas del programa (para tipar calls).
     */
    public InferenciaTiposC(List<Cuadrupla> cuadruplas,
                            GeneradorC3D.Firma firma,
                            Map<String, GeneradorC3D.Firma> firmasPrograma) {
        this.firma = firma;
        this.firmasPrograma = firmasPrograma;
        registrarParametros();
        if (cuadruplas != null) {
            for (Cuadrupla c : cuadruplas) {
                c.aceptar(this);
            }
        }
    }

    // ---------- API pública ----------

    /**
     * Declaraciones locales inferidas: {@code lugar -> tipo C} ("t0" -> "int",
     * "arr" -> "int*", ...), en el ORDEN en que cada lugar apareció por primera vez.
     * NO incluye los parámetros de la firma.
     */
    public Map<String, String> getDeclaraciones() {
        return declaracionesLocales;
    }

    /** Las declaraciones como líneas de C: "int t0;", "char* s;", una por línea. */
    public List<String> comoLineasDeC() {
        List<String> lineas = new ArrayList<>();
        for (Map.Entry<String, String> e : declaracionesLocales.entrySet()) {
            lineas.add(e.getValue() + " " + e.getKey() + ";");
        }
        return lineas;
    }

    // ---------- Registro inicial de parámetros ----------

    /**
     * Registra los parámetros formales de la firma como tipos ya conocidos. NO van
     * a {@link #declaracionesLocales} (los pone el ensamblador en la cabecera), pero
     * sí a {@link #tiposConocidos} para que cualquier cuádrupla que los mencione
     * pueda tiparse.
     */
    private void registrarParametros() {
        if (firma == null || firma.parametros() == null) return;
        for (GeneradorC3D.ParametroFirma p : firma.parametros()) {
            tiposConocidos.put(p.nombre(), tipoAC(p.tipo()));
        }
    }

    // ---------- Visitor ----------

    @Override
    public Void visitar(CuadruplaBinaria c) {
        String tipo;
        if (esAritmetico(c.operador())) {
            // El resultado es el tipo "más ancho" entre los dos operandos.
            tipo = masAncho(tipoDe(c.a()), tipoDe(c.b()));
        } else {
            // Relacionales y lógicos: bool en el lenguaje = int en C.
            tipo = "int";
        }
        declararSiNuevo(c.t(), tipo);
        return null;
    }

    @Override
    public Void visitar(CuadruplaUnaria c) {
        String op = c.operador();
        String tipo;
        if ("not".equals(op) || "!".equals(op)) {
            // Negación lógica: bool -> int en C.
            tipo = "int";
        } else {
            // "neg" / "-": mismo tipo que el operando.
            tipo = tipoDe(c.a());
        }
        declararSiNuevo(c.t(), tipo);
        return null;
    }

    @Override
    public Void visitar(CuadruplaAsignacion c) {
        // El destino hereda el tipo del valor si ya lo conocemos; si no, int.
        String tipo = tiposConocidos.getOrDefault(c.valor(), "int");
        declararSiNuevo(c.destino(), tipo);
        return null;
    }

    @Override
    public Void visitar(CuadruplaCall c) {
        String tipo = "int"; // fallback
        if (firmasPrograma != null && c.funcion() != null) {
            GeneradorC3D.Firma f = firmasPrograma.get(c.funcion());
            if (f != null && f.tipoRetorno() != null) {
                tipo = tipoAC(f.tipoRetorno());
            }
        }
        // Solo declarar si el resultado de la llamada se usa (destino != null).
        if (c.destino() != null) {
            declararSiNuevo(c.destino(), tipo);
        }
        return null;
    }

    @Override
    public Void visitar(CuadruplaRead c) {
        // Por ahora todas las lecturas se tratan como texto.
        declararSiNuevo(c.destino(), "char*");
        return null;
    }

    @Override
    public Void visitar(CuadruplaNew c) {
        // t = new Clase -> Clase* (heap).
        declararSiNuevo(c.destino(), c.clase() + "*");
        return null;
    }

    @Override
    public Void visitar(CuadruplaNewArray c) {
        // t = new T[n] -> T* (o T** para T[]=int[], etc.).
        // El descriptor puede ser "int" (1D) o "int[]" (2D o más); convertir "[]"
        // en "*" y añadir un "*" final da el tipo C correcto en todos los casos:
        //   "int"    -> "int*"
        //   "int[]"  -> "int**"
        //   "int[][]"-> "int***"
        String tipoC = c.tipoElemento().replace("[]", "*") + "*";
        declararSiNuevo(c.destino(), tipoC);
        return null;
    }

    @Override
    public Void visitar(CuadruplaIndiceCarga c) {
        // TODO(Fase 4.3): resolver el tipo real del elemento del arreglo.
        declararSiNuevo(c.destino(), "int");
        return null;
    }

    @Override
    public Void visitar(CuadruplaCampoCarga c) {
        // TODO(Fase 4.4): resolver el tipo real del campo consultando la tabla de tipos.
        declararSiNuevo(c.destino(), "int");
        return null;
    }

    // ---------- Cuádruplas que no declaran ningún lugar nuevo ----------

    @Override public Void visitar(CuadruplaGoto c)        { return null; }
    @Override public Void visitar(CuadruplaIfFalse c)     { return null; }
    @Override public Void visitar(CuadruplaIfTrue c)      { return null; }
    @Override public Void visitar(CuadruplaEtiqueta c)    { return null; }
    @Override public Void visitar(CuadruplaPrint c)       { return null; }
    @Override public Void visitar(CuadruplaParam c)       { return null; }
    @Override public Void visitar(CuadruplaReturn c)      { return null; }
    @Override public Void visitar(CuadruplaBeginFunc c)   { return null; }
    @Override public Void visitar(CuadruplaEndFunc c)     { return null; }
    @Override public Void visitar(CuadruplaIndiceGuarda c){ return null; }
    @Override public Void visitar(CuadruplaCampoGuarda c) { return null; }

    // ---------- Helpers ----------

    /**
     * Registra {@code lugar -> tipoC} si el lugar no estaba ya conocido. Si el lugar
     * es un parámetro o ya fue declarado por una cuádrupla anterior, no se cambia
     * (el primer tipo detectado gana).
     */
    private void declararSiNuevo(String lugar, String tipoC) {
        if (lugar == null) return;
        if (tiposConocidos.containsKey(lugar)) return;
        tiposConocidos.put(lugar, tipoC);
        declaracionesLocales.put(lugar, tipoC);
    }

    /**
     * Tipo C de un lugar ya conocido (parámetro o local declarado antes). Si no se
     * conoce todavía, devuelve {@code "int"} — la convención de "asumir int y seguir,
     * no fallar" que pide el prompt.
     */
    private String tipoDe(String lugar) {
        if (lugar == null) return "int";
        String t = tiposConocidos.get(lugar);
        return (t != null) ? t : "int";
    }

    /** ¿El operador es aritmético (+, -, *, /, %)? */
    private static boolean esAritmetico(String op) {
        return op != null && (
                op.equals("+") || op.equals("-") || op.equals("*")
                        || op.equals("/") || op.equals("%")
        );
    }

    /**
     * Devuelve el tipo C "más ancho" entre dos: {@code double} si cualquiera de los
     * dos es {@code double}, {@code int} en cualquier otro caso.
     */
    private static String masAncho(String a, String b) {
        if ("double".equals(a) || "double".equals(b)) return "double";
        return "int";
    }

    /**
     * Traduce un {@link Tipo} del lenguaje a su representación en C:
     * <ul>
     *   <li>ENTER O -> "int"</li>
     *   <li>FLOTANTE -> "double"</li>
     *   <li>CARACTER -> "char"</li>
     *   <li>CADENA -> "char*"</li>
     *   <li>BOOL -> "int" (bool en el lenguaje = int en C)</li>
     *   <li>VOID -> "void"</li>
     *   <li>NULO -> "void*"</li>
     *   <li>DESCONOCIDO -> "int" (fallback)</li>
     *   <li>TipoClase / TipoEstructura -> "NombreTipo*" (siempre puntero, viven en heap)</li>
     *   <li>TipoArreglo -> tipo del elemento + "*" (recursivamente, cubre multidimensionales)</li>
     * </ul>
     */
    private static String tipoAC(Tipo t) {
        if (t == null) return "int";
        if (t == TipoPrimitivo.ENTERO)      return "int";
        if (t == TipoPrimitivo.FLOTANTE)    return "double";
        if (t == TipoPrimitivo.CARACTER)    return "char";
        if (t == TipoPrimitivo.CADENA)      return "char*";
        if (t == TipoPrimitivo.BOOL)        return "int";
        if (t == TipoPrimitivo.VOID)        return "void";
        if (t == TipoPrimitivo.NULO)        return "void*";
        if (t == TipoPrimitivo.DESCONOCIDO) return "int";
        if (t instanceof TipoClase tc)      return tc.getDefinicion().getNombre() + "*";
        if (t instanceof TipoEstructura te) return te.getDefinicion().getNombre() + "*";
        if (t instanceof TipoArreglo ta)    return tipoAC(ta.getBase()) + "*";
        return "int";
    }
}