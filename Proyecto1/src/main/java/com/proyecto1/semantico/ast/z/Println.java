package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code PRINTLN LPAREN expression RPAREN} (#primarioPrintln): imprime con salto de línea. */
public final class Println extends NodoZ implements ExpresionZ {

    private final ExpresionZ argumento;

    public Println(ExpresionZ argumento, int linea, int columna) {
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
