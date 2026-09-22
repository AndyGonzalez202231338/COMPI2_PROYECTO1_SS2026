package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * Un literal de Z. Mismo diseño que el de Y: el valor ya viene parseado
 * (Long/Double/Character/String/Boolean/null), no como texto crudo.
 *
 * <p>Los dos {@code switch} sobre {@link CategoriaLiteral} son EXHAUSTIVOS, sin
 * {@code default}: si se añade una categoría nueva al enum, el compilador obligará
 * a mapearla aquí. Es deliberado — antes había un {@code default} que tragaba
 * silenciosamente las categorías nuevas.
 *
 * <p>Sobre {@link CategoriaLiteral#NULO}: el tipo semántico que se le asigna aquí
 * es {@link TipoPrimitivo#NULO} (ver {@link #tipoDeCategoria()}). Si tu
 * {@code TipoPrimitivo} todavía no tiene la constante {@code NULO}, hay dos
 * alternativas, en orden de preferencia:
 * <ol>
 *   <li>Añadir {@code NULO} a {@code TipoPrimitivo} y las correspondientes reglas
 *       a {@code Tipos.esAsignable} / {@code Tipos.esComparableIgualdad} (lo
 *       correcto: null es asignable a cualquier tipo de objeto, comparable a
 *       cualquier referencia).</li>
 *   <li>Cambiar el {@code case NULO ->} para que devuelva
 *       {@link TipoPrimitivo#DESCONOCIDO} (parche rápido, pero relaja el chequeo:
 *       null se vuelve compatible con todo sin distinción).</li>
 * </ol>
 */
public final class Literal extends NodoZ implements ExpresionZ {

    private final Object valor;
    private final CategoriaLiteral categoria;

    public Literal(Object valor, CategoriaLiteral categoria, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.categoria = categoria;
    }

    public Object getValor() { return valor; }
    public CategoriaLiteral getCategoria() { return categoria; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return tipoDeCategoria();
    }

    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        String texto = switch (categoria) {
            case ENTERO, FLOTANTE, BOOLEANO -> String.valueOf(valor);
            case CARACTER -> "'" + escapar(String.valueOf(valor), '\'') + "'";
            case CADENA   -> "\"" + escapar(String.valueOf(valor), '"') + "\"";
            case NULO     -> "null";
        };
        return ResultadoC3D.valor(texto, tipoDeCategoria());
    }

    /**
     * Mapeo categoría → tipo semántico. Es exhaustivo a propósito (sin default)
     * para que añadir una categoría al enum rompa la compilación aquí en vez de
     * caer silenciosamente a DESCONOCIDO.
     */
    private Tipo tipoDeCategoria() {
        return switch (categoria) {
            case ENTERO   -> TipoPrimitivo.ENTERO;
            case FLOTANTE -> TipoPrimitivo.FLOTANTE;
            case CARACTER -> TipoPrimitivo.CARACTER;
            case CADENA   -> TipoPrimitivo.CADENA;
            case BOOLEANO -> TipoPrimitivo.BOOL;
            case NULO     -> TipoPrimitivo.NULO;
        };
    }

    /** Re-escapa un texto ya desescapado para poder escribirlo entre comillas. */
    private static String escapar(String texto, char delimitador) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                case '\r' -> sb.append("\\r");
                case '\0' -> sb.append("\\0");
                default -> {
                    if (c == delimitador) sb.append('\\');
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}