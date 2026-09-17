package com.proyecto1.ui.servicio;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Servicio encargado de todas las operaciones sobre el sistema de
 * ficheros real: abrir, guardar, crear, eliminar, renombrar y listar
 * archivos y carpetas.
 * <p>
 * Se implementa como una clase de metodos estaticos (utilidad) ya que no
 * necesita mantener estado propio: cada operacion recibe el {@link File}
 * sobre el que debe actuar. Esto facilita su uso desde cualquier
 * controlador sin necesidad de inyectar una instancia.
 * </p>
 *
 * @author Proyecto1
 */
public final class GestorArchivos {

    /** Extensiones de archivo reconocidas por el compilador. */
    private static final Set<String> EXTENSIONES_SOPORTADAS = Set.of("y", "z", "pig");

    private GestorArchivos() {
        // Clase de utilidad: no debe instanciarse.
    }

    /**
     * Lee por completo el contenido de un archivo de texto.
     *
     * @param archivo archivo a leer
     * @return el contenido del archivo como cadena de texto
     * @throws IOException si ocurre un error de lectura
     */
    public static String abrirArchivo(File archivo) throws IOException {
        byte[] bytes = Files.readAllBytes(archivo.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Sobrescribe un archivo existente con el contenido indicado.
     *
     * @param archivo   archivo a sobrescribir
     * @param contenido nuevo contenido del archivo
     * @throws IOException si ocurre un error de escritura
     */
    public static void guardarArchivo(File archivo, String contenido) throws IOException {
        Files.write(archivo.toPath(), contenido.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Crea un archivo nuevo en disco con el contenido indicado. Si el
     * archivo ya existe, se lanza una excepcion para evitar sobrescrituras
     * accidentales.
     *
     * @param archivo   ruta del archivo nuevo
     * @param contenido contenido inicial del archivo (puede ser vacio)
     * @throws IOException si el archivo ya existe o hay un error de escritura
     */
    public static void crearArchivo(File archivo, String contenido) throws IOException {
        if (archivo.exists()) {
            throw new IOException("El archivo ya existe: " + archivo.getAbsolutePath());
        }
        File carpetaPadre = archivo.getParentFile();
        if (carpetaPadre != null && !carpetaPadre.exists()) {
            Files.createDirectories(carpetaPadre.toPath());
        }
        Files.write(archivo.toPath(), contenido.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Crea una carpeta nueva (y las carpetas intermedias que hagan falta).
     *
     * @param carpeta ruta de la carpeta a crear
     * @throws IOException si la carpeta ya existe o hay un error de escritura
     */
    public static void crearCarpeta(File carpeta) throws IOException {
        if (carpeta.exists()) {
            throw new IOException("La carpeta ya existe: " + carpeta.getAbsolutePath());
        }
        Files.createDirectories(carpeta.toPath());
    }

    /**
     * Elimina un archivo o una carpeta (recursivamente si tiene contenido).
     *
     * @param archivoOCarpeta elemento a eliminar
     * @throws IOException si ocurre un error al eliminar
     */
    public static void eliminar(File archivoOCarpeta) throws IOException {
        if (!archivoOCarpeta.exists()) {
            return;
        }
        if (archivoOCarpeta.isDirectory()) {
            File[] hijos = archivoOCarpeta.listFiles();
            if (hijos != null) {
                for (File hijo : hijos) {
                    eliminar(hijo);
                }
            }
        }
        Files.delete(archivoOCarpeta.toPath());
    }

    /**
     * Renombra un archivo o carpeta, conservandolo en la misma ubicacion.
     *
     * @param archivoOCarpeta elemento a renombrar
     * @param nuevoNombre     nuevo nombre simple (sin ruta)
     * @return el nuevo {@link File} tras el renombrado
     * @throws IOException si el renombrado falla
     */
    public static File renombrar(File archivoOCarpeta, String nuevoNombre) throws IOException {
        File destino = new File(archivoOCarpeta.getParentFile(), nuevoNombre);
        if (destino.exists()) {
            throw new IOException("Ya existe un elemento con ese nombre: " + destino.getAbsolutePath());
        }
        Files.move(archivoOCarpeta.toPath(), destino.toPath(), StandardCopyOption.ATOMIC_MOVE);
        return destino;
    }

    /**
     * Mueve un archivo o carpeta a otra carpeta destino (usado para el
     * arrastrar y soltar dentro del arbol de trabajo).
     *
     * @param origen          elemento a mover
     * @param carpetaDestino  carpeta a la que se movera el elemento
     * @return el nuevo {@link File} tras el movimiento
     * @throws IOException si el movimiento falla
     */
    public static File mover(File origen, File carpetaDestino) throws IOException {
        if (!carpetaDestino.isDirectory()) {
            throw new IOException("El destino no es una carpeta: " + carpetaDestino.getAbsolutePath());
        }
        File destino = new File(carpetaDestino, origen.getName());
        if (destino.exists()) {
            throw new IOException("Ya existe un elemento con ese nombre en el destino: " + destino.getAbsolutePath());
        }
        Path rutaFinal = Files.move(origen.toPath(), destino.toPath(), StandardCopyOption.ATOMIC_MOVE);
        return rutaFinal.toFile();
    }

    /**
     * Duplica un archivo dentro de la misma carpeta, agregando un sufijo
     * al nombre para evitar colisiones (por ejemplo "prueba_copia.y").
     *
     * @param archivo archivo a duplicar
     * @return el nuevo archivo creado
     * @throws IOException si ocurre un error de copia
     */
    public static File duplicar(File archivo) throws IOException {
        String nombre = archivo.getName();
        String base = nombre;
        String extension = "";
        int idx = nombre.lastIndexOf('.');
        if (idx > 0) {
            base = nombre.substring(0, idx);
            extension = nombre.substring(idx);
        }
        File carpetaPadre = archivo.getParentFile();
        File destino = new File(carpetaPadre, base + "_copia" + extension);
        int contador = 1;
        while (destino.exists()) {
            contador++;
            destino = new File(carpetaPadre, base + "_copia" + contador + extension);
        }
        Files.copy(archivo.toPath(), destino.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        return destino;
    }

    /**
     * Lista los hijos directos de una carpeta, ordenados de forma que las
     * carpetas aparezcan primero y luego los archivos, ambos en orden
     * alfabetico (ignorando mayusculas/minusculas).
     *
     * @param carpeta carpeta cuyos hijos se desean listar
     * @return lista de hijos, vacia si la carpeta no existe o esta vacia
     */
    public static List<File> listarHijos(File carpeta) {
        File[] hijos = carpeta.listFiles();
        if (hijos == null) {
            return new ArrayList<>();
        }
        List<File> lista = new ArrayList<>(Arrays.asList(hijos));
        lista.sort(Comparator
                .comparing(File::isFile)
                .thenComparing(f -> f.getName().toLowerCase(Locale.ROOT)));
        return lista;
    }

    /**
     * Indica si la extension del archivo corresponde a uno de los
     * lenguajes soportados por el compilador (.y, .z, .pig).
     *
     * @param archivo archivo a verificar
     * @return {@code true} si la extension es soportada
     */
    public static boolean esArchivoSoportado(File archivo) {
        String nombre = archivo.getName();
        int idx = nombre.lastIndexOf('.');
        if (idx < 0 || idx == nombre.length() - 1) {
            return false;
        }
        String extension = nombre.substring(idx + 1).toLowerCase(Locale.ROOT);
        return EXTENSIONES_SOPORTADAS.contains(extension);
    }
}
