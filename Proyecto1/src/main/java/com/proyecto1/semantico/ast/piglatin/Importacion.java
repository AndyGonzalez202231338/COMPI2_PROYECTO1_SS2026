package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * Una {@code importacion} (#importacionDef): {@code import ID (.ID)*}. Se guarda como
 * una lista de segmentos (uno por cada {@code ID} separado por {@code PUNTO}) en vez
 * de un único {@code String} ya unido, así el visitor no tiene que volver a partir el
 * texto si en algún momento se necesita validar/resolver cada segmento por separado.
 */
public final class Importacion extends NodoPigLatin {

    private final List<String> segmentos; // p. ej. ["modulo", "utilidades"] para "modulo.utilidades"

    public Importacion(List<String> segmentos, int linea, int columna) {
        super(linea, columna);
        this.segmentos = segmentos;
    }

    public List<String> getSegmentos() {
        return segmentos;
    }
}
