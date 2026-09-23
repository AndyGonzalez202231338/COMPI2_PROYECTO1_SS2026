package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.ast.GeneradorC3D;
import com.proyecto1.semantico.ast.ResultadoC3D;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Una {@code declaracionVariableSinPuntoYComa}, usada tanto como instrucción completa
 * ({@code declaracionVariable}, #declaracionVariableDef, con {@code ;}) como
 * "init" desnudo de un {@code per (...)} (#initForDeclaracion, sin {@code ;}). Las
 * tres alternativas de la gramática (#declaracionVarConTipo, #declaracionVarEstructura,
 * #declaracionVarSoloValor) se representan con esta única clase + su
 * {@link CategoriaDeclaracionVariable}, construida siempre a través de una de las tres
 * fábricas estáticas de abajo (así queda imposible construir, por ejemplo, una
 * declaración ESTRUCTURA sin su {@code inicializadorEstructura}) — mismo patrón que
 * {@code Parametro} en Y.
 */
public final class DeclaracionVariable extends NodoPigLatin implements InstruccionPigLatin {

    private final CategoriaDeclaracionVariable categoria;
    private final String nombre;
    private final NodoTipoRef tipo;                       // solo si categoria == CON_TIPO
    private final ExpresionPigLatin inicializador;         // CON_TIPO (opcional) | SOLO_VALOR (obligatorio)
    private final String nombreTipoEstructura;             // solo si categoria == ESTRUCTURA (el ID tras ':')
    private final InicializadorArreglo inicializadorEstructura; // solo si categoria == ESTRUCTURA (obligatorio)

    private DeclaracionVariable(CategoriaDeclaracionVariable categoria, String nombre, NodoTipoRef tipo,
                                 ExpresionPigLatin inicializador, String nombreTipoEstructura,
                                 InicializadorArreglo inicializadorEstructura, int linea, int columna) {
        super(linea, columna);
        this.categoria = categoria;
        this.nombre = nombre;
        this.tipo = tipo;
        this.inicializador = inicializador;
        this.nombreTipoEstructura = nombreTipoEstructura;
        this.inicializadorEstructura = inicializadorEstructura;
    }

    /** {@code esto ID : tipo (= expresion)?} (#declaracionVarConTipo). {@code inicializador} puede ser null. */
    public static DeclaracionVariable conTipo(String nombre, NodoTipoRef tipo, ExpresionPigLatin inicializador,
                                               int linea, int columna) {
        return new DeclaracionVariable(CategoriaDeclaracionVariable.CON_TIPO, nombre, tipo, inicializador,
                null, null, linea, columna);
    }

    /** {@code esto ID : ID inicializadorArreglo} (#declaracionVarEstructura). */
    public static DeclaracionVariable estructura(String nombre, String nombreTipoEstructura,
                                                  InicializadorArreglo inicializadorEstructura,
                                                  int linea, int columna) {
        return new DeclaracionVariable(CategoriaDeclaracionVariable.ESTRUCTURA, nombre, null, null,
                nombreTipoEstructura, inicializadorEstructura, linea, columna);
    }

    /** {@code esto ID : expresion} (#declaracionVarSoloValor). El tipo se infiere del valor. */
    public static DeclaracionVariable soloValor(String nombre, ExpresionPigLatin inicializador,
                                                 int linea, int columna) {
        return new DeclaracionVariable(CategoriaDeclaracionVariable.SOLO_VALOR, nombre, null, inicializador,
                null, null, linea, columna);
    }

    public CategoriaDeclaracionVariable getCategoria() { return categoria; }
    public String getNombre() { return nombre; }
    public NodoTipoRef getTipo() { return tipo; }
    public ExpresionPigLatin getInicializador() { return inicializador; }
    public String getNombreTipoEstructura() { return nombreTipoEstructura; }
    public InicializadorArreglo getInicializadorEstructura() { return inicializadorEstructura; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo t;
        ExpresionPigLatin initAUsar = inicializador;

        switch (categoria) {
            case CON_TIPO:
                t = tipo.resolver(ambito, errores);
                break;
            case SOLO_VALOR:
                // El tipo se infiere del valor
                t = inicializador.verificar(ambito, errores);
                break;
            case ESTRUCTURA:
                Simbolo s = ambito.resolver(nombreTipoEstructura);
                if (s == null) {
                    errores.reportar(linea, columna,
                            "Tipo importado desconocido: '" + nombreTipoEstructura + "'");
                    t = TipoPrimitivo.DESCONOCIDO;
                } else if (s.getCategoria() == CategoriaSimbolo.CLASE) {
                    t = new TipoClase(s);
                } else if (s.getCategoria() == CategoriaSimbolo.ESTRUCTURA) {
                    t = new TipoEstructura(s);
                } else {
                    errores.reportar(linea, columna, "'" + nombreTipoEstructura + "' no es un tipo");
                    t = TipoPrimitivo.DESCONOCIDO;
                }
                if (inicializadorEstructura != null)
                    inicializadorEstructura.verificar(ambito, errores);
                break;
            default:
                t = TipoPrimitivo.DESCONOCIDO;
        }

        Simbolo sim = new Simbolo(nombre, CategoriaSimbolo.VARIABLE, t, linea, columna);
        if (!ambito.declarar(sim)) {
            errores.reportar(linea, columna, "Variable ya declarada: '" + nombre + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }

        if (initAUsar != null && categoria == CategoriaDeclaracionVariable.CON_TIPO) {
            Tipo tInit = initAUsar.verificar(ambito, errores);
            if (!Tipos.esAsignable(t, tInit))
                errores.reportar(linea, columna,
                        "Inicialización incompatible: " + tInit.nombre() + " → " + t.nombre());
        }
        sim.marcarInicializado();
        return TipoPrimitivo.VOID;
    }

    @Override
    public ResultadoC3D generarC3D(GeneradorC3D generador) {
        switch (categoria) {
            case CON_TIPO: {
                // Inicializador opcional.
                if (inicializador != null) {
                    ResultadoC3D v = inicializador.generarC3D(generador);
                    generador.emitirAsignacion(v.getLugar(), nombre);
                }
                return ResultadoC3D.vacio();
            }
            case SOLO_VALOR: {
                // Inicializador obligatorio por gramática.
                ResultadoC3D v = inicializador.generarC3D(generador);
                generador.emitirAsignacion(v.getLugar(), nombre);
                return ResultadoC3D.vacio();
            }
            case ESTRUCTURA: {
                // t = new Tipo (malloc); luego t.campo_i = v_i en el ORDEN de declaración
                // de los campos (getMiembrosEnOrden(), no getMiembros()); luego nombre = t.
                Ambito amb = generador.getAmbito();
                Simbolo s = (amb != null) ? amb.resolver(nombreTipoEstructura) : null;

                String t = generador.nuevoTemporal();
                generador.emitirNew(nombreTipoEstructura, t);

                // Filtrar SOLO campos/atributos (no métodos ni constructores), en orden.
                List<Simbolo> campos = new ArrayList<>();
                if (s != null) {
                    for (Simbolo m : s.getMiembrosEnOrden()) {
                        if (m.getCategoria() == CategoriaSimbolo.CAMPO
                                || m.getCategoria() == CategoriaSimbolo.ATRIBUTO) {
                            campos.add(m);
                        }
                    }
                }

                // Emparejamiento posicional con los elementos del inicializador.
                List<ExpresionPigLatin> valores = inicializadorEstructura.getElementos();
                for (int i = 0; i < campos.size() && i < valores.size(); i++) {
                    ResultadoC3D v = valores.get(i).generarC3D(generador);
                    generador.emitirGuardarCampo(t, campos.get(i).getNombre(), v.getLugar());
                }

                generador.emitirAsignacion(t, nombre);
                return ResultadoC3D.vacio();
            }
            default:
                return ResultadoC3D.vacio();
        }
    }
}
