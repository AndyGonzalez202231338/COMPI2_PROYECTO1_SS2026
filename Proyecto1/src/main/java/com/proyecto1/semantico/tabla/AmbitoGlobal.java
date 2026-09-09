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

    @Override
    public String descripcion() {
        return "ámbito global";
    }
}