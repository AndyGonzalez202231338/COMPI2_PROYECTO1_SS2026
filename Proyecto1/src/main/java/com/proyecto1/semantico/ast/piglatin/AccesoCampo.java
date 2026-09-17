package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoEstructura;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code primaria . ID} (#primariaCampo): "objeto.campo". */
public final class AccesoCampo extends NodoPigLatin implements ExpresionPigLatin {

    private final ExpresionPigLatin objeto;
    private final String campo;

    public AccesoCampo(ExpresionPigLatin objeto, String campo, int linea, int columna) {
        super(linea, columna);
        this.objeto = objeto;
        this.campo = campo;
    }

    public ExpresionPigLatin getObjeto() {
        return objeto;
    }
    public String getCampo() {
        return campo;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tObj = objeto.verificar(ambito, errores);
        Simbolo def;
        if (tObj instanceof TipoEstructura te) def = te.getDefinicion();
        else if (tObj instanceof TipoClase tc) def = tc.getDefinicion();
        else {
            if (!tObj.esDesconocido())
                errores.reportar(linea, columna, "No se puede acceder a '" + campo + "' en tipo " + tObj.nombre());
            return TipoPrimitivo.DESCONOCIDO;
        }
        Simbolo m = def.buscarMiembro(campo);
        if (m == null) {
            errores.reportar(linea, columna,
                    "'" + def.getNombre() + "' no tiene miembro '" + campo + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }
        return m.getTipo();
    }
}
