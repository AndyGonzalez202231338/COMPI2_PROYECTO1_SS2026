package com.proyecto1.semantico.tipos;

import java.util.Objects;

/**
 * Tipo arreglo: envuelve un tipo base. Para Y siempre se usa con un solo nivel
 * (los arreglos se "aplanan", según la especificación); para Zetariano puede anidarse
 * (TipoArreglo(TipoArreglo(ENTERO)) para representar "int[][]").
 *
 * Es un tipo compuesto en el sentido de "tiene estructura interna", pero
 * esCompuesto() se deja en false (reservado para estructuras/clases con miembros
 * accesibles por nombre) un arreglo se accede por índice, no por nombre de campo.
 *
 * La LONGITUD se añadió para que Fase 4 sepa cuántas celdas reservar en la
 * declaración C ("int arr[5]"). Es METADATO, no parte de la identidad del tipo:
 * {@code int[5]} e {@code int[10]} son el mismo TIPO para efectos de asignación,
 * comparación y sobrecarga — por eso longitud NO entra en equals()/hashCode().
 * Vale {@link #LONGITUD_DESCONOCIDA} cuando no aplica: parámetros formales, tipo de
 * retorno, expresiones cuyo tamaño se calcula en runtime, etc.
 */
public final class TipoArreglo implements Tipo {

    /** Marca de "longitud no conocida en tiempo de compilación". */
    public static final int LONGITUD_DESCONOCIDA = -1;

    private final Tipo base;
    private final int longitud;

    /**
     * Constructor histórico (longitud desconocida). Se conserva para no romper
     * llamadas existentes: parámetros formales, tipos de retorno, usos genéricos
     * donde la longitud todavía no se conoce.
     */
    public TipoArreglo(Tipo base) {
        this(base, LONGITUD_DESCONOCIDA);
    }

    /** Constructor completo: usar cuando la longitud se conoce (declaraciones, literales). */
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
     * Nombre del TIPO, no de la instancia: no incluye la longitud. Así "int[5]" e
     * "int[10]" reportan ambos "int[]" en mensajes de error, que es lo correcto
     * porque el sistema de tipos los trata igual. Si en algún punto quieres el
     * nombre concreto con tamaño (p. ej. para depurar), usa {@link #toStringConLongitud()}.
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

    /**
     * DOS arreglos son iguales si sus bases lo son — la longitud NO cuenta.
     * Justificación: en un lenguaje con asignación de arreglos-como-vista (o que
     * simplemente no permite copia por valor), "int[5]" e "int[10]" son ambos "int[]"
     * y asignables entre sí si el sistema lo permite; meter la longitud en equals()
     * haría que `int[5] x = arr10;` fallara como si fueran tipos distintos, lo cual
     * contradice la semántica que ya tienes en Tipos.esAsignable.
     */
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