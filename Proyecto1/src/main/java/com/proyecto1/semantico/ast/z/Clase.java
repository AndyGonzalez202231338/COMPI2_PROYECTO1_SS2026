package com.proyecto1.semantico.ast.z;

import java.util.List;

/**
 * Nodo raíz del AST de Zetariano: {@code compilationUnit} (#compilationUnitDef),
 * "public class Nombre { ... }". Se llama "Clase" y no "Programa" (a diferencia de Y?)
 * porque un archivo .z define exactamente UNA clase, no un programa completo — útil
 * cuando Pig Latin más adelante importe varias clases .z distintas.
 */
public final class Clase extends NodoZ {

    private final String nombre;
    private final List<Atributo> atributos;
    private final List<Constructor> constructores;
    private final List<Metodo> metodos;

    public Clase(String nombre, List<Atributo> atributos, List<Constructor> constructores,
                 List<Metodo> metodos, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.atributos = atributos;
        this.constructores = constructores;
        this.metodos = metodos;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Atributo> getAtributos() {
        return atributos;
    }

    public List<Constructor> getConstructores() {
        return constructores;
    }

    public List<Metodo> getMetodos() {
        return metodos;
    }
}
