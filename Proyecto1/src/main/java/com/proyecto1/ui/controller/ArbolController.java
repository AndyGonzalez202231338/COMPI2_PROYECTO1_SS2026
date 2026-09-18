package com.proyecto1.ui.controller;

import com.proyecto1.ui.modelo.ArchivoUI;
import com.proyecto1.ui.modelo.CarpetaUI;
import com.proyecto1.ui.servicio.GestorArchivos;
import com.proyecto1.ui.util.Notificaciones;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador del arbol de trabajo (gestor de archivos y carpetas propio
 * del IDE, independiente del explorador del sistema operativo).
 * <p>
 * Responsabilidades:
 * </p>
 * <ul>
 *   <li>Construir el {@link TreeView} de forma perezosa: cada carpeta solo
 *       lee su contenido del disco cuando el usuario la expande.</li>
 *   <li>Mostrar un icono distinto segun la extension del archivo
 *       (.y, .z, .pig, u otro generico).</li>
 *   <li>Ofrecer menu contextual para crear, renombrar, eliminar, duplicar
 *       y refrescar.</li>
 *   <li>Permitir mover archivos entre carpetas arrastrando y soltando.</li>
 *   <li>Notificar (via {@link EscuchaArbol}) cuando el usuario hace doble
 *       clic en un archivo, para que otro componente (el editor) lo abra.</li>
 * </ul>
 * <p>
 * Esta clase NO depende de {@code EditorController} ni del backend del
 * compilador: solo conoce el disco y notifica eventos hacia afuera
 * mediante la interfaz {@link EscuchaArbol}, que sera implementada por
 * {@code MainController} en la Fase 5.
 * </p>
 *
 * @author Proyecto1
 */
public class ArbolController {

    /**
     * Contrato que debe cumplir quien use este controlador para enterarse
     * de los eventos relevantes del arbol de trabajo.
     */
    public interface EscuchaArbol {
        /** Se invoca cuando el usuario hace doble clic sobre un archivo. */
        void archivoAbierto(File archivo);

        /** Se invoca para reportar un mensaje informativo (para la consola). */
        void mensajeInfo(String mensaje);

        /** Se invoca para reportar un error (para la consola). */
        void mensajeError(String mensaje);
    }

    /** Formato interno usado por el drag&drop para transportar la ruta del archivo arrastrado. */
    private static final DataFormat FORMATO_RUTA_ARCHIVO = new DataFormat("proyecto1/ruta-archivo");

    private final TreeView<Object> treeView;
    private final EscuchaArbol escucha;

    /** Cache de iconos ya cargados, para no leerlos del classpath repetidas veces. */
    private final Map<String, Image> cacheIconos = new HashMap<>();

    /**
     * Crea el controlador y deja el {@link TreeView} listo para usarse
     * (fabrica de celdas configurada). Todavia no carga ninguna carpeta:
     * para eso se debe llamar a {@link #abrirCarpetaRaiz(File)}.
     *
     * @param treeView componente grafico del arbol, ya presente en el FXML
     * @param escucha  receptor de los eventos del arbol
     */
    public ArbolController(TreeView<Object> treeView, EscuchaArbol escucha) {
        this.treeView = treeView;
        this.escucha = escucha;
        this.treeView.setShowRoot(true);
        this.treeView.setCellFactory(vista -> new CeldaArbol());
    }

    /**
     * Abre una carpeta como raiz del proyecto, reemplazando el contenido
     * actual del arbol.
     *
     * @param carpetaRaiz carpeta del disco elegida por el usuario
     */
    public void abrirCarpetaRaiz(File carpetaRaiz) {
        if (carpetaRaiz == null || !carpetaRaiz.isDirectory()) {
            escucha.mensajeError("La ruta seleccionada no es una carpeta valida.");
            return;
        }
        NodoArbol nodoRaiz = new NodoArbol(new CarpetaUI(carpetaRaiz));
        nodoRaiz.setExpanded(true);
        treeView.setRoot(nodoRaiz);
        escucha.mensajeInfo("Carpeta de proyecto abierta: " + carpetaRaiz.getAbsolutePath());
    }

