package com.proyecto1.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Clase de arranque de la interfaz grafica del IDE del compilador.
 * <p>
 * Esta fase construye unicamente el frontend (editor, arbol de trabajo,
 * consola, menus) SIN conectarse todavia al backend del compilador
 * ({@code com.proyecto1.semantico}, etc.). La conexion se realizara en
 * una fase posterior, reemplazando los metodos placeholder del
 * {@code MainController} relacionados con "Analizar" y "Compilar".
 * </p>
 *
 * @author Proyecto1
 */
public class MainApp extends Application {

    /** Nombre de la hoja de estilos del tema claro (tema por defecto). */
    public static final String TEMA_CLARO = "claro.css";

    /** Nombre de la hoja de estilos del tema oscuro. */
    public static final String TEMA_OSCURO = "oscuro.css";

    @Override
    public void start(Stage escenarioPrincipal) throws IOException {
        URL urlFxml = obtenerRecurso("main.fxml");
        FXMLLoader cargador = new FXMLLoader(urlFxml);
        Parent raiz = cargador.load();

        Scene escena = new Scene(raiz, 1200, 800);
        escena.getStylesheets().add(obtenerRecurso(TEMA_CLARO).toExternalForm());

        escenarioPrincipal.setTitle("Compilador - Proyecto 1");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.setMinWidth(900);
        escenarioPrincipal.setMinHeight(600);
        escenarioPrincipal.show();
    }

    /**
     * Resuelve un recurso (FXML o CSS) ubicado junto a esta clase, dentro
     * del paquete {@code com.proyecto1.ui}.
     *
     * @param nombreArchivo nombre del archivo de recurso, por ejemplo "main.fxml"
     * @return la URL del recurso
     */
    private URL obtenerRecurso(String nombreArchivo) {
        return Objects.requireNonNull(
                MainApp.class.getResource(nombreArchivo),
                "No se encontro el recurso: " + nombreArchivo
                        + " (verifique que este en src/main/resources/com/proyecto1/ui/)");
    }

    /**
     * Punto de entrada de la aplicacion.
     *
     * @param args argumentos de linea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
