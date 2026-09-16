package com.proyecto1.semantico;

/**
 * Utilidad compartida entre {@code ASTBuilderY} y {@code ASTBuilderZ} (y, cuando se
 * escriba, {@code ASTBuilderPig}) para convertir el TEXTO CRUDO de un token
 * CADENA_LIT/CARACTER_LIT (que todavía incluye las comillas y las secuencias de
 * escape tal como las escribió el programador, p. ej. {@code "hola\n"} con backslash
 * literal) en el valor Java real que representa (quita comillas y desescapa
 * {@code \n \t \r \' \" \\}).
 *
 * Se deja en un solo lugar para que ningún visitor tenga que reinventar el
 * desescapado, y para que si algún día cambia el conjunto de escapes soportados
 * (ver {@code fragment ESCAPE} en LenguajeLexer.g4) solo haya que tocar un archivo.
 */
public final class LiteralUtil {

    private LiteralUtil() {}

    /** Quita la comilla inicial/final y desescapa el contenido de un CADENA_LIT o CARACTER_LIT. */
    public static String textoSinComillasNiEscapes(String textoCrudo) {
        String interior = textoCrudo.substring(1, textoCrudo.length() - 1);
        StringBuilder resultado = new StringBuilder(interior.length());
        for (int i = 0; i < interior.length(); i++) {
            char actual = interior.charAt(i);
            if (actual == '\\' && i + 1 < interior.length()) {
                char siguiente = interior.charAt(++i);
                switch (siguiente) {
                    case 'n' -> resultado.append('\n');
                    case 't' -> resultado.append('\t');
                    case 'r' -> resultado.append('\r');
                    case '\'' -> resultado.append('\'');
                    case '"' -> resultado.append('"');
                    case '\\' -> resultado.append('\\');
                    default -> resultado.append(siguiente); // escape desconocido: se deja tal cual
                }
            } else {
                resultado.append(actual);
            }
        }
        return resultado.toString();
    }

    /** Convierte el texto de un CARACTER_LIT ya desescapado a su único char. */
    public static char aCaracter(String textoCrudo) {
        return textoSinComillasNiEscapes(textoCrudo).charAt(0);
    }

    public static long aEntero(String textoCrudo) {
        return Long.parseLong(textoCrudo);
    }

    public static double aFlotante(String textoCrudo) {
        return Double.parseDouble(textoCrudo);
    }
}
