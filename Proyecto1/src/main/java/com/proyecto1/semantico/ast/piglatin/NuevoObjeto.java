package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoEstructura;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

import static com.proyecto1.semantico.tabla.CategoriaSimbolo.CLASE;

/**
 * {@code novus ID ( listaArgumentos? )} (#primariaNuevoObjeto): instanciación de un
 * objeto/estructura ("new" de PigLatin). Sin equivalente en Y (esa gramática no tiene
 * instanciación de objetos); {@code nombreTipo} es el {@code ID} que nombra la
 * clase/estructura a instanciar.
 */
public final class NuevoObjeto extends NodoPigLatin implements ExpresionPigLatin {

    private final String nombreTipo;
    private final List<ExpresionPigLatin> argumentos;

    public NuevoObjeto(String nombreTipo, List<ExpresionPigLatin> argumentos, int linea, int columna) {
        super(linea, columna);
        this.nombreTipo = nombreTipo;
        this.argumentos = argumentos;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public List<ExpresionPigLatin> getArgumentos() {
        return argumentos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Simbolo s = ambito.resolver(nombreTipo);
        if (s == null) {
            errores.reportar(linea, columna, "Tipo importado desconocido: '" + nombreTipo + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }
        Tipo tipo;
        switch (s.getCategoria()) {
            case CLASE      -> tipo = new TipoClase(s);
            case ESTRUCTURA -> tipo = new TipoEstructura(s);
            default -> {
                errores.reportar(linea, columna, "'" + nombreTipo + "' no es instanciable");
                return TipoPrimitivo.DESCONOCIDO;
            }
        }
        for (ExpresionPigLatin a : argumentos) a.verificar(ambito, errores);
        return tipo;
    }
}
