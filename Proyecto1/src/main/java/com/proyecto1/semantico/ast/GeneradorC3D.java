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
 *   - consultar el Ámbito (tabla de símbolos) para resolver tipos de identificadores,
 *   - construir etiquetas de función/método/constructor (mangling).
 *
 * Cada nodo recibe este generador en generarC3D(generador), de modo que ningún nodo
 * guarda estado propio ("en qué temporal voy"). Las cuádruplas viven solo en la
 * TablaCuadruplas de este generador (no en ResultadoC3D), lo que permite backpatching
 * global mediante siguienteIndice() y reemplazar(int, Cuadrupla).
 *
 * El Ámbito es opcional: si se construye sin él, getAmbito() devuelve null y los nodos
 * que lo necesitan caen a TipoPrimitivo.DESCONOCIDO. Para Z es OBLIGATORIO pasarlo
 * (Identificador y Asignación de Z lo necesitan para distinguir ATRIBUTO vs. variable).
 */
public class GeneradorC3D {

    private int contadorTemporales = 0;
    private int contadorEtiquetas  = 0;
    private final TablaCuadruplas tabla;
    private final Ambito ambito; // puede ser null (sin información de tipos)

    /**
     * Pilas de etiquetas de los ciclos abiertos, con el ciclo más interno en el tope.
     * Siempre se apilan y desapilan juntas (entrarCiclo/salirCiclo), así que tienen
     * el mismo tamaño. Las llenan Mientras, Para y HacerMientras y las consultan
     * Continuar y Romper. Se usan pilas (no un solo par) para que continuar/romper
     * siempre apunten al ciclo más interno cuando hay ciclos anidados.
     */
    private final Deque<String> pilaInicioCiclo = new ArrayDeque<>();
    private final Deque<String> pilaFinCiclo    = new ArrayDeque<>();

    public GeneradorC3D() {
        this(null, new TablaCuadruplas());
    }

    public GeneradorC3D(TablaCuadruplas tabla) {
        this(null, tabla);
    }

    public GeneradorC3D(Ambito ambito) {
        this(ambito, new TablaCuadruplas());
    }

    public GeneradorC3D(Ambito ambito, TablaCuadruplas tabla) {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla de cuádruplas no puede ser null");
        }
        this.ambito = ambito;
        this.tabla  = tabla;
    }

    // ---------- Ámbito ----------

    public Ambito getAmbito() {
        return ambito;
    }

    // ---------- Pilas de ciclos (para continuar / romper) ----------

    public void entrarCiclo(String inicio, String fin) {
        pilaInicioCiclo.push(inicio);
        pilaFinCiclo.push(fin);
    }

    public void salirCiclo() {
        if (pilaInicioCiclo.isEmpty()) {
            throw new IllegalStateException("salirCiclo() sin un entrarCiclo() previo");
        }
        pilaInicioCiclo.pop();
        pilaFinCiclo.pop();
    }

    public String etiquetaInicioCiclo() {
        return pilaInicioCiclo.peek();
    }

    public String etiquetaFinCiclo() {
        return pilaFinCiclo.peek();
    }

    public void entrarBloqueRompible(String fin) {
        pilaFinCiclo.push(fin);
    }

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

    public void emitirAsignacion(String v, String x) {
        emitir(Cuadrupla.OP_ASIGNACION, v, null, x);
    }

    public void emitirBinaria(String op, String a, String b, String t) {
        emitir(op, a, b, t);
    }

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

    /** param v : empuja v como argumento de la próxima call. Orden = orden fuente. */
    public void emitirParam(String v) {
        emitir(Cuadrupla.OP_PARAM, v, null, null);
    }

    public void emitirReturn(String v) {
        emitir(Cuadrupla.OP_RETURN, v, null, null);
    }

    public void emitirBeginFunc(String nombre, int nArgs) {
        emitir(Cuadrupla.OP_BEGIN_FUNC, nombre, String.valueOf(nArgs), null);
    }

    public void emitirEndFunc() {
        emitir(Cuadrupla.OP_END_FUNC, null, null, null);
    }

    // ---------- Fase 1.6: arreglos y campos ----------

    /** t = arr[i]  →  (=[], arr, i, t). Fase 4 aplica base + i*tamañoElemento. */
    public void emitirCargaIndice(String arr, String idx, String t) {
        emitir(Cuadrupla.OP_INDEX_LOAD, arr, idx, t);
    }

    /** arr[i] = v  →  ([]=, arr, i, v). */
    public void emitirGuardarIndice(String arr, String idx, String v) {
        emitir(Cuadrupla.OP_INDEX_STORE, arr, idx, v);
    }

    /** t = obj.f  →  (=., obj, f, t). El campo va por NOMBRE, no por offset. */
    public void emitirCargaCampo(String obj, String campo, String t) {
        emitir(Cuadrupla.OP_FIELD_LOAD, obj, campo, t);
    }

    /** obj.f = v  →  (.=, obj, f, v). */
    public void emitirGuardarCampo(String obj, String campo, String v) {
        emitir(Cuadrupla.OP_FIELD_STORE, obj, campo, v);
    }

    // ---------- Fase Z.0: objetos de Z ----------

    /**
     * t = new NombreClase  →  (new, NombreClase, null, t).
     * Fase 4 lo traduce a {@code t = malloc(sizeof(NombreClase))}.
     */
    public void emitirNew(String nombreClase, String t) {
        emitir(Cuadrupla.OP_NEW, nombreClase, null, t);
    }

    /** t = new Tipo[tamaño]  →  (newarr, Tipo, tamaño, t). Fase 4: malloc(tamaño * sizeof(Tipo)). */
    public void emitirNewArray(String tipoDescriptor, String tamano, String t) {
        emitir(Cuadrupla.OP_NEW_ARRAY, tipoDescriptor, tamano, t);
    }

    /**
     * Etiqueta para un método de una clase Z: "Clase_metodo".
     * Los métodos NO se sobrecargan en Z (declararMiembro los guarda por nombre plano).
     */
    public String etiquetaMetodo(String clase, String metodo) {
        return clase + "_" + metodo;
    }

    /**
     * Etiqueta para un constructor de una clase Z: "Clase_init@N".
     * Los constructores SÍ se sobrecargan por aridad, de ahí el sufijo "@N".
     *
     * <p><b>Deuda detectada</b>: hoy {@code Constructor.verificar} intenta resolver el
     * constructor buscando por {@code nombre + "@" + aridad}, pero
     * {@code AmbitoContenedor.declararMiembro} los guarda por nombre plano. Es una
     * incoherencia preexistente entre esas dos clases (no de C3D). Antes de generar
     * C3D con constructores sobrecargados, hay que decidir cuál de las dos se arregla:
     * o {@code declararMiembro} usa la clave {@code nombre@aridad} al declararlos, o
     * {@code Constructor.verificar} busca por nombre plano. La etiqueta de este helper
     * asume lo primero (sobrecarga permitida).
     */
    public String etiquetaConstructor(String clase, int aridad) {
        return clase + "_init@" + aridad;
    }

    // ---------- Acceso a la tabla / backpatching ----------

    public TablaCuadruplas getTabla() {
        return tabla;
    }

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