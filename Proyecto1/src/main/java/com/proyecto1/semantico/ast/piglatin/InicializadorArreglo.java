package com.proyecto1.semantico.ast.piglatin;

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

    public InicializadorArreglo(List<ExpresionPigLatin> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<ExpresionPigLatin> getElementos() {
        return elementos;
    }

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
        return new TipoArreglo(tipoElem);
    }
}
