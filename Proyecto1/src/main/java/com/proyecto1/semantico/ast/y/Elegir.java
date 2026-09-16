package com.proyecto1.semantico.ast.y;

import java.util.List;

/** {@code instruccionElegir} (#condicionElegirDef): "elegir(control): caso* siempre?". */
public final class Elegir extends NodoY implements InstruccionY {

    private final ExpresionY control;
    private final List<CasoElegir> casos;
    private final Bloque siempre; // null si no hay "siempre"

    public Elegir(ExpresionY control, List<CasoElegir> casos, Bloque siempre, int linea, int columna) {
        super(linea, columna);
        this.control = control;
        this.casos = casos;
        this.siempre = siempre;
    }

    public ExpresionY getControl() {
        return control;
    }

    public List<CasoElegir> getCasos() {
        return casos;
    }

    public Bloque getSiempre() {
        return siempre;
    }
}