    /** Refresca todo el arbol desde la raiz actual (relee el disco). */
    public void refrescarTodo() {
        TreeItem<Object> raiz = treeView.getRoot();
        if (raiz instanceof NodoArbol) {
            ((NodoArbol) raiz).recargar();
            escucha.mensajeInfo("Arbol de trabajo actualizado.");
        }
    }

    /**
     * @return el archivo actualmente seleccionado en el arbol, si lo hay
     *         y corresponde a un archivo (no a una carpeta)
     */
    public Optional<File> getArchivoSeleccionado() {
        TreeItem<Object> seleccionado = treeView.getSelectionModel().getSelectedItem();
        if (seleccionado != null && seleccionado.getValue() instanceof ArchivoUI) {
            return Optional.of(((ArchivoUI) seleccionado.getValue()).getArchivo());
        }
        return Optional.empty();
    }

    // ======================================================================
    // NODO DEL ARBOL CON CARGA PEREZOSA
    // ======================================================================

    /**
     * TreeItem especializado que solo lee el contenido de su carpeta
     * cuando JavaFX pide realmente sus hijos (al expandirlo por primera
     * vez), evitando leer todo el disco de una sola vez.
     */
    private class NodoArbol extends TreeItem<Object> {

        private boolean cargado = false;

        NodoArbol(Object valor) {
            super(valor);
        }

        @Override
        public ObservableList<TreeItem<Object>> getChildren() {
            if (!cargado) {
                cargado = true;
                super.getChildren().setAll(construirHijos());
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            // Solo los archivos son hojas; las carpetas siempre muestran
            // la flecha de expansion (aunque esten vacias).
            return getValue() instanceof ArchivoUI;
        }

        /** Fuerza una relectura del disco la proxima vez que se pidan los hijos. */
        void recargar() {
            cargado = false;
            super.getChildren().clear();
            if (isExpanded()) {
                // Forzar recarga inmediata si ya estaba expandido.
                getChildren();
            }
        }

        private List<TreeItem<Object>> construirHijos() {
            List<TreeItem<Object>> resultado = new ArrayList<>();
            Object valor = getValue();
            if (!(valor instanceof CarpetaUI)) {
                return resultado;
            }
            File carpeta = ((CarpetaUI) valor).getCarpeta();
            for (File hijo : GestorArchivos.listarHijos(carpeta)) {
                if (hijo.isDirectory()) {
                    resultado.add(new NodoArbol(new CarpetaUI(hijo)));
                } else {
                    resultado.add(new NodoArbol(new ArchivoUI(hijo)));
                }
            }
            return resultado;
        }
    }

    // ======================================================================
    // CELDA PERSONALIZADA (ICONO + TEXTO + MENU CONTEXTUAL + DRAG&DROP)
    // ======================================================================

    private class CeldaArbol extends TreeCell<Object> {

        CeldaArbol() {
            configurarDobleClic();
            configurarArrastre();
        }

        @Override
        protected void updateItem(Object valor, boolean vacio) {
            super.updateItem(valor, vacio);
            if (vacio || valor == null) {
                setText(null);
                setGraphic(null);
                setContextMenu(null);
                return;
            }

            if (valor instanceof ArchivoUI) {
                ArchivoUI archivoUI = (ArchivoUI) valor;
                setText(archivoUI.getNombreParaMostrar());
                setGraphic(crearIcono(iconoParaExtension(archivoUI.getExtension())));
                setContextMenu(crearMenuArchivo(archivoUI));
            } else if (valor instanceof CarpetaUI) {
                CarpetaUI carpetaUI = (CarpetaUI) valor;
                setText(carpetaUI.getNombre());
                setGraphic(crearIcono("carpeta.png"));
                setContextMenu(crearMenuCarpeta(carpetaUI));
            } else {
                setText(String.valueOf(valor));
                setGraphic(null);
                setContextMenu(null);
            }
        }

        private void configurarDobleClic() {
            setOnMouseClicked((MouseEvent evento) -> {
                if (evento.getButton() == MouseButton.PRIMARY
                        && evento.getClickCount() == 2
                        && !isEmpty()
                        && getItem() instanceof ArchivoUI) {
                    File archivo = ((ArchivoUI) getItem()).getArchivo();
                    escucha.archivoAbierto(archivo);
                }
            });
        }

