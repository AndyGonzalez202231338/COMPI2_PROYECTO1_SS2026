package com.proyecto1.semantico.ast.cuadruplas;

/**
 * Contrato común de toda instrucción de C3D.
 *
 * Antes había una sola clase {@code Cuadrupla(operador, arg1, arg2, resultado)} con
 * cuatro campos String genéricos, y todo el mundo (impresión, backpatching, y en
 * Fase 4 la traducción a C) tenía que hacer switch sobre el String "operador" para
 * saber qué significaban arg1/arg2/resultado en cada caso — significaban cosas
 * distintas según la operación (p. ej. en {@code []=} y {@code .=}, "resultado" es
 * el VALOR a guardar, no un destino).
 *
 * Este archivo lo reemplaza por una interfaz sellada con una clase concreta (record,
 * inmutable) por cada tipo de instrucción, cada una con SUS PROPIOS campos con
 * nombre y tipo reales (p. ej. {@link CuadruplaCall#nArgs()} es un {@code int}, ya
 * no un String que hay que parsear). Así:
 *   - Cada clase es responsable solo de su propia forma y de imprimirse a sí misma
 *     ({@link #toStringLegible()}) — no hay un switch gigante en un solo lugar.
 *   - Agregar una instrucción nueva es agregar una clase nueva + un caso en
 *     {@link VisitanteCuadrupla}, sin tocar las que ya existen.
 *   - El compilador avisa (permits) si falta cubrir un tipo en un sealed hierarchy.
 *   - La Fase 4 (traducir a C) implementa un {@link VisitanteCuadrupla}, con un
 *     método por tipo de instrucción, en vez de un switch sobre Strings.
 *
 * GeneradorC3D sigue exponiendo los mismos métodos emitirXxx(...) que ya
 * usan todos los nodos de Y, Z y PigLatin — por eso este cambio no toca ninguno de
 * esos ~100 archivos: por dentro, cada emitirXxx() ahora construye el record
 * correspondiente en vez del Cuadrupla genérico de antes.
 */
public sealed interface Cuadrupla
        permits CuadruplaSalto,
                CuadruplaBinaria, CuadruplaUnaria, CuadruplaAsignacion, CuadruplaEtiqueta,
                CuadruplaPrint, CuadruplaRead,
                CuadruplaCall, CuadruplaParam, CuadruplaReturn,
                CuadruplaBeginFunc, CuadruplaEndFunc,
                CuadruplaIndiceCarga, CuadruplaIndiceGuarda,
                CuadruplaCampoCarga, CuadruplaCampoGuarda,
                CuadruplaNew, CuadruplaNewArray {

    /** Formato legible para humanos: "t0 = a + b", "goto L1", "if_false t0 goto L2", etc. */
    String toStringLegible();

    /**
     * Punto de entrada del patrón Visitor: cada implementación hace
     * {@code return visitante.visitar(this);} — así el dispatch a la sobrecarga
     * correcta lo resuelve el compilador (por el tipo estático de "this" dentro de
     * cada clase), no un switch sobre un String de operador.
     */
    <T> T aceptar(VisitanteCuadrupla<T> visitante);
}
