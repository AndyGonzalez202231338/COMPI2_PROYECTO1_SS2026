package com.proyecto1.semantico.ast.z;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * Operación unaria, prefija o postfija: !, - (negación aritmética), ++, --. Cubre
 * #unaryNegacionDef, #unaryMenosDef, #unaryIncrementoPrefijoDef, #unaryDecrementoPrefijoDef
 * (prefijo=true) y la parte opcional de #postfixExpressionDef (prefijo=false).
 */
public final class Unaria extends NodoZ implements ExpresionZ {

    private final String operador;
    private final ExpresionZ operando;
    private final boolean prefijo;

    public Unaria(String operador, ExpresionZ operando, boolean prefijo, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.operando = operando;
        this.prefijo = prefijo;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionZ getOperando() {
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
                    errores.reportar(linea, columna, "'-' requiere numérico");
                return t;
            case "++": case "--":
                if (!Tipos.admiteIncrementoDecremento(t))
                    errores.reportar(linea, columna, "'" + operador + "' requiere numérico");
                return t;
        }
        return TipoPrimitivo.DESCONOCIDO;
    }
}
