package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.List;

/** {@code switchStatement} (#switchStatementDef): "switch(control) { caso* default? }". */
public final class Elegir extends NodoZ implements InstruccionZ {

    private final ExpresionZ control;
    private final List<CasoElegir> casos;
    private final CasoDefecto porDefecto; // null si no hay "default"

    public Elegir(ExpresionZ control, List<CasoElegir> casos, CasoDefecto porDefecto, int linea, int columna) {
        super(linea, columna);
        this.control = control;
        this.casos = casos;
        this.porDefecto = porDefecto;
    }

    public ExpresionZ getControl() {
        return control;
    }

    public List<CasoElegir> getCasos() {
        return casos;
    }
    public CasoDefecto getPorDefecto() {
        return porDefecto;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tControl = control.verificar(ambito, errores);

        for (CasoElegir c : casos) {
            Tipo tCaso = c.getValor().verificar(ambito, errores);
            if (!Tipos.esComparableIgualdad(tControl, tCaso))
                errores.reportar(c.getValor().getLinea(), c.getValor().getColumna(),
                        "Caso incompatible con el control: " + tCaso.nombre() + " vs " + tControl.nombre());

            AmbitoBloque ambCaso = new AmbitoBloque(ambito, true); // permite break
            for (InstruccionZ i : c.getInstrucciones()) i.verificar(ambCaso, errores);
        }

        if (porDefecto != null) {
            AmbitoBloque ambDef = new AmbitoBloque(ambito, true);
            for (InstruccionZ i : porDefecto.getInstrucciones()) i.verificar(ambDef, errores);
        }
        return TipoPrimitivo.VOID;
    }
}
