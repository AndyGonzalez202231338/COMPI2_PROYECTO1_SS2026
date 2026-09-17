package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.AmbitoClase;
import com.proyecto1.semantico.tabla.AmbitoFuncion;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;

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

    public void verificar(AmbitoClase ambClase, ManejadorErrores errores) {
        Simbolo simbolo = ambClase.getSimboloContenedor().buscarMiembro(nombre);
        AmbitoFuncion amb = new AmbitoFuncion(ambClase, simbolo);

        for (Parametro p : parametros) {
            Tipo t = p.resolverTipo(amb, errores);
            Simbolo sp = new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO, t, p.getLinea(), p.getColumna());
            if (!amb.declarar(sp))
                errores.reportar(p.getLinea(), p.getColumna(), "Parámetro duplicado: '" + p.getNombre() + "'");
            simbolo.agregarParametro(sp);
        }

        cuerpo.verificar(amb, errores);

        if (!esVoid() && !amb.isTuvoRetorno())
            errores.reportar(linea, columna,
                    "El método '" + nombre + "' debe retornar un valor de tipo " + amb.getTipoRetorno().nombre());
    }
}
