package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.List;

/** {@code LLAVEIZQ initializerList? LLAVEDER} (#primarioListaLiteral): "{1, 2, 3}". */
public final class ListaLiteral extends NodoZ implements ExpresionZ {

    private final List<ExpresionZ> elementos;

    public ListaLiteral(List<ExpresionZ> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = elementos;
    }

    public List<ExpresionZ> getElementos() {
        return elementos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tipoElemento = null;
        for (ExpresionZ e : elementos) {
            Tipo t = e.verificar(ambito, errores);
            if (tipoElemento == null) tipoElemento = t;
            else if (!Tipos.esAsignable(tipoElemento, t) && !Tipos.esAsignable(t, tipoElemento))
                errores.reportar(e.getLinea(), e.getColumna(),
                        "Elemento de lista incompatible: " + t.nombre() + " vs " + tipoElemento.nombre());
        }
        if (tipoElemento == null) tipoElemento = TipoPrimitivo.DESCONOCIDO;
        return new TipoArreglo(tipoElemento);
    }
}
