package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/** {@code primaryExpression CORIZQ expression CORDER} (#primarioIndice): "arreglo[indice]". */
public final class Indice extends NodoZ implements ExpresionZ {

    private final ExpresionZ arreglo;
    private final ExpresionZ indice;

    public Indice(ExpresionZ arreglo, ExpresionZ indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public ExpresionZ getArreglo() {
        return arreglo;
    }

    public ExpresionZ getIndice() {
        return indice;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tArr = arreglo.verificar(ambito, errores);
        Tipo tIdx = indice.verificar(ambito, errores);

        if (!Tipos.esIndiceValido(tIdx))
            errores.reportar(indice.getLinea(), indice.getColumna(),
                    "El índice debe ser entero, se recibió " + tIdx.nombre());

        if (!(tArr instanceof TipoArreglo ta)) {
            if (!tArr.esDesconocido())
                errores.reportar(linea, columna, "Se indexó algo que no es arreglo: " + tArr.nombre());
            return TipoPrimitivo.DESCONOCIDO;
        }

        if (arreglo instanceof Identificador idArr) {
            Simbolo sArr = ambito.resolver(idArr.getNombre());
            if (sArr != null && sArr.esArregloDeTamanoFijo()
                    && indice instanceof Literal litIdx
                    && litIdx.getCategoria() == CategoriaLiteral.ENTERO) {
                int idx = ((Long) litIdx.getValor()).intValue();
                int size = sArr.getTamanosArreglo().get(0);
                if (idx < 0 || idx >= size) {
                    errores.reportar(indice.getLinea(), indice.getColumna(),
                            "Índice " + idx + " fuera de rango (tamaño " + size + ")");
                }
            }
        }

        return ta.getBase();
    }
}
