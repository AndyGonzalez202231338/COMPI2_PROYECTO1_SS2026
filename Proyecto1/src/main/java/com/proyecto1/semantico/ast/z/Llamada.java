package com.proyecto1.semantico.ast.z;

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
 * {@code primaryExpression LPAREN argumentList? RPAREN} (#primarioLlamada): llamada a
 * método/función. Cubre "metodo(args)" (objetivo = {@link Identificador}, llamada
 * dentro de la propia clase) y "obj.metodo(args)" (objetivo = {@link AccesoCampo}).
 */
public final class Llamada extends NodoZ implements ExpresionZ {

    private final ExpresionZ objetivo;
    private final List<ExpresionZ> argumentos;

    /**
     * Símbolo del método resuelto por verificar(). Se usa en generarC3D para el tipo
     * de retorno, sin volver a navegar el ámbito. Es null si verificar() no corrió.
     */
    private Simbolo simboloMetodo;

    /**
     * Nombre de la clase cuyo método se está llamando, CUANDO el objetivo es un
     * {@link AccesoCampo} (llamada "obj.metodo"). Para el caso {@link Identificador}
     * no se cachea aquí: se lee del generador ({@link GeneradorC3D#getClaseActual()}),
     * que Clase.generarC3D deja fijado mientras se generan sus métodos.
     */
    private String nombreClaseObjetivo;

    public Llamada(ExpresionZ objetivo, List<ExpresionZ> argumentos, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.argumentos = argumentos;
    }

    public ExpresionZ getObjetivo() { return objetivo; }
    public List<ExpresionZ> getArgumentos() { return argumentos; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // Caso 1: llamada a método de la propia clase -> "metodo(args)".
        if (objetivo instanceof Identificador id) {
            Simbolo m = ambito.resolver(id.getNombre());
            if (m == null || (m.getCategoria() != CategoriaSimbolo.METODO
                    && m.getCategoria() != CategoriaSimbolo.CONSTRUCTOR)) {
                errores.reportar(linea, columna, "Método no declarado: '" + id.getNombre() + "'");
                return TipoPrimitivo.DESCONOCIDO;
            }
            this.simboloMetodo = m;
            // Sin cache de clase: en generarC3D lo sacamos del generador.
            return verificarArgumentosYRetorno(m, ambito, errores);
        }

        // Caso 2: método de otro objeto -> "obj.metodo(args)".
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
            this.simboloMetodo = m;
            this.nombreClaseObjetivo = tc.getDefinicion().getNombre();
            return verificarArgumentosYRetorno(m, ambito, errores);
        }

        errores.reportar(linea, columna, "Llamada inválida");
        return TipoPrimitivo.DESCONOCIDO;
    }

    private Tipo verificarArgumentosYRetorno(Simbolo m, Ambito ambito, ManejadorErrores errores) {
        List<Simbolo> params = m.getParametros();
        if (params.size() != argumentos.size()) {
            errores.reportar(linea, columna,
                    "Método '" + m.getNombre() + "' espera " + params.size() +
                            " argumentos, recibió " + argumentos.size());
            return m.getTipo();
        }
        for (int i = 0; i < argumentos.size(); i++) {
            Tipo ta = argumentos.get(i).verificar(ambito, errores);
            if (!Tipos.esAsignable(params.get(i).getTipo(), ta))
                errores.reportar(argumentos.get(i).getLinea(), argumentos.get(i).getColumna(),
                        "Argumento " + (i+1) + " incompatible: se esperaba " +
                                params.get(i).getTipo().nombre() + ", se recibió " + ta.nombre());
        }
        return m.getTipo();
    }

    /**
     * Emite, en este orden:
     * <ol>
     *   <li>Determinar el receptor:
     *       <ul>
     *         <li>{@link Identificador}: llamada a método de la propia clase; el receptor
     *             es la palabra literal {@code "this"} (parámetro implícito que todo
     *             método Z recibe).</li>
     *         <li>{@link AccesoCampo}: se genera el C3D de la expresión del objeto; su
     *             lugar pasa a ser el receptor.</li>
     *       </ul>
     *   </li>
     *   <li>C3D de cada argumento (en orden), guardando su lugar.</li>
     *   <li>Bloque de params: el receptor, luego cada argumento.</li>
     *   <li>{@code (call, etiquetaMetodo(Clase, metodo), nArgs+1, t)}: el {@code +1}
     *       es el receptor.</li>
     * </ol>
     * Devuelve {@code temporal(t, tipoRetornoDelMétodo)}.
     *
     * <p>La clase con la que se construye la etiqueta se resuelve así:
     * <ul>
     *   <li>{@code Identificador}: {@code generador.getClaseActual()}, que
     *       {@link Clase#generarC3D} deja fijado mientras recorre sus métodos. Si por
     *       algún motivo no estuviera fijado (p. ej. generación fuera de una clase),
     *       la etiqueta queda con prefijo {@code "?"} — visible al inspeccionar la
     *       tabla y no un crash silencioso.</li>
     *   <li>{@code AccesoCampo}: el nombre cacheado por {@code verificar} a partir de
     *       {@code TipoClase.getDefinicion().getNombre()}.</li>
     * </ul>
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        // 1) Receptor.
        String receptor;
        if (objetivo instanceof Identificador) {
            receptor = "this";
        } else if (objetivo instanceof AccesoCampo ac) {
            ResultadoC3D r = ac.getObjeto().generarC3D(generador);
            receptor = r.getLugar();
        } else {
            throw new UnsupportedOperationException(
                    "Llamada con objetivo " + objetivo.getClass().getSimpleName()
                            + ": no soportado en C3D");
        }

        // 2) Evaluar todos los argumentos, guardando sus lugares.
        List<String> lugaresArgs = new ArrayList<>();
        for (ExpresionZ a : argumentos) {
            ResultadoC3D v = a.generarC3D(generador);
            lugaresArgs.add(v.getLugar());
        }

        // 3) Bloque de params: receptor + args en orden.
        generador.emitirParam(receptor);
        for (String lugar : lugaresArgs) {
            generador.emitirParam(lugar);
        }

        // 4) Llamada. Nombre del método: en ambos casos lo sabemos por el objetivo.
        String metodo = (objetivo instanceof Identificador id)
                ? id.getNombre()
                : ((AccesoCampo) objetivo).getCampo();

        String clase;
        if (objetivo instanceof Identificador) {
            // Llamada propia: la clase activa la fija Clase.generarC3D.
            clase = generador.getClaseActual();
            if (clase == null) clase = "?";
        } else {
            // Llamada sobre objeto: clase cacheada en verificar.
            clase = (nombreClaseObjetivo != null) ? nombreClaseObjetivo : "?";
        }
        String etiqueta = generador.etiquetaMetodo(clase, metodo);

        String t = generador.nuevoTemporal();
        generador.emitirCall(etiqueta, argumentos.size() + 1, t);

        // Tipo del resultado: cacheado por verificar().
        Tipo tipo = (simboloMetodo != null && simboloMetodo.getTipo() != null)
                ? simboloMetodo.getTipo()
                : TipoPrimitivo.DESCONOCIDO;
        return ResultadoC3D.temporal(t, tipo);
    }
}