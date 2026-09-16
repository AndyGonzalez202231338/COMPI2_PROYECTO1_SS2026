package com.proyecto1.semantico.ast.z;

/** Un identificador usado como expresión (#primarioIdentificador): variable/atributo, o base de "obj.campo" / "arr[i]" / "f(...)". */
public final class Identificador extends NodoZ implements ExpresionZ {

    private final String nombre;

    public Identificador(String nombre, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