        /** Configura esta celda como origen y destino de arrastrar/soltar. */
        private void configurarArrastre() {
            setOnDragDetected(evento -> {
                if (isEmpty() || getItem() == null) {
                    return;
                }
                File origen = obtenerArchivoODirectorio(getItem());
                if (origen == null) {
                    return;
                }
                Dragboard tablero = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent contenido = new ClipboardContent();
                contenido.put(FORMATO_RUTA_ARCHIVO, origen.getAbsolutePath());
                tablero.setContent(contenido);
                evento.consume();
            });

            setOnDragOver(evento -> {
                if (evento.getGestureSource() != this
                        && evento.getDragboard().hasContent(FORMATO_RUTA_ARCHIVO)
                        && getItem() instanceof CarpetaUI) {
                    evento.acceptTransferModes(TransferMode.MOVE);
                }
                evento.consume();
            });

            setOnDragDropped(evento -> {
                Dragboard tablero = evento.getDragboard();
                boolean exito = false;
                if (tablero.hasContent(FORMATO_RUTA_ARCHIVO) && getItem() instanceof CarpetaUI) {
                    String rutaOrigen = (String) tablero.getContent(FORMATO_RUTA_ARCHIVO);
                    File origen = new File(rutaOrigen);
                    File carpetaDestino = ((CarpetaUI) getItem()).getCarpeta();
                    try {
                        GestorArchivos.mover(origen, carpetaDestino);
                        escucha.mensajeInfo("Movido \"" + origen.getName() + "\" a \"" + carpetaDestino.getName() + "\".");
                        refrescarTodo();
                        exito = true;
                    } catch (IOException ex) {
                        escucha.mensajeError("No se pudo mover el archivo: " + ex.getMessage());
                        Notificaciones.mostrarError("Mover archivo", ex.getMessage());
                    }
                }
                evento.setDropCompleted(exito);
                evento.consume();
            });
        }

        private File obtenerArchivoODirectorio(Object valor) {
            if (valor instanceof ArchivoUI) {
                return ((ArchivoUI) valor).getArchivo();
            }
            if (valor instanceof CarpetaUI) {
                return ((CarpetaUI) valor).getCarpeta();
            }
            return null;
        }

        // -------------------- MENUS CONTEXTUALES --------------------

        private ContextMenu crearMenuArchivo(ArchivoUI archivoUI) {
            MenuItem abrir = new MenuItem("Abrir");
            abrir.setOnAction(e -> escucha.archivoAbierto(archivoUI.getArchivo()));

            MenuItem renombrar = new MenuItem("Renombrar");
            renombrar.setOnAction(e -> renombrarElemento(archivoUI.getArchivo()));

            MenuItem eliminar = new MenuItem("Eliminar");
            eliminar.setOnAction(e -> eliminarElemento(archivoUI.getArchivo()));

            MenuItem duplicar = new MenuItem("Duplicar");
            duplicar.setOnAction(e -> duplicarArchivo(archivoUI.getArchivo()));

            return new ContextMenu(abrir, renombrar, eliminar, duplicar);
        }

        private ContextMenu crearMenuCarpeta(CarpetaUI carpetaUI) {
            MenuItem nuevoArchivo = new MenuItem("Nuevo archivo");
            nuevoArchivo.setOnAction(e -> crearArchivoEnCarpeta(carpetaUI.getCarpeta()));

            MenuItem nuevaCarpeta = new MenuItem("Nueva carpeta");
            nuevaCarpeta.setOnAction(e -> crearSubcarpeta(carpetaUI.getCarpeta()));

            MenuItem renombrar = new MenuItem("Renombrar");
            renombrar.setOnAction(e -> renombrarElemento(carpetaUI.getCarpeta()));

            MenuItem eliminar = new MenuItem("Eliminar");
            eliminar.setOnAction(e -> eliminarElemento(carpetaUI.getCarpeta()));

            MenuItem refrescar = new MenuItem("Refrescar");
            refrescar.setOnAction(e -> refrescarTodo());

            return new ContextMenu(nuevoArchivo, nuevaCarpeta, renombrar, eliminar, refrescar);
        }

        // -------------------- ACCIONES --------------------

