package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.cuadruplas.*;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.*;

import java.util.ArrayList;
import java.util.HashMap;
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
 * {@link VisitanteCuadrupla}{@code <Void>}.
 *
 * <p><b>Resolución de tipos compuestos:</b> cuando una cuádrupla carga un campo
 * ({@code CuadruplaCampoCarga}) o un elemento de arreglo ({@code CuadruplaIndiceCarga}),
 * el tipo del resultado NO puede deducirse solo del nombre del campo/índice — hace
 * falta consultar la tabla de tipos del programa. Por eso el inferidor recibe
 * {@code tiposDefinidos} (la lista de {@link Simbolo} de las clases/estructuras
 * declaradas a nivel global): con eso puede hacer
 * {@code tiposPorNombre.get(nombreClase).buscarMiembro(nombreCampo).getTipo()} y
 * traducir el resultado. Si el tipo no se encuentra (clase desconocida, campo
 * inexistente, etc.) se cae a {@code int} como fallback.
 *
 * <p><b>Límites de la inferencia:</b> cuando no se puede deducir un tipo se usa
 * {@code int} como fallback y se sigue, sin fallar — el objetivo es producir C
 * compilable, no un sistema de tipos preciso.
 *
 * <p><b>Limitación single-pass:</b> si un mismo lugar se usa primero en un contexto
 * {@code int} y después en uno {@code double}, se queda con {@code int} (el primero
 * que se detectó).
 */
public final class InferenciaTiposC implements VisitanteCuadrupla<Void> {

    /** Firma de la función cuyo cuerpo estamos analizando. */
    private final GeneradorC3D.Firma firma;

    /** Todas las firmas del programa, indexadas por etiqueta. Necesario para tipar CuadruplaCall. */
    private final Map<String, GeneradorC3D.Firma> firmasPrograma;

