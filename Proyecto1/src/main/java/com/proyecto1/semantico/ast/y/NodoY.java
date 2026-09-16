package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.ast.NodoAST;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * Base de TODOS los nodos del AST de Y?. Guarda línea/columna (para poder reportar
 * errores exactamente donde ocurren) y provee una implementación de
 * {@link #verificar(Ambito, ManejadorErrores)} que, POR AHORA, es un placeholder:
 * no valida nada y siempre devuelve {@link TipoPrimitivo#DESCONOCIDO}.
 *
 * <p><b>Esto es intencional y temporal.</b> El alcance de esta entrega es SOLO el
 * visitor que construye el AST (recorre el árbol que entrega ANTLR y arma estos
 * nodos); las reglas semánticas reales se agregan en la siguiente parte,
 * sobreescribiendo verificar() en cada subclase concreta que lo necesite. Quedan
 * pendientes explícitamente:
 * <ul>
 *   <li>Variables declaradas antes de usarse.</li>
 *   <li>Funciones con retorno correcto.</li>
 *   <li>Arreglos con índices enteros y dimensiones correctas.</li>
 *   <li>Estructuras anidadas y objetos.</li>
 * </ul>
 * Al dejar la implementación por defecto AQUÍ (en la clase base) y no repetida en
 * cada subclase, cuando llegue el momento de implementar las reglas de verdad basta
 * con sobreescribir el método en la subclase que corresponda — nada de lo que ya
 * existe en el AST necesita cambiar de forma.
 */
public abstract class NodoY implements NodoAST {

    protected final int linea;
    protected final int columna;

    protected NodoY(int linea, int columna) {
        this.linea = linea;
        this.columna = columna;
    }

    @Override
    public int getLinea() { return linea; }

    @Override
    public int getColumna() { return columna; }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return TipoPrimitivo.DESCONOCIDO; // pendiente a propósito, ver Javadoc de la clase
    }
}
