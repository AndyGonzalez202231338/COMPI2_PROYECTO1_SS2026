package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/** {@code primaria [ expresion ]} (#primariaIndice): "arreglo[indice]". */
public final class Indice extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin arreglo;
    private final ExpresionPigLatin indice;

    public Indice(ExpresionPigLatin arreglo, ExpresionPigLatin indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public ExpresionPigLatin getArreglo() {
        return arreglo;
    }

    public ExpresionPigLatin getIndice() {
        return indice;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tArr = arreglo.verificar(ambito, errores);
        Tipo tIdx = indice.verificar(ambito, errores);

        if (!Tipos.esIndiceValido(tIdx))
            errores.reportar(indice.getLinea(), indice.getColumna(),
                    "El índice debe ser entero");

        if (!(tArr instanceof TipoArreglo ta)) {
            if (!tArr.esDesconocido())
                errores.reportar(linea, columna, "Se indexó algo que no es arreglo");
            return TipoPrimitivo.DESCONOCIDO;
        }
        return ta.getBase();
    }
}
