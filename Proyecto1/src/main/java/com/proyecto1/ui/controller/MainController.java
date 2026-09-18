package com.proyecto1.ui.controller;

import com.proyecto1.ui.MainApp;
import com.proyecto1.ui.modelo.ArchivoUI;
import com.proyecto1.ui.modelo.ProyectoUI;
import com.proyecto1.ui.servicio.GestorArchivos;
import com.proyecto1.ui.util.Notificaciones;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controlador de la ventana principal del IDE.
 * <p>
 * Este controlador coordina los tres componentes visuales principales
 * del frontend:
 * </p>
 * <ul>
 *   <li>{@link ArbolController}: gestor de archivos y carpetas propio del
 *       IDE (arbol de trabajo).</li>
 *   <li>{@link EditorController}: una instancia por cada pestana abierta
 *       en {@code panelPestanas}.</li>
 *   <li>La consola de salida, el menu/toolbar y la barra de estado,
 *       propios de esta clase.</li>
 * </ul>
 * <p>
 * {@code MainController} implementa {@link ArbolController.EscuchaArbol}
 * y {@link EditorController.EscuchaEditor} para enterarse de los eventos
 * de ambos (doble clic en un archivo del arbol, cambios de texto o de
 * cursor en el editor) y reaccionar actualizando la barra de estado, el
 * titulo de la ventana y la consola.
 * </p>
 * <p>
 * Las acciones del menu "Ejecutar" (Analizar, Compilar, Ejecutar ultimo
 * analisis) siguen siendo placeholders: la conexion con el backend del
 * compilador se realizara en una fase posterior.
 * </p>
 *
 * @author Proyecto1
 */
public class MainController implements ArbolController.EscuchaArbol, EditorController.EscuchaEditor {

    // ---------- Referencias inyectadas desde main.fxml ----------

    @FXML private BorderPane raizPrincipal;
    @FXML private MenuBar barraMenu;
    @FXML private ToolBar barraHerramientas;

    @FXML private CheckMenuItem miMostrarArbol;
    @FXML private CheckMenuItem miMostrarConsola;
    @FXML private CheckMenuItem miMostrarPanelErrores;

    @FXML private TitledPane panelArbol;
    @FXML private TreeView<Object> arbolTrabajo;

    @FXML private TabPane panelPestanas;

    @FXML private VBox contenedorConsola;
    @FXML private TextArea consolaSalida;

    @FXML private TitledPane panelErrores;

    @FXML private HBox barraEstado;
    @FXML private Label lblRutaActiva;
    @FXML private Label lblPosicionCursor;
    @FXML private Label lblLenguajeDetectado;
    @FXML private Label lblEstadoModificado;

    @FXML private Button btnNuevoArchivo;
    @FXML private Button btnAbrirArchivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnGuardarComo;
    @FXML private Button btnDeshacer;
    @FXML private Button btnRehacer;
    @FXML private Button btnBuscar;
    @FXML private Button btnAnalizar;
    @FXML private Button btnEjecutar;
    @FXML private Button btnTema;

    /** Controlador del arbol de trabajo, creado en {@link #initialize()}. */
    private ArbolController arbolController;

    /** Proyecto (carpeta raiz) actualmente abierto, si lo hay. */
    private final ProyectoUI proyectoActual = new ProyectoUI();

    /** Recuerda si el tema activo es el oscuro, para el boton "alternar tema". */
    private boolean temaOscuroActivo = false;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Filtro de extensiones para los dialogos de abrir/guardar archivo. */
    private static final FileChooser.ExtensionFilter FILTRO_LENGUAJES = new FileChooser.ExtensionFilter(
            "Archivos soportados (*.y, *.z, *.pig)", "*.y", "*.z", "*.pig");
    private static final FileChooser.ExtensionFilter FILTRO_TODOS = new FileChooser.ExtensionFilter(
            "Todos los archivos", "*.*");

