package com.proyecto1.ui.controller;

import com.proyecto1.ui.MainApp;
import com.proyecto1.ui.util.Notificaciones;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Controlador de la ventana principal del IDE.
 * <p>
 * En esta fase (construccion del frontend) este controlador:
 * </p>
 * <ul>
 *   <li>Gestiona el menu Ver (mostrar/ocultar arbol, consola, panel de
 *       errores) y el cambio de tema claro/oscuro.</li>
 *   <li>Gestiona el menu Ayuda (Acerca de, Manual de usuario).</li>
 *   <li>Muestra el mensaje placeholder para las acciones del menu
 *       Ejecutar (Analizar, Compilar, Ejecutar ultimo analisis), ya que
 *       la conexion con el backend del compilador se hara en una fase
 *       posterior.</li>
 * </ul>
 * <p>
 * <b>PENDIENTE (ver PROGRESO_FRONTEND.md, Fase 5):</b> las acciones de
 * archivo (nuevo, abrir, guardar, cerrar pestana), edicion (deshacer,
 * rehacer, cortar, copiar, pegar, buscar/reemplazar) y la integracion
 * completa con {@code ArbolController} y {@code EditorController} se
 * implementaran cuando esas dos clases esten listas (Fases 3 y 4).
 * Por ahora esos metodos solo escriben un aviso en la consola para que
 * la aplicacion sea ejecutable y probable desde ya.
 * </p>
 *
 * @author Proyecto1
 */
public class MainController {

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

    /** Recuerda si el tema activo es el oscuro, para el boton "alternar tema". */
    private boolean temaOscuroActivo = false;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Metodo de inicializacion invocado automaticamente por JavaFX luego
     * de inyectar todos los campos {@code @FXML}.
     */
    @FXML
    private void initialize() {
        escribirInfo("IDE iniciado. Arbol y editor listos para conectarse (Fases 3 y 4).");
        // NOTA (Fase 3 / Fase 4 / Fase 5): aqui se instanciaran y conectaran
        // ArbolController y EditorController, por ejemplo:
        //   this.arbolController = new ArbolController(arbolTrabajo, this::abrirArchivoEnEditor);
        //   this.editorController = new EditorController(panelPestanas, this::actualizarBarraEstado);
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
        if (ventana != null) {
            ventana.close();
        }
    }

    // ======================================================================
    // MENU ARCHIVO / EDITAR (PENDIENTES - ver PROGRESO_FRONTEND.md Fase 5)
    // ======================================================================
    // Estos metodos se completaran cuando ArbolController (Fase 3) y
    // EditorController (Fase 4) esten listos, ya que necesitan pedirles la
    // pestana/archivo activo. Por ahora dejan constancia en la consola.

    @FXML
    private void accionNuevoArchivo() {
        escribirInfo("[PENDIENTE Fase 5] Nuevo archivo.");
    }

    @FXML
    private void accionNuevoProyecto() {
        escribirInfo("[PENDIENTE Fase 5] Nuevo proyecto/carpeta.");
    }

    @FXML
    private void accionAbrirArchivo() {
        escribirInfo("[PENDIENTE Fase 5] Abrir archivo.");
    }

    @FXML
    private void accionAbrirCarpeta() {
        escribirInfo("[PENDIENTE Fase 3/5] Abrir carpeta (requiere ArbolController).");
    }

    @FXML
    private void accionGuardar() {
        escribirInfo("[PENDIENTE Fase 4/5] Guardar (requiere EditorController).");
    }

    @FXML
    private void accionGuardarComo() {
        escribirInfo("[PENDIENTE Fase 4/5] Guardar como (requiere EditorController).");
    }

    @FXML
    private void accionCerrarPestanaActual() {
        escribirInfo("[PENDIENTE Fase 4/5] Cerrar pestana actual (requiere EditorController).");
    }

    @FXML
    private void accionDeshacer() {
        escribirInfo("[PENDIENTE Fase 4/5] Deshacer.");
    }

    @FXML
    private void accionRehacer() {
        escribirInfo("[PENDIENTE Fase 4/5] Rehacer.");
    }

    @FXML
    private void accionCortar() {
        escribirInfo("[PENDIENTE Fase 4/5] Cortar.");
    }

    @FXML
    private void accionCopiar() {
        escribirInfo("[PENDIENTE Fase 4/5] Copiar.");
    }

    @FXML
    private void accionPegar() {
        escribirInfo("[PENDIENTE Fase 4/5] Pegar.");
    }

    @FXML
    private void accionSeleccionarTodo() {
        escribirInfo("[PENDIENTE Fase 4/5] Seleccionar todo.");
    }

    @FXML
    private void accionBuscarReemplazar() {
        escribirInfo("[PENDIENTE Fase 5] Buscar / Reemplazar.");
    }

    // ======================================================================
    // CONSOLA DE SALIDA
    // ======================================================================
    // NOTA: en la Fase 5 estos metodos se moveran o se delegaran a una
    // pequena clase ConsolaController si se requiere colorear el texto con
    // TextFlow; por ahora se deja en TextArea de solo lectura, tal como
    // permiten los requisitos ("si se usa TextArea, solo texto plano").

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
}
