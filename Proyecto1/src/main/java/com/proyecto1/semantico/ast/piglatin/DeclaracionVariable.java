package com.proyecto1.semantico.ast.piglatin;

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
}
