package com.proyecto1.semantico.ast.piglatin;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.AmbitoBloque;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/**
 * Un {@code bloque} (#bloqueDef): {@code { sentencia* }}. A diferencia de
 * {@code Bloque} en Y, aquí SÍ implementa {@link InstruccionPigLatin}: la gramática
 * de PigLatin permite un bloque suelto directamente como {@code sentencia}
 * (alternativa {@code #stmtBloque}), es decir, un bloque anidado sin ningún si/dum/etc.
 * que lo introduzca es una instrucción válida por sí sola.
 */
public final class Bloque extends NodoPigLatin implements InstruccionPigLatin {

    private final List<InstruccionPigLatin> instrucciones;

    public Bloque(List<InstruccionPigLatin> instrucciones, int linea, int columna) {
        super(linea, columna);
        this.instrucciones = instrucciones;
    }

    public List<InstruccionPigLatin> getInstrucciones() {
        return instrucciones;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        AmbitoBloque amb = new AmbitoBloque(ambito, false);
        for (InstruccionPigLatin i : instrucciones) i.verificar(amb, errores);
        return TipoPrimitivo.VOID;
    }
}
