package com.proyecto1.codigo.c;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.cuadruplas.*;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.*;

/**
 * Ensambla un archivo C completo a partir de las cuádruplas y firmas de un programa
 * de Y o de PigLatin (Z tiene particularidades — this, mangling de métodos — que se
 * tratan en una fase aparte).
 *
 * <p><b>Responsabilidades:</b>
 * <ol>
 *   <li>Dividir la lista plana de cuádruplas por función, detectando begin_func...
 *       end_func.</li>
 *   <li>Para cada función: emitir su cabecera (con el tipo de retorno y los
 *       parámetros de la {@link GeneradorC3D.Firma}), las declaraciones locales
 *       (delegadas a {@link InferenciaTiposC}) y su cuerpo (delegado a
 *       {@link TraductorCuadrupla}, salvo {@code param}/{@code call}, que se
 *       manejan agrupados aquí).</li>
 *   <li>Emitir prototipos de todas las funciones.</li>
 *   <li>Emitir un {@code main} de C que llama a la función de entrada del lenguaje
 *       ("main" en PigLatin, la función con el nombre configurado en Y).</li>
 * </ol>
 *
 * <p><b>Renombrado de funciones:</b> todas las funciones del C3D se prefijan con
 * {@code prefijoLenguaje} para evitar colisiones con las funciones de la
 * biblioteca estándar de C ({@code printf}, {@code malloc}, ...) y con el propio
 * {@code main} de C. Así una función {@code "main"} del C3D pasa a ser
 * {@code "y_main"} o {@code "pig_main"} en el C. El {@code main} real de C es un
 * wrapper que llama a la función de entrada ya prefijada.
 */
public final class OrquestadorC3DaC {

    private final List<Cuadrupla> cuadruplas;
    private final Map<String, GeneradorC3D.Firma> firmas;
    private final String prefijoLenguaje;   // ej. "y_", "pig_"
    private final String nombreFuncionEntrada; // nombre en el C3D ("main", "principal", ...)

    public OrquestadorC3DaC(List<Cuadrupla> cuadruplas,
                            Map<String, GeneradorC3D.Firma> firmas,
                            String prefijoLenguaje,
                            String nombreFuncionEntrada) {
        this.cuadruplas = cuadruplas != null ? cuadruplas : List.of();
        this.firmas = firmas != null ? firmas : Map.of();
        this.prefijoLenguaje = (prefijoLenguaje != null) ? prefijoLenguaje : "";
        this.nombreFuncionEntrada = nombreFuncionEntrada;
    }

    // ---------- API pública ----------

    /** Genera el archivo C completo: includes + runtime + prototipos + funciones + main. */
    public String generarArchivoCompleto() {
        List<FuncionCompilada> funciones = dividirPorFuncion();

        StringBuilder sb = new StringBuilder();
        sb.append("/* Archivo generado automáticamente por OrquestadorC3DaC */\n");
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n\n");
        sb.append(runtimeReadString());
        sb.append("\n");

        // Prototipos
        sb.append("/* Prototipos */\n");
        for (FuncionCompilada fc : funciones) {
            sb.append(prototipo(fc)).append(";\n");
        }
        sb.append("\n");

        // Definiciones
        sb.append("/* Definiciones */\n");
        for (FuncionCompilada fc : funciones) {
            sb.append(definicion(fc)).append("\n");
        }

        // main wrapper
        sb.append(mainWrapper(funciones));
        return sb.toString();
    }

    // ---------- División por función ----------

    /** Una función del C3D: su begin_func y las cuádruplas de su cuerpo (sin end_func). */
    private record FuncionCompilada(CuadruplaBeginFunc begin, List<Cuadrupla> cuerpo) {}

    private List<FuncionCompilada> dividirPorFuncion() {
        List<FuncionCompilada> resultado = new ArrayList<>();
        int i = 0;
        while (i < cuadruplas.size()) {
            Cuadrupla c = cuadruplas.get(i);
            if (c instanceof CuadruplaBeginFunc bf) {
                int j = i + 1;
                while (j < cuadruplas.size() && !(cuadruplas.get(j) instanceof CuadruplaEndFunc)) {
                    j++;
                }
                resultado.add(new FuncionCompilada(bf, cuadruplas.subList(i + 1, j)));
                i = j + 1;
            } else {
                i++; // cuádruplas fuera de toda función (no debería pasar): se ignoran
            }
        }
        return resultado;
    }

    // ---------- Cabecera + cuerpo ----------

    private String prototipo(FuncionCompilada fc) {
        return cabecera(fc, false);
    }

