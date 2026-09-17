package com.proyecto1.semantico.ast.piglatin;

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
}
