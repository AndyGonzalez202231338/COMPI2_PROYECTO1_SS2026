package com.proyecto1.codigo.c;

import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoEstructura;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * Traduce un {@link Tipo} del lenguaje a su representación en C.
 *
 * <p><b>Clases y estructuras son SIEMPRE punteros.</b> No hay distinción entre
 * "valor" y "referencia" en el C3D: un valor de tipo {@code Persona} se representa
 * como {@code Persona*}, porque:
 * <ul>
 *   <li>Las clases de Z viven en heap por decisión del lenguaje (PDF).</li>
 *   <li>Las estructuras de Y también, cuando se crean con {@code new} (decisión
 *       tomada en la Fase P.3/P.6 de PigLatin: {@code new} de una estructura hace
 *       {@code malloc} igual que una clase).</li>
 * </ul>
 * Esto es lo que justifica que {@link TraductorCuadrupla} traduzca todo acceso a
 * campo con {@code ->} y nunca con {@code .}: el lado izquierdo de un acceso es
 * siempre un puntero.
 *
 * <p>El método no valida el programa ni lanza excepciones: si el tipo no encaja en
 * ningún caso conocido (p. ej. {@code DESCONOCIDO} por errores semánticos
 * anteriores), cae a {@code "int"} como valor neutro. Es responsabilidad de las
 * fases anteriores haber reportado los errores; aquí solo se traduce.
 */
public final class TraductorTipos {

    private TraductorTipos() {}  // clase de utilidades, no instanciable

    /**
     * Traduce {@code tipo} a su representación en C:
     * <ul>
     *   <li>{@code ENTERO} → {@code "int"}</li>
     *   <li>{@code FLOTANTE} → {@code "double"}</li>
     *   <li>{@code CARACTER} → {@code "char"}</li>
     *   <li>{@code CADENA} → {@code "char*"}</li>
     *   <li>{@code BOOL} → {@code "int"} (C no tiene bool nativo antes de C99;
     *       y aunque lo tuviera, mantener {@code int} uniforma la ABI)</li>
     *   <li>{@code VOID} → {@code "void"}</li>
     *   <li>Cualquier otro caso (incluye {@code DESCONOCIDO}, {@code NULO}, etc.)
     *       → {@code "int"}</li>
     *   <li>{@link TipoClase} → {@code "<NombreClase>*"}</li>
     *   <li>{@link TipoEstructura} → {@code "<NombreEstructura>*"}</li>
     *   <li>{@link TipoArreglo} → {@code aC(tipoBase) + "*"}</li>
     * </ul>
     */
    /**
     * Traduce {@code tipo} a su representación en C:
     * <ul>
     *   <li>{@code ENTERO} → {@code "int"}</li>
     *   <li>{@code FLOTANTE} → {@code "double"}</li>
     *   <li>{@code CARACTER} → {@code "char"}</li>
     *   <li>{@code CADENA} → {@code "char*"}</li>
     *   <li>{@code BOOL} → {@code "int"} (C no tiene bool nativo antes de C99;
     *       mantener int uniforma la ABI)</li>
     *   <li>{@code VOID} → {@code "void"}</li>
     *   <li>Cualquier otro caso (incluye {@code DESCONOCIDO}, {@code NULO}) → {@code "int"}</li>
     *   <li>{@link TipoClase} → {@code "<NombreClase>*"}</li>
     *   <li>{@link TipoEstructura} → {@code "<NombreEstructura>*"}</li>
     *   <li>{@link TipoArreglo} → {@code aC(tipoBase) + "*"}</li>
     * </ul>
     */
    public static String aC(Tipo tipo) {
        if (tipo == null) return "int";

        if (tipo == TipoPrimitivo.ENTERO)   return "int";
        if (tipo == TipoPrimitivo.FLOTANTE) return "double";
        if (tipo == TipoPrimitivo.CARACTER) return "char";
        if (tipo == TipoPrimitivo.CADENA)   return "char*";
        if (tipo == TipoPrimitivo.BOOL)     return "int";
        if (tipo == TipoPrimitivo.VOID)     return "void";

        if (tipo instanceof TipoClase tc)
            return tc.getDefinicion().getNombre() + "*";
        if (tipo instanceof TipoEstructura te)
            return te.getDefinicion().getNombre() + "*";
        if (tipo instanceof TipoArreglo ta)
            return aC(ta.getBase()) + "*";

        return "int";  // DESCONOCIDO, NULO, o cualquier tipo futuro sin mapeo
    }
}