package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
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
     *   <li>{@code x = v}: el C3D del RHS y la escritura en el lugar del lvalue.
     *       Devuelve el lugar del RHS con el tipo del lvalue (para que
     *       {@code a = b = 5} encadene).</li>
     *   <li>{@code x op= v}: lee el valor actual del lvalue ({@code cargarDe}), emite
     *       la binaria {@code t = actual op v}, guarda {@code t} en el lvalue y devuelve
     *       {@code temporal(t, tipoLvalue)}.</li>
     * </ul>
     *
     * <p><b>Orden de evaluación (coherente con Z):</b> primero se resuelve el lvalue
     * (evalúa subexpresiones del lvalue: {@code obj} en {@code obj.f = v}, {@code arr}
     * e {@code idx} en {@code arr[i] = v}), después el RHS. Así {@code arr[i()] = f()}
     * evalúa {@code i()} antes que {@code f()}.
     *
     * <p>Sin rama de categoría ATRIBUTO: en PigLatin no hay "this" ni métodos con self
     * implícito. Los 3 casos del lvalue son: {@link Identificador} (variable local o
     * parámetro), {@link AccesoCampo}, {@link Indice}. Los dos últimos se dejan armados
     * con los emisores que ya expone {@link GeneradorC3D}
     * ({@code emitirCargaCampo}/{@code emitirGuardarCampo}/{@code emitirCargaIndice}/
     * {@code emitirGuardarIndice}); sus respectivos {@code generarC3D} se implementarán
     * en la siguiente fase.
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

    /**
     * Descripción "sin resolver a dirección" de un lvalue: guarda los lugares donde
     * quedaron la base (nombre de variable o temporal), el nombre del campo (si aplica)
     * y el índice ya evaluado (si aplica). Solo uno de {campo, indice} es no-null.
     */
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
            ResultadoC3D base = ac.getObjeto().generarC3D(generador);
            // TODO(Fase siguiente): cuando AccesoCampo tenga generarC3D y cachee el tipo
            // del campo (patrón de Z), usar ac.getTipoCampo() en vez de DESCONOCIDO.
            Tipo tipo = TipoPrimitivo.DESCONOCIDO;
            return new LValue(base.getLugar(), ac.getCampo(), null, tipo);
        }

        // Caso 3: arr[i]
        if (objetivo instanceof Indice ind) {
            ResultadoC3D base = ind.getArreglo().generarC3D(generador);
            ResultadoC3D idx  = ind.getIndice().generarC3D(generador);
            // TODO(Fase siguiente): cuando Indice tenga generarC3D y cachee el tipo del
            // elemento (patrón de Z), usar ind.getTipoElemento() en vez de DESCONOCIDO.
            Tipo tipo = TipoPrimitivo.DESCONOCIDO;
            return new LValue(base.getLugar(), null, idx.getLugar(), tipo);
        }

        throw new UnsupportedOperationException(
                "Asignación a " + objetivo.getClass().getSimpleName() + ": no soportado en C3D");
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