    private String definicion(FuncionCompilada fc) {
        StringBuilder sb = new StringBuilder();
        sb.append(cabecera(fc, true)).append(" {\n");

        // Declaraciones locales inferidas
        Map<String, String> tipos = new HashMap<>();
        // Arrancamos los tipos del registro de parámetros (el inferidor los añade también)
        GeneradorC3D.Firma firma = firmas.get(fc.begin().nombre());
        if (firma != null) {
            for (GeneradorC3D.ParametroFirma p : firma.parametros()) {
                tipos.put(p.nombre(), tipoAC(p.tipo()));
            }
        }

        InferenciaTiposC inf = new InferenciaTiposC(fc.cuerpo(), firma, firmas);
        // El inferidor ya conoce los tipos de parámetros y locales; volcamos todo al mapa.
        tipos.putAll(inf.getDeclaraciones());

        for (String linea : inf.comoLineasDeC()) {
            sb.append("    ").append(linea).append("\n");
        }
        if (!inf.getDeclaraciones().isEmpty()) {
            sb.append("\n");
        }

        // Cuerpo
        sb.append(cuerpoATexto(fc.cuerpo(), tipos));

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Cabecera C de una función: {@code tipoRetorno <prefijo>nombre(params)}.
     * Si {@code conNombre}, incluye el nombre (definición); si no, es un prototipo
     * (mismo texto, ambos casos son idénticos en C).
     */
    private String cabecera(FuncionCompilada fc, boolean conNombre) {
        GeneradorC3D.Firma firma = firmas.get(fc.begin().nombre());
        String tipoRet = (firma != null && firma.tipoRetorno() != null)
                ? tipoAC(firma.tipoRetorno()) : "void";

        List<GeneradorC3D.ParametroFirma> params =
                (firma != null) ? firma.parametros() : List.of();

        StringBuilder sb = new StringBuilder();
        sb.append(tipoRet).append(" ").append(prefijoLenguaje).append(fc.begin().nombre()).append("(");
        if (params.isEmpty()) {
            sb.append("void");
        } else {
            List<String> ps = new ArrayList<>();
            for (GeneradorC3D.ParametroFirma p : params) {
                ps.add(tipoAC(p.tipo()) + " " + p.nombre());
            }
            sb.append(String.join(", ", ps));
        }
        sb.append(")");
        return sb.toString();
    }

    // ---------- Traducción del cuerpo con contexto ----------

    /**
     * Traduce el cuerpo de una función. Las cuádruplas "planas" las delega a
     * {@link TraductorCuadrupla}; las que necesitan contexto (param + call, print,
     * read) las maneja aquí.
     */
    private String cuerpoATexto(List<Cuadrupla> cuerpo, Map<String, String> tipos) {
        StringBuilder sb = new StringBuilder();
        TraductorCuadrupla tr = new TraductorCuadrupla(tipos);
        List<String> paramsPendientes = new ArrayList<>();

        for (Cuadrupla c : cuerpo) {
            if (c instanceof CuadruplaParam p) {
                paramsPendientes.add(p.valor());
                continue;
            }
            if (c instanceof CuadruplaCall call) {
                sb.append("    ").append(traducirCall(call, paramsPendientes)).append("\n");
                paramsPendientes.clear();
                continue;
            }
            // El resto: print/read los maneja TraductorCuadrupla con tipos; planas también.
            sb.append("    ").append(tr.traducir(c)).append("\n");
        }
        return sb.toString();
    }

    /** Construye {@code t = f(a, b, c);} o {@code f(a, b, c);} a partir de los params. */
    private String traducirCall(CuadruplaCall call, List<String> params) {
        String nombre = prefijoLenguaje + call.funcion();
        String args = String.join(", ", params);
        String expr = nombre + "(" + args + ")";
        return (call.destino() != null) ? call.destino() + " = " + expr + ";" : expr + ";";
    }

    // ---------- main de C ----------

    /**
     * Emite el {@code main} de C. Busca una función cuyo nombre en el C3D coincida
     * con {@link #nombreFuncionEntrada}; si existe, el main la llama y devuelve 0.
     * Si no existe, emite un main vacío que devuelve 0.
     *
     * <p>La función de entrada SIEMPRE se prefija (queda como {@code <prefijo>main}),
     * así que C's {@code main} no colisiona con ella.
     */
    private String mainWrapper(List<FuncionCompilada> funciones) {
        boolean existeEntrada = funciones.stream()
                .anyMatch(fc -> fc.begin().nombre().equals(nombreFuncionEntrada));

        StringBuilder sb = new StringBuilder();
        sb.append("/* Punto de entrada */\n");
        sb.append("int main(void) {\n");
        if (existeEntrada) {
            sb.append("    ").append(prefijoLenguaje).append(nombreFuncionEntrada).append("();\n");
        }
        sb.append("    return 0;\n");
        sb.append("}\n");
        return sb.toString();
    }

    // ---------- Runtime mínimo ----------

    /**
     * Helper de runtime para leer strings. Se inyecta siempre en el archivo; si el
     * programa no usa {@code read} con strings, el compilador de C puede descartarlo
     * con {@code -Wunused-function} o similar.
     */
    private static String runtimeReadString() {
        return """
                /* Runtime mínimo */
                static char* rt_read_string(void) {
                    char buf[1024];
                    if (fgets(buf, sizeof(buf), stdin) == NULL) return NULL;
                    size_t n = strlen(buf);
                    if (n > 0 && buf[n-1] == '\\n') buf[n-1] = '\\0';
                    char* r = (char*)malloc(n + 1);
                    if (r) strcpy(r, buf);
                    return r;
                }
                """;
    }

    // ---------- Tipos internos a C ----------

    /**
     * Traduce un {@link Tipo} a su representación en C.
     * Mismo mapeo que {@link InferenciaTiposC}, replicado aquí para no acoplar el
     * orquestador a los detalles internos de esa clase.
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
        if (t instanceof com.proyecto1.semantico.tipos.TipoClase tc)
            return tc.getDefinicion().getNombre() + "*";
        if (t instanceof com.proyecto1.semantico.tipos.TipoEstructura te)
            return te.getDefinicion().getNombre() + "*";
        if (t instanceof com.proyecto1.semantico.tipos.TipoArreglo ta)
            return tipoAC(ta.getBase()) + "*";
        return "int";
    }
}