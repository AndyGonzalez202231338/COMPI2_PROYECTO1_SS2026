package com.proyecto1.ui.modelo;

import java.io.File;
import java.util.Locale;

/**
 * Representa un archivo dentro del arbol de trabajo del IDE.
 * <p>
 * Esta clase es un modelo de UI: envuelve un {@link File} real del disco
 * y agrega informacion util para la interfaz grafica, como el estado de
 * "modificado" (cambios sin guardar) y el lenguaje detectado segun la
 * extension del archivo.
 * </p>
 *
 * @author Proyecto1
 */
public class ArchivoUI {

    /** Extensiones soportadas por el compilador y su lenguaje asociado. */
    public enum Lenguaje {
        Y_INTERROGACION("Y?"),
        ZETARIANO("Zetariano"),
        PIG_LATIN("PigLatin"),
        DESCONOCIDO("Desconocido");

        private final String nombreVisible;

        Lenguaje(String nombreVisible) {
            this.nombreVisible = nombreVisible;
        }

        public String getNombreVisible() {
            return nombreVisible;
        }
    }

    private File archivo;
    private boolean modificado;

    /**
     * Crea un modelo de archivo a partir de un archivo real en disco.
     *
     * @param archivo archivo del sistema de ficheros que este nodo representa
     */
    public ArchivoUI(File archivo) {
        this.archivo = archivo;
        this.modificado = false;
    }

    public File getArchivo() {
        return archivo;
    }

    public void setArchivo(File archivo) {
        this.archivo = archivo;
    }

    /**
     * @return el nombre simple del archivo (sin la ruta completa)
     */
    public String getNombre() {
        return archivo.getName();
    }

    /**
     * @return la ruta absoluta del archivo en disco
     */
    public String getRutaAbsoluta() {
        return archivo.getAbsolutePath();
    }

    public boolean isModificado() {
        return modificado;
    }

    public void setModificado(boolean modificado) {
        this.modificado = modificado;
    }

    /**
     * Calcula la extension del archivo (sin el punto), en minusculas.
     *
     * @return la extension o cadena vacia si el archivo no tiene extension
     */
    public String getExtension() {
        String nombre = archivo.getName();
        int idx = nombre.lastIndexOf('.');
        if (idx < 0 || idx == nombre.length() - 1) {
            return "";
        }
        return nombre.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Determina el lenguaje del archivo segun su extension.
     *
     * @return el {@link Lenguaje} detectado
     */
    public Lenguaje getLenguaje() {
        switch (getExtension()) {
            case "y":
                return Lenguaje.Y_INTERROGACION;
            case "z":
                return Lenguaje.ZETARIANO;
            case "pig":
                return Lenguaje.PIG_LATIN;
            default:
                return Lenguaje.DESCONOCIDO;
        }
    }

    /**
     * Nombre a mostrar en la interfaz (pestanas, arbol), agregando un
     * asterisco cuando el archivo tiene cambios sin guardar.
     *
     * @return el nombre formateado para mostrar en la UI
     */
    public String getNombreParaMostrar() {
        return modificado ? getNombre() + " *" : getNombre();
    }

    @Override
    public String toString() {
        return getNombreParaMostrar();
    }
}
