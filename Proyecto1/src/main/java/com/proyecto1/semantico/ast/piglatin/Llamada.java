package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.*;

import java.util.List;

/**
 * {@code primaria ( listaArgumentos? )} (#primariaLlamada): llamada a función o
 * método. {@code objetivo} es normalmente un {@link Identificador} (llamada simple,
 * {@code funcion()}) o un {@link AccesoCampo} (llamada encadenada,
 * {@code obj.metodo()}), reutilizando el encadenado que ya arma {@code primaria} de
 * forma recursiva a la izquierda.
 */
public final class Llamada extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objetivo;
    private final List<ExpresionPigLatin> argumentos;

    public Llamada(ExpresionPigLatin objetivo, List<ExpresionPigLatin> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionPigLatin getObjetivo() {
        return objetivo;
    }

    public List<ExpresionPigLatin> getArgumentos() {
        return argumentos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // Caso 1: función importada de .y -> "funcion(args)"
        if (objetivo instanceof Identificador id) {
            Simbolo f = ambito.resolver(id.getNombre());
            if (f == null || f.getCategoria() != CategoriaSimbolo.FUNCION) {
                errores.reportar(linea, columna, "Función no declarada: '" + id.getNombre() + "'");
                return TipoPrimitivo.DESCONOCIDO;
            }
            return verificarArgsYRetorno(f, ambito, errores);
        }
        // Caso 2: método de objeto importado de .z -> "obj.metodo(args)"
        if (objetivo instanceof AccesoCampo ac) {
            Tipo tObj = ac.getObjeto().verificar(ambito, errores);
            Simbolo def;
            if (tObj instanceof TipoClase tc) def = tc.getDefinicion();
            else if (tObj instanceof TipoEstructura te) def = te.getDefinicion();
            else {
                if (!tObj.esDesconocido())
                    errores.reportar(linea, columna, "No se puede llamar método sobre " + tObj.nombre());
                return TipoPrimitivo.DESCONOCIDO;
            }
            Simbolo m = def.buscarMiembro(ac.getCampo());
            if (m == null || (m.getCategoria() != CategoriaSimbolo.METODO
                    && m.getCategoria() != CategoriaSimbolo.FUNCION)) {
                errores.reportar(linea, columna,
                        "'" + def.getNombre() + "' no tiene método '" + ac.getCampo() + "'");
                return TipoPrimitivo.DESCONOCIDO;
            }
            return verificarArgsYRetorno(m, ambito, errores);
        }
        errores.reportar(linea, columna, "Llamada inválida");
        return TipoPrimitivo.DESCONOCIDO;
    }

    private Tipo verificarArgsYRetorno(Simbolo f, Ambito ambito, ManejadorErrores errores) {
        List<Simbolo> params = f.getParametros();
        if (params.size() != argumentos.size()) {
            errores.reportar(linea, columna,
                    "'" + f.getNombre() + "' espera " + params.size() +
                            " argumentos, recibió " + argumentos.size());
            return f.getTipo();
        }
        for (int i = 0; i < argumentos.size(); i++) {
            Tipo ta = argumentos.get(i).verificar(ambito, errores);
            if (!Tipos.esAsignable(params.get(i).getTipo(), ta))
                errores.reportar(argumentos.get(i).getLinea(), argumentos.get(i).getColumna(),
                        "Argumento " + (i+1) + " incompatible");
        }
        return f.getTipo();
    }
}
