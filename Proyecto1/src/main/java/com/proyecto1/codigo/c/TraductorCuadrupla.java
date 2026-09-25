package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.cuadruplas.*;

/**
 * Traduce UNA cuádrupla del C3D a su línea equivalente en C.
 *
 * <p>El despacho es por Visitor sobre la jerarquía sellada de {@link Cuadrupla}:
 * cada tipo de instrucción tiene su propio método {@code visitar(...)}. Si mañana
 * se agrega un tipo de cuádrupla nuevo, {@link VisitanteCuadrupla} obliga a agregar
 * el método aquí también (no compila si falta).
 *
 * <p><b>Convención de salida:</b> cada línea se emite CON {@code ;} final (o con
 * {@code :;} en el caso de las etiquetas — el {@code ;} extra evita el error de C
 * "label at end of compound statement" cuando la etiqueta queda como última línea
 * de un bloque). Sin indentación ni estructura de bloques: eso lo añade quien
 * ensambla el archivo completo (fase posterior).
 *
 * <p>Esta clase NO maneja indentación ni agrupa cuádruplas relacionadas
 * ({@code param} + {@code call}, {@code begin_func} + cuerpo + {@code end_func}):
 * esas agrupaciones requieren contexto y son responsabilidad del orquestador.
 *
 * <hr>
 *
 * <h2>Fase 4.4 — el modelo de heap</h2>
 *
 * <p>Esta fase introduce la traducción de las operaciones sobre objetos de
 * Zetariano (y las estructuras de Y cuando se usan con {@code new}). El modelo de
 * memoria subyacente se rige por cuatro decisiones:
 *
 * <p><b>1. {@code new} usa {@code sizeof} y no un tamaño calculado a mano.</b>
 * La cuádrupla {@code CuadruplaNew} solo lleva el NOMBRE de la clase, nunca su
 * tamaño en bytes. El traductor emite {@code malloc(sizeof(Clase))} y deja que el
 * compilador de C resuelva el tamaño en base al {@code typedef struct} emitido en
 * la Fase 4.3. Motivo: el C3D no carga con la responsabilidad de calcular layouts
 * de structs (padding, alineación, orden de campos) — eso es precisamente lo que C
 * ya sabe hacer.
 *
 * <p><b>2. El acceso a campo siempre usa {@code ->}, nunca {@code .}.</b> Tanto
 * {@code TipoClase} como {@code TipoEstructura} se traducen SIEMPRE a punteros
 * (ver {@link TraductorTipos#aC}). Por eso una variable {@code obj} de tipo
 * clase/estructura contiene SIEMPRE un puntero, y el operador correcto es
 * {@code ->} en todos los casos. Esto evita ramificar "¿es valor o referencia?"
 * en cada acceso a campo.
 *
 * <p><b>3. {@code "this"} no es un caso especial.</b> Dentro de un método o
 * constructor de Z, el receptor implícito se representa con el string literal
 * {@code "this"}. El traductor lo trata como cualquier otro operando:
 * {@code (=., this, edad, t)} produce {@code t = this->edad;}, exactamente igual
 * que si {@code this} fuera una variable local. No hay rama "si obj es this".
 *
 * <p><b>4. Esta fase NUNCA emite {@code free}.</b> Es una decisión documentada,
 * no un olvido. El PDF del proyecto permite aceptar los leaks de memoria como
 * parte del alcance del compilador: no hay recolección de basura ni liberación
 * manual. Si en el futuro se quiere añadir un GC o un {@code rt_free}, se haría
 * en una fase separada, no aquí.
 */
public final class TraductorCuadrupla implements VisitanteCuadrupla<String> {

    /** Traduce una cuádrupla a su línea de C. Punto de entrada único de esta clase. */
    public String traducir(Cuadrupla c) {
        return c.aceptar(this);
    }

