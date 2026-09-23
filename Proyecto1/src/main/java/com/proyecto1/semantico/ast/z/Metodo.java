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
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/**
 * {@code metodoDef} (#metodoDef): "tipo? ID ( parametros? ) { cuerpo }".
 *
 * <p>Al igual que {@link Constructor}, todo método recibe un "this" implícito extra
 * (por eso {@code begin_func} lleva {@code parametros.size() + 1}).
 *
 * <p>No inyecta field initializers: eso es trabajo exclusivo de los constructores
 * (los initializers corren una sola vez, al construir el objeto).
 */
public final class Metodo extends NodoZ {

    private final String nombre;
    private final List<Parametro> parametros;
    private final NodoTipoRef tipoRetorno; // null si es void
    private final Bloque cuerpo;

    /** Ámbito de la función, cacheado por verificar() y reutilizado por generarC3D(). */
    private AmbitoFuncion ambitoPropio;

    public Metodo(String nombre, List<Parametro> parametros, NodoTipoRef tipoRetorno,
                  Bloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
        this.cuerpo = cuerpo;
    }

    public String getNombre() { return nombre; }
    public List<Parametro> getParametros() { return parametros; }
    public NodoTipoRef getTipoRetorno() { return tipoRetorno; }
    public boolean esVoid() { return tipoRetorno == null; }
    public Bloque getCuerpo() { return cuerpo; }
    public AmbitoFuncion getAmbitoPropio() { return ambitoPropio; }

    /**
     * Idéntico al verificar() que ya tenías, MÁS la línea que cachea {@link AmbitoFuncion}
     * en {@link #ambitoPropio}. Si tu verificar() ya marcaba "tuvoRetorno" o validaba
     * el retorno esperado, se conserva; lo que sigue es la forma genérica:
     */
    public Tipo verificar(AmbitoClase ambClase, ManejadorErrores errores) {
        Simbolo simbolo = ambClase.resolverLocal(nombre);

        AmbitoFuncion amb = new AmbitoFuncion(ambClase, simbolo);
        this.ambitoPropio = amb;  // <-- única línea nueva

        for (Parametro p : parametros) {
            Tipo t = p.resolverTipo(amb, errores);
            Simbolo sp = new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO,
                    t, p.getLinea(), p.getColumna());
            if (!amb.declarar(sp)) {
                errores.reportar(p.getLinea(), p.getColumna(),
                        "Parámetro duplicado: '" + p.getNombre() + "'");
            }
            if (simbolo != null) simbolo.agregarParametro(sp);
        }

        cuerpo.verificar(amb, errores);

        if (!amb.esVoid() && !amb.isTuvoRetorno()) {
            errores.reportar(linea, columna,
                    "El método '" + nombre + "' debe retornar un valor de tipo "
                            + amb.getTipoRetorno().nombre());
        }
        return TipoPrimitivo.VOID;
    }

    /**
     * Emite, en este orden:
     * <ol>
     *   <li>{@code (begin_func, etiquetaMetodo(nombreClase, nombre), parametros.size()+1, null)}.</li>
     *   <li>{@code entrarAmbito(ambitoPropio)}.</li>
     *   <li>C3D del cuerpo.</li>
     *   <li>{@code salirAmbito(anterior)}.</li>
     *   <li>{@code (end_func)}.</li>
     * </ol>
     * La firma lleva {@code nombreClase} porque {@link Metodo} no lo conoce por sí
     * mismo (solo sabe su nombre corto). {@link Clase#generarC3D} lo pasa.
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