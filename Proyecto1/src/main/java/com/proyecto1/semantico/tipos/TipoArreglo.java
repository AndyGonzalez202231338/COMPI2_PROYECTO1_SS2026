package com.proyecto1.semantico.tipos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tipo arreglo: envuelve un tipo base. Para Y siempre se usa con un solo nivel
 * (los arreglos se "aplanan", según la especificación); para Zetariano puede anidarse
 * (TipoArreglo(TipoArreglo(ENTERO)) para representar "int[][]").
 *
 * <p>Es un tipo compuesto en el sentido de "tiene estructura interna", pero
 * esCompuesto() se deja en false (reservado para estructuras/clases con miembros
 * accesibles por nombre) un arreglo se accede por índice, no por nombre de campo.
 *
 * <p>La LONGITUD se añadió para que Fase 4 sepa cuántas celdas reservar en la
 * declaración C ("int arr[5]"). Es METADATO, no parte de la identidad del tipo:
 * {@code int[5]} e {@code int[10]} son el mismo TIPO para efectos de asignación,
 * comparación y sobrecarga — por eso longitud NO entra en equals()/hashCode().
 * Vale {@link #LONGITUD_DESCONOCIDA} cuando no aplica: parámetros formales, tipo de
 * retorno, expresiones cuyo tamaño se calcula en runtime, etc.
 *
 * <p>Un arreglo multidimensional se representa ANIDANDO TipoArreglo:
 * {@code int[3][4]} es {@code TipoArreglo(TipoArreglo(ENTERO, 4), 3)}. La
 * longitud del nivel más externo (3) va en el TipoArreglo externo; la del
 * siguiente nivel (4) va en el TipoArreglo interno; y así sucesivamente.
 */
public final class TipoArreglo implements Tipo {

    /** Marca de "longitud no conocida en tiempo de compilación". */
    public static final int LONGITUD_DESCONOCIDA = -1;

    private final Tipo base;
    private final int longitud;

    /** Constructor histórico (longitud desconocida). */
    public TipoArreglo(Tipo base) {
        this(base, LONGITUD_DESCONOCIDA);
    }

    /** Constructor completo: usar cuando la longitud se conoce. */
    public TipoArreglo(Tipo base, int longitud) {
        this.base = base;
        this.longitud = longitud;
    }

    public Tipo getBase() { return base; }

    /** Longitud del nivel MÁS EXTERNO, o {@link #LONGITUD_DESCONOCIDA}. */
    public int getLongitud() { return longitud; }

    /** true si la longitud del nivel más externo se conoce en compile-time. */
    public boolean tieneLongitudConocida() {
        return longitud != LONGITUD_DESCONOCIDA;
    }

    /** Para "int[][][]" devuelve el tipo escalar final (ENTERO), atravesando todos los niveles. */
    public Tipo baseEscalar() {
        Tipo actual = base;
        while (actual instanceof TipoArreglo ta) actual = ta.getBase();
        return actual;
    }

    /** Cuántos pares "[]" tiene este arreglo (1 para "int[]", 2 para "int[][]", etc.). */
    public int dimensiones() {
        int contador = 1;
        Tipo actual = base;
        while (actual instanceof TipoArreglo ta) {
            contador++;
            actual = ta.getBase();
        }
        return contador;
    }

    /**
     * Longitudes de TODOS los niveles, del externo al interno. Ejemplos:
     * <ul>
     *   <li>int[5]        -> [5]</li>
     *   <li>int[3][4]     -> [3, 4]</li>
     *   <li>int[n][3]     -> [-1, 3]</li>
     *   <li>int[n][m]     -> [-1, -1]</li>
     *   <li>int[3][4][5]  -> [3, 4, 5]</li>
     * </ul>
     * Cada -1 ({@link #LONGITUD_DESCONOCIDA}) indica que ese nivel solo se conoce
     * en runtime.
     */
    public List<Integer> tamanosCompletos() {
        List<Integer> out = new ArrayList<>();
        Tipo actual = this;
        while (actual instanceof TipoArreglo ta) {
            out.add(ta.getLongitud());
            actual = ta.getBase();
        }
        return out;
    }

    /**
     * ¿Se puede aplicar el aplanado (flat) a este arreglo?
     *
     * <p>Regla: SÍ si TODAS las dimensiones internas (d2, d3, ..., dn) se conocen
     * en compile-time. La dimensión externa (d1) NO cuenta para esta decisión:
     * solo se usa en el malloc del {@code new}, no en el cálculo del índice
     * aplanado.
     *
     * <p>Ejemplos:
     * <ul>
     *   <li>int[5][3]  -> internas [3]    -> true</li>
     *   <li>int[n][3]  -> internas [3]    -> true</li>
     *   <li>int[3][n]  -> internas [-1]   -> false (jagged)</li>
     *   <li>int[n][m]  -> internas [-1,-1]-> false (jagged)</li>
     *   <li>int[5]     -> sin internas    -> true</li>
     * </ul>
     */
    public boolean esAplanable() {
        List<Integer> dims = tamanosCompletos();
        for (int i = 1; i < dims.size(); i++) {
            if (dims.get(i) == LONGITUD_DESCONOCIDA) return false;
        }
        return true;
    }

    /**
     * Nombre del TIPO, no de la instancia: no incluye la longitud.
     */
    @Override
    public String nombre() {
        return base.nombre() + "[]";
    }

    /** Solo para depuración: "int[5]" o "int[]" si la longitud es desconocida. */
    public String toStringConLongitud() {
        return tieneLongitudConocida()
                ? base.nombre() + "[" + longitud + "]"
                : base.nombre() + "[]";
    }

    @Override
    public boolean esArreglo() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TipoArreglo otro)) return false;
        return Objects.equals(this.base, otro.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TipoArreglo.class, base);
    }

    @Override
    public String toString() {
        return nombre();
    }
}