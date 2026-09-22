package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/** {@code primaria CORIZQ expresion CORDER} (#primariaIndice): "arreglo[indice]". */
public final class Indice extends NodoY implements ExpresionY {

    private final ExpresionY arreglo;
    private final ExpresionY indice;

    /** Tipo del elemento (ta.getBase()), cacheado por verificar(). */
    private Tipo tipoElemento;

    public Indice(ExpresionY arreglo, ExpresionY indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice  = indice;
    }

    public ExpresionY getArreglo() { return arreglo; }
    public ExpresionY getIndice()  { return indice;  }
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
            return TipoPrimitivo.DESCONOCIDO;
        }
        tipoElemento = ta.getBase();
        return tipoElemento;
    }

    /**
     * Emite, en este orden: C3D del arreglo (nombre de variable o temporal con la base),
     * C3D del índice, y una única cuádrupla {@code (=[], base, idx, t)}. Devuelve
     * {@code temporal(t, tipoElemento)}.
     *
     * La multiplicación por tamaño de elemento y la suma a la base las hará Fase 4,
     * que es la que conoce sizeof. Aquí el índice se pasa como entero crudo.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        ResultadoC3D base = arreglo.generarC3D(generador);
        ResultadoC3D idx  = indice.generarC3D(generador);
        String t = generador.nuevoTemporal();
        generador.emitirCargaIndice(base.getLugar(), idx.getLugar(), t);
        Tipo tipo = (tipoElemento != null) ? tipoElemento : TipoPrimitivo.DESCONOCIDO;
        return ResultadoC3D.temporal(t, tipo);
    }
}