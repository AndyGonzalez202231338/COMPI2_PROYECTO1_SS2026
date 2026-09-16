package com.proyecto1.semantico.ast.y;

/**
 * {@code instruccionPara} (#cicloParaDef): "para(init; cond; act): cuerpo". Tanto
 * "init" como "act" pueden faltar (los "?" de la gramática), y cuando están presentes
 * "init" es una {@link DeclaracionVariable} o una {@link Asignacion}, y "act" es una
 * {@link Asignacion} o cualquier {@link ExpresionY} (p. ej. "i++") — por eso ambos se
 * guardan como {@code InstruccionY} genérico en vez de un tipo más estrecho.
 */
public final class Para extends NodoY implements InstruccionY {

    private final InstruccionY inicializacion; // DeclaracionVariable | Asignacion | null
    private final ExpresionY condicion;         // null si se omitió
    private final InstruccionY actualizacion;   // Asignacion | ExpresionStmt | null
    private final Bloque cuerpo;

    public Para(InstruccionY inicializacion, ExpresionY condicion, InstruccionY actualizacion,
                 Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
        this.cuerpo = cuerpo;
    }

    public InstruccionY getInicializacion() {
        return inicializacion;
    }

    public ExpresionY getCondicion() {
        return condicion;
    }

    public InstruccionY getActualizacion() {
        return actualizacion;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
