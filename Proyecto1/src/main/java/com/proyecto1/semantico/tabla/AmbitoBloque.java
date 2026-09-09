package com.proyecto1.semantico.tabla;

/**
 * Ámbito de un bloque: el cuerpo de un si/sino, para, mientras, hacer-mientras, un caso
 * de elegir/switch, o un bloque "{}"/INDENT-DEDENT suelto. Las variables declaradas
 * aquí dejan de existir al salir del bloque (dejan de ser resolubles), que es
 * justamente lo que se logra al no volver a consultar este Ambito una vez terminado
 * de visitar sus instrucciones.
 *
 * También lleva la marca "esCiclo": true si este bloque es el cuerpo directo de un
 * para/mientras/hacer-mientras/for/while/do-while, lo que permite validar que
 * "romper"/"continuar" (Y?) o "break"/"continue" (Zetariano) solo se usen dentro de
 * un ciclo, sin importar cuántos si/bloques anidados haya en el medio.
 */
public class AmbitoBloque extends Ambito {

    private final boolean esCiclo;

    public AmbitoBloque(Ambito padre, boolean esCiclo) {
        super(padre);
        this.esCiclo = esCiclo;
    }

    public boolean isEsCiclo() { return esCiclo; }

    /** Sube por la cadena de ámbitos (sin cruzar el límite de una función) buscando un ciclo. */
    public boolean dentroDeAlgunCiclo() {
        Ambito actual = this;
        while (actual != null && !(actual instanceof AmbitoFuncion)) {
            if (actual instanceof AmbitoBloque ab && ab.esCiclo) return true;
            actual = actual.getPadre();
        }
        return false;
    }

    @Override
    public String descripcion() {
        return "bloque";
    }
}