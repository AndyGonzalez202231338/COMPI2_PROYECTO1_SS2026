package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** Un identificador usado como expresión (#primariaIdentificador): variable, o base de "obj.campo" / "arr[i]" / "f(...)". */
public final class Identificador extends NodoPigLatin implements ExpresionPigLatin {

    private final String nombre;

    public Identificador(String nombre, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Simbolo s = ambito.resolver(nombre);
        if (s == null) {
            errores.reportar(linea, columna, "Identificador no declarado: '" + nombre + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }
        return s.getTipo();
    }
}
