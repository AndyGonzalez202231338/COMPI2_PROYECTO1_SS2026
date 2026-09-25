package com.proyecto1.codigo.c;

/**
 * Runtime C mínimo del compilador. Se inyecta al principio del archivo .c generado
 * (típicamente por el OrquestadorC3DaC) para cubrir operaciones que el C3D expone
 * pero C puro no tiene:
 *
 * <ul>
 *   <li>{@code rt_read_string()}: lee una línea completa de stdin (incluidos espacios)
 *       y devuelve un {@code char*} nuevo en heap. Es lo que usa Z para {@code readln()}
 *       y lo que usa Y/PigLatin para {@code read} sobre {@code char*}.</li>
 *   <li>{@code rt_concat(a, b)}: concatena dos strings en un buffer nuevo. Es lo que
 *       usa el traductor cuando traduce {@code a + b} sobre {@code char*} (en C puro
 *       {@code +} no concatena cadenas).</li>
 *   <li>{@code rt_strcmp(a, b)}: compara dos strings con manejo de {@code NULL}
 *       (nuestro lenguaje admite {@code null}, {@code strcmp} de la libc no).
 *       Es lo que usa el traductor cuando traduce {@code ==} / {@code !=} sobre
 *       {@code char*}.</li>
 *   <li>{@code rt_print_int}, {@code rt_print_double}, {@code rt_print_string} y sus
 *       variantes {@code rt_println_*}: impresión tipada sin formato. El orquestador
 *       las usa para traducir las llamadas {@code rt_print} / {@code rt_println}
 *       que emite Z (que no llevan formato explícito).</li>
 * </ul>
 *
 * <p>Todas las funciones del runtime son {@code static} para no ensuciar el namespace
 * global del C generado. Si el programa del usuario declara una función con el mismo
 * nombre (poco probable dado el prefijo {@code rt_}), el linker se queja, lo cual es
 * deseable.
 */
public final class RuntimeC {

    private RuntimeC() {}  // clase de utilidades

    /**
     * Devuelve el código C completo del runtime, listo para inyectarse tras los
     * {@code #include} estándar. Incluye {@code <stdio.h>}, {@code <stdlib.h>} y
     * {@code <string.h>} por seguridad (por si el orquestador se olvida).
     */
    public static String codigo() {
        return """
                /* ==== Runtime C generado ==== */
                #include <stdio.h>
                #include <stdlib.h>
                #include <string.h>

                /* Lee una línea completa de stdin, sin el '\\n'. Devuelve un char* en heap. */
                static char* rt_read_string(void) {
                    char buf[4096];
                    if (fgets(buf, sizeof(buf), stdin) == NULL) return NULL;
                    size_t n = strlen(buf);
                    if (n > 0 && buf[n-1] == '\\n') buf[n-1] = '\\0';
                    char* r = (char*)malloc(n + 1);
                    if (r) memcpy(r, buf, n + 1);
                    return r;
                }

                /* Concatena dos strings en un buffer nuevo. Trata NULL como "". */
                static char* rt_concat(const char* a, const char* b) {
                    size_t la = a ? strlen(a) : 0;
                    size_t lb = b ? strlen(b) : 0;
                    char* r = (char*)malloc(la + lb + 1);
                    if (!r) return NULL;
                    if (la) memcpy(r, a, la);
                    if (lb) memcpy(r + la, b, lb);
                    r[la + lb] = '\\0';
                    return r;
                }

                /* Compara strings con manejo de NULL. Devuelve <0, 0, >0. */
                static int rt_strcmp(const char* a, const char* b) {
                    if (a == NULL && b == NULL) return 0;
                    if (a == NULL) return -1;
                    if (b == NULL) return  1;
                    return strcmp(a, b);
                }

                /* ==== Impresión tipada (para las llamadas rt_print* de Z) ==== */
                static void rt_print_int(int x)             { printf("%d",  x); }
                static void rt_print_double(double x)       { printf("%lf", x); }
                static void rt_print_string(const char* s)  { printf("%s",  s ? s : "(null)"); }
                static void rt_print_char(char c)           { printf("%c",  c); }
                static void rt_print_bool(int b)            { printf("%d",  b); }

                static void rt_println_int(int x)           { printf("%d\\n",  x); }
                static void rt_println_double(double x)     { printf("%lf\\n", x); }
                static void rt_println_string(const char* s){ printf("%s\\n",  s ? s : "(null)"); }
                static void rt_println_char(char c)         { printf("%c\\n",  c); }
                static void rt_println_bool(int b)          { printf("%d\\n",  b); }

                /* ==== Fin del runtime ==== */
                """;
    }
}