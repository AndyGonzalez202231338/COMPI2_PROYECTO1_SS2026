package com.proyecto1.ui.resaltado;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orquesta el coloreado sintactico en tiempo real de UNA pestana del
 * editor: escucha los cambios de texto del {@link TextArea} (con
 * debounce), lanza {@link HiloResaltado} en un hilo de fondo y aplica el
 * resultado a un {@link TextFlow} superpuesto que es lo que el usuario
 * realmente ve (ver la explicacion de la "capa transparente" dada antes
 * de esta fase).
 * <p>
 * Esta clase se instancia una vez por pestana, desde
 * {@code EditorController} (Fase C), y vive mientras la pestana este
 * abierta.
 * </p>
 *
 * @author Proyecto1
 */
public class ResaltadorSintaxis {

    /** Tiempo de espera tras la ultima pulsacion antes de re-tokenizar. */
    private static final Duration RETRASO_DEBOUNCE = Duration.millis(250);

    /**
     * Pool de hilos compartido por todas las pestanas para no crear un
     * hilo nuevo en cada re-tokenizacion. Los hilos son "daemon" para no
     * impedir el cierre de la aplicacion.
     */
    private static final ExecutorService POOL_HILOS = crearPool();

    private static ExecutorService crearPool() {
        ThreadFactory fabrica = new ThreadFactory() {
            private final AtomicInteger contador = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable tarea) {
                Thread hilo = new Thread(tarea, "hilo-resaltado-" + contador.getAndIncrement());
                hilo.setDaemon(true);
                return hilo;
            }
        };
        return Executors.newCachedThreadPool(fabrica);
    }

    private final TextArea areaCodigo;
    private final TextFlow areaResaltado;
    private final PauseTransition pausaDebounce;

    /** Ultima tarea de tokenizacion lanzada, para poder cancelarla si llega texto nuevo antes de que termine. */
    private Task<List<TramoColoreado>> tareaActual;

    /**
     * Crea el resaltador y lo deja escuchando los cambios de
     * {@code areaCodigo}. Dispara un primer resaltado inmediatamente
     * (util cuando se abre un archivo con contenido existente).
     *
     * @param areaCodigo    area de texto real donde el usuario escribe
     * @param areaResaltado TextFlow de solo lectura, superpuesto visualmente
     *                      a {@code areaCodigo}, donde se pintan los colores
     */
    public ResaltadorSintaxis(TextArea areaCodigo, TextFlow areaResaltado) {
        this.areaCodigo = areaCodigo;
        this.areaResaltado = areaResaltado;

        this.pausaDebounce = new PauseTransition(RETRASO_DEBOUNCE);
        this.pausaDebounce.setOnFinished(evento -> lanzarResaltado());

        areaCodigo.textProperty().addListener((obs, textoViejo, textoNuevo) -> solicitarResaltado());

        // El TextFlow no tiene scroll propio: se traslada para simular el
        // scroll del TextArea real (misma tecnica que el gutter de
        // numeros de linea de la Fase 4).
        areaCodigo.scrollTopProperty().addListener(
                (obs, viejo, nuevo) -> areaResaltado.setTranslateY(-nuevo.doubleValue()));
        areaCodigo.scrollLeftProperty().addListener(
                (obs, viejo, nuevo) -> areaResaltado.setTranslateX(-nuevo.doubleValue()));

        solicitarResaltado();
    }

    /**
     * Reinicia el temporizador de debounce; se llama en cada pulsacion de
     * tecla (via el listener de texto) para que solo se re-tokenice
     * cuando el usuario deja de escribir por {@link #RETRASO_DEBOUNCE}.
     */
    private void solicitarResaltado() {
        pausaDebounce.stop();
        pausaDebounce.playFromStart();
    }

    /** Cancela la tokenizacion en curso (si la hay) y lanza una nueva en segundo plano. */
    private void lanzarResaltado() {
        if (tareaActual != null && tareaActual.isRunning()) {
            tareaActual.cancel();
        }

        String textoActual = areaCodigo.getText();
        HiloResaltado nuevaTarea = new HiloResaltado(textoActual);
        nuevaTarea.setOnSucceeded(evento -> aplicarTramos(textoActual, nuevaTarea.getValue()));
        // Si el lexer llegara a fallar por algo inesperado, no se rompe la
        // edicion: simplemente no se actualiza el color esta vez.
        nuevaTarea.setOnFailed(evento -> { });

        this.tareaActual = nuevaTarea;
        POOL_HILOS.submit(nuevaTarea);
    }

    /**
     * Aplica los tramos calculados al {@link TextFlow}, en el hilo de
     * JavaFX (los manejadores {@code setOnSucceeded} de un {@link Task}
     * siempre se ejecutan ahi).
     *
     * @param textoTokenizado texto que se tokenizo (para detectar resultados obsoletos)
     * @param tramos          tramos calculados por {@link HiloResaltado}
     */
    private void aplicarTramos(String textoTokenizado, List<TramoColoreado> tramos) {
        // Si el usuario siguio escribiendo mientras la tarea corria, el
        // texto actual ya no coincide con el tokenizado: se descarta este
        // resultado obsoleto (el siguiente debounce ya esta en camino).
        if (!textoTokenizado.equals(areaCodigo.getText())) {
            return;
        }

        List<Text> nodos = new ArrayList<>();
        int posicion = 0;
        for (TramoColoreado tramo : tramos) {
            if (tramo.getInicio() < posicion) {
                // Se superpone con un tramo ya agregado (caso raro, ver
                // nota en HiloResaltado sobre errores lexicos); se ignora
                // para no duplicar texto.
                continue;
            }
            if (tramo.getInicio() > posicion) {
                nodos.add(crearNodoTexto(textoTokenizado.substring(posicion, tramo.getInicio()), null));
            }
            int fin = Math.min(tramo.getFin(), textoTokenizado.length());
            nodos.add(crearNodoTexto(textoTokenizado.substring(tramo.getInicio(), fin), tramo.getClaseCss()));
            posicion = fin;
        }
        if (posicion < textoTokenizado.length()) {
            nodos.add(crearNodoTexto(textoTokenizado.substring(posicion), null));
        }

        areaResaltado.getChildren().setAll(nodos);
    }

    private Text crearNodoTexto(String contenido, String claseCss) {
        Text texto = new Text(contenido);
        texto.getStyleClass().add("token-base");
        if (claseCss != null) {
            texto.getStyleClass().add(claseCss);
        }
        return texto;
    }

    /**
     * Detiene el debounce y cancela cualquier tokenizacion pendiente.
     * Debe llamarse cuando se cierra la pestana correspondiente, para no
     * dejar tareas de fondo trabajando sobre un editor que ya no existe.
     */
    public void detener() {
        pausaDebounce.stop();
        if (tareaActual != null) {
            tareaActual.cancel();
        }
    }
}