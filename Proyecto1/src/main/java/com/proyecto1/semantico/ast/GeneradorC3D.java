package com.proyecto1.semantico.ast;

import com.proyecto1.semantico.tabla.Ambito;

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