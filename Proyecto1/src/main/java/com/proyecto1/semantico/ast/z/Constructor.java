package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * Un {@code constructorDeclaration} (#constructorDeclarationDef): "public Nombre(params) bloque".
 * Que "nombre" coincida con el nombre de la propia clase es una validación semántica
 * pendiente (Parte 2), no algo que la gramática obligue.
 */
public final class Constructor extends NodoZ {

    private final String nombre;
    private final List<Parametro> parametros;
    private final Bloque cuerpo;

    public Constructor(String nombre, List<Parametro> parametros, Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = parametros;
        this.cuerpo = cuerpo;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Parametro> getParametros() {
        return parametros;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }
}
