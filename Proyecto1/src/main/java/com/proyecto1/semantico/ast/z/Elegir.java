package com.proyecto1.semantico.ast.z;

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
}
