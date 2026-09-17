package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code LEER LPAREN RPAREN} (#primariaLeer): lectura de entrada estándar. */
public final class Leer extends NodoY implements ExpresionY {
    public Leer(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        // leer() se asigna a variables; se asume cadena por defecto.
        // Si quieres un comportamiento más fino, devuelve el tipo del contexto de asignación.
        return TipoPrimitivo.CADENA;
    }
}
