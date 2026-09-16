package com.proyecto1.semantico.ast.y;

/** Un identificador usado como expresión (#primariaIdentificador): variable, o base de "obj.campo" / "arr[i]" / "f(...)". */
public final class Identificador extends NodoY implements ExpresionY {

    private final String nombre;

    public Identificador(String nombre, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
