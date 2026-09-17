package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

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

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        AmbitoBloque ambCiclo = new AmbitoBloque(ambito, true);

        if (inicializacionDeclaracion != null)
            inicializacionDeclaracion.verificar(ambCiclo, errores);
        if (inicializacionExpresiones != null)
            for (ExpresionZ e : inicializacionExpresiones) e.verificar(ambCiclo, errores);

        if (condicion != null) {
            Tipo tc = condicion.verificar(ambCiclo, errores);
            if (!Tipos.esBooleano(tc))
                errores.reportar(condicion.getLinea(), condicion.getColumna(),
                        "Condición del 'for' debe ser bool");
        }
        if (actualizacion != null)
            for (ExpresionZ e : actualizacion) e.verificar(ambCiclo, errores);

        AmbitoBloque ambCuerpo = new AmbitoBloque(ambCiclo, false);
        cuerpo.verificar(ambCuerpo, errores);
        return TipoPrimitivo.VOID;
    }
}
