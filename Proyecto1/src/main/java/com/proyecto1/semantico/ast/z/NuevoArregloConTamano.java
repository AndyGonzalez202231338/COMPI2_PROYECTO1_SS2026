package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code NEW tipoBase (CORIZQ expression CORDER)+} (#primarioArregloConTamano):
 * "new int[5]" o "new int[3][3]". Los tamaños son expresiones en runtime.
 */
public final class NuevoArregloConTamano extends NodoZ implements ExpresionZ {

    private final NodoTipoRef tipoElemento;
    private final List<ExpresionZ> tamanos;

    public NuevoArregloConTamano(NodoTipoRef tipoElemento, List<ExpresionZ> tamanos,
                                 int linea, int columna) {
        super(linea, columna);
        this.tipoElemento = tipoElemento;
        this.tamanos = tamanos;
    }

    public NodoTipoRef getTipoElemento() { return tipoElemento; }
    public List<ExpresionZ> getTamanos()  { return tamanos; }
    public int getDimensiones()           { return tamanos.size(); }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo base = tipoElemento.resolver(ambito, errores);
        for (ExpresionZ tam : tamanos) {
            Tipo tTam = tam.verificar(ambito, errores);
            if (!Tipos.esIndiceValido(tTam))
                errores.reportar(tam.getLinea(), tam.getColumna(),
                        "Tamaño de arreglo debe ser entero, se recibió " + tTam.nombre());
        }
        for (int i = 0; i < tamanos.size(); i++) base = new TipoArreglo(base);
        return base;
    }

    /**
     * Emite: evalúa todos los tamaños en orden (efectos laterales una sola vez), luego
     * la asignación recursiva del arreglo jagged.
     *
     * <p>Caso 1D ({@code new int[n]}): UNA cuádrupla
     * {@code (newarr, "int", n, t)} y se devuelve {@code t}.
     *
     * <p>Caso N-D ({@code new int[n][m]}):
     * <ol>
     *   <li>Asigna el arreglo externo: {@code t0 = newarr("int[]", n)} (los elementos
     *       son referencias, no ints).</li>
     *   <li>Emite un bucle {@code for i in 0..n} que, por cada slot, hace
     *       {@code t0[i] = allocRec(nivel+1)} — recursivamente el mismo tratamiento
     *       hasta llegar a la última dimensión, donde es 1D.</li>
     * </ol>
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        // Evaluar cada tamaño en orden, guardando el resultado.
        List<ResultadoC3D> tamRes = new ArrayList<>();
        for (ExpresionZ tam : tamanos) {
            tamRes.add(tam.generarC3D(generador));
        }

        String resultado = allocArregloRec(generador, tamRes, 0, tipoElemento.getNombreBase());

        Tipo tipoResultado = verificar(null, null);
        return ResultadoC3D.temporal(resultado, tipoResultado);
    }

    /**
     * Asigna (recursivamente) el arreglo del nivel {@code nivel} con los tamaños
     * {@code tamRes}. Devuelve el temporal con la referencia al arreglo creado.
     *
     * <ul>
     *   <li>Si {@code nivel == tamRes.size()-1} (última dimensión): es un arreglo 1D
     *       de {@code tipoBaseEscalar}, se emite directo sin bucle.</li>
     *   <li>Si no: se asigna el arreglo externo y se rellena con un bucle que llama
     *       recursivamente a {@code allocArregloRec(nivel+1)} por cada slot.</li>
     * </ul>
     */
    private String allocArregloRec(GeneradorC3D g, List<ResultadoC3D> tamRes,
                                   int nivel, String tipoBaseEscalar) {
        ResultadoC3D tam = tamRes.get(nivel);
        boolean esUltimaDimension = (nivel == tamRes.size() - 1);

        // Descriptor del tipo de los elementos que guardará este arreglo:
        //   - Última dimensión -> tipo escalar (int, Persona, ...)
        //   - Si no           -> tipo con tantos "[]" como dimensiones restantes
        // El "[]" indica a Fase 4 que el elemento es una REFERENCIA (puntero), no valor.
        String descriptor = esUltimaDimension
                ? tipoBaseEscalar
                : tipoBaseEscalar + "[]".repeat(tamRes.size() - nivel - 1);

        String arr = g.nuevoTemporal();
        g.emitirNewArray(descriptor, tam.getLugar(), arr);

        if (esUltimaDimension) {
            return arr;   // 1D: nada más que hacer
        }

        // Bucle i = 0; i < tam; i++: arr[i] = allocArregloRec(nivel+1)
        String i = g.nuevoTemporal();
        g.emitirAsignacion("0", i);

        String L0 = g.nuevaEtiqueta();
        String L1 = g.nuevaEtiqueta();
        g.emitirEtiqueta(L0);

        String cond = g.nuevoTemporal();
        g.emitirBinaria("<", i, tam.getLugar(), cond);
        g.emitirIfFalse(cond, L1);

        String inner = allocArregloRec(g, tamRes, nivel + 1, tipoBaseEscalar);
        g.emitirGuardarIndice(arr, i, inner);

        String next = g.nuevoTemporal();
        g.emitirBinaria("+", i, "1", next);
        g.emitirAsignacion(next, i);
        g.emitirGoto(L0);
        g.emitirEtiqueta(L1);

        return arr;
    }
}