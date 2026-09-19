package com.proyecto1.ui.controller;

import com.proyecto1.ui.modelo.ArchivoUI;
import com.proyecto1.ui.servicio.GestorArchivos;
import com.proyecto1.ui.util.Notificaciones;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Controlador principal del IDE. Coordina arbol, editor, consola y menus. */
public class MainController implements ArbolController.EscuchaArbol, EditorController.EscuchaEditor {

    @FXML private BorderPane raizPrincipal;
    @FXML private MenuBar barraMenu;
    @FXML private ToolBar barraHerramientas;

    @FXML private CheckMenuItem miMostrarArbol;
    @FXML private CheckMenuItem miMostrarConsola;
    @FXML private CheckMenuItem miMostrarPanelErrores;

    @FXML private TitledPane panelArbol;
    @FXML private TreeView<Object> arbolTrabajo;
    @FXML private VBox panelArbolVacio;

    @FXML private TabPane panelPestanas;
    @FXML private VBox panelBienvenida;

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

    private ArbolController arbolController;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final FileChooser.ExtensionFilter FILTRO_LENGUAJES =
            new FileChooser.ExtensionFilter("Archivos soportados (*.y, *.z, *.pig)", "*.y", "*.z", "*.pig");
    private static final FileChooser.ExtensionFilter FILTRO_TODOS =
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*");

    @FXML
    private void initialize() {
        arbolController = new ArbolController(arbolTrabajo, this);

        // Al cambiar de pestana, actualizar barra de estado y dar foco
        // al editor de la pestana seleccionada. EditorCodigo ya gestiona
        // su propio caret/seleccion internamente; solo hay que pedirle
        // el foco para que empiece a recibir teclado.
        panelPestanas.getSelectionModel().selectedItemProperty()
                .addListener((obs, vieja, nueva) -> {
                    actualizarBarraEstado(EditorController.desdeTab(nueva));
                    EditorController ed = EditorController.desdeTab(nueva);
                    if (ed != null) {
                        Platform.runLater(ed::enfocar);
                    }
                });

        // El overlay de bienvenida se ve cuando no hay pestanas abiertas.
        panelBienvenida.visibleProperty().bind(Bindings.isEmpty(panelPestanas.getTabs()));
        panelBienvenida.managedProperty().bind(Bindings.isEmpty(panelPestanas.getTabs()));

        // El overlay del arbol se ve cuando no hay NINGUN proyecto abierto.
        panelArbolVacio.visibleProperty().bind(Bindings.isEmpty(arbolController.getProyectos()));
        panelArbolVacio.managedProperty().bind(Bindings.isEmpty(arbolController.getProyectos()));

        configurarMenuWorkspace();

        actualizarBarraEstado(null);
        escribirInfo("IDE iniciado. Cree un proyecto nuevo o abra una carpeta para comenzar.");
    }

    /**
     * Menu contextual del arbol cuando se hace clic derecho sobre el FONDO.
     * Las celdas tienen su propio menu contextual definido en ArbolController.
     */
    private void configurarMenuWorkspace() {
        MenuItem itemNuevoProyecto = new MenuItem("Nuevo proyecto...");
        itemNuevoProyecto.setOnAction(e -> accionNuevoProyecto());

        MenuItem itemAbrirCarpeta = new MenuItem("Abrir carpeta...");
        itemAbrirCarpeta.setOnAction(e -> accionAbrirCarpeta());

        ContextMenu menu = new ContextMenu(
                itemNuevoProyecto,
                new SeparatorMenuItem(),
                itemAbrirCarpeta);

        arbolTrabajo.setContextMenu(menu);
    }

    // ==================== MENU VER ====================

    @FXML private void accionAlternarArbol() {
        boolean v = miMostrarArbol.isSelected();
        panelArbol.setVisible(v); panelArbol.setManaged(v);
    }
    @FXML private void accionAlternarConsola() {
        boolean v = miMostrarConsola.isSelected();
        contenedorConsola.setVisible(v); contenedorConsola.setManaged(v);
    }
    @FXML private void accionAlternarPanelErrores() {
        boolean v = miMostrarPanelErrores.isSelected();
        panelErrores.setVisible(v); panelErrores.setManaged(v);
    }

