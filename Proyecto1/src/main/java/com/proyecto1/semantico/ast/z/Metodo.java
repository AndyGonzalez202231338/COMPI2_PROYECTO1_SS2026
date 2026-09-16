package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * Un {@code methodDeclaration} (#methodDeclarationDef): "public (tipo|void) Nombre(params) bloque".
 * A diferencia de {@code Funcion} de Y? (donde omitir "-> tipo" significa void),
 * aquí SIEMPRE hay una de las dos alternativas presente en la gramática (tipo o la
 * palabra reservada VOID); {@code tipoRetorno == null} representa justamente el caso
 * "era VOID", para que {@link #esVoid()} funcione igual que en Y?.
 */
public final class Metodo extends NodoZ {

    private final String nombre;
    private final List<Parametro> parametros;
    private final NodoTipoRef tipoRetorno; // null == era "void"
    private final Bloque cuerpo;

    public Metodo(String nombre, List<Parametro> parametros, NodoTipoRef tipoRetorno,
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
