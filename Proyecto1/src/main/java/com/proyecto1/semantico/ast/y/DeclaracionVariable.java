package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoArreglo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;
import com.proyecto1.semantico.tipos.Tipos;

import java.util.List;

/** {@code declaracionVariable} (#declVarDef) usada como instrucción (#instDeclaracion). */
public final class DeclaracionVariable extends NodoY implements InstruccionY {

    private final NodoTipoRef tipo;
    private final String nombre;
    private final List<Integer> tamanosArreglo; // vacío si no es arreglo
    private final ExpresionY inicializador;      // null si no hay "= expresion"

    public DeclaracionVariable(NodoTipoRef tipo, String nombre, List<Integer> tamanosArreglo,
                               ExpresionY inicializador, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
        this.tamanosArreglo = tamanosArreglo;
        this.inicializador = inicializador;
    }

    public NodoTipoRef getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }
    public List<Integer> getTamanosArreglo() {
        return tamanosArreglo;
    }

    public ExpresionY getInicializador() {
        return inicializador;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo t = tipo.resolver(ambito, errores);

        // Si es arreglo, envolver en TipoArreglo (un nivel, según Y?)
        if (!tamanosArreglo.isEmpty()) {
            // Validar que solo tenga UN nivel (Y? no admite multi-dimensional)
            if (tamanosArreglo.size() > 1)
                errores.reportar(linea, columna, "Y? solo admite arreglos de un nivel");
            t = new TipoArreglo(t);
        }

        Simbolo s = new Simbolo(nombre, CategoriaSimbolo.VARIABLE, t, linea, columna);
        if (!tamanosArreglo.isEmpty()) s.getTamanosArreglo().addAll(tamanosArreglo);

        if (!ambito.declarar(s)) {
            errores.reportar(linea, columna, "Variable ya declarada en este ámbito: '" + nombre + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }

        if (inicializador != null) {
            Tipo tInit = inicializador.verificar(ambito, errores);
            if (!Tipos.esAsignable(t, tInit))
                errores.reportar(linea, columna,
                        "Inicialización incompatible: " + tInit.nombre() + " → " + t.nombre());
            s.marcarInicializado();
        }
        return TipoPrimitivo.VOID;
    }

    /**
     * Emite: NADA si no hay inicializador (en C3D "reservar" la variable no necesita
     * cuádrupla: el tipo y nombre viven en la tabla de símbolos, que la Fase 4 usará
     * para declararla en C). Con inicializador: primero el C3D de la expresión y luego
     * {@code (=, v, null, nombre)}, es decir {@code nombre = v}.
     * Devuelve {@code ResultadoC3D.vacio()}.
     *
     * Las declaraciones de arreglo lanzan {@link UnsupportedOperationException}: quedan
     * fuera de la Fase 1.1.
     */
    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        if (!tamanosArreglo.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Declaración de arreglos: pendiente en C3D");
        }
        if (inicializador != null) {
            //entero x   este no se le inicaliza con valor
            ResultadoC3D v = inicializador.generarC3D(generador); // x = 1
            //(1, x)
            generador.emitirAsignacion(v.getLugar(), nombre);
        }
        return ResultadoC3D.vacio();
    }
}