package com.proyecto1.semantico;

import com.proyecto1.semantico.ast.z.Atributo;
import com.proyecto1.semantico.ast.z.Clase;
import com.proyecto1.semantico.ast.z.Constructor;
import com.proyecto1.semantico.ast.z.Metodo;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.AmbitoClase;
import com.proyecto1.semantico.tabla.AmbitoGlobal;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

public class AnalizadorSemanticoZ {

    public ManejadorErrores analizar(Clase clase) {
        ManejadorErrores errores = new ManejadorErrores();
        AmbitoGlobal global = new AmbitoGlobal();

        // ---- PRIMERA PASADA: registrar la clase ----
        Simbolo sClase = new Simbolo(clase.getNombre(), CategoriaSimbolo.CLASE,
                null, clase.getLinea(), clase.getColumna());
        if (!global.declarar(sClase)) {
            errores.reportar(clase.getLinea(), clase.getColumna(),
                    "Clase duplicada: '" + clase.getNombre() + "'");
            errores.imprimir();
            return errores;
        }
        AmbitoClase ambClase = new AmbitoClase(global, sClase);

        // Registrar atributos (sin verificar inicializadores todavía)
        for (Atributo a : clase.getAtributos()) {
            Simbolo sa = new Simbolo(a.getNombre(), CategoriaSimbolo.ATRIBUTO,
                    a.getTipo().resolver(global, errores),
                    a.getLinea(), a.getColumna());
            ambClase.declararMiembro(sa);
        }

        // Registrar métodos (clave: nombre#aridad)
        for (Metodo m : clase.getMetodos()) {
            Tipo tRet = m.esVoid() ? TipoPrimitivo.VOID : m.getTipoRetorno().resolver(global, errores);
            Simbolo sm = new Simbolo(m.getNombre(), CategoriaSimbolo.METODO,
                    tRet, m.getLinea(), m.getColumna());
            ambClase.declararMiembro(sm);
        }

        // Registrar constructores (clave: nombre#aridad)
        for (Constructor c : clase.getConstructores()) {
            Simbolo sc = new Simbolo(c.getNombre(), CategoriaSimbolo.CONSTRUCTOR,
                    null, c.getLinea(), c.getColumna());
            ambClase.declararMiembro(sc);
        }

        // ---- SEGUNDA PASADA: verificar cuerpos ----
        for (Atributo a : clase.getAtributos()) a.verificar(ambClase, errores);
        for (Metodo m : clase.getMetodos())       m.verificar(ambClase, errores);
        for (Constructor c : clase.getConstructores()) c.verificar(ambClase, errores);

        errores.imprimir();
        return errores;
    }
}