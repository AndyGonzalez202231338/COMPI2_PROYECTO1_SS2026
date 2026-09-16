package com.proyecto1.semantico.ast.y;

import java.util.List;

/** Una {@code definicionFuncion} (#funcionDef): "definir nombre(params) -> tipo: cuerpo". */
public final class Funcion extends NodoY {

    private final String nombre;
    private final List<Parametro> parametros;
    private final NodoTipoRef tipoRetorno; // null == sin "-> tipo" == void
    private final Bloque cuerpo;

    public Funcion(String nombre, List<Parametro> parametros, NodoTipoRef tipoRetorno,
                    Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
        this.cuerpo = cuerpo;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Parametro> getParametros() {
        return parametros;
    }

    public NodoTipoRef getTipoRetorno() {
        return tipoRetorno;
    }

    public boolean esVoid() {
        return tipoRetorno == null;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
