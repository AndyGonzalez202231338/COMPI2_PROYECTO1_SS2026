package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.List;

/**
 * {@code inicializadorArreglo} (#inicializadorArregloDef): {@code { expresion (, expresion)* }}.
 * Cumple dos roles, igual que {@code ListaLiteral} en Y:
 * <ul>
 *   <li>Estructural: como inicializador de {@link DeclaracionArreglo} y de la
 *       variante ESTRUCTURA de {@link DeclaracionVariable}.</li>
 *   <li>Como expresión: {@code primaria} también la referencia directamente
 *       ({@code #primariaListaLiteral}), por lo que implementa {@link ExpresionPigLatin}
 *       para poder aparecer en cualquier lugar donde se espera una expresión.</li>
 * </ul>
 */
public final class InicializadorArreglo extends NodoPigLatin implements ExpresionPigLatin {

    private final List<ExpresionPigLatin> elementos;

    /** TipoArreglo(tipoElemento), cacheado por verificar(). */
    private Tipo tipoArreglo;

    public InicializadorArreglo(List<ExpresionPigLatin> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<ExpresionPigLatin> getElementos() { return elementos; }
    public Tipo getTipoArreglo() { return tipoArreglo; }

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
        tipoArreglo = new TipoArreglo(tipoElem);
        return tipoArreglo;
    }

    /**
     * Emite: pide un temporal nuevo {@code tArr} (que representa el arreglo en
     * construcción), y por cada elemento en orden genera su C3D y emite
     * {@code ([]=, tArr, i, v)} con {@code i} como literal entero.
     *
     * <p>NO se emite ningún {@code newarr} ni allocación: la reserva la resuelve
     * Fase 4, que ve el {@link TipoArreglo} del resultado (o el tamaño del símbolo
     * en la declaración) y reserva la memoria adecuada. Aquí solo se dejan las
     * cuádruplas de escritura por posición, exactamente igual que {@code ListaLiteral(Y)}.
     *
     * <p>Devuelve {@code temporal(tArr, tipoArreglo)}.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        String tArr = generador.nuevoTemporal();
        for (int i = 0; i < elementos.size(); i++) {
            ResultadoC3D v = elementos.get(i).generarC3D(generador);
            generador.emitirGuardarIndice(tArr, String.valueOf(i), v.getLugar());
        }
        Tipo tipo = (tipoArreglo != null) ? tipoArreglo : TipoPrimitivo.DESCONOCIDO;
        return ResultadoC3D.temporal(tArr, tipo);
    }
}