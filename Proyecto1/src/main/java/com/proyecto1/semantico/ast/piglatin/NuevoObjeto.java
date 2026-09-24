package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoEstructura;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code primaria NUEVO ID LPAREN argumentos? RPAREN} (#primariaInstancia): creación
 * de un objeto nuevo. El ID puede referirse a una CLASE (importada de {@code .z},
 * tiene constructor) o a una ESTRUCTURA (importada de {@code .y}, sin constructor:
 * los argumentos se asignan posicionalmente a los campos en orden de declaración).
 */
public final class NuevoObjeto extends NodoPigLatin implements ExpresionPigLatin {

    private final String nombreTipo;
    private final List<ExpresionPigLatin> argumentos;

    public NuevoObjeto(String nombreTipo, List<ExpresionPigLatin> argumentos, int linea, int columna) {
        super(linea, columna);
        this.nombreTipo = nombreTipo;
        this.argumentos = argumentos;
    }

    public String getNombreTipo() { return nombreTipo; }
    public List<ExpresionPigLatin> getArgumentos() { return argumentos; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Simbolo s = ambito.ambitoGlobal().resolverLocal(nombreTipo);
        if (s == null) {
            errores.reportar(linea, columna, "Tipo desconocido: '" + nombreTipo + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }

        if (s.getCategoria() == CategoriaSimbolo.CLASE) {
            // Verificación de aridad contra el constructor (mismo patrón que Z).
            boolean hayConstructor = s.getMiembros().valores().stream()
                    .anyMatch(m -> m.getCategoria() == CategoriaSimbolo.CONSTRUCTOR
                            && m.getParametros().size() == argumentos.size());
            if (!hayConstructor)
                errores.reportar(linea, columna,
                        "No existe constructor de '" + nombreTipo + "' con " +
                                argumentos.size() + " argumentos");
            for (ExpresionPigLatin a : argumentos) a.verificar(ambito, errores);
            return new TipoClase(s);
        }

        if (s.getCategoria() == CategoriaSimbolo.ESTRUCTURA) {
            // Verificación de aridad contra los campos declarados.
            long campos = s.getMiembrosEnOrden().stream()
                    .filter(m -> m.getCategoria() == CategoriaSimbolo.CAMPO)
                    .count();
            if (campos != argumentos.size())
                errores.reportar(linea, columna,
                        "La estructura '" + nombreTipo + "' tiene " + campos +
                                " campos, se dieron " + argumentos.size() + " valores");
            for (ExpresionPigLatin a : argumentos) a.verificar(ambito, errores);
            return new TipoEstructura(s);
        }

        errores.reportar(linea, columna, "'" + nombreTipo + "' no es un tipo instanciable");
        return TipoPrimitivo.DESCONOCIDO;
    }

    /**
     * Emite, en este orden:
     * <ol>
     *   <li>{@code (new, nombreTipo, null, t)}: reserva la celda en heap; {@code t}
     *       es la referencia al objeto recién creado.</li>
     *   <li>Según la categoría del símbolo resuelto:
     *       <ul>
     *         <li><b>CLASE</b>: como {@code NuevoObjeto(Z)}. Evalúa los argumentos en
     *             orden, emite {@code param(t)} + un {@code param} por argumento, y
     *             {@code (call, etiquetaConstructor(nombreTipo, nArgs), nArgs+1, null)}.
     *             El {@code +1} es el {@code this} del constructor.</li>
     *         <li><b>ESTRUCTURA</b>: asignación posicional de campos, mismo patrón que
     *             el caso ESTRUCTURA de {@code DeclaracionVariable(PigLatin)}. Filtra
     *             {@code s.getMiembrosEnOrden()} quedándose solo con los de categoría
     *             {@code CAMPO}, y empareja por posición con los argumentos: por cada
     *             {@code i}, genera el C3D del argumento y emite
     *             {@code (.=, t, campo_i, v_i)}.</li>
     *         <li>Otra categoría o símbolo null: solo se emite el {@code new}, sin
     *             inicialización. Degradación controlada.</li>
     *       </ul>
     *   </li>
     * </ol>
     * Devuelve {@code ResultadoC3D.temporal(t, tipo)} donde {@code tipo} es
     * {@link TipoClase} o {@link TipoEstructura} según corresponda (o
     * {@link TipoPrimitivo#DESCONOCIDO} si no se pudo resolver).
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        Ambito amb = generador.getAmbito();
        Simbolo s = (amb != null) ? amb.ambitoGlobal().resolverLocal(nombreTipo) : null;

        String t = generador.nuevoTemporal();
        generador.emitirNew(nombreTipo, t);

        if (s != null && s.getCategoria() == CategoriaSimbolo.CLASE) {
            // --- Caso CLASE: constructor con `this` + args ---
            List<String> lugaresArgs = new ArrayList<>();
            for (ExpresionPigLatin a : argumentos) {
                ResultadoC3D v = a.generarC3D(generador);
                lugaresArgs.add(v.getLugar());
            }
            generador.emitirParam(t);
            for (String lugar : lugaresArgs) {
                generador.emitirParam(lugar);
            }
            String etiqueta = generador.etiquetaConstructor(nombreTipo, argumentos.size());
            generador.emitirCall(etiqueta, argumentos.size() + 1, null);

            return ResultadoC3D.temporal(t, new TipoClase(s));
        }

        if (s != null && s.getCategoria() == CategoriaSimbolo.ESTRUCTURA) {
            // --- Caso ESTRUCTURA: asignación posicional de campos ---
            List<Simbolo> campos = new ArrayList<>();
            for (Simbolo m : s.getMiembrosEnOrden()) {
                if (m.getCategoria() == CategoriaSimbolo.CAMPO) {
                    campos.add(m);
                }
            }
            for (int i = 0; i < campos.size() && i < argumentos.size(); i++) {
                ResultadoC3D v = argumentos.get(i).generarC3D(generador);
                generador.emitirGuardarCampo(t, campos.get(i).getNombre(), v.getLugar());
            }
            return ResultadoC3D.temporal(t, new TipoEstructura(s));
        }

        // Símbolo no resuelto o categoría inesperada: solo el `new`, sin init.
        return ResultadoC3D.temporal(t, TipoPrimitivo.DESCONOCIDO);
    }
}