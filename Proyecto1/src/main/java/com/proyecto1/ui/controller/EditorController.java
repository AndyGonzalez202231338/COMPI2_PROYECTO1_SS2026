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
 * Controlador de UNA pestana del editor (editor.fxml). Cada archivo abierto
 * tiene su propia instancia, creada con crearPestana(...).
 */
public class EditorController {

    private static final int ESPACIOS_POR_TAB = 4;

    public interface EscuchaEditor {
        void mensajeInfo(String mensaje);
        void mensajeError(String mensaje);
        void estadoCambiado(EditorController editor);
    }

    @FXML private TextArea areaNumeros;
    @FXML private TextArea areaCodigo;

    private ArchivoUI archivoUI;
    private EscuchaEditor escucha;
    private Tab tabAsociada;
    private boolean modificado = false;
    private boolean cargandoContenido = false;

    // ---------- Fabrica ----------

    public static EditorController crearPestana(File archivo, TabPane tabPane, EscuchaEditor escucha)
            throws IOException {
        URL urlFxml = Objects.requireNonNull(
                EditorController.class.getResource("/com/proyecto1/ui/editor.fxml"),
                "No se encontro editor.fxml");
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

    public static EditorController desdeTab(Tab tab) {
        if (tab != null && tab.getUserData() instanceof EditorController) {
            return (EditorController) tab.getUserData();
        }
        return null;
    }

    @FXML
    private void initialize() {
        areaCodigo.textProperty().addListener((obs, viejo, nuevo) -> {
            actualizarNumerosDeLinea(nuevo);
            if (!cargandoContenido) marcarModificado(true);
        });
        areaCodigo.caretPositionProperty().addListener((obs, viejo, nuevo) -> notificarEstadoCambiado());
        areaCodigo.addEventFilter(KeyEvent.KEY_PRESSED, this::manejarTeclasEspeciales);
        areaCodigo.scrollTopProperty().addListener((obs, v, n) -> areaNumeros.setScrollTop(n.doubleValue()));
    }

    // ---------- Carga / guardado ----------

    private void cargarContenidoDesdeDisco() throws IOException {
        String contenido = GestorArchivos.abrirArchivo(archivoUI.getArchivo());
        cargandoContenido = true;
        areaCodigo.setText(contenido);
        areaCodigo.positionCaret(0);
        cargandoContenido = false;
        marcarModificado(false);
    }

    public void guardar() throws IOException {
        GestorArchivos.guardarArchivo(archivoUI.getArchivo(), areaCodigo.getText());
        marcarModificado(false);
        escucha.mensajeInfo("Archivo guardado: " + archivoUI.getNombre());
    }

    public void guardarComo(File nuevoArchivo) throws IOException {
        GestorArchivos.guardarArchivo(nuevoArchivo, areaCodigo.getText());
        this.archivoUI = new ArchivoUI(nuevoArchivo);
        marcarModificado(false);
        escucha.mensajeInfo("Archivo guardado como: " + nuevoArchivo.getName());
    }

    private void alIntentarCerrar(Event evento) {
        if (!modificado) return;
        Notificaciones.RespuestaGuardar respuesta =
                Notificaciones.preguntarGuardarCambios(archivoUI.getNombre());
        switch (respuesta) {
            case GUARDAR:
                try { guardar(); }
                catch (IOException ex) {
                    Notificaciones.mostrarError("Guardar", ex.getMessage());
                    evento.consume();
                }
                break;
            case CANCELAR:
                evento.consume();
                break;
            case DESCARTAR:
            default:
                break;
        }
    }

    // ---------- Numeracion de lineas ----------

    private void actualizarNumerosDeLinea(String texto) {
        int cantidad = texto.isEmpty() ? 1 : texto.split("\n", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= cantidad; i++) {
            sb.append(i);
            if (i < cantidad) sb.append("\n");
        }
        areaNumeros.setText(sb.toString());
        areaNumeros.setScrollTop(areaCodigo.getScrollTop());
    }

    // ---------- Auto-indentacion y Tab ----------

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

    private String obtenerIndentacionLineaActual() {
        String textoHastaCursor = areaCodigo.getText(0, areaCodigo.getCaretPosition());
        int inicio = textoHastaCursor.lastIndexOf('\n') + 1;
        String linea = textoHastaCursor.substring(inicio);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == ' ' || c == '\t') sb.append(c);
            else break;
        }
        return sb.toString();
    }

    // ---------- Estado ----------

    private void marcarModificado(boolean valor) {
        this.modificado = valor;
        if (tabAsociada != null) tabAsociada.setText(archivoUI.getNombreParaMostrar());
        notificarEstadoCambiado();
    }

    private void notificarEstadoCambiado() {
        if (escucha != null) escucha.estadoCambiado(this);
    }

    public ArchivoUI getArchivoUI() { return archivoUI; }
    public boolean isModificado() { return modificado; }
    public Tab getTab() { return tabAsociada; }
    public String getContenido() { return areaCodigo.getText(); }
    public void enfocar() { areaCodigo.requestFocus(); }

    public int[] getLineaYColumna() {
        String texto = areaCodigo.getText(0, areaCodigo.getCaretPosition());
        String[] lineas = texto.split("\n", -1);
        return new int[] { lineas.length, lineas[lineas.length - 1].length() + 1 };
    }

    // ---------- Delegados del menu Editar ----------

    public void deshacer() { areaCodigo.undo(); }
    public void rehacer()  { areaCodigo.redo(); }
    public void cortar()   { areaCodigo.cut(); }
    public void copiar()   { areaCodigo.copy(); }
    public void pegar()    { areaCodigo.paste(); }
    public void seleccionarTodo() { areaCodigo.selectAll(); }

    public void buscarYReemplazar(String buscar, String reemplazar) {
        if (buscar == null || buscar.isEmpty()) return;
        areaCodigo.setText(areaCodigo.getText().replace(buscar, reemplazar));
    }

    public boolean buscarSiguiente(String texto) {
        if (texto == null || texto.isEmpty()) return false;
        String contenido = areaCodigo.getText();
        int desde = areaCodigo.getCaretPosition();
        int idx = contenido.indexOf(texto, desde);
        if (idx < 0) idx = contenido.indexOf(texto, 0);
        if (idx < 0) return false;
        areaCodigo.selectRange(idx, idx + texto.length());
        areaCodigo.requestFocus();
        return true;
    }
}