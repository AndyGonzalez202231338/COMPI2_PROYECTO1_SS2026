package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.GeneradorC3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Traduce una {@link GeneradorC3D.Firma} a la cabecera de una función C.
 *
 * <p>Produce {@code "<tipoRetorno> <etiqueta>(<param1>, <param2>, ...)"}, SIN llave
 * de apertura y SIN ";" final: el ensamblador del archivo completo decide si la
 * línea va seguida de "{" (definición) o ";" (prototipo).
 *
 * <p><b>Sobre "this":</b> no hay caso especial. Para un método o constructor de Z,
 * {@link GeneradorC3D.Firma#parametros()} YA INCLUYE al receptor implícito como su
 * PRIMER elemento (así lo decidieron {@code Constructor.generarC3D} y
 * {@code Metodo.generarC3D} al llamar {@code registrarFirma(...)}). Aquí se trata
 * como un {@link GeneradorC3D.ParametroFirma} más: se traduce su tipo (que será
 * {@code <Clase>*}) y su nombre (que será {@code "this"}), y se emite igual que
 * cualquier otro parámetro.
 *
 * <p><b>Función sin parámetros:</b> C exige {@code (void)} explícito, no
 * {@code ()} (aunque {@code ()} es válido en prototipos modernos, evita warnings
 * con compiladores antiguos).
 */
public final class TraductorFirma {

    private TraductorFirma() {}  // clase de utilidades

    /**
     * Devuelve la cabecera C de {@code firma}: tipoRetorno + etiqueta + parámetros.
     * Ejemplo: {@code "int Persona_getEdad(Persona* this)"}.
     * No incluye llave de apertura ni ";" final.
     */
    public static String traducirCabecera(GeneradorC3D.Firma firma) {
        if (firma == null) return "void __firma_null(void)";
        String tipoRetorno = TraductorTipos.aC(firma.tipoRetorno());
        return tipoRetorno + " " + firma.etiqueta() + traducirParametros(firma);
    }

    /**
     * Devuelve SOLO la lista de parámetros entre paréntesis, con los tipos ya
     * traducidos a C. Ejemplos:
     * <ul>
     *   <li>Sin parámetros → {@code "(void)"}</li>
     *   <li>Un parámetro {@code (int x)} → {@code "(int x)"}</li>
     *   <li>Dos parámetros {@code (int a, char* s)} → {@code "(int a, char* s)"}</li>
     *   <li>Método de Z {@code (Persona* this, int x)} → {@code "(Persona* this, int x)"}</li>
     * </ul>
     *
     * <p>Se expone aparte de {@link #traducirCabecera} porque un llamador puede
     * querer armar la cabecera con otro formato (alineación, indentación en varias
     * líneas) sin tener que reconstruir la lista.
     */
    public static String traducirParametros(GeneradorC3D.Firma firma) {
        if (firma == null || firma.parametros() == null || firma.parametros().isEmpty()) {
            return "(void)";
        }
        List<String> partes = new ArrayList<>();
        for (GeneradorC3D.ParametroFirma p : firma.parametros()) {
            partes.add(TraductorTipos.aC(p.tipo()) + " " + p.nombre());
        }
        return "(" + String.join(", ", partes) + ")";
    }
}