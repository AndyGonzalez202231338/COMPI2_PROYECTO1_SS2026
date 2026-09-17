package com.proyecto1.ui.modelo;

import java.io.File;

/**
 * Representa el proyecto actualmente abierto en el IDE, es decir, la
 * carpeta raiz que el usuario selecciono mediante "Abrir carpeta".
 * <p>
 * Esta clase centraliza el estado global relacionado con el proyecto,
 * de forma que los distintos controladores (arbol, editor, ventana
 * principal) puedan consultar cual es la raiz activa sin depender unos
 * de otros directamente.
 * </p>
 *
 * @author Proyecto1
 */
public class ProyectoUI {

    private File carpetaRaiz;

    /**
     * Crea un proyecto vacio, sin carpeta raiz asignada todavia.
     */
    public ProyectoUI() {
        this.carpetaRaiz = null;
    }

    /**
     * Crea un proyecto a partir de una carpeta raiz.
     *
     * @param carpetaRaiz carpeta del disco que sera la raiz del proyecto
     */
    public ProyectoUI(File carpetaRaiz) {
        this.carpetaRaiz = carpetaRaiz;
    }

    public File getCarpetaRaiz() {
        return carpetaRaiz;
    }

    public void setCarpetaRaiz(File carpetaRaiz) {
        this.carpetaRaiz = carpetaRaiz;
    }

    /**
     * @return {@code true} si actualmente hay una carpeta de proyecto abierta
     */
    public boolean hayProyectoAbierto() {
        return carpetaRaiz != null && carpetaRaiz.isDirectory();
    }

    /**
     * @return el nombre de la carpeta raiz, o una cadena por defecto si no
     *         hay proyecto abierto
     */
    public String getNombreProyecto() {
        if (!hayProyectoAbierto()) {
            return "(Sin proyecto abierto)";
        }
        return carpetaRaiz.getName();
    }
}
