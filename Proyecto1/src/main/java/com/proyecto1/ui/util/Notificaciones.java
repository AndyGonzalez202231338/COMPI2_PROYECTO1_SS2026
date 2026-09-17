package com.proyecto1.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Clase de utilidad para mostrar dialogos y alertas estandar de la
 * aplicacion (informacion, error, confirmacion, entrada de texto).
 * <p>
 * Centralizar estos dialogos aqui evita repetir codigo de construccion de
 * {@link Alert} en cada controlador y permite mantener un estilo
 * consistente en toda la aplicacion.
 * </p>
 *
 * @author Proyecto1
 */
public final class Notificaciones {

    private Notificaciones() {
        // Clase de utilidad: no debe instanciarse.
    }

    /**
     * Muestra un dialogo de informacion simple.
     *
     * @param titulo  titulo de la ventana del dialogo
     * @param mensaje mensaje a mostrar
     */
    public static void mostrarInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Muestra un dialogo de advertencia.
     *
     * @param titulo  titulo de la ventana del dialogo
     * @param mensaje mensaje a mostrar
     */
    public static void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Muestra un dialogo de error.
     *
     * @param titulo  titulo de la ventana del dialogo
     * @param mensaje mensaje a mostrar
     */
    public static void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Muestra un dialogo de confirmacion con botones "Aceptar" y
     * "Cancelar".
     *
     * @param titulo  titulo de la ventana del dialogo
     * @param mensaje pregunta a confirmar
     * @return {@code true} si el usuario acepto, {@code false} en caso
     *         contrario (incluye cerrar el dialogo)
     */
    public static boolean confirmar(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    /** Resultado posible al preguntar si se desean guardar cambios pendientes. */
    public enum RespuestaGuardar {
        GUARDAR,
        DESCARTAR,
        CANCELAR
    }

    /**
     * Pregunta al usuario que desea hacer con un archivo que tiene
     * cambios sin guardar (usado al cerrar pestanas o al salir).
     *
     * @param nombreArchivo nombre del archivo con cambios pendientes
     * @return la opcion elegida por el usuario
     */
    public static RespuestaGuardar preguntarGuardarCambios(String nombreArchivo) {
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle("Cambios sin guardar");
        alerta.setHeaderText("El archivo \"" + nombreArchivo + "\" tiene cambios sin guardar.");
        alerta.setContentText("¿Desea guardar los cambios antes de continuar?");

        ButtonType botonGuardar = new ButtonType("Guardar");
        ButtonType botonDescartar = new ButtonType("Descartar");
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());

        alerta.getButtonTypes().setAll(botonGuardar, botonDescartar, botonCancelar);

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isEmpty()) {
            return RespuestaGuardar.CANCELAR;
        }
        if (resultado.get() == botonGuardar) {
            return RespuestaGuardar.GUARDAR;
        }
        if (resultado.get() == botonDescartar) {
            return RespuestaGuardar.DESCARTAR;
        }
        return RespuestaGuardar.CANCELAR;
    }

    /**
     * Solicita al usuario un texto de entrada (usado para nombres de
     * archivos, carpetas o para renombrar elementos).
     *
     * @param titulo         titulo de la ventana del dialogo
     * @param mensaje        etiqueta que describe el dato solicitado
     * @param valorPorDefecto valor inicial sugerido en el campo de texto
     * @return el texto ingresado, o {@link Optional#empty()} si el usuario cancelo
     */
    public static Optional<String> pedirTexto(String titulo, String mensaje, String valorPorDefecto) {
        TextInputDialog dialogo = new TextInputDialog(valorPorDefecto);
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(null);
        dialogo.setContentText(mensaje);
        return dialogo.showAndWait();
    }

    /**
     * Muestra el mensaje estandar para funciones que aun no estan
     * conectadas al backend del compilador (Analizar, Compilar, Ejecutar).
     *
     * @param propietario ventana propietaria del dialogo (puede ser {@code null})
     */
    public static void mostrarFuncionPendiente(Stage propietario) {
        Alert alerta = new Alert(AlertType.INFORMATION);
        if (propietario != null) {
            alerta.initOwner(propietario);
        }
        alerta.setTitle("Funcion pendiente");
        alerta.setHeaderText(null);
        alerta.setContentText("Función pendiente de implementación. La conexión con el analizador se realizará en una fase posterior.");
        alerta.showAndWait();
    }
}