    /**
     * Metodo de inicializacion invocado automaticamente por JavaFX luego
     * de inyectar todos los campos {@code @FXML}.
     */
    @FXML
    private void initialize() {
        arbolController = new ArbolController(arbolTrabajo, this);

        // Al cambiar de pestana se debe reflejar en la barra de estado y
        // en el titulo de la ventana (Fase 5).
        panelPestanas.getSelectionModel().selectedItemProperty()
                .addListener((obs, pestanaVieja, pestanaNueva) ->
                        actualizarBarraEstado(EditorController.desdeTab(pestanaNueva)));

        actualizarBarraEstado(null);
        escribirInfo("IDE iniciado. Utilice \"Archivo > Abrir carpeta\" para comenzar a trabajar.");
    }

    // ======================================================================
    // MENU VER
    // ======================================================================

    @FXML
    private void accionAlternarArbol() {
        boolean mostrar = miMostrarArbol.isSelected();
        panelArbol.setVisible(mostrar);
        panelArbol.setManaged(mostrar);
    }

    @FXML
    private void accionAlternarConsola() {
        boolean mostrar = miMostrarConsola.isSelected();
        contenedorConsola.setVisible(mostrar);
        contenedorConsola.setManaged(mostrar);
    }

    @FXML
    private void accionAlternarPanelErrores() {
        boolean mostrar = miMostrarPanelErrores.isSelected();
        panelErrores.setVisible(mostrar);
        panelErrores.setManaged(mostrar);
    }

    @FXML
    private void accionTemaClaro() {
        aplicarTema(MainApp.TEMA_CLARO);
        temaOscuroActivo = false;
    }

    @FXML
    private void accionTemaOscuro() {
        aplicarTema(MainApp.TEMA_OSCURO);
        temaOscuroActivo = true;
    }

    /** Accion del boton de la toolbar: alterna entre tema claro y oscuro. */
    @FXML
    private void accionAlternarTema() {
        if (temaOscuroActivo) {
            accionTemaClaro();
        } else {
            accionTemaOscuro();
        }
    }

    /**
     * Reemplaza la hoja de estilos activa de la escena por la indicada,
     * sin reiniciar la aplicacion.
     *
     * @param nombreArchivoCss nombre del archivo CSS ("claro.css" u "oscuro.css")
     */
    private void aplicarTema(String nombreArchivoCss) {
        Scene escena = raizPrincipal.getScene();
        if (escena == null) {
            return;
        }
        URL urlCss = Objects.requireNonNull(
                getClass().getResource(nombreArchivoCss),
                "No se encontro la hoja de estilos: " + nombreArchivoCss);
        escena.getStylesheets().setAll(urlCss.toExternalForm());
        escribirInfo("Tema aplicado: " + nombreArchivoCss);
    }

    // ======================================================================
    // MENU EJECUTAR (placeholders - conexion en fase posterior)
    // ======================================================================

    @FXML
    private void accionAnalizarArchivoActual() {
        escribirAdvertencia("Analizar archivo actual: pendiente de conexion con el backend.");
        Notificaciones.mostrarFuncionPendiente(obtenerVentana());
    }

    @FXML
    private void accionCompilarProyecto() {
        escribirAdvertencia("Compilar todo el proyecto: pendiente de conexion con el backend.");
        Notificaciones.mostrarFuncionPendiente(obtenerVentana());
    }

    @FXML
    private void accionEjecutarUltimoAnalisis() {
        escribirAdvertencia("Ejecutar ultimo analisis: pendiente de conexion con el backend.");
        Notificaciones.mostrarFuncionPendiente(obtenerVentana());
    }

    // ======================================================================
    // MENU AYUDA
    // ======================================================================

    @FXML
    private void accionAcercaDe() {
        Notificaciones.mostrarInformacion(
                "Acerca de",
                "Compilador - Proyecto 1\n"
                        + "IDE para los lenguajes Y?, Zetariano y PigLatin.\n"
                        + "Proyecto de Compiladores 2 - Fase de interfaz grafica.");
    }

    @FXML
    private void accionManualUsuario() {
        Notificaciones.mostrarInformacion(
                "Manual de usuario",
                "Manual de usuario pendiente de redaccion.\n"
                        + "Aqui se explicara como crear proyectos, editar archivos "
                        + "y ejecutar el analisis una vez conectado el backend.");
    }

