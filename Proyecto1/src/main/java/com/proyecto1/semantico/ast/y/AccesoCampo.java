package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoEstructura;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/** {@code primaria PUNTO ID} (#primariaCampo): "objeto.campo". */
public final class AccesoCampo extends NodoY implements ExpresionY {

    private final ExpresionY objeto;
    private final String campo;

    public AccesoCampo(ExpresionY objeto, String campo, int linea, int columna) {
        super(linea, columna);
        this.objeto = objeto;
        this.campo = campo;
    }

    public ExpresionY getObjeto() {
        return objeto;
    }
    public String getCampo() {
        return campo;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Tipo tObj = objeto.verificar(ambito, errores);
        if (!(tObj instanceof TipoEstructura te)) {
            if (!tObj.esDesconocido())
                errores.reportar(linea, columna, "No se puede acceder a '" + campo + "' en tipo " + tObj.nombre());
            return TipoPrimitivo.DESCONOCIDO;
        }
        Simbolo campoSim = te.getDefinicion().buscarMiembro(campo);
        if (campoSim == null) {
            errores.reportar(linea, columna, "La estructura '" + te.nombre() + "' no tiene campo '" + campo + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }
        return campoSim.getTipo();
    }
}
