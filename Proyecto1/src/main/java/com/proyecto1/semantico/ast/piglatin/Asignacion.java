package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoEstructura;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

/**
 * {@code expresionAsignacion} (#expresionAsignacionDef), cuando trae operador.
 * Es una EXPRESIÓN (no instrucción): {@code a = b = 5} es válido; usada como sentencia
 * queda envuelta en {@link ExpresionStmt}.
 */
public final class Asignacion extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objetivo;
    private final String operador; // "=", "+=", "-=", "*=", "/=", "%="
    private final ExpresionPigLatin valor;

    public Asignacion(ExpresionPigLatin objetivo, String operador, ExpresionPigLatin valor,
                      int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
        this.operador = operador;
        this.valor = valor;
    }

    public ExpresionPigLatin getObjetivo() { return objetivo; }
    public String getOperador() { return operador; }
    public ExpresionPigLatin getValor() { return valor; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tIzq = objetivo.verificar(ambito, errores);
        Tipo tDer = valor.verificar(ambito, errores);

        if (!operador.equals("=")) {
            Tipo r = Tipos.resultadoAritmetico(tIzq, tDer, operador.equals("+="));
            if (r == null)
                errores.reportar(linea, columna, "Operador '" + operador + "' no válido");
        } else if (!Tipos.esAsignable(tIzq, tDer)) {
            errores.reportar(linea, columna,
                    "No se puede asignar " + tDer.nombre() + " a " + tIzq.nombre());
        }

        if (objetivo instanceof Identificador id) {
            Simbolo s = ambito.resolver(id.getNombre());
            if (s != null) s.marcarInicializado();
        }
        return tIzq;
    }

    /**
     * Emite:
     * <ul>
     *   <li>{@code x = v}: el C3D del RHS y la escritura en el lugar del lvalue.</li>
     *   <li>{@code x op= v}: lee el valor actual, emite la binaria, guarda el resultado.</li>
     * </ul>
     *
     * <p><b>Auto-malloc de slots (arr[i]):</b> cuando el lvalue es
     * {@code arr[i].campo} y el elemento del arreglo es una estructura/clase, se
     * emite un chequeo de NULL + {@code malloc} del slot ANTES de escribir el campo.
     * Sin esto, {@code personas[0].nombre = "Carlos"} escribiría sobre un puntero
     * basura del array recién reservado, produciendo segfault en runtime.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        LValue lv = resolverLValue(objetivo, generador);

        if (operador.equals("=")) {
            ResultadoC3D rhs = valor.generarC3D(generador);
            guardarEn(lv, rhs.getLugar(), generador);
            return ResultadoC3D.valor(rhs.getLugar(), lv.tipo());
        }

        ResultadoC3D actual = cargarDe(lv, generador);
        ResultadoC3D rhs    = valor.generarC3D(generador);
        String opBin = operador.substring(0, operador.length() - 1); // "+=" -> "+"
        String t = generador.nuevoTemporal();
        generador.emitirBinaria(opBin, actual.getLugar(), rhs.getLugar(), t);
        guardarEn(lv, t, generador);
        return ResultadoC3D.temporal(t, lv.tipo());
    }

    // ---------- ayudantes privados ----------

    private record LValue(String base, String campo, String indice, Tipo tipo) {}

    private LValue resolverLValue(ExpresionPigLatin objetivo, GeneradorC3D generador) {
        // Caso 1: identificador simple (variable local o parámetro).
        if (objetivo instanceof Identificador id) {
            Tipo tipo = TipoPrimitivo.DESCONOCIDO;
            Ambito amb = generador.getAmbito();
            if (amb != null) {
                Simbolo s = amb.resolver(id.getNombre());
                if (s != null) tipo = s.getTipo();
            }
            return new LValue(id.getNombre(), null, null, tipo);
        }

        // Caso 2: obj.campo
        if (objetivo instanceof AccesoCampo ac) {
            // Subcaso especial: obj.campo donde obj es arr[i] sobre un arreglo de
            // estructuras/clases → auto-malloc del slot antes de escribir el campo.
            if (ac.getObjeto() instanceof Indice ind) {
                return resolverCampoDeElemento(ind, ac, generador);
            }
            ResultadoC3D base = ac.getObjeto().generarC3D(generador);
            Tipo tipo = TipoPrimitivo.DESCONOCIDO;
            return new LValue(base.getLugar(), ac.getCampo(), null, tipo);
        }

        // Caso 3: arr[i]
        if (objetivo instanceof Indice ind) {
            ResultadoC3D base = ind.getArreglo().generarC3D(generador);
            ResultadoC3D idx  = ind.getIndice().generarC3D(generador);
            Tipo tipo = TipoPrimitivo.DESCONOCIDO;
            return new LValue(base.getLugar(), null, idx.getLugar(), tipo);
        }

        throw new UnsupportedOperationException(
                "Asignación a " + objetivo.getClass().getSimpleName() + ": no soportado en C3D");
    }

    /**
     * Maneja el caso {@code arr[i].campo = v} cuando {@code arr} es un arreglo de
     * estructuras o clases. Antes de escribir el campo, garantiza que el slot
     * {@code arr[i]} esté allocado (malloc si es NULL). Sin esto, escribir
     * {@code personas[0].nombre = "..."} sobre un arreglo recién creado con
     * {@code series personas[3] : Persona;} escribiría sobre basura y segfaultearía.
     *
     * <p>C3D emitido:
     * <pre>
     *   t_slot0 = arr[i]
     *   t_cmp   = t_slot0 == NULL
     *   if_false t_cmp goto L_skip
     *   t_new   = new Tipo
     *   arr[i]  = t_new
     *   L_skip:
     *   t_slot  = arr[i]        // recarga tras el posible malloc
     * </pre>
     * El lvalue devuelto usa {@code t_slot} como base. El campo se escribe con
     * {@code t_slot.campo = v} después.
     */
    private LValue resolverCampoDeElemento(Indice ind, AccesoCampo ac, GeneradorC3D generador) {
        ResultadoC3D arrRes = ind.getArreglo().generarC3D(generador);
        ResultadoC3D idxRes = ind.getIndice().generarC3D(generador);
        String arr = arrRes.getLugar();
        String idx = idxRes.getLugar();

        Tipo tipoElem = ind.getTipoElemento();
        String nombreTipo = nombreDeTipoInstanciable(tipoElem);

        // Si el elemento no es estructura/clase, no hay nada que allocar.
        // Usamos el camino normal: leer el elemento y tratar ese temporal como base.
        if (nombreTipo == null) {
            String t = generador.nuevoTemporal();
            generador.emitirCargaIndice(arr, idx, t);
            Tipo tipoCampo = (ac.getTipoCampo() != null) ? ac.getTipoCampo() : TipoPrimitivo.DESCONOCIDO;
            return new LValue(t, ac.getCampo(), null, tipoCampo);
        }

        // Auto-malloc del slot si es NULL.
        String t_slot0 = generador.nuevoTemporal();
        generador.emitirCargaIndice(arr, idx, t_slot0);

        String t_cmp = generador.nuevoTemporal();
        generador.emitirBinaria("==", t_slot0, "NULL", t_cmp);

        String lSkip = generador.nuevaEtiqueta();
        generador.emitirIfFalse(t_cmp, lSkip);

        String t_new = generador.nuevoTemporal();
        generador.emitirNew(nombreTipo, t_new);
        generador.emitirGuardarIndice(arr, idx, t_new);

        generador.emitirEtiqueta(lSkip);

        // Recargar el slot (puede haber cambiado por el malloc de arriba).
        String t_slot = generador.nuevoTemporal();
        generador.emitirCargaIndice(arr, idx, t_slot);

        Tipo tipoCampo = (ac.getTipoCampo() != null) ? ac.getTipoCampo() : TipoPrimitivo.DESCONOCIDO;
        return new LValue(t_slot, ac.getCampo(), null, tipoCampo);
    }

    /**
     * Nombre de clase/estructura si el tipo es instanciable con malloc; null si no.
     * {@code TipoEstructura} y {@code TipoClase} devuelven el nombre del símbolo;
     * cualquier otro tipo (primitivo, arreglo, desconocido) devuelve null.
     */
    private static String nombreDeTipoInstanciable(Tipo t) {
        if (t instanceof TipoEstructura te) return te.getDefinicion().getNombre();
        if (t instanceof TipoClase tc)      return tc.getDefinicion().getNombre();
        return null;
    }

    private ResultadoC3D cargarDe(LValue lv, GeneradorC3D generador) {
        if (lv.campo() != null) {
            String t = generador.nuevoTemporal();
            generador.emitirCargaCampo(lv.base(), lv.campo(), t);
            return ResultadoC3D.temporal(t, lv.tipo());
        }
        if (lv.indice() != null) {
            String t = generador.nuevoTemporal();
            generador.emitirCargaIndice(lv.base(), lv.indice(), t);
            return ResultadoC3D.temporal(t, lv.tipo());
        }
        return ResultadoC3D.valor(lv.base(), lv.tipo());
    }

    private void guardarEn(LValue lv, String v, GeneradorC3D generador) {
        if (lv.campo() != null) {
            generador.emitirGuardarCampo(lv.base(), lv.campo(), v);
        } else if (lv.indice() != null) {
            generador.emitirGuardarIndice(lv.base(), lv.indice(), v);
        } else {
            generador.emitirAsignacion(v, lv.base());
        }
    }
}