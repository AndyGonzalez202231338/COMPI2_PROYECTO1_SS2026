package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;

import java.util.List;

/**
 * {@code NEW tipoBase (CORIZQ CORDER)+ LLAVEIZQ initializerList? LLAVEDER}
 * (#primarioArregloConInicializador): "new int[]{1, 2, 3}" o "new int[][]{{1,2},{3,4}}".
 */
public final class NuevoArregloConInicializador extends NodoZ implements ExpresionZ {

    private final NodoTipoRef tipoElemento;
    private final int dimensiones;
    private final List<ExpresionZ> elementos;

    public NuevoArregloConInicializador(NodoTipoRef tipoElemento, int dimensiones,
                                        List<ExpresionZ> elementos, int linea, int columna) {
        super(linea, columna);
        this.tipoElemento = tipoElemento;
        this.dimensiones = dimensiones;
        this.elementos = elementos;
    }

    public NodoTipoRef getTipoElemento() { return tipoElemento; }
    public int getDimensiones()           { return dimensiones; }
    public List<ExpresionZ> getElementos() { return elementos; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo base = tipoElemento.resolver(ambito, errores);
        for (ExpresionZ e : elementos) e.verificar(ambito, errores);
        for (int i = 0; i < dimensiones; i++) base = new TipoArreglo(base);
        return base;
    }

    /**
     * Emite el arreglo jagged construido por sus elementos:
     * <ol>
     *   <li>Calcula el tamaño del nivel externo ({@code elementos.size()}).</li>
     *   <li>Elige el descriptor de tipo del elemento:
     *       <ul>
     *         <li>Si {@code dimensiones == 1}: escalar, "int" → guarda VALORES.</li>
     *         <li>Si {@code dimensiones > 1}: "int[]" (o "Persona[]"…) → guarda
     *             REFERENCIAS a sub-arreglos; cada elemento ya producirá su propio
     *             newarr al llamar a su {@code generarC3D}.</li>
     *       </ul>
     *   </li>
     *   <li>Emite {@code (newarr, descriptor, tamaño, t)}.</li>
     *   <li>Por cada elemento, genera su C3D y lo guarda en el slot correspondiente
     *       con {@code ([]=, t, i, v)}. Si el elemento es un sub-arreglo (1D anidado
     *       o {@code NuevoArregloConInicializador}), {@code v} es la referencia que
     *       ese sub-nodo ya generó.</li>
     * </ol>
     *
     * <p>No hay caso especial para "escalar vs sub-arreglo": el mismo
     * {@code ([]=, t, i, v)} funciona en los dos casos. La diferencia (guardar valor
     * o guardar puntero) la resuelve Fase 4 mirando el tipo del slot.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        String descriptor = descriptorDeElemento();
        String tam = String.valueOf(elementos.size());
        String arr = generador.nuevoTemporal();
        generador.emitirNewArray(descriptor, tam, arr);

        for (int i = 0; i < elementos.size(); i++) {
            ResultadoC3D v = elementos.get(i).generarC3D(generador);
            generador.emitirGuardarIndice(arr, String.valueOf(i), v.getLugar());
        }

        Tipo tipoResultado = verificar(null, null);
        return ResultadoC3D.temporal(arr, tipoResultado);
    }

    /**
     * "int" si es 1D; "int[]" si es 2D; "int[][]" si es 3D; idem para clases.
     * El sufijo "[]" indica a Fase 4 que ese nivel guarda referencias, no valores.
     */
    private String descriptorDeElemento() {
        String base = tipoElemento.getNombreBase();
        if (dimensiones == 1) return base;
        return base + "[]".repeat(dimensiones - 1);
    }
}