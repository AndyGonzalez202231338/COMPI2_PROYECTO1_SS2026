package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code ifStatement} (#ifStatementDef): "if (cond) entonces (else contrario)?".
 * A diferencia del {@code Si} de Y? (que junta todas las ramas "sino" en una lista
 * porque la gramática de Y? las repite con "*"), aquí NO hace falta: en Z un
 * "else if (...) ..." es sintácticamente solo "ELSE statement" donde ese statement
 * ES otro {@code Si} — el encadenado sale gratis anidando este mismo nodo.
 *
 * "entonces"/"contrario" son {@link InstruccionZ} (no {@link Bloque}) porque en Z las
 * llaves son opcionales para una sola sentencia ("if (x > 0) return x;" es válido).
 */
public final class Si extends NodoZ implements InstruccionZ {

    private final ExpresionZ condicion;
    private final InstruccionZ entonces;
    private final InstruccionZ contrario; // null si no hay "else"

    public Si(ExpresionZ condicion, InstruccionZ entonces, InstruccionZ contrario, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.entonces = entonces;
        this.contrario = contrario;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }
    public InstruccionZ getEntonces() {
        return entonces;
    }

    public InstruccionZ getContrario() {
        return contrario;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tc = condicion.verificar(ambito, errores);
        if (!Tipos.esBooleano(tc))
            errores.reportar(condicion.getLinea(), condicion.getColumna(),
                    "Condición del 'if' debe ser bool, se recibió " + tc.nombre());

        AmbitoBloque ambSi = new AmbitoBloque(ambito, false);
        entonces.verificar(ambSi, errores);

        if (contrario != null) {
            AmbitoBloque ambNo = new AmbitoBloque(ambito, false);
            contrario.verificar(ambNo, errores);
        }
        return TipoPrimitivo.VOID;
    }
}
