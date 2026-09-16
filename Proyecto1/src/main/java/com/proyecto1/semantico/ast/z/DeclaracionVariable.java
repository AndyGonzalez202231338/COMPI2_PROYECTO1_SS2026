package com.proyecto1.semantico.ast.z;

/**
 * La regla compartida {@code declaracion} (#declaracionDef): "tipo ID (= expresion)?".
 * Se usa tanto como instrucción suelta (#declarationStatement -> #stmtDeclaracion)
 * como dentro del "init" de un {@code for} (#forInitDeclaracion, ver {@link Para}).
 */
public final class DeclaracionVariable extends NodoZ implements InstruccionZ {

    private final NodoTipoRef tipo;
    private final String nombre;
    private final ExpresionZ inicializador; // null si no hay "= expresion"

    public DeclaracionVariable(NodoTipoRef tipo, String nombre, ExpresionZ inicializador, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.inicializador = inicializador;
    }

    public NodoTipoRef getTipo() {
        return tipo;
    }
    public String getNombre() {
        return nombre;
    }

    public ExpresionZ getInicializador() {
        return inicializador;
    }
}
