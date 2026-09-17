package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

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

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tControl = control.verificar(ambito, errores);

        // El control debe ser entero/caracter/cadena según (caso literal)
        // Validamos que cada caso sea compatible
        for (CasoElegir c : casos) {
            Tipo tCaso = c.getValor().verificar(ambito, errores);
            if (!Tipos.esComparableIgualdad(tControl, tCaso))
                errores.reportar(c.getValor().getLinea(), c.getValor().getColumna(),
                        "Caso incompatible con el control: " + tCaso.nombre() + " vs " + tControl.nombre());

            AmbitoBloque amb = new AmbitoBloque(ambito, false);
            c.getCuerpo().verificar(amb, errores);
        }

        if (siempre != null) {
            AmbitoBloque amb = new AmbitoBloque(ambito, false);
            siempre.verificar(amb, errores);
        }
        return TipoPrimitivo.VOID;
    }
}
