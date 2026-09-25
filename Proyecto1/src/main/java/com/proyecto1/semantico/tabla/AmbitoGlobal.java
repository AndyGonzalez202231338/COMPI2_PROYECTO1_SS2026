package com.proyecto1.semantico.tabla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ámbito raíz (sin padre). Aquí viven, según el lenguaje que se esté analizando:
 *   - Y?: las ESTRUCTURA definidas en %estructuras y las FUNCION definidas en %funciones.
 *   - Zetariano: la (o las, si se linkean varios archivos) CLASE.
 */
public class AmbitoGlobal extends Ambito {

    public AmbitoGlobal() {
        super(null);
    }

    public AmbitoGlobal(Ambito padre) {
        super(padre);
    }

    /**
     * Guarda {@code simbolo} sobreescribiendo, si lo hay, otro con el mismo nombre
     * (declarar() lo rechazaría). También hay que mantener coherente {@code simbolosEnOrden}:
     * si ya existía uno con ese nombre, se reemplaza en la misma posición; si no,
     * se añade al final.
     */
    public void reemplazar(Simbolo simbolo) {
        String nombre = simbolo.getNombre();
        if (simbolos.contiene(nombre)) {
            // Reemplazo: mantener la POSICIÓN original en simbolosEnOrden.
            for (int i = 0; i < simbolosEnOrden.size(); i++) {
                if (simbolosEnOrden.get(i).getNombre().equals(nombre)) {
                    simbolosEnOrden.set(i, simbolo);
                    break;
                }
            }
        } else {
            simbolosEnOrden.add(simbolo);
        }
        simbolos.insertar(nombre, simbolo);
    }

    /**
     * Tipos definidos por el usuario (estructuras de Y y clases de Z), en orden de
     * declaración. Se usa para emitir los {@code typedef struct} del C generado
     * (Fase 4.3) — el orden importa porque cada struct puede contener punteros a
     * otros tipos, y aunque las forward decls hacen que el orden no sea estrictamente
     * necesario, mantener el orden del fuente hace el C más legible.
     */
    public List<Simbolo> getTiposDefinidos() {
        List<Simbolo> out = new ArrayList<>();
        for (Simbolo s : simbolosEnOrden) {
            CategoriaSimbolo c = s.getCategoria();
            if (c == CategoriaSimbolo.ESTRUCTURA || c == CategoriaSimbolo.CLASE) {
                out.add(s);
            }
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public String descripcion() {
        return "ámbito global";
    }
}