package com.proyecto1.semantico.tabla;

/**
 * Ámbito raíz (sin padre). Aquí viven, según el lenguaje que se esté analizando:
 *   - Y?: las ESTRUCTURA definidas en %estructuras y las FUNCION definidas en %funciones.
 *   - Zetariano: la (o las, si se linkean varios archivos) CLASE.
 */
public class AmbitoGlobal extends Ambito {

    public AmbitoGlobal() {
        super(null);
    }

    /** Para PigLatin: el global del .pig tiene como padre el global combinado de los imports. */
    public AmbitoGlobal(Ambito padre) {
        super(padre);
    }

    /** Guarda {@code simbolo} sobreescribiendo, si lo hay, otro con el mismo nombre (declarar() lo rechazaria). */
    public void reemplazar(Simbolo simbolo) {
        simbolos.insertar(simbolo.getNombre(), simbolo);
    }

    @Override
    public String descripcion() {
        return "ámbito global";
    }
}