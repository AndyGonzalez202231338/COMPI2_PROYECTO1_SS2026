package com.proyecto1.ui.controller;

import com.proyecto1.ui.modelo.ArchivoUI;
import com.proyecto1.ui.servicio.GestorArchivos;
import com.proyecto1.ui.util.Notificaciones;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Controlador de UNA pestana del editor de codigo.
 * <p>
 * Cada archivo abierto tiene su propia instancia de esta clase, creada
 * mediante el metodo de fabrica {@link #crearPestana(File, TabPane, EscuchaEditor)}
 * a partir de {@code editor.fxml}. Se encarga de:
 * </p>
 * <ul>
 *   <li>Mostrar el contenido del archivo en un {@link TextArea} plano
 *       (sin resaltado de sintaxis, que se agregara en una fase futura).</li>
 *   <li>Mantener una numeracion de lineas sincronizada mediante un
 *       segundo {@link TextArea} de solo lectura.</li>
 *   <li>Auto-indentar al presionar Enter y expandir Tab a 4 espacios.</li>
 *   <li>Detectar cambios para marcar la pestana como "modificada".</li>
 *   <li>Preguntar si se desea guardar al intentar cerrar con cambios
 *       pendientes.</li>
 * </ul>
 * <p>
 * Esta clase NO conoce al backend del compilador ni al arbol de trabajo:
 * solo reporta eventos hacia afuera mediante {@link EscuchaEditor}.
 * </p>
 *
 * @author Proyecto1
 */
public class EditorController {

    /** Cantidad de espacios que representa una tabulacion. */
    private static final int ESPACIOS_POR_TAB = 4;

    /**
     * Contrato que debe cumplir quien use este controlador para enterarse
     * de los eventos relevantes de una pestana de edicion.
     */
    public interface EscuchaEditor {
        /** Se invoca para reportar un mensaje informativo (para la consola). */
        void mensajeInfo(String mensaje);

        /** Se invoca para reportar un error (para la consola). */
        void mensajeError(String mensaje);

        /**
         * Se invoca cada vez que cambia algo relevante de esta pestana
         * (texto modificado/guardado, o movimiento del cursor), para que
         * quien escucha (normalmente {@code MainController}) actualice la
         * barra de estado y el titulo de la pestana si corresponde.
         */
        void estadoCambiado(EditorController editor);
    }

    @FXML private TextArea areaNumeros;
    @FXML private TextArea areaCodigo;

    private ArchivoUI archivoUI;
    private EscuchaEditor escucha;
    private Tab tabAsociada;
    private boolean modificado = false;
    private boolean cargandoContenido = false;

    // ======================================================================
    // FABRICA: crea una pestana nueva a partir de editor.fxml
    // ======================================================================

    /**
     * Crea una nueva pestana de edicion para el archivo indicado, la agrega
     * al {@link TabPane} recibido y la selecciona.
     *
     * @param archivo  archivo de disco a abrir
     * @param tabPane  panel de pestanas donde se insertara la nueva pestana
     * @param escucha  receptor de los eventos de esta pestana
     * @return el controlador de la pestana recien creada
     * @throws IOException si el archivo no se puede leer o el FXML no carga
     */
    public static EditorController crearPestana(File archivo, TabPane tabPane, EscuchaEditor escucha)
            throws IOException {
        URL urlFxml = Objects.requireNonNull(
                EditorController.class.getResource("/com/proyecto1/ui/editor.fxml"),
                "No se encontro editor.fxml en resources/com/proyecto1/ui/");
        FXMLLoader cargador = new FXMLLoader(urlFxml);
        Parent contenido = cargador.load();
        EditorController controlador = cargador.getController();

        controlador.escucha = escucha;
        controlador.archivoUI = new ArchivoUI(archivo);
        controlador.cargarContenidoDesdeDisco();

        Tab tab = new Tab(controlador.archivoUI.getNombre());
        tab.setContent(contenido);
        tab.setUserData(controlador);
        controlador.tabAsociada = tab;

        tab.setOnCloseRequest(evento -> controlador.alIntentarCerrar(evento));

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return controlador;
    }

    /**
     * Obtiene el {@code EditorController} asociado a una pestana, si la
     * pestana fue creada mediante {@link #crearPestana}.
     *
     * @param tab pestana del TabPane
     * @return el controlador asociado, o {@code null} si no corresponde
     */
    public static EditorController desdeTab(Tab tab) {
        if (tab != null && tab.getUserData() instanceof EditorController) {
            return (EditorController) tab.getUserData();
        }
        return null;
    }

    // ======================================================================
    // INICIALIZACION (llamada automaticamente por FXMLLoader)
    // ======================================================================

    @FXML
    private void initialize() {
        areaCodigo.textProperty().addListener((obs, viejo, nuevo) -> {
            actualizarNumerosDeLinea(nuevo);
            if (!cargandoContenido) {
                marcarModificado(true);
            }
        });

        areaCodigo.caretPositionProperty().addListener((obs, viejo, nuevo) -> notificarEstadoCambiado());

        // Tab = 4 espacios; Enter = mantener la indentacion de la linea anterior.
        areaCodigo.addEventFilter(KeyEvent.KEY_PRESSED, this::manejarTeclasEspeciales);
    }

    // ======================================================================
    // CARGA / GUARDADO
    // ======================================================================

    private void cargarContenidoDesdeDisco() throws IOException {
        String contenido = GestorArchivos.abrirArchivo(archivoUI.getArchivo());
        cargandoContenido = true;
        areaCodigo.setText(contenido);
        areaCodigo.positionCaret(0);
        cargandoContenido = false;
        marcarModificado(false);
    }

    /**
     * Guarda el contenido actual en el archivo original.
     *
     * @throws IOException si ocurre un error de escritura
     */
    public void guardar() throws IOException {
        GestorArchivos.guardarArchivo(archivoUI.getArchivo(), areaCodigo.getText());
        marcarModificado(false);
        escucha.mensajeInfo("Archivo guardado: " + archivoUI.getNombre());
    }

    /**
     * Guarda el contenido actual en un archivo nuevo (Guardar como) y hace
     * que esta pestana pase a representar ese nuevo archivo.
     *
     * @param nuevoArchivo archivo de destino
     * @throws IOException si ocurre un error de escritura
     */
    public void guardarComo(File nuevoArchivo) throws IOException {
        GestorArchivos.guardarArchivo(nuevoArchivo, areaCodigo.getText());
        this.archivoUI = new ArchivoUI(nuevoArchivo);
        marcarModificado(false);
        escucha.mensajeInfo("Archivo guardado como: " + nuevoArchivo.getName());
    }

    /**
     * Maneja el cierre de la pestana: si hay cambios sin guardar, pregunta
     * al usuario que desea hacer y cancela el cierre si corresponde.
     *
     * @param evento evento de cierre de la pestana (puede consumirse para cancelar)
     */
    private void alIntentarCerrar(Event evento) {
        if (!modificado) {
            return;
        }
        Notificaciones.RespuestaGuardar respuesta = Notificaciones.preguntarGuardarCambios(archivoUI.getNombre());
        switch (respuesta) {
            case GUARDAR:
                try {
                    guardar();
                } catch (IOException ex) {
                    Notificaciones.mostrarError("Guardar", ex.getMessage());
                    evento.consume();
                }
                break;
            case CANCELAR:
                evento.consume();
                break;
            case DESCARTAR:
            default:
                // No se hace nada: se permite cerrar sin guardar.
                break;
        }
    }

    // ======================================================================
    // NUMERACION DE LINEAS
    // ======================================================================

    private void actualizarNumerosDeLinea(String texto) {
        int cantidadLineas = texto.isEmpty() ? 1 : texto.split("\n", -1).length;
        StringBuilder numeros = new StringBuilder();
        for (int i = 1; i <= cantidadLineas; i++) {
            numeros.append(i);
            if (i < cantidadLineas) {
                numeros.append("\n");
            }
        }
        areaNumeros.setText(numeros.toString());
        // Mantener el scroll del gutter sincronizado con el del codigo.
        areaNumeros.setScrollTop(areaCodigo.getScrollTop());
    }

    // ======================================================================
    // AUTO-INDENTACION Y TAB = 4 ESPACIOS
    // ======================================================================

    private void manejarTeclasEspeciales(KeyEvent evento) {
        if (evento.getCode() == KeyCode.TAB) {
            evento.consume();
            areaCodigo.insertText(areaCodigo.getCaretPosition(), " ".repeat(ESPACIOS_POR_TAB));
            return;
        }
        if (evento.getCode() == KeyCode.ENTER) {
            evento.consume();
            String indentacion = obtenerIndentacionLineaActual();
            areaCodigo.insertText(areaCodigo.getCaretPosition(), "\n" + indentacion);
        }
    }

    /**
     * Calcula los espacios/tabs iniciales de la linea donde esta el cursor,
     * para repetirlos en la linea nueva al presionar Enter.
     *
     * @return la cadena de indentacion (solo espacios y tabs) a repetir
     */
    private String obtenerIndentacionLineaActual() {
        String textoHastaCursor = areaCodigo.getText(0, areaCodigo.getCaretPosition());
        int inicioLinea = textoHastaCursor.lastIndexOf('\n') + 1;
        String lineaActual = textoHastaCursor.substring(inicioLinea);

        StringBuilder indentacion = new StringBuilder();
        for (int i = 0; i < lineaActual.length(); i++) {
            char c = lineaActual.charAt(i);
            if (c == ' ' || c == '\t') {
                indentacion.append(c);
            } else {
                break;
            }
        }
        return indentacion.toString();
    }

    // ======================================================================
    // ESTADO (MODIFICADO / CURSOR) Y CONSULTAS PUBLICAS
    // ======================================================================

    private void marcarModificado(boolean valor) {
        this.modificado = valor;
        actualizarTituloPestana();
        notificarEstadoCambiado();
    }

    private void actualizarTituloPestana() {
        if (tabAsociada != null) {
            tabAsociada.setText(archivoUI.getNombreParaMostrar());
        }
    }

    private void notificarEstadoCambiado() {
        if (escucha != null) {
            escucha.estadoCambiado(this);
        }
    }

    /** @return el archivo (modelo de UI) que representa esta pestana */
    public ArchivoUI getArchivoUI() {
        return archivoUI;
    }

    /** @return {@code true} si hay cambios sin guardar en esta pestana */
    public boolean isModificado() {
        return modificado;
    }

    /** @return la pestana (Tab) asociada a este controlador */
    public Tab getTab() {
        return tabAsociada;
    }

    /** @return el contenido actual del area de codigo */
    public String getContenido() {
        return areaCodigo.getText();
    }

    /**
     * Calcula la linea y columna actuales del cursor (base 1), utiles
     * para la barra de estado.
     *
     * @return arreglo de dos posiciones: {@code [linea, columna]}
     */
    public int[] getLineaYColumna() {
        String textoHastaCursor = areaCodigo.getText(0, areaCodigo.getCaretPosition());
        String[] lineas = textoHastaCursor.split("\n", -1);
        int linea = lineas.length;
        int columna = lineas[lineas.length - 1].length() + 1;
        return new int[] { linea, columna };
    }

    /** Da el foco al area de codigo de esta pestana. */
    public void enfocar() {
        areaCodigo.requestFocus();
    }

    // -------- Delegados de edicion basica, usados por el menu "Editar" --------

    public void deshacer() {
        areaCodigo.undo();
    }

    public void rehacer() {
        areaCodigo.redo();
    }

    public void cortar() {
        areaCodigo.cut();
    }

    public void copiar() {
        areaCodigo.copy();
    }

    public void pegar() {
        areaCodigo.paste();
    }

    public void seleccionarTodo() {
        areaCodigo.selectAll();
    }

    /**
     * Reemplaza todas las apariciones de {@code buscar} por {@code reemplazar}
     * en el contenido (usado por el dialogo basico de Buscar/Reemplazar).
     *
     * @param buscar     texto a buscar (si esta vacio, no hace nada)
     * @param reemplazar texto de reemplazo
     */
    public void buscarYReemplazar(String buscar, String reemplazar) {
        if (buscar == null || buscar.isEmpty()) {
            return;
        }
        areaCodigo.setText(areaCodigo.getText().replace(buscar, reemplazar));
    }
}