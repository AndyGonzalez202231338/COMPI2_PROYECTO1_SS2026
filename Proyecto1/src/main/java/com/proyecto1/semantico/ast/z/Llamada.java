package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.List;

/**
 * {@code primaryExpression LPAREN argumentList? RPAREN} (#primarioLlamada): llamada a
 * método/función. Cubre tanto "metodo(args)" sueltas (objetivo = {@link Identificador})
 * como encadenadas "obj.metodo(args)" (objetivo = {@link AccesoCampo}) — misma unificación
 * que en Y?.
 */
public final class Llamada extends NodoZ implements ExpresionZ {

    private final ExpresionZ objetivo;
    private final List<ExpresionZ> argumentos;

    public Llamada(ExpresionZ objetivo, List<ExpresionZ> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionZ getObjetivo() {
        return objetivo;
    }

    public List<ExpresionZ> getArgumentos() {
        return argumentos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // Caso 1: llamada directa a método de la propia clase -> "metodo(args)"
        if (objetivo instanceof Identificador id) {
            Simbolo m = ambito.resolver(id.getNombre());
            if (m == null || (m.getCategoria() != CategoriaSimbolo.METODO
                    && m.getCategoria() != CategoriaSimbolo.CONSTRUCTOR)) {
                errores.reportar(linea, columna, "Método no declarado: '" + id.getNombre() + "'");
                return TipoPrimitivo.DESCONOCIDO;
            }
            return verificarArgumentosYRetorno(m, ambito, errores);
        }
        // Caso 2: método de otro objeto -> "obj.metodo(args)"
        if (objetivo instanceof AccesoCampo ac) {
            Tipo tObj = ac.getObjeto().verificar(ambito, errores);
            if (!(tObj instanceof TipoClase tc)) {
                if (!tObj.esDesconocido())
                    errores.reportar(linea, columna, "No se puede llamar método sobre " + tObj.nombre());
                return TipoPrimitivo.DESCONOCIDO;
            }
            Simbolo m = tc.getDefinicion().buscarMiembro(ac.getCampo());
            if (m == null || m.getCategoria() != CategoriaSimbolo.METODO) {
                errores.reportar(linea, columna,
                        "La clase '" + tc.nombre() + "' no tiene método '" + ac.getCampo() + "'");
                return TipoPrimitivo.DESCONOCIDO;
            }
            return verificarArgumentosYRetorno(m, ambito, errores);
        }
        errores.reportar(linea, columna, "Llamada inválida");
        return TipoPrimitivo.DESCONOCIDO;
    }

    private Tipo verificarArgumentosYRetorno(Simbolo m, Ambito ambito, ManejadorErrores errores) {
        List<Simbolo> params = m.getParametros();
        if (params.size() != argumentos.size()) {
            errores.reportar(linea, columna,
                    "Método '" + m.getNombre() + "' espera " + params.size() +
                            " argumentos, recibió " + argumentos.size());
            return m.getTipo();
        }
        for (int i = 0; i < argumentos.size(); i++) {
            Tipo ta = argumentos.get(i).verificar(ambito, errores);
            if (!Tipos.esAsignable(params.get(i).getTipo(), ta))
                errores.reportar(argumentos.get(i).getLinea(), argumentos.get(i).getColumna(),
                        "Argumento " + (i+1) + " incompatible: se esperaba " +
                                params.get(i).getTipo().nombre() + ", se recibió " + ta.nombre());
        }
        return m.getTipo();
    }
}
