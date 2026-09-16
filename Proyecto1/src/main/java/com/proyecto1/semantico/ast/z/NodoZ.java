package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.ast.NodoAST;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * Base de TODOS los nodos del AST de Zetariano. Mismo rol que {@code NodoY}: guarda
 * línea/columna y deja {@link #verificar(Ambito, ManejadorErrores)} como placeholder
 * (siempre {@link TipoPrimitivo#DESCONOCIDO}) hasta que se escriban las reglas
 * semánticas reales en cada subclase concreta. Pendiente explícitamente para Z:
 * <ul>
 *   <li>Variables/atributos declarados antes de usarse.</li>
 *   <li>Métodos y constructores con retorno correcto (y constructor con el mismo
 *       nombre que la clase).</li>
 *   <li>Arreglos con índices enteros y dimensiones correctas (incluye
 *       multidimensionales, "int[][]").</li>
 *   <li>Objetos: resolución de "obj.atributo"/"obj.metodo(...)" contra los miembros
 *       registrados en el {@code Simbolo} de la clase, y compatibilidad de tipos
 *       nominal entre clases.</li>
 * </ul>
 */
public abstract class NodoZ implements NodoAST {

    protected final int linea;
    protected final int columna;

    protected NodoZ(int linea, int columna) {
        this.linea = linea;
        this.columna = columna;
    }

    @Override
    public int getLinea() {
        return linea;
    }

    @Override
    public int getColumna() {
        return columna;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return TipoPrimitivo.DESCONOCIDO; // pendiente a propósito, ver Javadoc de la clase
    }
}
