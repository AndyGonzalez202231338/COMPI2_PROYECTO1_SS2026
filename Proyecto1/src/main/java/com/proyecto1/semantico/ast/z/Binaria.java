package com.proyecto1.semantico.ast.z;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * Cualquier operación binaria: ||, &&, ==, !=, <, >, <=, >=, +, -, *, /, %. Igual que
 * en Y?, los niveles de precedencia se colapsan en esta única clase (#logicalOrExpressionDef,
 * #logicalAndExpressionDef, #equalityExpressionDef, #relationalExpressionDef,
 * #additiveExpressionDef, #multiplicativeExpressionDef): la precedencia ya quedó
 * resuelta por la FORMA del árbol que entrega ANTLR.
 */
public final class Binaria extends NodoZ implements ExpresionZ {

    private final String operador;
    private final ExpresionZ izquierdo;
    private final ExpresionZ derecho;

    public Binaria(String operador, ExpresionZ izquierdo, ExpresionZ derecho, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionZ getIzquierdo() {
        return izquierdo;
    }

    public ExpresionZ getDerecho() {
        return derecho;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo ti = izquierdo.verificar(ambito, errores);
        Tipo td = derecho.verificar(ambito, errores);

        switch (operador) {
            case "+": case "-": case "*": case "/": case "%":
                Tipo r = Tipos.resultadoAritmetico(ti, td, operador.equals("+"));
                if (r == null) {
                    errores.reportar(linea, columna,
                            "Operación '" + operador + "' no válida entre " + ti.nombre() + " y " + td.nombre());
                    return TipoPrimitivo.DESCONOCIDO;
                }
                return r;
            case "==": case "!=":
                if (!Tipos.esComparableIgualdad(ti, td))
                    errores.reportar(linea, columna, "Comparación '" + operador + "' no válida");
                return TipoPrimitivo.BOOL;
            case "<": case ">": case "<=": case ">=":
                if (!Tipos.esComparableOrden(ti, td))
                    errores.reportar(linea, columna, "Comparación de orden no válida");
                return TipoPrimitivo.BOOL;
            case "&&": case "||":
                if (!Tipos.esBooleano(ti) || !Tipos.esBooleano(td))
                    errores.reportar(linea, columna, "Operador lógico requiere bool");
                return TipoPrimitivo.BOOL;
        }
        return TipoPrimitivo.DESCONOCIDO;
    }
}
