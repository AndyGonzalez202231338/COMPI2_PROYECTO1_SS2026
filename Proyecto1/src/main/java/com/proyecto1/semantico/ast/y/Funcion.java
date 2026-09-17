package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoFuncion;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

public final class Funcion extends NodoY {

    private final String nombre;
    private final List<Parametro> parametros;
    private final NodoTipoRef tipoRetorno;
    private final Bloque cuerpo;

    public Funcion(String nombre, List<Parametro> parametros, NodoTipoRef tipoRetorno,
                   Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
        this.cuerpo = cuerpo;
    }

    public String getNombre() { return nombre; }
    public List<Parametro> getParametros() { return parametros; }
    public NodoTipoRef getTipoRetorno() { return tipoRetorno; }
    public boolean esVoid() { return tipoRetorno == null; }
    public Bloque getCuerpo() { return cuerpo; }

    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Simbolo simbolo = ambito.resolverLocal(nombre);
        AmbitoFuncion amb = new AmbitoFuncion(ambito, simbolo);

        for (Parametro p : parametros) {
            Tipo t = p.resolverTipo(amb, errores);
            Simbolo sp = new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO,
                    t, p.getLinea(), p.getColumna());
            if (!amb.declarar(sp)) {
                errores.reportar(p.getLinea(), p.getColumna(),
                        "Parámetro duplicado: '" + p.getNombre() + "'");
            }
            simbolo.agregarParametro(sp);
        }

        cuerpo.verificar(amb, errores);

        if (!amb.esVoid() && !amb.isTuvoRetorno()) {
            errores.reportar(linea, columna,
                    "La función '" + nombre + "' debe retornar un valor de tipo "
                            + amb.getTipoRetorno().nombre());
        }
        return TipoPrimitivo.VOID;
    }
}