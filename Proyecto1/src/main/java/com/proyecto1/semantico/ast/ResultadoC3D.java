package com.proyecto1.semantico.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que devuelve generarC3D(...): dónde quedó "guardado" el valor de este nodo (una
 * variable, un temporal como "t3", o null si el nodo no produce un valor — por ejemplo
 * una instrucción "romper") y las cuádruplas que hicieron falta para calcularlo.
 *
 * El formato exacto de cada cuádrupla (por ahora un String "libre") se terminará de
 * definir en la Fase 3, junto con el resto del generador; esta clase ya deja el punto
 * de enganche para que ningún nodo de la Fase 2 tenga que cambiar de forma cuando eso
 * pase — solo su implementación (todavía no escrita) de generarC3D().
 */
public final class ResultadoC3D {

    private final String lugar;
    private final List<String> codigo;

    public ResultadoC3D(String lugar, List<String> codigo) {
        this.lugar = lugar;
        this.codigo = codigo;
    }

    public static ResultadoC3D vacio() {
        return new ResultadoC3D(null, new ArrayList<>());
    }

    public String getLugar() { return lugar; }
    public List<String> getCodigo() { return codigo; }
}
