package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoClase;
import com.proyecto1.semantico.tabla.AmbitoFuncion;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;

import java.util.List;

/**
 * Un {@code methodDeclaration} (#methodDeclarationDef): "public (tipo|void) Nombre(params) bloque".
 * A diferencia de {@code Funcion} de Y? (donde omitir "-> tipo" significa void),
 * aquí SIEMPRE hay una de las dos alternativas presente en la gramática (tipo o la
 * palabra reservada VOID); {@code tipoRetorno == null} representa justamente el caso
 * "era VOID", para que {@link #esVoid()} funcione igual que en Y?.
 */
public final class Metodo extends NodoZ {

    private final String nombre;
    private final List<Parametro> parametros;
    private final NodoTipoRef tipoRetorno; // null == era "void"
    private final Bloque cuerpo;

    /**
     * Ámbito de la función, creado por verificar() y reutilizado por generarC3D().
     * Mismo patrón que {@code AccesoCampo} usa para cachear {@code tipoCampo}: se
     * evita reconstruir parámetros y volver a tocar {@link ManejadorErrores} en la
     * fase de generación. Es null si verificar() aún no corrió.
     */
    private AmbitoFuncion ambitoPropio;

    public Metodo(String nombre, List<Parametro> parametros, NodoTipoRef tipoRetorno,
                  Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
        this.cuerpo = cuerpo;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Parametro> getParametros() {
        return parametros;
    }

    public NodoTipoRef getTipoRetorno() {
        return tipoRetorno;
    }

    public boolean esVoid() {
        return tipoRetorno == null;
    }

    public Bloque getCuerpo() {
        return cuerpo;
    }

    public AmbitoFuncion getAmbitoPropio() {
        return ambitoPropio;
    }

    public void verificar(AmbitoClase ambClase, ManejadorErrores errores) {
        Simbolo simbolo = ambClase.getSimboloContenedor().buscarMiembro(nombre + "#" + parametros.size());
        AmbitoFuncion amb = new AmbitoFuncion(ambClase, simbolo);
        this.ambitoPropio = amb;   // <-- única línea nueva respecto al original

        // Los parámetros YA quedaron registrados en simbolo (con su tipo) por
        // AnalizadorSemanticoZ.registrarMiembros -- es parte de la FIRMA, se resuelve antes de
        // verificar ningún cuerpo (ver su Javadoc). Aquí solo falta declararlos como variables
        // LOCALES de este método, para que el cuerpo pueda usarlos por nombre.
        for (Parametro p : parametros) {
            Tipo t = p.resolverTipo(amb, errores);
            Simbolo sp = new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO, t, p.getLinea(), p.getColumna());
            if (!amb.declarar(sp))
                errores.reportar(p.getLinea(), p.getColumna(), "Parámetro duplicado: '" + p.getNombre() + "'");
        }

        cuerpo.verificar(amb, errores);

        if (!esVoid() && !amb.isTuvoRetorno())
            errores.reportar(linea, columna,
                    "El método '" + nombre + "' debe retornar un valor de tipo " + amb.getTipoRetorno().nombre());
    }

    /**
     * Emite, en este orden:
     * <ol>
     *   <li>{@code (begin_func, etiquetaMetodo(nombreClase, nombre), parametros.size()+1, null)}.
     *       El {@code +1} es el "this" implícito que todo método de Z recibe.</li>
     *   <li>{@code entrarAmbito(ambitoPropio)} para que, mientras se genera el cuerpo,
     *       cualquier {@code Identificador} que resuelva a un ATRIBUTO emita
     *       {@code (=., this, campo, t)} en vez de tratarlo como variable local. Y para
     *       que cualquier {@code Llamada} con objetivo {@link Identificador} lea del
     *       generador el nombre de la clase actual ({@code generador.getClaseActual()},
     *       que {@link Clase#generarC3D} deja fijado antes de recorrer sus métodos).</li>
     *   <li>C3D del cuerpo.</li>
     *   <li>{@code salirAmbito(anterior)} para restaurar el ámbito previo.</li>
     *   <li>{@code (end_func)}.</li>
     * </ol>
     * Devuelve {@code ResultadoC3D.vacio()}.
     *
     * <p>La firma lleva {@code nombreClase} porque {@link Metodo} por sí solo no conoce
     * su clase contenedora (solo sabe su nombre corto). {@link Clase#generarC3D} es
     * quien lo pasa al iterar sus métodos.
     *
     * <p>No emite un {@code return} implícito al final de métodos void; la Fase 4 puede
     * resolverlo al traducir {@code end_func}.
     */
    public ResultadoC3D generarC3D(GeneradorC3D generador, String nombreClase) {
        String etiqueta = generador.etiquetaMetodo(nombreClase, nombre);
        generador.emitirBeginFunc(etiqueta, parametros.size() + 1);

        Ambito anterior = generador.entrarAmbito(ambitoPropio);
        cuerpo.generarC3D(generador);
        generador.salirAmbito(anterior);

        generador.emitirEndFunc();
        return ResultadoC3D.vacio();
    }
}