    // ==================== MENU EJECUTAR (placeholders) ====================

    @FXML private void accionAnalizarArchivoActual() {
        escribirAdvertencia("Analizar: pendiente de conexion con el backend.");
        Notificaciones.mostrarFuncionPendiente(obtenerVentana());
    }
    @FXML private void accionCompilarProyecto() {
        escribirAdvertencia("Compilar: pendiente de conexion con el backend.");
        Notificaciones.mostrarFuncionPendiente(obtenerVentana());
    }
    @FXML private void accionEjecutarUltimoAnalisis() {
        escribirAdvertencia("Ejecutar: pendiente de conexion con el backend.");
        Notificaciones.mostrarFuncionPendiente(obtenerVentana());
    }

    // ==================== MENU AYUDA ====================

    @FXML private void accionAcercaDe() {
        Notificaciones.mostrarInformacion("Acerca de",
                "Compilador - Proyecto 1\n"
                        + "IDE para los lenguajes Y?, Zetariano y PigLatin.\n"
                        + "Proyecto de Compiladores 2.");
    }
    @FXML private void accionManualUsuario() {
        Notificaciones.mostrarInformacion("Manual de usuario",
                "Manual pendiente de redaccion.");
    }

    @FXML private void accionSalir() {
        Stage v = obtenerVentana();
        if (v == null) return;
        if (cerrarTodasLasPestanas()) v.close();
    }

    // ==================== MENU ARCHIVO ====================

    @FXML private void accionNuevoArchivo() {
        FileChooser d = new FileChooser();
        d.setTitle("Nuevo archivo");
        d.setInitialDirectory(carpetaInicial());
        d.getExtensionFilters().setAll(FILTRO_LENGUAJES, FILTRO_TODOS);
        d.setInitialFileName("nuevo.y");
        File archivo = d.showSaveDialog(obtenerVentana());
        if (archivo == null) return;
        try {
            GestorArchivos.crearArchivo(archivo, "");
            escribirInfo("Archivo creado: " + archivo.getAbsolutePath());
            arbolController.refrescarTodo();
            abrirArchivoEnEditor(archivo);
        } catch (IOException ex) {
            escribirError("No se pudo crear: " + ex.getMessage());
            Notificaciones.mostrarError("Nuevo archivo", ex.getMessage());
        }
    }

    @FXML
    private void accionNuevoProyecto() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Elegir ubicacion para el nuevo proyecto");
        dc.setInitialDirectory(carpetaInicial());
        File padre = dc.showDialog(obtenerVentana());
        if (padre == null) return;

