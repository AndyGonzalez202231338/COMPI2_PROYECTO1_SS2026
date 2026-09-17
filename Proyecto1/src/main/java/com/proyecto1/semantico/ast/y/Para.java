package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

public final class Para extends NodoY implements InstruccionY {

    private final InstruccionY inicializacion;
    private final ExpresionY condicion;
    private final InstruccionY actualizacion;
    private final Bloque cuerpo;

    public Para(InstruccionY inicializacion, ExpresionY condicion, InstruccionY actualizacion,
                Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
        this.cuerpo = cuerpo;
    }

    public InstruccionY getInicializacion() { return inicializacion; }
    public ExpresionY getCondicion() { return condicion; }
    public InstruccionY getActualizacion() { return actualizacion; }
    public Bloque getCuerpo() { return cuerpo; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // El "para" introduce su propio ámbito (la variable de init vive solo ahí)
        AmbitoBloque ambCiclo = new AmbitoBloque(ambito, true); // esCiclo = true

        if (inicializacion != null) inicializacion.verificar(ambCiclo, errores);

        if (condicion != null) {
            Tipo tc = condicion.verificar(ambCiclo, errores);
            if (!Tipos.esBooleano(tc))
                errores.reportar(condicion.getLinea(), condicion.getColumna(),
                        "La condición del 'para' debe ser bool, se recibió " + tc.nombre());
        }

        if (actualizacion != null) actualizacion.verificar(ambCiclo, errores);

        // El cuerpo tiene su propio sub-ámbito (hijo del de ciclo)
        AmbitoBloque ambCuerpo = new AmbitoBloque(ambCiclo, false);
        cuerpo.verificar(ambCuerpo, errores);

        return TipoPrimitivo.VOID;
    }
}