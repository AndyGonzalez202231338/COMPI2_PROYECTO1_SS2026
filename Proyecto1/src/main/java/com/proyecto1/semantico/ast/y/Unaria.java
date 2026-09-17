package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * Operación unaria, prefija o postfija: !, - (negación aritmética), ++, --. Cubre
 * #expUnariaPrefijaDef (prefijo=true) y la parte opcional de #expPostfijaDef
 * (prefijo=false, solo aplica a ++/--).
 */
public final class Unaria extends NodoY implements ExpresionY {

    private final String operador;
    private final ExpresionY operando;
    private final boolean prefijo;

    public Unaria(String operador, ExpresionY operando, boolean prefijo, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.operando = operando;
        this.prefijo = prefijo;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionY getOperando() {
        return operando;
    }

    public boolean isPrefijo() {
        return prefijo;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo t = operando.verificar(ambito, errores);
        switch (operador) {
            case "!":
                if (!Tipos.esBooleano(t))
                    errores.reportar(linea, columna, "'!' requiere bool, se recibió " + t.nombre());
                return TipoPrimitivo.BOOL;
            case "-":
                if (!t.esNumerico() && !t.esDesconocido())
                    errores.reportar(linea, columna, "'-' requiere numérico, se recibió " + t.nombre());
                return t;
            case "++": case "--":
                if (!Tipos.admiteIncrementoDecremento(t))
                    errores.reportar(linea, columna, "'" + operador + "' requiere numérico, se recibió " + t.nombre());
                return t;
        }
        return TipoPrimitivo.DESCONOCIDO;
    }
}
