package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * {@code forStatement} (#forStatementDef): "for(init? ; cond? ; act?) cuerpo".
 * A diferencia del {@code Para} de Y? (cuyo "init"/"act" son SIEMPRE una sola
 * instrucción), en Z el "init" puede ser una declaración única O una lista de
 * expresiones separadas por coma (#forInitDeclaracion / #forInitExpresiones), y el
 * "act" siempre es una lista de expresiones (#forUpdateDef) — por eso aquí van dos
 * campos separados para el init (exactamente uno de los dos no-nulo cuando hay
 * inicialización) en vez de un único {@code InstruccionZ} genérico como en Y?.
 */
public final class Para extends NodoZ implements InstruccionZ {

    private final DeclaracionVariable inicializacionDeclaracion; // no-null si el init fue una declaración
    private final List<ExpresionZ> inicializacionExpresiones;    // no-null si el init fue una lista de expresiones
    private final ExpresionZ condicion;                          // null si se omitió
    private final List<ExpresionZ> actualizacion;                // null si se omitió
    private final InstruccionZ cuerpo;

    public Para(DeclaracionVariable inicializacionDeclaracion, List<ExpresionZ> inicializacionExpresiones,
                ExpresionZ condicion, List<ExpresionZ> actualizacion, InstruccionZ cuerpo,
                int linea, int columna) {
        super(linea, columna);
        this.inicializacionDeclaracion = inicializacionDeclaracion;
        this.inicializacionExpresiones = inicializacionExpresiones;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
        this.cuerpo = cuerpo;
    }

    public DeclaracionVariable getInicializacionDeclaracion() {
        return inicializacionDeclaracion;
    }

    public List<ExpresionZ> getInicializacionExpresiones() {
        return inicializacionExpresiones;
    }

    public ExpresionZ getCondicion() {
        return condicion;
    }

    public List<ExpresionZ> getActualizacion() {
        return actualizacion;
    }

    public InstruccionZ getCuerpo() {
        return cuerpo;
    }
}
