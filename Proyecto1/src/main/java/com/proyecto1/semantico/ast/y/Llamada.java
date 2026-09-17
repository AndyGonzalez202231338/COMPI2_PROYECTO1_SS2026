package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.List;

/** {@code primaria LPAREN argumentos? RPAREN} (#primariaLlamada): llamada a función. */
public final class Llamada extends NodoY implements ExpresionY {

    private final ExpresionY objetivo; // normalmente un Identificador con el nombre de la función
    private final List<ExpresionY> argumentos;

    public Llamada(ExpresionY objetivo, List<ExpresionY> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionY getObjetivo() {
        return objetivo;
    }

    public List<ExpresionY> getArgumentos() {
        return argumentos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        if (!(objetivo instanceof Identificador id)) {
            errores.reportar(linea, columna, "Llamada inválida");
            return TipoPrimitivo.DESCONOCIDO;
        }
        Simbolo f = ambito.resolver(id.getNombre());
        if (f == null || (f.getCategoria() != CategoriaSimbolo.FUNCION
                && f.getCategoria() != CategoriaSimbolo.METODO)) {
            errores.reportar(linea, columna, "Función no declarada: '" + id.getNombre() + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }
        List<Simbolo> params = f.getParametros();
        if (params.size() != argumentos.size()) {
            errores.reportar(linea, columna,
                    "Función '" + f.getNombre() + "' espera " + params.size() +
                            " argumentos, recibió " + argumentos.size());
            return f.getTipo(); // sigue devolviendo el tipo de retorno
        }
        for (int i = 0; i < argumentos.size(); i++) {
            Tipo ta = argumentos.get(i).verificar(ambito, errores);
            if (!Tipos.esAsignable(params.get(i).getTipo(), ta))
                errores.reportar(argumentos.get(i).getLinea(), argumentos.get(i).getColumna(),
                        "Argumento " + (i+1) + " incompatible: se esperaba " +
                                params.get(i).getTipo().nombre() + ", se recibió " + ta.nombre());
        }
        return f.getTipo();
    }
}
