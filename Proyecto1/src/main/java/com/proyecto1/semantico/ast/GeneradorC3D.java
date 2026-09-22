package com.proyecto1.semantico.ast;

import com.proyecto1.semantico.tabla.Ambito;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Servicios compartidos que necesita cualquier nodo al traducirse a C3D:
 *   - pedir un temporal nuevo (t0, t1, ...),
 *   - pedir una etiqueta nueva (L0, L1, ... para saltos de si/mientras/para),
 *   - emitir cuádruplas ESTRUCTURADAS en la tabla global,
 *   - consultar el Ámbito (tabla de símbolos) para resolver tipos de identificadores.
 *
 * Cada nodo recibe este generador en generarC3D(generador), de modo que ningún nodo
 * guarda estado propio ("en qué temporal voy"). Las cuádruplas viven solo en la
 * TablaCuadruplas de este generador (no en ResultadoC3D), lo que permite backpatching
 * global mediante siguienteIndice() y reemplazar(int, Cuadrupla).
 *
 * El Ámbito es opcional: si se construye sin él, getAmbito() devuelve null y los nodos
 * que lo necesitan (p. ej. Identificador) caen a TipoPrimitivo.DESCONOCIDO.
 */
public class GeneradorC3D {

    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;
    private final TablaCuadruplas tabla;
    private final Ambito ambito; // puede ser null (sin información de tipos)

    /**
     * Pilas de etiquetas de los ciclos abiertos, con el ciclo más interno en el tope.
     * Siempre se apilan y desapilan juntas (entrarCiclo/salirCiclo), así que tienen
     * el mismo tamaño. Las llenarán Mientras, Para y HacerMientras (Fase 1.4) y las
     * consultan Continuar y Romper. Se usan pilas (no un solo par de etiquetas) para
     * que "continuar"/"romper" siempre apunten al ciclo más interno cuando hay ciclos
     * anidados.
     */
    private final Deque<String> pilaInicioCiclo = new ArrayDeque<>();
    private final Deque<String> pilaFinCiclo = new ArrayDeque<>();

    /** Sin ámbito: los tipos de los identificadores saldrán DESCONOCIDO. */
    public GeneradorC3D() {
        this(null, new TablaCuadruplas());
    }

    /** Sin ámbito, con tabla inyectada (útil para pruebas). */
    public GeneradorC3D(TablaCuadruplas tabla) {
        this(null, tabla);
    }

    /** Con ámbito para resolver tipos de identificadores. */
    public GeneradorC3D(Ambito ambito) {
        this(ambito, new TablaCuadruplas());
    }