    // ---------- Aritmética / lógica / relacionales ----------
    // El operador (+, -, ==, &&, ...) es el mismo símbolo en C, así que se copia
    // tal cual. La única excepción es la unaria "not": en C3D el operador es la
    // palabra "not", pero en C es "!".

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
        // "label at end of compound statement" cuando la etiqueta queda como
        // última línea de un bloque (p. ej. L_fin: justo antes de la llave de
        // cierre de una función). El ";" convierte la línea en "etiqueta +
        // sentencia vacía", lo cual es válido.
        return c.etiqueta() + ":;";
    }

    @Override
    public String visitar(CuadruplaReturn c) {
        return c.valor() != null ? "return " + c.valor() + ";" : "return;";
    }

    // ---------- Fase 4.4: heap ----------

    /**
     * {@code destino = new Clase} → {@code destino = (Clase*) malloc(sizeof(Clase));}.
     *
     * <p>El cast a {@code Clase*} es idiomático (aunque en C puro {@code malloc}
     * devuelve {@code void*} convertible implícitamente): documenta la intención y
     * hace el código aceptable si en algún momento se compila con {@code g++}.
     *
     * <p>El tamaño lo resuelve {@code sizeof(Clase)} en tiempo de compilación de C,
     * a partir del {@code typedef struct Clase {...};} emitido en la Fase 4.3. El
     * C3D nunca calcula bytes.
     */
    @Override
    public String visitar(CuadruplaNew c) {
        return c.destino() + " = (" + c.clase() + "*) malloc(sizeof(" + c.clase() + "));";
    }

    /**
     * {@code destino = obj.campo} → {@code destino = obj->campo;}.
     *
     * <p>{@code obj} puede ser el nombre de una variable local/parámetro, un
     * temporal, o el string literal {@code "this"} cuando el acceso ocurre dentro
     * de un método/constructor. En TODOS los casos la traducción es idéntica.
     */
    @Override
    public String visitar(CuadruplaCampoCarga c) {
        return c.destino() + " = " + c.objeto() + "->" + c.campo() + ";";
    }

    /**
     * {@code obj.campo = valor} → {@code obj->campo = valor;}.
     *
     * <p>En este record, el campo {@code valor} es lo que se guarda (no hay un
     * "destino" separado): la escritura va directamente a la dirección
     * {@code obj->campo}. El nombre del campo del record deja esto explícito, así
     * que no hay riesgo de confundirlo con un destino.
     */
    @Override
    public String visitar(CuadruplaCampoGuarda c) {
        return c.objeto() + "->" + c.campo() + " = " + c.valor() + ";";
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

    // ---------- I/O, arreglos: fases posteriores ----------

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

    /**
     * {@code destino = new tipoElemento[t1][t2]...[tn]}.
     *
     * <p>Emitido SOLO para el nivel externo de un arreglo (flat o jagged). La
     * traducción es un malloc del producto de los tamaños por sizeof(tipoElemento):
     *
     *     destino = (tipoElemento*) malloc((t1 * t2 * ... * tn) * sizeof(tipoElemento));
     *
     * <p>Esta cuádrupla es agnóstica a flat vs. jagged:
     * <ul>
     *   <li>En FLAT, tipoElemento es el tipo ESCALAR ("int") y tamanos contiene
     *       TODAS las dimensiones. El malloc reserva el bloque completo.</li>
     *   <li>En JAGGED, tipoElemento es el tipo del SIGUIENTE nivel ("int*" para
     *       int[n][m]) y tamanos contiene SOLO el tamaño del nivel externo. Los
     *       niveles internos se construyen con cuádruplas adicionales emitidas por
     *       NuevoArregloConTamano (bucles con más newarr).</li>
     * </ul>
     * El cast "(tipoElemento*)" es idiomático (aunque en C puro malloc devuelve
     * void* convertible implícitamente): documenta la intención y funciona si en
     * algún momento se compila con g++.
     */
    @Override
    public String visitar(CuadruplaNewArray c) {
        java.util.List<String> ts = c.tamanos();
        String producto = (ts.size() == 1) ? ts.get(0) : String.join(" * ", ts);
        return c.destino() + " = (" + c.tipoElemento() + "*) malloc(("
                + producto + ") * sizeof(" + c.tipoElemento() + "));";
    }

    private static UnsupportedOperationException pendiente(String queNoEstaHecho) {
        return new UnsupportedOperationException(
                "TraductorCuadrupla: '" + queNoEstaHecho + "' todavía no está implementado (fase posterior)");
    }
}