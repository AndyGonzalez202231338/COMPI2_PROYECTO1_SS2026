package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

public final class Indice extends NodoY implements ExpresionY {

    private final ExpresionY arreglo;
    private final ExpresionY indice;
    private Tipo tipoElemento;   // ya lo tenías como campo

    public Indice(ExpresionY arreglo, ExpresionY indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public ExpresionY getArreglo() { return arreglo; }
    public ExpresionY getIndice()  { return indice; }
    public Tipo getTipoElemento()  { return tipoElemento; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tArr = arreglo.verificar(ambito, errores);
        Tipo tIdx = indice.verificar(ambito, errores);

        if (!Tipos.esIndiceValido(tIdx))
            errores.reportar(indice.getLinea(), indice.getColumna(),
                    "El índice debe ser entero, se recibió " + tIdx.nombre());

        if (!(tArr instanceof TipoArreglo ta)) {
            if (!tArr.esDesconocido())
                errores.reportar(linea, columna,
                        "Se indexó algo que no es arreglo: " + tArr.nombre());
            tipoElemento = TipoPrimitivo.DESCONOCIDO;
            return tipoElemento;
        }

        tipoElemento = ta.getBase();

        if (arreglo instanceof Identificador idArr
                && indice instanceof Literal litIdx
                && litIdx.getCategoria() == CategoriaLiteral.ENTERO) {

            Simbolo sArr = ambito.resolver(idArr.getNombre());
            if (sArr != null && sArr.esArregloDeTamanoFijo()) {
                int idx = ((Long) litIdx.getValor()).intValue();
                int size = sArr.getTamanosArreglo().get(0);
                if (idx < 0 || idx >= size) {
                    errores.reportar(indice.getLinea(), indice.getColumna(),
                            "Índice " + idx + " fuera de rango (tamaño " + size + ")");
                }
            }
        }

        return tipoElemento;
    }
}