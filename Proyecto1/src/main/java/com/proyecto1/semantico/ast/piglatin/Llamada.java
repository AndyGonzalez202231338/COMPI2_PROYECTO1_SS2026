package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code primaria LPAREN argumentos? RPAREN} (#primariaLlamada): llamada a función
 * o método. Cubre dos casos:
 * <ul>
 *   <li>{@code f(args)} — objetivo = {@link Identificador}. Función suelta,
 *       probablemente importada de {@code .y}. SIN receptor.</li>
 *   <li>{@code obj.m(args)} — objetivo = {@link AccesoCampo}. Método de un objeto
 *       (típicamente importado de {@code .z}). CON receptor explícito: el objeto
 *       real, NO {@code this} (PigLatin no tiene métodos con self implícito).</li>
 * </ul>
 */
public final class Llamada extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objetivo;
    private final List<ExpresionPigLatin> argumentos;

    /**
     * Símbolo de la función/método resuelto por verificar(). Se usa en generarC3D
     * para el tipo de retorno, sin volver a navegar el ámbito. Null si verificar()
     * no corrió o falló.
     */
    private Simbolo simboloResuelto;

    /**
     * Clase del objeto en el caso "obj.m(args)". Null en el caso "f(args)" (o si
     * verificar() no corrió). Cacheado en verificar() a partir de TipoClase.
     */
    private String nombreClaseObjetivo;

    public Llamada(ExpresionPigLatin objetivo, List<ExpresionPigLatin> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionPigLatin getObjetivo() { return objetivo; }
    public List<ExpresionPigLatin> getArgumentos() { return argumentos; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // Caso 1: función suelta "f(args)".
        if (objetivo instanceof Identificador id) {
            Simbolo f = ambito.resolver(id.getNombre());
            if (f == null || (f.getCategoria() != CategoriaSimbolo.FUNCION
                    && f.getCategoria() != CategoriaSimbolo.METODO)) {
                errores.reportar(linea, columna, "Función no declarada: '" + id.getNombre() + "'");
                return TipoPrimitivo.DESCONOCIDO;
            }
            this.simboloResuelto = f;
            return verificarArgumentosYRetorno(f, ambito, errores);
        }

        // Caso 2: método sobre objeto "obj.m(args)".
        if (objetivo instanceof AccesoCampo ac) {
            Tipo tObj = ac.getObjeto().verificar(ambito, errores);
            if (!(tObj instanceof TipoClase tc)) {
                if (!tObj.esDesconocido())
                    errores.reportar(linea, columna,
                            "No se puede llamar método sobre " + tObj.nombre());
                return TipoPrimitivo.DESCONOCIDO;
            }
            Simbolo m = tc.getDefinicion().buscarMiembro(ac.getCampo());
            if (m == null || m.getCategoria() != CategoriaSimbolo.METODO) {
                errores.reportar(linea, columna,
                        "La clase '" + tc.nombre() + "' no tiene método '" + ac.getCampo() + "'");
                return TipoPrimitivo.DESCONOCIDO;
            }
            this.simboloResuelto = m;
            this.nombreClaseObjetivo = tc.getDefinicion().getNombre();
            return verificarArgumentosYRetorno(m, ambito, errores);
        }

        errores.reportar(linea, columna, "Llamada inválida");
        return TipoPrimitivo.DESCONOCIDO;
    }

    private Tipo verificarArgumentosYRetorno(Simbolo f, Ambito ambito, ManejadorErrores errores) {
        List<Simbolo> params = f.getParametros();
        if (params.size() != argumentos.size()) {
            errores.reportar(linea, columna,
                    "Función '" + f.getNombre() + "' espera " + params.size() +
                            " argumentos, recibió " + argumentos.size());
            return f.getTipo();
        }
        for (int i = 0; i < argumentos.size(); i++) {
            Tipo ta = argumentos.get(i).verificar(ambito, errores);
            if (!Tipos.esAsignable(params.get(i).getTipo(), ta))
                errores.reportar(argumentos.get(i).getLinea(), argumentos.get(i).getColumna(),
                        "Argumento " + (i+1) + " incompatible: se esperaba " +
                                params.get(i).getTipo().nombre() + ", se recibió " + ta.nombre());
        }
        return f.getTipo();
    }

    /**
     * Emite, en este orden:
     * <ol>
     *   <li>Determinar el receptor:
     *       <ul>
     *         <li>{@link Identificador}: SIN receptor. No se emite ningún {@code param}
     *             antes de los argumentos.</li>
     *         <li>{@link AccesoCampo}: la expresión del objeto es el receptor; se
     *             genera su C3D y su lugar pasa a ser el primer {@code param}.</li>
     *       </ul>
     *   </li>
     *   <li>C3D de cada argumento, en orden.</li>
     *   <li>Bloque de {@code param}: primero el receptor (solo en el caso
     *       {@link AccesoCampo}), luego cada argumento.</li>
     *   <li>Llamada:
     *       <ul>
     *         <li>{@link Identificador}: {@code (call, nombreFuncion, nArgs, t)}.</li>
     *         <li>{@link AccesoCampo}: {@code etiquetaMetodo(clase, metodo)}, con
     *             {@code nArgs+1} (el +1 es el receptor).</li>
     *       </ul>
     *   </li>
     * </ol>
     * Los {@code param} contiguos anteriores al {@code call} son exactamente sus
     * argumentos, en orden — misma convención que en Z. Devuelve
     * {@code ResultadoC3D.temporal(t, tipoDelSímboloCacheado)} (o {@code DESCONOCIDO}
     * si el símbolo no se pudo cachear).
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        // 1) Receptor: solo si es "obj.m(args)".
        String receptor = null;
        if (objetivo instanceof AccesoCampo ac) {
            ResultadoC3D r = ac.getObjeto().generarC3D(generador);
            receptor = r.getLugar();
        }
        // Caso Identificador: sin receptor, nada que evaluar.

        // 2) Evaluar todos los argumentos, guardando sus lugares.
        List<String> lugaresArgs = new ArrayList<>();
        for (ExpresionPigLatin a : argumentos) {
            ResultadoC3D v = a.generarC3D(generador);
            lugaresArgs.add(v.getLugar());
        }

        // 3) Bloque de params: receptor (si aplica) + args.
        if (receptor != null) {
            generador.emitirParam(receptor);
        }
        for (String lugar : lugaresArgs) {
            generador.emitirParam(lugar);
        }

        // 4) Llamada. La firma y la etiqueta dependen del caso.
        String t = generador.nuevoTemporal();
        if (objetivo instanceof Identificador id) {
            generador.emitirCall(id.getNombre(), argumentos.size(), t);
        } else {
            AccesoCampo ac = (AccesoCampo) objetivo;
            String clase = (nombreClaseObjetivo != null) ? nombreClaseObjetivo : "?";
            String etiqueta = generador.etiquetaMetodo(clase, ac.getCampo());
            generador.emitirCall(etiqueta, argumentos.size() + 1, t);
        }

        // 5) Tipo del resultado: cacheado por verificar().
        Tipo tipo = (simboloResuelto != null && simboloResuelto.getTipo() != null)
                ? simboloResuelto.getTipo()
                : TipoPrimitivo.DESCONOCIDO;
        return ResultadoC3D.temporal(t, tipo);
    }
}