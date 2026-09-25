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
 * {@code constructorDef} (#constructorDef): "NombreClase ( parametros? ) { cuerpo }".
 *
 * <p>El nombre del constructor coincide con el de su clase; {@link #nombre} guarda ese
 * nombre (es lo que después alimenta a
 * {@link GeneradorC3D#etiquetaConstructor(String, int)}).
 *
 * <p>Todo método/constructor de Z recibe un parámetro implícito extra: "this". Por eso
 * el {@code begin_func} lleva {@code parametros.size() + 1} argumentos. El "this" no se
 * declara como símbolo en la tabla (no es un identificador resoluble); es una
 * convención de generación: cualquier acceso a un atributo emite
 * {@code (=., this, campo, t)} / {@code (.=, this, campo, v)}.
 *
 * <p>Antes del cuerpo del usuario, se inyectan los inicializadores de atributo de la
 * clase (field initializers), en el orden en que aparecen en la clase. Es el mismo
 * comportamiento que Java: los inicializadores de campo corren antes del cuerpo del
 * constructor.
 */
public final class Constructor extends NodoZ /* o la base que ya uses */ {

    private final String nombre;              // == nombre de la clase
    private final List<Parametro> parametros;
    private final Bloque cuerpo;              // o InstruccionZ, según tu gramática

    /**
     * Ámbito de la función, creado y validado por verificar() y reutilizado por
     * generarC3D() para no reconstruir parámetros ni volver a tocar ManejadorErrores.
     * Es el mismo patrón que AccesoCampo usa para cachear tipoCampo.
     */
    private AmbitoFuncion ambitoPropio;
    private String nombreClaseReal;

    public Constructor(String nombre, List<Parametro> parametros, Bloque cuerpo,
                       int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = parametros;
        this.cuerpo = cuerpo;
    }

    public String getNombre() { return nombre; }
    public List<Parametro> getParametros() { return parametros; }
    public Bloque getCuerpo() { return cuerpo; }
    public AmbitoFuncion getAmbitoPropio() { return ambitoPropio; }

    /**
     * Idéntico al verificar() que ya tenías, MÁS una línea al final del setup del
     * ámbito: cachear el {@link AmbitoFuncion} recién creado en {@link #ambitoPropio}
     * para que generarC3D() lo reutilice.
     */
    public Tipo verificar(AmbitoClase ambClase, ManejadorErrores errores) {
        // (1) Guardar el nombre REAL de la clase para usarlo al generar la etiqueta C3D.
        //     El error por nombre incorrecto YA se reportó en AnalizadorSemanticoZ;
        //     aquí no se vuelve a chequear (evita el mensaje duplicado).
        this.nombreClaseReal = ambClase.getSimboloContenedor().getNombre();

        // (2) Lookup con clave específica: nombreClaseReal#aridad#Tipo1#Tipo2...
        StringBuilder sb = new StringBuilder(nombreClaseReal)
                .append("#").append(parametros.size());
        for (Parametro p : parametros) {
            Tipo tp = p.getTipo().resolver(ambClase, errores);
            sb.append("#").append(tp.nombre());
        }
        Simbolo simbolo = ambClase.getSimboloContenedor().buscarMiembro(sb.toString());

        AmbitoFuncion amb = new AmbitoFuncion(ambClase, simbolo);
        this.ambitoPropio = amb;

        for (Parametro p : parametros) {
            Tipo t = p.resolverTipo(amb, errores);
            Simbolo sp = new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO,
                    t, p.getLinea(), p.getColumna());
            if (!amb.declarar(sp)) {
                errores.reportar(p.getLinea(), p.getColumna(),
                        "Parámetro duplicado: '" + p.getNombre() + "'");
            }
            // (3) Se ELIMINA el "if (simbolo != null) simbolo.agregarParametro(sp);"
            //     que tenías: registrarParametros() en el analizador YA los agregó,
            //     y hacerlo aquí duplicaba la lista en el símbolo.
        }

        cuerpo.verificar(amb, errores);
        return TipoPrimitivo.VOID;
    }


    /**
     * Emite, en este orden:
     * <ol>
     *   <li>{@code (begin_func, etiquetaConstructor(nombre, aridad), parametros.size()+1, null)}.
     *       El +1 es el "this" implícito.</li>
     *   <li>{@code entrarAmbito(ambitoPropio)}.</li>
     *   <li>Por cada atributo con inicializador no nulo:
     *       {@code this.<campo> = <C3D del inicializador>} (una cuádrupla {@code (.=, this, campo, v)}).</li>
     *   <li>C3D del cuerpo del usuario.</li>
     *   <li>{@code salirAmbito(anterior)}.</li>
     *   <li>{@code (end_func)}.</li>
     * </ol>
     * No emite un {@code return} implícito; Fase 4 puede añadirlo al traducir
     * {@code end_func}.
     *
     * <p>La firma lleva {@code atributosClase} porque el constructor debe inyectar los
     * field initializers de la clase y {@link Constructor} por sí solo no los conoce.
     * {@link Clase#generarC3D} es quien los pasa.
     */
    public ResultadoC3D generarC3D(GeneradorC3D generador, List<Atributo> atributosClase) {
        // Usar el nombre real de la clase (si verificar ya corrió), no el declarado.
        String nombreParaEtiqueta = (nombreClaseReal != null) ? nombreClaseReal : nombre;
        String etiqueta = generador.etiquetaConstructor(nombreParaEtiqueta, parametros.size());
        generador.emitirBeginFunc(etiqueta, parametros.size() + 1);

        Ambito anterior = generador.entrarAmbito(ambitoPropio);

        for (Atributo a : atributosClase) {
            if (a.getInicializador() != null) {
                ResultadoC3D v = a.getInicializador().generarC3D(generador);
                generador.emitirGuardarCampo("this", a.getNombre(), v.getLugar());
            }
        }

        cuerpo.generarC3D(generador);

        generador.salirAmbito(anterior);
        generador.emitirEndFunc();
        return ResultadoC3D.vacio();
    }
}