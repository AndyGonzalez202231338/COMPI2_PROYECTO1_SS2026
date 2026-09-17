package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * Cualquier operación binaria: {@code ||, &&, ==, !=, <, >, <=, >=, +, -, *, /, %}.
 * Cubre las etiquetas #expresionOrDef, #expresionAndDef, #expresionIgualdadDef,
 * #expresionRelacionalDef, #expresionAditivaDef y #expresionMultiplicativaDef (cuando
 * traen operador) — los seis niveles de precedencia de la gramática se colapsan en
 * esta única clase porque la precedencia ya quedó resuelta por la FORMA del árbol que
 * entrega ANTLR; una vez hecho ese trabajo, guardar "de qué nivel de precedencia
 * venía" no aporta nada.
 */
public final class Binaria extends NodoPigLatin implements ExpresionPigLatin {

    private final String operador; // texto exacto del operador: "||", "&&", "==", "+", ...
    private final ExpresionPigLatin izquierdo;
    private final ExpresionPigLatin derecho;

    public Binaria(String operador, ExpresionPigLatin izquierdo, ExpresionPigLatin derecho,
                    int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionPigLatin getIzquierdo() {
        return izquierdo;
    }

    public ExpresionPigLatin getDerecho() {
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
