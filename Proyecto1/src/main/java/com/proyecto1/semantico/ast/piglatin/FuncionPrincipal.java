package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/**
 * La {@code funcionPrincipal} (#funcionPrincipalDef): {@code MAIOR >> sentencia*
 * FINIS ;}. Es el único punto de entrada del programa PigLatin (no tiene nombre,
 * parámetros ni tipo de retorno como {@code Funcion} en Y  por eso no se reutiliza
 * esa forma aquí).
 */
public final class FuncionPrincipal extends NodoPigLatin {

    private final List<InstruccionPigLatin> cuerpo;

    public FuncionPrincipal(List<InstruccionPigLatin> cuerpo, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = cuerpo;
    }

    public List<InstruccionPigLatin> getCuerpo() {
        return cuerpo;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        AmbitoBloque amb = new AmbitoBloque(ambito, false);
        for (InstruccionPigLatin i : cuerpo) i.verificar(amb, errores);
        return TipoPrimitivo.VOID;
    }
}
