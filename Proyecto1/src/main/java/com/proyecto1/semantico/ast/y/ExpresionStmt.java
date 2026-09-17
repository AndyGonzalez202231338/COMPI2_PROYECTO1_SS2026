package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;

/** Una expresión usada como instrucción suelta (#instExpresion), p. ej. "leer();" o "contador++;". */
public final class ExpresionStmt extends NodoY implements InstruccionY {

    private final ExpresionY expresion;

    public ExpresionStmt(ExpresionY expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public ExpresionY getExpresion() {
        return expresion;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return expresion.verificar(ambito, errores);
    }
}
