package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.AmbitoClase;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * Un {@code fieldDeclaration} (#fieldDeclarationDef): "tipo ID (= expresion)? ;".
 * A diferencia de {@code CampoEstructura} de Y?, no necesita una lista de tamaños de
 * arreglo aparte: en Z el arreglo ya viene incluido en {@code tipo} (ver
 * {@link NodoTipoRef#getDimensiones()}).
 */
public final class Atributo extends NodoZ {

    private final NodoTipoRef tipo;
    private final String nombre;
    private final ExpresionZ inicializador; // null si no hay "= expresion"

    public Atributo(NodoTipoRef tipo, String nombre, ExpresionZ inicializador, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.inicializador = inicializador;
    }

    public NodoTipoRef getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public ExpresionZ getInicializador() {
        return inicializador;
    }

    public void verificar(AmbitoClase amb, ManejadorErrores errores) {
        Tipo t = tipo.resolver(amb, errores);
        Simbolo s = new Simbolo(nombre, CategoriaSimbolo.ATRIBUTO, t, linea, columna);
        if (!amb.declararMiembro(s))
            errores.reportar(linea, columna, "Atributo duplicado: '" + nombre + "'");

        if (inicializador != null) {
            Tipo tInit = inicializador.verificar(amb, errores);
            if (!Tipos.esAsignable(t, tInit))
                errores.reportar(linea, columna,
                        "Inicialización incompatible: " + tInit.nombre() + " → " + t.nombre());
        }
    }
}
