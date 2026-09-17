package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.AmbitoClase;
import com.proyecto1.semantico.tabla.AmbitoFuncion;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;

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

    public void verificar(AmbitoClase ambClase, ManejadorErrores errores) {
        // El nombre del constructor debe coincidir con el de la clase
        if (!nombre.equals(ambClase.getSimboloContenedor().getNombre()))
            errores.reportar(linea, columna,
                    "El constructor debe llamarse igual que la clase '" +
                            ambClase.getSimboloContenedor().getNombre() + "'");

        Simbolo simbolo = ambClase.getSimboloContenedor().buscarMiembro(nombre + "@" + parametros.size());
        // Si no hay símbolo registrado con clave única, no lo uses; ver nota abajo.

        AmbitoFuncion amb = new AmbitoFuncion(ambClase, simbolo);

        for (Parametro p : parametros) {
            Tipo t = p.resolverTipo(amb, errores);
            Simbolo sp = new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO, t, p.getLinea(), p.getColumna());
            if (!amb.declarar(sp))
                errores.reportar(p.getLinea(), p.getColumna(), "Parámetro duplicado: '" + p.getNombre() + "'");
        }
        cuerpo.verificar(amb, errores);
    }
}