        private void crearArchivoEnCarpeta(File carpeta) {
            Optional<String> nombre = Notificaciones.pedirTexto(
                    "Nuevo archivo", "Nombre del nuevo archivo (incluya extension .y, .z o .pig):", "nuevo.y");
            nombre.ifPresent(n -> {
                try {
                    GestorArchivos.crearArchivo(new File(carpeta, n), "");
                    escucha.mensajeInfo("Archivo creado: " + n);
                    refrescarTodo();
                } catch (IOException ex) {
                    escucha.mensajeError("No se pudo crear el archivo: " + ex.getMessage());
                    Notificaciones.mostrarError("Nuevo archivo", ex.getMessage());
                }
            });
        }

        private void crearSubcarpeta(File carpetaPadre) {
            Optional<String> nombre = Notificaciones.pedirTexto(
                    "Nueva carpeta", "Nombre de la nueva carpeta:", "nueva_carpeta");
            nombre.ifPresent(n -> {
                try {
                    GestorArchivos.crearCarpeta(new File(carpetaPadre, n));
                    escucha.mensajeInfo("Carpeta creada: " + n);
                    refrescarTodo();
                } catch (IOException ex) {
                    escucha.mensajeError("No se pudo crear la carpeta: " + ex.getMessage());
                    Notificaciones.mostrarError("Nueva carpeta", ex.getMessage());
                }
            });
        }

        private void renombrarElemento(File elemento) {
            Optional<String> nuevoNombre = Notificaciones.pedirTexto(
                    "Renombrar", "Nuevo nombre para \"" + elemento.getName() + "\":", elemento.getName());
            nuevoNombre.ifPresent(n -> {
                try {
                    GestorArchivos.renombrar(elemento, n);
                    escucha.mensajeInfo("Renombrado \"" + elemento.getName() + "\" a \"" + n + "\".");
                    refrescarTodo();
                } catch (IOException ex) {
                    escucha.mensajeError("No se pudo renombrar: " + ex.getMessage());
                    Notificaciones.mostrarError("Renombrar", ex.getMessage());
                }
            });
        }

        private void eliminarElemento(File elemento) {
            boolean confirmado = Notificaciones.confirmar(
                    "Eliminar",
                    "¿Seguro que desea eliminar \"" + elemento.getName() + "\"? Esta accion no se puede deshacer.");
            if (!confirmado) {
                return;
            }
            try {
                GestorArchivos.eliminar(elemento);
                escucha.mensajeInfo("Eliminado: " + elemento.getName());
                refrescarTodo();
            } catch (IOException ex) {
                escucha.mensajeError("No se pudo eliminar: " + ex.getMessage());
                Notificaciones.mostrarError("Eliminar", ex.getMessage());
            }
        }

        private void duplicarArchivo(File archivo) {
            try {
                File copia = GestorArchivos.duplicar(archivo);
                escucha.mensajeInfo("Archivo duplicado como: " + copia.getName());
                refrescarTodo();
            } catch (IOException ex) {
                escucha.mensajeError("No se pudo duplicar el archivo: " + ex.getMessage());
                Notificaciones.mostrarError("Duplicar", ex.getMessage());
            }
        }

        // -------------------- ICONOS --------------------

        private String iconoParaExtension(String extension) {
            switch (extension.toLowerCase(Locale.ROOT)) {
                case "y":
                    return "archivo-y.png";
                case "z":
                    return "archivo-z.png";
                case "pig":
                    return "archivo-pig.png";
                default:
                    return "archivo-generico.png";
            }
        }

        /**
         * Crea un {@link ImageView} de 16x16 para el icono indicado. Si el
         * archivo PNG todavia no existe en {@code resources/icons/} (ver
         * Fase 6 en PROGRESO_FRONTEND.md), no se produce ningun error: la
         * celda simplemente se muestra sin icono.
         */
        private ImageView crearIcono(String nombreArchivoPng) {
            Image imagen = cacheIconos.computeIfAbsent(nombreArchivoPng, this::cargarIconoOVacio);
            if (imagen == null) {
                return null;
            }
            ImageView vista = new ImageView(imagen);
            vista.setFitWidth(16);
            vista.setFitHeight(16);
            vista.setPreserveRatio(true);
            return vista;
        }

        private Image cargarIconoOVacio(String nombreArchivoPng) {
            var flujo = getClass().getResourceAsStream("/icons/" + nombreArchivoPng);
            if (flujo == null) {
                return null;
            }
            return new Image(flujo);
        }
    }
}