        Optional<String> nombre = Notificaciones.pedirTexto(
                "Nuevo proyecto",
                "Nombre de la carpeta del nuevo proyecto:",
                "proyecto_nuevo");
        nombre.ifPresent(n -> {
            File nueva = new File(padre, n);
            try {
                GestorArchivos.crearCarpeta(nueva);
                escribirInfo("Proyecto creado: " + nueva.getAbsolutePath());
                agregarProyecto(nueva);
            } catch (IOException ex) {
                escribirError("No se pudo crear: " + ex.getMessage());
                Notificaciones.mostrarError("Nuevo proyecto", ex.getMessage());
            }
        });
    }

    @FXML private void accionAbrirArchivo() {
        FileChooser d = new FileChooser();
        d.setTitle("Abrir archivo");
        d.setInitialDirectory(carpetaInicial());
        d.getExtensionFilters().setAll(FILTRO_LENGUAJES, FILTRO_TODOS);
        File archivo = d.showOpenDialog(obtenerVentana());
        if (archivo != null) abrirArchivoEnEditor(archivo);
    }

    @FXML
    private void accionAbrirCarpeta() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Abrir carpeta de proyecto");
        dc.setInitialDirectory(carpetaInicial());
        File carpeta = dc.showDialog(obtenerVentana());
        if (carpeta != null) agregarProyecto(carpeta);
    }

    /** Agrega una carpeta como proyecto al workspace (no reemplaza los existentes). */
    private void agregarProyecto(File carpeta) {
        arbolController.agregarProyecto(carpeta);
    }

    @FXML private void accionGuardar() {
        EditorController ed = obtenerEditorActivo();
        if (ed == null) { escribirAdvertencia("No hay archivo abierto."); return; }
        try { ed.guardar(); }
        catch (IOException ex) {
            escribirError("No se pudo guardar: " + ex.getMessage());
            Notificaciones.mostrarError("Guardar", ex.getMessage());
        }
    }

    @FXML private void accionGuardarComo() {
        EditorController ed = obtenerEditorActivo();
        if (ed == null) { escribirAdvertencia("No hay archivo abierto."); return; }
        FileChooser d = new FileChooser();
        d.setTitle("Guardar como");
        d.setInitialDirectory(carpetaInicial());
        d.setInitialFileName(ed.getArchivoUI().getNombre());
        d.getExtensionFilters().setAll(FILTRO_LENGUAJES, FILTRO_TODOS);
        File destino = d.showSaveDialog(obtenerVentana());
        if (destino == null) return;
        try { ed.guardarComo(destino); arbolController.refrescarTodo(); }
        catch (IOException ex) {
            escribirError("No se pudo guardar: " + ex.getMessage());
            Notificaciones.mostrarError("Guardar como", ex.getMessage());
        }
    }

    @FXML private void accionCerrarPestanaActual() {
        cerrarPestana(panelPestanas.getSelectionModel().getSelectedItem());
    }

    private void cerrarPestana(Tab tab) {
        if (tab == null) return;
        Event e = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, e);
        if (!e.isConsumed()) panelPestanas.getTabs().remove(tab);
    }

    private boolean cerrarTodasLasPestanas() {
        List<Tab> copia = new ArrayList<>(panelPestanas.getTabs());
        for (Tab t : copia) {
            EditorController ed = EditorController.desdeTab(t);
            if (ed != null && ed.isModificado()) {
                Event e = new Event(t, t, Tab.TAB_CLOSE_REQUEST_EVENT);
                Event.fireEvent(t, e);
                if (e.isConsumed()) return false;
            }
        }
        return true;
    }

    // ==================== MENU EDITAR ====================

    @FXML private void accionDeshacer() { EditorController e = obtenerEditorActivo(); if (e != null) e.deshacer(); }
    @FXML private void accionRehacer()  { EditorController e = obtenerEditorActivo(); if (e != null) e.rehacer(); }
    @FXML private void accionCortar()   { EditorController e = obtenerEditorActivo(); if (e != null) e.cortar(); }
    @FXML private void accionCopiar()   { EditorController e = obtenerEditorActivo(); if (e != null) e.copiar(); }
    @FXML private void accionPegar()    { EditorController e = obtenerEditorActivo(); if (e != null) e.pegar(); }
    @FXML private void accionSeleccionarTodo() { EditorController e = obtenerEditorActivo(); if (e != null) e.seleccionarTodo(); }

    @FXML private void accionBuscarReemplazar() {
        EditorController editor = obtenerEditorActivo();
        if (editor == null) {
            escribirAdvertencia("Abra un archivo para buscar o reemplazar.");
            return;
        }
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Buscar / Reemplazar");
        dlg.initOwner(obtenerVentana());
        dlg.initModality(Modality.NONE);

        TextField cBuscar = new TextField(); cBuscar.setPromptText("Texto a buscar");
        TextField cReempl = new TextField(); cReempl.setPromptText("Texto de reemplazo");

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(12));
        gp.add(new Label("Buscar:"), 0, 0);          gp.add(cBuscar, 1, 0);
        gp.add(new Label("Reemplazar por:"), 0, 1);  gp.add(cReempl, 1, 1);
        dlg.getDialogPane().setContent(gp);

        ButtonType bSig  = new ButtonType("Buscar siguiente", ButtonBar.ButtonData.OTHER);
        ButtonType bTodo = new ButtonType("Reemplazar todo", ButtonBar.ButtonData.APPLY);
        ButtonType bCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().setAll(bSig, bTodo, bCerrar);

        Button btnSig = (Button) dlg.getDialogPane().lookupButton(bSig);
        btnSig.addEventFilter(ActionEvent.ACTION, ev -> {
            if (!editor.buscarSiguiente(cBuscar.getText()))
                escribirAdvertencia("Sin coincidencias de \"" + cBuscar.getText() + "\".");
            ev.consume();
        });
        Button btnTodo = (Button) dlg.getDialogPane().lookupButton(bTodo);
        btnTodo.addEventFilter(ActionEvent.ACTION, ev -> {
            editor.buscarYReemplazar(cBuscar.getText(), cReempl.getText());
            escribirInfo("Reemplazo realizado en " + editor.getArchivoUI().getNombre() + ".");
            ev.consume();
        });
        dlg.show();
    }

    // ==================== EscuchaArbol ====================

    @Override public void archivoAbierto(File archivo) { abrirArchivoEnEditor(archivo); }

    private void abrirArchivoEnEditor(File archivo) {
        for (Tab t : panelPestanas.getTabs()) {
            EditorController ed = EditorController.desdeTab(t);
            if (ed != null && ed.getArchivoUI().getArchivo().getAbsolutePath()
                    .equals(archivo.getAbsolutePath())) {
                panelPestanas.getSelectionModel().select(t);
                ed.enfocar();
                return;
            }
        }
        try {
            EditorController.crearPestana(archivo, panelPestanas, this);
            escribirInfo("Archivo abierto: " + archivo.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();   // ← AÑADIR
            escribirError("No se pudo abrir: " + ex.getMessage());
            Notificaciones.mostrarError("Abrir archivo", ex.getMessage());
        }
    }

    // ==================== EscuchaEditor ====================

    @Override public void estadoCambiado(EditorController editor) {
        if (editor != null && editor.getTab() == panelPestanas.getSelectionModel().getSelectedItem())
            actualizarBarraEstado(editor);
    }

    private void actualizarBarraEstado(EditorController editor) {
        if (editor == null) {
            lblRutaActiva.setText("Sin archivo abierto");
            lblPosicionCursor.setText("Linea 1, Columna 1");
            lblLenguajeDetectado.setText("Lenguaje: Desconocido");
            lblEstadoModificado.setText("");
            actualizarTituloVentana(null);
            return;
        }
        ArchivoUI a = editor.getArchivoUI();
        int[] lc = editor.getLineaYColumna();
        lblRutaActiva.setText(a.getRutaAbsoluta());
        lblPosicionCursor.setText("Linea " + lc[0] + ", Columna " + lc[1]);
        lblLenguajeDetectado.setText("Lenguaje: " + a.getLenguaje().getNombreVisible());
        lblEstadoModificado.setText(editor.isModificado() ? "Modificado" : "");
        actualizarTituloVentana(a);
    }

    private void actualizarTituloVentana(ArchivoUI a) {
        Stage v = obtenerVentana();
        if (v == null) return;
        v.setTitle(a == null ? "Compilador - Proyecto 1"
                : "Compilador - Proyecto 1 - " + a.getNombreParaMostrar());
    }

    private EditorController obtenerEditorActivo() {
        return EditorController.desdeTab(panelPestanas.getSelectionModel().getSelectedItem());
    }

    // ==================== Consola ====================

    @Override public void mensajeInfo(String m)  { escribirInfo(m); }
    @Override public void mensajeError(String m) { escribirError(m); }

    public void escribirInfo(String m)        { agregarLinea("INFO", m); }
    public void escribirError(String m)       { agregarLinea("ERROR", m); }
    public void escribirAdvertencia(String m) { agregarLinea("ADVERTENCIA", m); }

    public void limpiar() { if (consolaSalida != null) consolaSalida.clear(); }

    private void agregarLinea(String etiqueta, String mensaje) {
        if (consolaSalida == null) return;
        String hora = LocalTime.now().format(FORMATO_HORA);
        consolaSalida.appendText("[" + hora + "] [" + etiqueta + "] " + mensaje + System.lineSeparator());
    }

    // ==================== Utilidades ====================

    private Stage obtenerVentana() {
        if (raizPrincipal == null || raizPrincipal.getScene() == null) return null;
        return (Stage) raizPrincipal.getScene().getWindow();
    }

    private File carpetaInicial() {
        File proy = arbolController.getProyectoSeleccionado();
        if (proy != null) return proy;
        return new File(System.getProperty("user.home"));
    }
}