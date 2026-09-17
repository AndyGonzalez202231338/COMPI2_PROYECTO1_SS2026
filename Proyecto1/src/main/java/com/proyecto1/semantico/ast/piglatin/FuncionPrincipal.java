package com.proyecto1.semantico.ast.piglatin;

import java.util.List;

/**
 * La {@code funcionPrincipal} (#funcionPrincipalDef): {@code MAIOR >> sentencia*
 * FINIS ;}. Es el único punto de entrada del programa PigLatin (no tiene nombre,
 * parámetros ni tipo de retorno como {@code Funcion} en Y — por eso no se reutiliza
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
}