    /**
     * Tipos definidos por el usuario (estructuras de Y, clases de Z) indexados por
     * nombre. Se usa para resolver el tipo de un campo: dado {@code obj.campo}, se
     * busca el nombre de la clase de {@code obj}, se encuentra su símbolo aquí, y
     * se consulta {@code buscarMiembro(campo)} para obtener el tipo real.
     */
    private final Map<String, Simbolo> tiposPorNombre = new HashMap<>();

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
     * Constructor principal. Registra los parámetros de la firma, indexa los tipos
     * definidos por nombre, y recorre el cuerpo UNA vez. Al terminar, {@link #getDeclaraciones()}
     * y {@link #comoLineasDeC()} están listos.
     *
     * @param cuadruplas       cuádruplas del cuerpo de UNA función, sin su begin_func
     *                         ni su end_func. Puede ser vacía (función sin cuerpo).
     * @param firma            firma de esa función, con parámetros y tipo de retorno.
     * @param firmasPrograma   todas las firmas del programa (para tipar calls).
     * @param tiposDefinidos   símbolos de clases/estructuras (para resolver campos).
     *                         Puede ser null o vacío (fallback: campos → "int").
     */
    public InferenciaTiposC(List<Cuadrupla> cuadruplas,
                            GeneradorC3D.Firma firma,
                            Map<String, GeneradorC3D.Firma> firmasPrograma,
                            List<Simbolo> tiposDefinidos) {
        this.firma = firma;
        this.firmasPrograma = firmasPrograma;
        if (tiposDefinidos != null) {
            for (Simbolo s : tiposDefinidos) {
                if (s != null && s.getNombre() != null) {
                    tiposPorNombre.put(s.getNombre(), s);
                }
            }
        }
        registrarParametros();
        if (cuadruplas != null) {
            for (Cuadrupla c : cuadruplas) {
                c.aceptar(this);
            }
        }
    }

    /** Constructor sin tipos definidos (retrocompatible). Campos → "int". */
    public InferenciaTiposC(List<Cuadrupla> cuadruplas,
                            GeneradorC3D.Firma firma,
                            Map<String, GeneradorC3D.Firma> firmasPrograma) {
        this(cuadruplas, firma, firmasPrograma, null);
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

    private void registrarParametros() {
        if (firma == null || firma.parametros() == null) return;
        for (GeneradorC3D.ParametroFirma p : firma.parametros()) {
            tiposConocidos.put(p.nombre(), tipoAC(p.tipo()));
        }
    }

    // ---------- Visitor ----------

    @Override
    public Void visitar(CuadruplaBinaria c) {
        String aTipo = tipoDeConLiterales(c.a());
        String bTipo = tipoDeConLiterales(c.b());
        boolean aEsString = "char*".equals(aTipo);
        boolean bEsString = "char*".equals(bTipo);

        String tipo;
        if ("+".equals(c.operador()) && (aEsString || bEsString)) {
            tipo = "char*";          // concatenación → char*
        } else if (esAritmetico(c.operador())) {
            tipo = masAncho(aTipo, bTipo);
        } else {
            tipo = "int";            // relacionales/lógicos → bool
        }
        declararSiNuevo(c.t(), tipo);
        return null;
    }

    @Override
    public Void visitar(CuadruplaUnaria c) {
        String op = c.operador();
        String tipo;
        if ("not".equals(op) || "!".equals(op)) {
            tipo = "int";
        } else {
            tipo = tipoDe(c.a());
        }
        declararSiNuevo(c.t(), tipo);
        return null;
    }

    @Override
    public Void visitar(CuadruplaAsignacion c) {
        String tipo = tipoDeConLiterales(c.valor());
        declararSiNuevo(c.destino(), tipo);
        return null;
    }


    @Override
    public Void visitar(CuadruplaCall c) {
        String tipo = "int";
        if (firmasPrograma != null && c.funcion() != null) {
            GeneradorC3D.Firma f = firmasPrograma.get(c.funcion());
            if (f != null && f.tipoRetorno() != null) {
                tipo = tipoAC(f.tipoRetorno());
            }
        }
        // No declarar el destino si la función devuelve void.
        if (c.destino() != null && !"void".equals(tipo)) {
            declararSiNuevo(c.destino(), tipo);
        }
        return null;
    }

    @Override
    public Void visitar(CuadruplaRead c) {
        declararSiNuevo(c.destino(), "char*");
        return null;
    }

    @Override
    public Void visitar(CuadruplaNew c) {
        declararSiNuevo(c.destino(), c.clase() + "*");
        return null;
    }

    @Override
    public Void visitar(CuadruplaNewArray c) {
        String tipoC = c.tipoElemento().replace("[]", "*") + "*";
        declararSiNuevo(c.destino(), tipoC);
        return null;
    }

    /**
     * {@code destino = arreglo[idx]}. El tipo del elemento es el tipo del arreglo
     * "sin un nivel de indirección": {@code int*} → {@code int}, {@code int**} →
     * {@code int*}, {@code Persona**} → {@code Persona*}. Si el arreglo no se conoce
     * o su tipo no termina en {@code *}, cae a {@code int}.
     */
    @Override
    public Void visitar(CuadruplaIndiceCarga c) {
        String tipoArr = tiposConocidos.get(c.arreglo());
        String tipoElem = "int";
        if (tipoArr != null && tipoArr.endsWith("*")) {
            tipoElem = tipoArr.substring(0, tipoArr.length() - 1);
        }
        declararSiNuevo(c.destino(), tipoElem);
        return null;
    }

    /**
     * {@code destino = obj.campo}. Resuelve el tipo real del campo consultando la
     * tabla de tipos definidos:
     * <ol>
     *   <li>Obtiene el tipo C del objeto desde {@code tiposConocidos}.</li>
     *   <li>Le quita el {@code *} final para obtener el nombre de la clase/estructura.</li>
     *   <li>Busca esa clase en {@code tiposPorNombre} y consulta {@code buscarMiembro(campo)}.</li>
     *   <li>Traduce el {@link Tipo} del miembro a C con {@link #tipoAC}.</li>
     * </ol>
     * Si cualquier paso falla (objeto desconocido, tipo no registrado, campo no
     * existente), cae a {@code int}.
     */
    @Override
    public Void visitar(CuadruplaCampoCarga c) {
        String tipoC = resolverTipoCampo(c.objeto(), c.campo());
        declararSiNuevo(c.destino(), tipoC);
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
     * Resuelve el tipo C del campo {@code campo} accedido sobre {@code objeto}.
     * Devuelve {@code "int"} si no se puede resolver (degradación controlada).
     */
    private String resolverTipoCampo(String objeto, String campo) {
        if (objeto == null || campo == null) return "int";

        // 1) Tipo C del objeto (ej. "Estudiante*").
        String tipoObjC = tiposConocidos.get(objeto);
        if (tipoObjC == null) return "int";

        // 2) Nombre de la clase/estructura (sin el "*" final).
        String nombreTipo = tipoObjC.endsWith("*")
                ? tipoObjC.substring(0, tipoObjC.length() - 1)
                : tipoObjC;

        // 3) Buscar el símbolo del tipo en los tipos definidos.
        Simbolo sTipo = tiposPorNombre.get(nombreTipo);
        if (sTipo == null) return "int";

        // 4) Buscar el miembro (campo/atributo).
        Simbolo sCampo = sTipo.buscarMiembro(campo);
        if (sCampo == null) return "int";

        // 5) Traducir el Tipo del campo a C.
        return tipoAC(sCampo.getTipo());
    }

    private void declararSiNuevo(String lugar, String tipoC) {
        if (lugar == null) return;
        if (tiposConocidos.containsKey(lugar)) return;
        tiposConocidos.put(lugar, tipoC);
        declaracionesLocales.put(lugar, tipoC);
    }

    private String tipoDe(String lugar) {
        if (lugar == null) return "int";
        String t = tiposConocidos.get(lugar);
        return (t != null) ? t : "int";
    }

    private static boolean esAritmetico(String op) {
        return op != null && (
                op.equals("+") || op.equals("-") || op.equals("*")
                        || op.equals("/") || op.equals("%")
        );
    }

    private static String masAncho(String a, String b) {
        if ("double".equals(a) || "double".equals(b)) return "double";
        return "int";
    }

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

    /**
     * Tipo C de un lugar, extendido para detectar literales por su forma:
     * <ul>
     *   <li>{@code "..."} → char* (literal string)</li>
     *   <li>{@code '...'} → char (literal char)</li>
     *   <li>{@code true}/{@code false}/{@code null} → int/void*</li>
     *   <li>numérico con punto → double</li>
     *   <li>numérico sin punto → int</li>
     *   <li>cualquier otra cosa → consulta {@link #tiposConocidos}</li>
     * </ul>
     */
    private String tipoDeConLiterales(String lugar) {
        if (lugar == null) return "int";
        if (lugar.length() >= 2 && lugar.startsWith("\"") && lugar.endsWith("\"")) return "char*";
        if (lugar.length() >= 2 && lugar.startsWith("'") && lugar.endsWith("'")) return "char";
        if ("true".equals(lugar) || "false".equals(lugar)) return "int";
        if ("null".equals(lugar)) return "void*";
        if (lugar.matches("-?\\d+\\.\\d+")) return "double";
        if (lugar.matches("-?\\d+")) return "int";
        return tipoDe(lugar);
    }
}