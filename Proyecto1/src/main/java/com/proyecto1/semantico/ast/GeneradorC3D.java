package com.proyecto1.semantico.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicios compartidos que necesitará cualquier nodo al traducirse a C3D: pedir un
 * nombre de temporal nuevo (t1, t2, ...), pedir una etiqueta nueva (L1, L2, ... para
 * saltos de si/mientras/para), y la lista global de cuádruplas ya emitidas por todo el
 * árbol. Cada nodo la recibe como parámetro de generarC3D(generador) así ningún nodo
 * necesita guardar estado propio de "en qué temporal voy" (eso viviría acá, cuando se
 * implemente de verdad en la Fase 3).
 *
 * Se deja mínima a propósito: el objetivo de la Fase 2 es solo dejar el enganche listo
 * (que NodoAST.generarC3D(GeneradorC3D) ya tenga con qué compilar), no adelantar
 * decisiones de diseño de la Fase 3 que todavía no se han tomado.
 */
public class GeneradorC3D {

    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;
    private final List<String> cuadruplas = new ArrayList<>();

    public String nuevoTemporal() {
        return "t" + (++contadorTemporales);
    }

    public String nuevaEtiqueta() {
        return "et" + (++contadorEtiquetas);
    }

    public void emitir(String cuadrupla) {
        cuadruplas.add(cuadrupla);
    }

    public List<String> getCuadruplas() {
        return cuadruplas;
    }
}
