package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoEstructura;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.List;

/**
 * {@code inicializadorArreglo} (#inicializadorArregloDef): {@code { expr, expr, ... }}.
 *
 * <p>Cuando se usa como inicializador, reserva el arreglo (un solo {@code newarr}
 * flat) y escribe cada elemento en su posición con {@code []=}.
 */
public final class InicializadorArreglo extends NodoPigLatin implements ExpresionPigLatin {

    private final List<ExpresionPigLatin> elementos;

    /** TipoArreglo(tipoElemento), cacheado por verificar(). */
    private TipoArreglo tipoArreglo;

    public InicializadorArreglo(List<ExpresionPigLatin> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<ExpresionPigLatin> getElementos() { return elementos; }
    public TipoArreglo getTipoArreglo()          { return tipoArreglo; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tipoElem = null;
        for (ExpresionPigLatin e : elementos) {
            Tipo t = e.verificar(ambito, errores);
            if (tipoElem == null) tipoElem = t;
            else if (!Tipos.esAsignable(tipoElem, t) && !Tipos.esAsignable(t, tipoElem))
                errores.reportar(e.getLinea(), e.getColumna(),
                        "Elemento incompatible: " + t.nombre() + " vs " + tipoElem.nombre());
        }
        if (tipoElem == null) tipoElem = TipoPrimitivo.DESCONOCIDO;
        tipoArreglo = new TipoArreglo(tipoElem, elementos.size());
        return tipoArreglo;
    }

    /**
     * Emite:
     * <ol>
     *   <li>{@code (newarr, descriptorBase, [N], t)}: un solo malloc del bloque
     *       contiguo. {@code descriptorBase} es el tipo C del ELEMENTO, no del
     *       arreglo (p. ej. {@code "int"} para un arreglo de enteros).</li>
     *   <li>Por cada elemento en orden: {@code ([]=, t, i, v)} con índice constante.</li>
     * </ol>
     * Devuelve {@code temporal(t, tipoArreglo)}.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        Tipo baseElem = (tipoArreglo != null) ? tipoArreglo.getBase() : TipoPrimitivo.DESCONOCIDO;
        String descriptor = descriptorBase(baseElem);

        List<String> tamanos = List.of(String.valueOf(elementos.size()));
        String arr = generador.nuevoTemporal();
        generador.emitirNewArray(descriptor, tamanos, arr);

        for (int i = 0; i < elementos.size(); i++) {
            ResultadoC3D v = elementos.get(i).generarC3D(generador);
            generador.emitirGuardarIndice(arr, String.valueOf(i), v.getLugar());
        }

        Tipo tipoResultado = (tipoArreglo != null) ? tipoArreglo : TipoPrimitivo.DESCONOCIDO;
        return ResultadoC3D.temporal(arr, tipoResultado);
    }

    /**
     * Descriptor C del tipo de un elemento (no del arreglo). Igual que el mapeo de
     * {@code TraductorTipos.aC}, pero local para no acoplar el AST al paquete del
     * traductor C.
     */
    private static String descriptorBase(Tipo t) {
        if (t == null) return "int";
        if (t == TipoPrimitivo.ENTERO)   return "int";
        if (t == TipoPrimitivo.FLOTANTE) return "double";
        if (t == TipoPrimitivo.CARACTER) return "char";
        if (t == TipoPrimitivo.CADENA)   return "char*";
        if (t == TipoPrimitivo.BOOL)     return "int";
        if (t instanceof TipoClase tc)      return tc.getDefinicion().getNombre() + "*";
        if (t instanceof TipoEstructura te) return te.getDefinicion().getNombre() + "*";
        if (t instanceof TipoArreglo ta)    return descriptorBase(ta.getBase()) + "*";
        return "int";
    }
}