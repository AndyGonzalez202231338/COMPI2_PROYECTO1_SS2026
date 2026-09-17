package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * Operación unaria, prefija o postfija: {@code !, -, ++, --}. Cubre
 * #expUnariaNegacion, #expUnariaMenos, #expUnariaIncPrefijo y #expUnariaDecPrefijo
 * (prefijo = true), y la parte opcional de #expresionPostfijaDef (prefijo = false,
 * solo aplica a {@code ++}/{@code --}).
 */
public final class Unaria extends NodoPigLatin implements ExpresionPigLatin {

    private final String operador;
    private final ExpresionPigLatin operando;
    private final boolean prefijo;

    public Unaria(String operador, ExpresionPigLatin operando, boolean prefijo, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.operando = operando;
        this.prefijo = prefijo;
    }

    public String getOperador() {
        return operador;
    }

    public ExpresionPigLatin getOperando() {
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
                    errores.reportar(linea, columna, "'!' requiere bool");
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
