package com.proyecto1.ui.modelo;

import java.io.File;

/**
 * Representa una carpeta dentro del arbol de trabajo del IDE.
 * <p>
 * Es un modelo liviano: no mantiene una lista propia de hijos en memoria,
 * ya que el {@code TreeView} de JavaFX construye sus nodos consultando el
 * disco en el momento de expandir cada carpeta (ver {@code ArbolController}).
 * Esto evita que el arbol en memoria y el disco real se desincronicen.
 * </p>
 *
 * @author Proyecto1
 */
public class CarpetaUI {

    private File carpeta;

    /**
     * Crea un modelo de carpeta a partir de una carpeta real en disco.
     *
     * @param carpeta carpeta del sistema de ficheros que este nodo representa
     */
    public CarpetaUI(File carpeta) {
        this.carpeta = carpeta;
    }

    public File getCarpeta() {
        return carpeta;
    }

    public void setCarpeta(File carpeta) {
        this.carpeta = carpeta;
    }

    /**
     * @return el nombre simple de la carpeta (sin la ruta completa)
     */
    public String getNombre() {
        return carpeta.getName();
    }

    /**
     * @return la ruta absoluta de la carpeta en disco
     */
    public String getRutaAbsoluta() {
        return carpeta.getAbsolutePath();
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
