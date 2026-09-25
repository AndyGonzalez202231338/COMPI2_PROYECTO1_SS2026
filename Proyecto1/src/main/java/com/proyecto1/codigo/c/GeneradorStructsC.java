package com.proyecto1.codigo.c;

import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.*;

import java.util.List;

/**
 * Emite los {@code typedef struct} de las estructuras de Y y las clases de Z.
 *
 * <p><b>Entrada:</b> la lista de {@link Simbolo} que representan tipos definidos por
 * el usuario (categoría {@link CategoriaSimbolo#ESTRUCTURA} o
 * {@link CategoriaSimbolo#CLASE}), en orden de declaración. Ese orden lo garantiza
 * el llamador (típicamente el analizador semántico, que ya recorre las declaraciones
 * en el orden del fuente y las registra en el ámbito global).
 *
 * <p><b>Salida:</b> el bloque C con un {@code typedef struct} por tipo. Se emiten en
 * dos pasadas:
 * <ol>
 *   <li>Todos los {@code typedef struct X X;} (forward declarations). Necesario
 *       porque los campos son punteros ({@code X*}) y pueden referirse a tipos
 *       aún no definidos, incluyendo auto-referencias (listas, árboles).</li>
 *   <li>Todos los {@code struct X { ... };} con sus campos completos.</li>
 * </ol>
 * El orden de declaración se respeta dentro de cada pasada, así que el archivo C
 * refleja el orden del fuente para facilitar la lectura.
 *
 * <p><b>Sólo se emiten CAMPOS:</b> {@link CategoriaSimbolo#CAMPO} (Y) y
 * {@link CategoriaSimbolo#ATRIBUTO} (Z). Constructores, métodos y cualquier otro
 * miembro se ignoran aquí (van en otras fases).
 *
 * <p><b>Arreglos:</b> un campo {@code int[5]} de Y (arreglo de tamaño conocido) se
 * emite como {@code int arr[5];} para preservar el {@code sizeof} del struct. Un
 * campo arreglo de tamaño desconocido (típico de Z, donde los arreglos son dinámicos)
 * se emite como puntero {@code int* arr;}.
 */
public final class GeneradorStructsC {

    /**
     * Genera el bloque {@code typedef struct} completo. Si la lista viene vacía,
     * devuelve la cadena vacía (sin comentario de cabecera siquiera).
     */
    public String generar(List<Simbolo> definicionesTipo) {
        if (definicionesTipo == null || definicionesTipo.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("/* Definiciones de tipos */\n");

        // --- Pass 1: forward declarations ---
        for (Simbolo s : definicionesTipo) {
            if (!esTipoValido(s)) continue;
            sb.append("typedef struct ").append(s.getNombre())
                    .append(" ").append(s.getNombre()).append(";\n");
        }
        sb.append("\n");

        // --- Pass 2: struct bodies ---
        for (Simbolo s : definicionesTipo) {
            if (!esTipoValido(s)) continue;
            sb.append("struct ").append(s.getNombre()).append(" {\n");

            boolean hayCampos = false;
            for (Simbolo m : s.getMiembrosEnOrden()) {
                if (!esCampo(m)) continue;
                hayCampos = true;
                sb.append("    ").append(campoAC(m)).append(";\n");
            }

            // C no admite structs vacíos (C89). Si no hay campos, se mete un
            // placeholder para que el compilador acepte el typedef.
            if (!hayCampos) {
                sb.append("    char _empty;  /* struct sin campos */\n");
            }

            sb.append("};\n\n");
        }

        return sb.toString();
    }

    // ---------- Helpers ----------

    /** ¿Es un tipo definido por el usuario que debe generar un typedef struct? */
    private static boolean esTipoValido(Simbolo s) {
        if (s == null) return false;
        CategoriaSimbolo c = s.getCategoria();
        return c == CategoriaSimbolo.ESTRUCTURA || c == CategoriaSimbolo.CLASE;
    }

    /**
     * Un miembro es "campo visible" si es {@link CategoriaSimbolo#CAMPO} (Y) o
     * {@link CategoriaSimbolo#ATRIBUTO} (Z). Todo lo demás (constructores, métodos,
     * variables de ámbito) se ignora al emitir el struct.
     */
    private static boolean esCampo(Simbolo m) {
        if (m == null) return false;
        CategoriaSimbolo c = m.getCategoria();
        return c == CategoriaSimbolo.CAMPO || c == CategoriaSimbolo.ATRIBUTO;
    }

    /**
     * Emite "tipoC nombre" para un campo. Caso especial: si el tipo es un arreglo
     * con longitud conocida en compile-time (típico de Y), se emite como arreglo
     * fijo de C: {@code int arr[5]} en vez de {@code int* arr}.
     */
    private static String campoAC(Simbolo m) {
        Tipo t = m.getTipo();

        if (t instanceof TipoArreglo ta && ta.tieneLongitudConocida()) {
            // Y: "int[5]" dentro de un struct → "int arr[5];"
            return tipoAC(ta.getBase()) + " " + m.getNombre() + "[" + ta.getLongitud() + "]";
        }
        // Todo lo demás: tipo + nombre. Si es arreglo sin longitud, tipoAC ya da
        // "int*", "Persona*", etc.
        return tipoAC(t) + " " + m.getNombre();
    }

    /**
     * Mismo mapeo que {@link InferenciaTiposC} y {@link OrquestadorC3DaC}: traduce
     * un {@link Tipo} del lenguaje a su representación en C.
     */
    private static String tipoAC(Tipo t) {
        if (t == null) return "void";
        if (t == TipoPrimitivo.ENTERO)      return "int";
        if (t == TipoPrimitivo.FLOTANTE)    return "double";
        if (t == TipoPrimitivo.CARACTER)    return "char";
        if (t == TipoPrimitivo.CADENA)      return "char*";
        if (t == TipoPrimitivo.BOOL)        return "int";
        if (t == TipoPrimitivo.VOID)        return "void";
        if (t == TipoPrimitivo.NULO)        return "void*";
        if (t == TipoPrimitivo.DESCONOCIDO) return "int";
        if (t instanceof TipoClase tc)      return tc.getDefinicion().getNombre() + "*";
        if (t instanceof TipoEstructura te) return te.getDefinicion().getNombre() + "*";
        if (t instanceof TipoArreglo ta)    return tipoAC(ta.getBase()) + "*";
        return "int";
    }
}