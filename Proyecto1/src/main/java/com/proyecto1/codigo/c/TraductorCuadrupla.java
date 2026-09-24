package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.Cuadrupla;

/**
 * Traduce UNA cuádrupla del C3D a su línea equivalente en C.
 *
 * <p>Esta primera versión cubre solo las cuádruplas "planas": asignación, saltos,
 * etiquetas, return, y los operadores binarios/unarios genéricos (aritméticos,
 * relacionales y lógicos). Todo lo demás — I/O, llamadas, arreglos, campos,
 * instanciación — queda explícitamente pendiente y lanza
 * {@link UnsupportedOperationException} (no se adivina la traducción).
 *
 * <p><b>Convención de salida</b>: cada línea termina en {@code ;} cuando aplica
 * (la etiqueta lleva {@code :;} para evitar el error de C "label at end of compound
 * statement" si resulta ser la última línea de un bloque). Sin indentación — eso
 * lo añade quien ensambla el archivo completo en una fase posterior.
 *
 * <p>Los operandos ({@code arg1}, {@code arg2}, {@code resultado}) se emiten tal
 * cual: es responsabilidad del C3D haber dejado identificadores válidos de C
 * (nombres de variables, temporales "tN", etiquetas "LN", literales).
 */
public final class TraductorCuadrupla {

    /**
     * Devuelve la línea de C correspondiente a {@code c}, sin indentación y con
     * {@code ;} final donde aplique. Nunca devuelve null.
     *
     * @throws UnsupportedOperationException si el operador es de una categoría que
     *         esta fase aún no traduce.
     */
    public String traducir(Cuadrupla c) {
        String op = c.getOperador();

        switch (op) {
            case Cuadrupla.OP_ASIGNACION:
                return c.getResultado() + " = " + c.getArg1() + ";";

            case Cuadrupla.OP_GOTO:
                return "goto " + c.getResultado() + ";";

            case Cuadrupla.OP_IF_FALSE:
                return "if (!" + c.getArg1() + ") goto " + c.getResultado() + ";";

            case Cuadrupla.OP_IF_TRUE:
                return "if (" + c.getArg1() + ") goto " + c.getResultado() + ";";

            case Cuadrupla.OP_ETIQUETA:
                // ":;" en vez de ":" evita el error de C "label at end of compound
                // statement" cuando la etiqueta queda como última línea de un bloque.
                return c.getResultado() + ":;";

            case Cuadrupla.OP_RETURN:
                if (c.getArg1() != null) {
                    return "return " + c.getArg1() + ";";
                }
                return "return;";

            // --- Pendientes de fases posteriores ---
            case Cuadrupla.OP_PRINT:
            case Cuadrupla.OP_READ:
            case Cuadrupla.OP_CALL:
            case Cuadrupla.OP_PARAM:
            case Cuadrupla.OP_BEGIN_FUNC:
            case Cuadrupla.OP_END_FUNC:
            case Cuadrupla.OP_INDEX_LOAD:
            case Cuadrupla.OP_INDEX_STORE:
            case Cuadrupla.OP_FIELD_LOAD:
            case Cuadrupla.OP_FIELD_STORE:
            case Cuadrupla.OP_NEW:
            case Cuadrupla.OP_NEW_ARRAY:
                throw new UnsupportedOperationException(
                        "Traducción C pendiente (Fase 4 posterior) para operador '"
                                + op + "': " + c);

            default:
                // Todo lo demás debe ser binaria o unaria genérica.
                return traducirBinariaOUnaria(c);
        }
    }

    /**
     * Traduce un operador NO predefinido (los aritméticos/relacionales/lógicos y los
     * unarios, cuyos operadores son strings como "+", "-", "==", "&&", "!"...).
     *
     * <p>Forma esperada de la cuádrupla:
     * <ul>
     *   <li>Binaria: {@code (op, a, b, t)} → {@code t = a op b;}</li>
     *   <li>Unaria:  {@code (op, a, null, t)} → {@code t = op a;} (op puede ser
     *       "!", "-", o las palabras "not"/"neg" que se normalizan a "!"/"-").</li>
     * </ul>
     */
    private String traducirBinariaOUnaria(Cuadrupla c) {
        String op = c.getOperador();
        String arg1 = c.getArg1();
        String arg2 = c.getArg2();
        String resultado = c.getResultado();

        if (arg1 != null && arg2 != null) {
            return resultado + " = " + arg1 + " " + op + " " + arg2 + ";";
        }
        if (arg1 != null) {
            return resultado + " = " + normalizarUnario(op) + arg1 + ";";
        }
        throw new UnsupportedOperationException(
                "Cuádrupla no reconocida para traducción C: " + c);
    }

    /**
     * Normaliza el nombre del operador unario a su símbolo C:
     * "not" → "!", "neg" -> "-". Cualquier otro operador se deja tal cual
     * (así "!" y "-" que ya vienen en símbolo pasan sin cambios).
     */
    private static String normalizarUnario(String op) {
        if (op == null) return "";
        if (op.equals("not")) return "!";
        if (op.equals("neg")) return "-";
        return op;
    }
}