    @FXML
    private void accionSalir() {
        Stage ventana = obtenerVentana();
        if (ventana == null) {
            return;
        }
        if (cerrarTodasLasPestanas()) {
            ventana.close();
        }
    }

    // ======================================================================
    // MENU ARCHIVO
    // ======================================================================

    @FXML
    private void accionNuevoArchivo() {
        FileChooser dialogo = new FileChooser();
        dialogo.setTitle("Nuevo archivo");
        dialogo.setInitialDirectory(carpetaInicialParaDialogos());
        dialogo.getExtensionFilters().setAll(FILTRO_LENGUAJES, FILTRO_TODOS);
        dialogo.setInitialFileName("nuevo.y");

        File archivo = dialogo.showSaveDialog(obtenerVentana());
        if (archivo == null) {
            return;
        }
        try {
            GestorArchivos.crearArchivo(archivo, "");
            escribirInfo("Archivo creado: " + archivo.getAbsolutePath());
            arbolController.refrescarTodo();
            abrirArchivoEnEditor(archivo);
        } catch (IOException ex) {
            escribirError("No se pudo crear el archivo: " + ex.getMessage());
            Notificaciones.mostrarError("Nuevo archivo", ex.getMessage());
        }
    }

    @FXML
    private void accionNuevoProyecto() {
        DirectoryChooser dialogoCarpeta = new DirectoryChooser();
        dialogoCarpeta.setTitle("Elegir ubicacion para el nuevo proyecto");
        dialogoCarpeta.setInitialDirectory(carpetaInicialParaDialogos());
        File carpetaPadre = dialogoCarpeta.showDialog(obtenerVentana());
        if (carpetaPadre == null) {
            return;
        }

        Optional<String> nombre = Notificaciones.pedirTexto(
                "Nuevo proyecto/carpeta", "Nombre de la carpeta del nuevo proyecto:", "proyecto_nuevo");
        nombre.ifPresent(n -> {
            File nuevaCarpeta = new File(carpetaPadre, n);
            try {
                GestorArchivos.crearCarpeta(nuevaCarpeta);
                escribirInfo("Proyecto creado: " + nuevaCarpeta.getAbsolutePath());
                abrirCarpetaComoRaiz(nuevaCarpeta);
            } catch (IOException ex) {
                escribirError("No se pudo crear el proyecto: " + ex.getMessage());
                Notificaciones.mostrarError("Nuevo proyecto/carpeta", ex.getMessage());
            }
        });
    }

    @FXML
    private void accionAbrirArchivo() {
        FileChooser dialogo = new FileChooser();
        dialogo.setTitle("Abrir archivo");
        dialogo.setInitialDirectory(carpetaInicialParaDialogos());
        dialogo.getExtensionFilters().setAll(FILTRO_LENGUAJES, FILTRO_TODOS);

        File archivo = dialogo.showOpenDialog(obtenerVentana());
        if (archivo != null) {
            abrirArchivoEnEditor(archivo);
        }
    }

    @FXML
    private void accionAbrirCarpeta() {
        DirectoryChooser dialogo = new DirectoryChooser();
        dialogo.setTitle("Abrir carpeta de proyecto");
        dialogo.setInitialDirectory(carpetaInicialParaDialogos());
        File carpeta = dialogo.showDialog(obtenerVentana());
        if (carpeta != null) {
            abrirCarpetaComoRaiz(carpeta);
        }
    }

    /** Centraliza el "abrir carpeta como raiz", usado por el menu y por "Nuevo proyecto". */
    private void abrirCarpetaComoRaiz(File carpeta) {
        arbolController.abrirCarpetaRaiz(carpeta);
        proyectoActual.setCarpetaRaiz(carpeta);
        panelArbol.setText("Arbol de trabajo - " + carpeta.getName());
    }

    @FXML
    private void accionGuardar() {
        EditorController editor = obtenerEditorActivo();
        if (editor == null) {
            escribirAdvertencia("No hay ningun archivo abierto para guardar.");
            return;
        }
        try {
            editor.guardar();
        } catch (IOException ex) {
            escribirError("No se pudo guardar el archivo: " + ex.getMessage());
            Notificaciones.mostrarError("Guardar", ex.getMessage());
        }
    }

