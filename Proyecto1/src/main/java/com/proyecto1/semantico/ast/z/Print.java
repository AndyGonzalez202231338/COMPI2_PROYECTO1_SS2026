package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code PRINT LPAREN expression RPAREN} (#primarioPrint): imprime sin salto de línea. */
public final class Print extends NodoZ implements ExpresionZ {

    private final ExpresionZ argumento;

    public Print(ExpresionZ argumento, int linea, int columna) {
        super(linea, columna);
        this.argumento = argumento;
    }

    public ExpresionZ getArgumento() {
        return argumento;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        argumento.verificar(ambito, errores);
        return TipoPrimitivo.VOID;
    }
}
