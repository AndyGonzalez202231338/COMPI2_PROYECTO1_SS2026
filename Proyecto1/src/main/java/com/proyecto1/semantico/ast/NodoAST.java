package com.proyecto1.semantico.ast;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;

/**
 * Contrato que implementa CADA nodo del AST (de Y?, de Zetariano, y más adelante de
 * Pig Latin). Es la pieza que evita tener una sola clase "AnalizadorSemantico" con
 * cientos de líneas y un switch gigante: en vez de eso, cada nodo concreto
 * (NodoAsignacion, NodoLlamada, NodoSi, etc.) implementa su propio verificar(...) con
 * SOLO las reglas que le corresponden a él, y llama recursivamente a verificar(...) de
 * sus hijos para que ellos hagan lo mismo con las suyas. El "analizador semántico" de
 * cada lenguaje (AnalizadorSemanticoY, AnalizadorSemanticoZ) termina siendo apenas el
 * punto de entrada: arma el AmbitoGlobal y llama raiz.verificar(ambitoGlobal, errores).
 *
 * MISMA IDEA para la Fase 3: en vez de escribir un generador de código que tenga que
 * conocer la forma exacta de cada tipo de nodo (otro switch gigante, esta vez sobre
 * "qué cuádruplas emitir"), cada nodo implementará generarC3D(...) y sabrá emitir SU
 * PROPIA traducción, delegando en sus hijos para las suyas — el generador de código
 * (GeneradorC3D) solo aporta servicios compartidos (pedir un temporal nuevo, pedir una
 * etiqueta nueva, la lista de cuádruplas ya emitidas).
 */
public interface NodoAST {

    int getLinea();

    int getColumna();

    /**
     * Revisa las reglas semánticas que le tocan a ESTE nodo (y, dentro de su propia
     * implementación, llama a verificar(...) de sus hijos para que hagan lo mismo).
     * Reporta directamente a "errores" cualquier problema que encuentre, usando su
     * propia línea/columna (getLinea()/getColumna()), y NUNCA lanza excepciones por un
     * error semántico — así el análisis puede seguir y reportar más de un error por
     * pasada.
     *
     * @param ambito  ámbito en el que aparece este nodo (para declarar/resolver símbolos)
     * @param errores recolector compartido donde reportar cualquier problema encontrado
     * @return el tipo resultante de este nodo (TipoPrimitivo.DESCONOCIDO si no aplica o
     *         si ya hubo un error y no tiene sentido seguir infiriendo un tipo real)
     */
    Tipo verificar(Ambito ambito, ManejadorErrores errores);

    /**
     * Fase 3: el nodo se traduce a sí mismo a Código de Tres Direcciones. Implementación
     * real pendiente (es la siguiente fase del proyecto); se deja como default con
     * excepción para que TODO lo de la Fase 2 compile y funcione ya sin depender de que
     * esto esté resuelto, pero la firma queda lista para que cada nodo concreto la
     * sobreescriba cuando toque, sin tener que rediseñar nada de lo hecho aquí.
     */
    default ResultadoC3D generarC3D(GeneradorC3D generador) {
        throw new UnsupportedOperationException(
                "Generación de C3D pendiente (Fase 3): " + getClass().getSimpleName());
    }
}