    @FXML
    private void accionGuardarComo() {
        EditorController editor = obtenerEditorActivo();
        if (editor == null) {
            escribirAdvertencia("No hay ningun archivo abierto para guardar.");
            return;
        }
        FileChooser dialogo = new FileChooser();
        dialogo.setTitle("Guardar como");
        dialogo.setInitialDirectory(carpetaInicialParaDialogos());
        dialogo.setInitialFileName(editor.getArchivoUI().getNombre());
        dialogo.getExtensionFilters().setAll(FILTRO_LENGUAJES, FILTRO_TODOS);

        File destino = dialogo.showSaveDialog(obtenerVentana());
        if (destino == null) {
            return;
        }
        try {
            editor.guardarComo(destino);
            arbolController.refrescarTodo();
        } catch (IOException ex) {
            escribirError("No se pudo guardar el archivo: " + ex.getMessage());
            Notificaciones.mostrarError("Guardar como", ex.getMessage());
        }
    }

    @FXML
    private void accionCerrarPestanaActual() {
        cerrarPestana(panelPestanas.getSelectionModel().getSelectedItem());
    }

    /**
     * Cierra la pestana indicada respetando el aviso de cambios sin
     * guardar (dispara el mismo evento que produce el boton "x" de la
     * pestana, ya manejado por {@code EditorController}).
     *
     * @param tab pestana a cerrar (si es {@code null}, no hace nada)
     */
    private void cerrarPestana(Tab tab) {
        if (tab == null) {
            return;
        }
        Event evento = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, evento);
        if (!evento.isConsumed()) {
            panelPestanas.getTabs().remove(tab);
        }
    }

    /**
     * Intenta cerrar todas las pestanas abiertas, preguntando por los
     * cambios sin guardar en cada una. Se usa al salir de la aplicacion.
     *
     * @return {@code true} si todas las pestanas se pudieron cerrar (o no
     *         tenian cambios pendientes); {@code false} si el usuario
     *         cancelo el cierre de alguna
     */
    private boolean cerrarTodasLasPestanas() {
        List<Tab> pestanas = new ArrayList<>(panelPestanas.getTabs());
        for (Tab tab : pestanas) {
            EditorController editor = EditorController.desdeTab(tab);
            if (editor != null && editor.isModificado()) {
                Event evento = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
                Event.fireEvent(tab, evento);
                if (evento.isConsumed()) {
                    return false;
                }
            }
        }
        return true;
    }

    // ======================================================================
    // MENU EDITAR (delegado a la pestana activa del editor)
    // ======================================================================

    @FXML
    private void accionDeshacer() {
        EditorController editor = obtenerEditorActivo();
        if (editor != null) {
            editor.deshacer();
        }
    }

    @FXML
    private void accionRehacer() {
        EditorController editor = obtenerEditorActivo();
        if (editor != null) {
            editor.rehacer();
        }
    }

    @FXML
    private void accionCortar() {
        EditorController editor = obtenerEditorActivo();
        if (editor != null) {
            editor.cortar();
        }
    }

    @FXML
    private void accionCopiar() {
        EditorController editor = obtenerEditorActivo();
        if (editor != null) {
            editor.copiar();
        }
    }

    @FXML
    private void accionPegar() {
        EditorController editor = obtenerEditorActivo();
        if (editor != null) {
            editor.pegar();
        }
    }

    @FXML
    private void accionSeleccionarTodo() {
        EditorController editor = obtenerEditorActivo();
        if (editor != null) {
            editor.seleccionarTodo();
        }
    }

    /**
     * Abre un dialogo basico (no modal) de Buscar/Reemplazar sobre la
     * pestana de editor actualmente activa. Permite buscar la siguiente
     * coincidencia (busqueda ciclica) y reemplazar todas las apariciones.
     */
    @FXML
    private void accionBuscarReemplazar() {
        EditorController editor = obtenerEditorActivo();
        if (editor == null) {
            escribirAdvertencia("Abra un archivo para poder buscar o reemplazar texto.");
            return;
        }

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Buscar / Reemplazar");
        dialogo.initOwner(obtenerVentana());
        dialogo.initModality(Modality.NONE);

        TextField campoBuscar = new TextField();
        campoBuscar.setPromptText("Texto a buscar");
        TextField campoReemplazar = new TextField();
        campoReemplazar.setPromptText("Texto de reemplazo");

        GridPane panel = new GridPane();
        panel.setHgap(8);
        panel.setVgap(8);
        panel.setPadding(new Insets(12));
        panel.add(new Label("Buscar:"), 0, 0);
        panel.add(campoBuscar, 1, 0);
        panel.add(new Label("Reemplazar por:"), 0, 1);
        panel.add(campoReemplazar, 1, 1);
        dialogo.getDialogPane().setContent(panel);

        ButtonType btnBuscarSiguiente = new ButtonType("Buscar siguiente", ButtonBar.ButtonData.OTHER);
        ButtonType btnReemplazarTodo = new ButtonType("Reemplazar todo", ButtonBar.ButtonData.APPLY);
        ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(btnBuscarSiguiente, btnReemplazarTodo, btnCerrar);

        // "Buscar siguiente" y "Reemplazar todo" no deben cerrar el dialogo,
        // para poder repetir la busqueda o el reemplazo varias veces.
        Button botonBuscar = (Button) dialogo.getDialogPane().lookupButton(btnBuscarSiguiente);
        botonBuscar.addEventFilter(ActionEvent.ACTION, evento -> {
            String texto = campoBuscar.getText();
            boolean encontrado = editor.buscarSiguiente(texto);
            if (!encontrado) {
                escribirAdvertencia("No se encontraron coincidencias de \"" + texto + "\".");
            }
            evento.consume();
        });

        Button botonReemplazar = (Button) dialogo.getDialogPane().lookupButton(btnReemplazarTodo);
        botonReemplazar.addEventFilter(ActionEvent.ACTION, evento -> {
            editor.buscarYReemplazar(campoBuscar.getText(), campoReemplazar.getText());
            escribirInfo("Reemplazo de \"" + campoBuscar.getText() + "\" realizado en "
                    + editor.getArchivoUI().getNombre() + ".");
            evento.consume();
        });

        dialogo.show();
    }

    // ======================================================================
    // INTEGRACION CON EL ARBOL DE TRABAJO (ArbolController.EscuchaArbol)
    // ======================================================================

    @Override
    public void archivoAbierto(File archivo) {
        abrirArchivoEnEditor(archivo);
    }

    /**
     * Abre un archivo en el editor: si ya existe una pestana para ese
     * archivo, simplemente la selecciona; en caso contrario crea una
     * pestana nueva.
     *
     * @param archivo archivo de disco a abrir
     */
    private void abrirArchivoEnEditor(File archivo) {
        for (Tab tab : panelPestanas.getTabs()) {
            EditorController existente = EditorController.desdeTab(tab);
            if (existente != null
                    && existente.getArchivoUI().getArchivo().getAbsolutePath().equals(archivo.getAbsolutePath())) {
                panelPestanas.getSelectionModel().select(tab);
                existente.enfocar();
                return;
            }
        }
        try {
            EditorController.crearPestana(archivo, panelPestanas, this);
            escribirInfo("Archivo abierto: " + archivo.getAbsolutePath());
        } catch (IOException ex) {
            escribirError("No se pudo abrir el archivo: " + ex.getMessage());
            Notificaciones.mostrarError("Abrir archivo", ex.getMessage());
        }
    }

    // ======================================================================
    // INTEGRACION CON EL EDITOR (EditorController.EscuchaEditor)
    // ======================================================================

    @Override
    public void estadoCambiado(EditorController editor) {
        // Solo se refresca la barra de estado si el editor que cambio es
        // el que esta actualmente visible; el titulo de su propia pestana
        // ya lo actualiza el propio EditorController.
        if (editor != null && editor.getTab() == panelPestanas.getSelectionModel().getSelectedItem()) {
            actualizarBarraEstado(editor);
        }
    }

    /**
     * Actualiza la barra de estado (ruta, cursor, lenguaje, modificado) y
     * el titulo de la ventana en funcion de la pestana de editor activa.
     *
     * @param editor pestana activa, o {@code null} si no hay ninguna abierta
     */
    private void actualizarBarraEstado(EditorController editor) {
        if (editor == null) {
            lblRutaActiva.setText("Sin archivo abierto");
            lblPosicionCursor.setText("Linea 1, Columna 1");
            lblLenguajeDetectado.setText("Lenguaje: Desconocido");
            lblEstadoModificado.setText("");
            actualizarTituloVentana(null);
            return;
        }
        ArchivoUI archivoUI = editor.getArchivoUI();
        int[] lineaYColumna = editor.getLineaYColumna();

        lblRutaActiva.setText(archivoUI.getRutaAbsoluta());
        lblPosicionCursor.setText("Linea " + lineaYColumna[0] + ", Columna " + lineaYColumna[1]);
        lblLenguajeDetectado.setText("Lenguaje: " + archivoUI.getLenguaje().getNombreVisible());
        lblEstadoModificado.setText(editor.isModificado() ? "Modificado" : "");
        actualizarTituloVentana(archivoUI);
    }

    private void actualizarTituloVentana(ArchivoUI archivoUI) {
        Stage ventana = obtenerVentana();
        if (ventana == null) {
            return;
        }
        if (archivoUI == null) {
            ventana.setTitle("Compilador - Proyecto 1");
        } else {
            ventana.setTitle("Compilador - Proyecto 1 - " + archivoUI.getNombreParaMostrar());
        }
    }

    /** @return el controlador de la pestana de editor actualmente seleccionada, o {@code null} */
    private EditorController obtenerEditorActivo() {
        return EditorController.desdeTab(panelPestanas.getSelectionModel().getSelectedItem());
    }

    // ======================================================================
    // CONSOLA DE SALIDA
    // ======================================================================

    /** Escribe un mensaje informativo con marca de hora en la consola. */
    @Override
    public void mensajeInfo(String mensaje) {
        escribirInfo(mensaje);
    }

    /** Escribe un mensaje de error con marca de hora en la consola. */
    @Override
    public void mensajeError(String mensaje) {
        escribirError(mensaje);
    }

    /** Escribe un mensaje informativo con marca de hora en la consola. */
    public void escribirInfo(String mensaje) {
        agregarLineaConsola("INFO", mensaje);
    }

    /** Escribe un mensaje de error con marca de hora en la consola. */
    public void escribirError(String mensaje) {
        agregarLineaConsola("ERROR", mensaje);
    }

    /** Escribe un mensaje de advertencia con marca de hora en la consola. */
    public void escribirAdvertencia(String mensaje) {
        agregarLineaConsola("ADVERTENCIA", mensaje);
    }

    /** Limpia todo el contenido de la consola. */
    public void limpiar() {
        if (consolaSalida != null) {
            consolaSalida.clear();
        }
    }

    private void agregarLineaConsola(String etiqueta, String mensaje) {
        if (consolaSalida == null) {
            return;
        }
        String hora = LocalTime.now().format(FORMATO_HORA);
        consolaSalida.appendText("[" + hora + "] [" + etiqueta + "] " + mensaje + System.lineSeparator());
    }

    // ======================================================================
    // UTILIDADES INTERNAS
    // ======================================================================

    private Stage obtenerVentana() {
        if (raizPrincipal == null || raizPrincipal.getScene() == null) {
            return null;
        }
        return (Stage) raizPrincipal.getScene().getWindow();
    }

    /**
     * Calcula la carpeta inicial mas util para los dialogos de
     * abrir/guardar/nuevo: la raiz del proyecto abierto si existe, o la
     * carpeta personal del usuario en caso contrario.
     *
     * @return una carpeta existente para usar como punto de partida
     */
    private File carpetaInicialParaDialogos() {
        if (proyectoActual.hayProyectoAbierto()) {
            return proyectoActual.getCarpetaRaiz();
        }
        return new File(System.getProperty("user.home"));
    }
}