    /** Con ámbito y tabla inyectada (útil para pruebas). */
    public GeneradorC3D(Ambito ambito, TablaCuadruplas tabla) {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla de cuádruplas no puede ser null");
        }
        this.ambito = ambito;
        this.tabla = tabla;
    }

    // ---------- Ámbito ----------

    /** Ámbito para resolver símbolos, o null si el generador se creó sin él. */
    public Ambito getAmbito() {
        return ambito;
    }

    // ---------- Pilas de ciclos (para continuar / romper) ----------

    /**
     * Registra un ciclo que se empieza a generar. Lo llamarán Mientras, Para y
     * HacerMientras (Fase 1.4) justo antes de generar el cuerpo, y {@link #salirCiclo()}
     * justo después.
     *
     * @param inicio etiqueta a la que debe saltar "continuar". OJO: es el destino de
     *               "continuar", que no siempre es el comienzo del ciclo: en un "para"
     *               es el paso de actualización, y en un hacer-mientras la evaluación
     *               de la condición.
     * @param fin    etiqueta a la que debe saltar "romper" (la salida del ciclo).
     */
    public void entrarCiclo(String inicio, String fin) {
        pilaInicioCiclo.push(inicio);
        pilaFinCiclo.push(fin);
    }

    /** Cierra el ciclo más interno (desapila ambas etiquetas). */
    public void salirCiclo() {
        if (pilaInicioCiclo.isEmpty()) {
            throw new IllegalStateException("salirCiclo() sin un entrarCiclo() previo");
        }
        pilaInicioCiclo.pop();
        pilaFinCiclo.pop();
    }

    /** Destino de "continuar" del ciclo más interno, o null si no hay ningún ciclo abierto. */
    public String etiquetaInicioCiclo() {
        return pilaInicioCiclo.peek();
    }

    /** Destino de "romper" del ciclo más interno, o null si no hay ningún ciclo abierto. */
    public String etiquetaFinCiclo() {
        return pilaFinCiclo.peek();
    }

    /**
     * Registra un bloque "rompible" que NO es un ciclo: el caso/siempre de un
     * {@code elegir}. Solo empuja a la pila de "fin" (la que consulta "romper"); a
     * diferencia de {@link #entrarCiclo(String, String)}, NO toca la pila de "inicio"
     * (la que consulta "continuar"), porque un elegir no habilita "continuar": si está
     * anidado dentro de un ciclo, "continuar" debe seguir refiriéndose a ESE ciclo, no
     * al elegir. Así, "romper" dentro de un caso salta al fin del elegir (el más
     * cercano), y "continuar" dentro del mismo caso sigue apuntando al ciclo externo,
     * si lo hay.
     */
    public void entrarBloqueRompible(String fin) {
        pilaFinCiclo.push(fin);
    }

    /** Cierra el bloque rompible más interno abierto con {@link #entrarBloqueRompible(String)}. */
    public void salirBloqueRompible() {
        if (pilaFinCiclo.isEmpty()) {
            throw new IllegalStateException("salirBloqueRompible() sin un entrarBloqueRompible() previo");
        }
        pilaFinCiclo.pop();
    }

    // ---------- Temporales y etiquetas ----------

    public String nuevoTemporal() {
        return "t" + (contadorTemporales++);
    }

    public String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }

    // ---------- Emisores tipados ----------

    /** Genérico para casos raros. */
    public void emitir(String op, String arg1, String arg2, String resultado) {
        tabla.agregar(new Cuadrupla(op, arg1, arg2, resultado));
    }

    /** x = v */
    public void emitirAsignacion(String v, String x) {
        emitir(Cuadrupla.OP_ASIGNACION, v, null, x);
    }

    /** t = a op b */
    public void emitirBinaria(String op, String a, String b, String t) {
        emitir(op, a, b, t);
    }

    /** t = op a */
    public void emitirUnaria(String op, String a, String t) {
        emitir(op, a, null, t);
    }

    public void emitirGoto(String etiqueta) {
        emitir(Cuadrupla.OP_GOTO, null, null, etiqueta);
    }

    public void emitirIfFalse(String condicion, String etiqueta) {
        emitir(Cuadrupla.OP_IF_FALSE, condicion, null, etiqueta);
    }

    public void emitirIfTrue(String condicion, String etiqueta) {
        emitir(Cuadrupla.OP_IF_TRUE, condicion, null, etiqueta);
    }

    public void emitirEtiqueta(String etiqueta) {
        emitir(Cuadrupla.OP_ETIQUETA, null, null, etiqueta);
    }

    public void emitirPrint(String v) {
        emitir(Cuadrupla.OP_PRINT, v, null, null);
    }

    public void emitirRead(String x) {
        emitir(Cuadrupla.OP_READ, null, null, x);
    }

    /** t = call f(nArgs argumentos); t puede ser null si no se usa el valor. */
    public void emitirCall(String f, int nArgs, String t) {
        emitir(Cuadrupla.OP_CALL, f, String.valueOf(nArgs), t);
    }

    /** v puede ser null para un return sin valor. */
    public void emitirReturn(String v) {
        emitir(Cuadrupla.OP_RETURN, v, null, null);
    }

    public void emitirBeginFunc(String nombre, int nArgs) {
        emitir(Cuadrupla.OP_BEGIN_FUNC, nombre, String.valueOf(nArgs), null);
    }

    public void emitirEndFunc() {
        emitir(Cuadrupla.OP_END_FUNC, null, null, null);
    }

    /** t = arr[i]  (=[], arr, i, t). */
    public void emitirCargaIndice(String arr, String idx, String t) {
        emitir(Cuadrupla.OP_INDEX_LOAD, arr, idx, t);
    }

    /** arr[i] = v   ([]=, arr, i, v). */
    public void emitirGuardarIndice(String arr, String idx, String v) {
        emitir(Cuadrupla.OP_INDEX_STORE, arr, idx, v);
    }

    /** t = obj.f   (=., obj, f, t). El campo va por NOMBRE, no por offset. */
    public void emitirCargaCampo(String obj, String campo, String t) {
        emitir(Cuadrupla.OP_FIELD_LOAD, obj, campo, t);
    }

    /** obj.f = v   (.=, obj, f, v). */
    public void emitirGuardarCampo(String obj, String campo, String v) {
        emitir(Cuadrupla.OP_FIELD_STORE, obj, campo, v);
    }

    /** param v : registra v como argumento de la próxima {@code call}. */
    public void emitirParam(String v) {
        emitir(Cuadrupla.OP_PARAM, v, null, null);
    }

    // ---------- Acceso a la tabla / backpatching ----------

    public TablaCuadruplas getTabla() {
        return tabla;
    }

    /** Índice de la próxima cuádrupla a emitir (para guardarlo y hacer backpatching luego). */
    public int siguienteIndice() {
        return tabla.siguienteIndice();
    }

    public void reemplazar(int indice, Cuadrupla nueva) {
        tabla.reemplazar(indice, nueva);
    }

    public List<Cuadrupla> getCuadruplas() {
        return tabla.getCuadruplas();
    }
}