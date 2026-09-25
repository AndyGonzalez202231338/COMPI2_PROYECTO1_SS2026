package com.proyecto1.semantico;

import com.proyecto1.semantico.ast.z.*;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.AmbitoClase;
import com.proyecto1.semantico.tabla.AmbitoGlobal;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

public class AnalizadorSemanticoZ {

    public ManejadorErrores analizar(Clase clase) {
        return analizar(clase, new AmbitoGlobal());
    }

    /**
     * Igual que {@link #analizar(Clase)} pero llenando el {@code global} que entrega quien
     * llama. Así, al terminar, quien llamó conserva la clase ya resuelta (con sus atributos
     * y métodos) -- es lo que necesita un .pig para poder importarla, y lo que necesita
     * {@link com.proyecto1.servicio.CargadorClasesZ} para que las clases HERMANAS del mismo
     * proyecto (otros .z, sin "import": se ven entre sí como en Java) puedan resolverse
     * mutuamente incluso con referencias circulares (Nodo &lt;-&gt; Pila).
     *
     * <p>Internamente se hace en dos pasos reutilizables por separado ({@link #registrarFirma}
     * y {@link #registrarMiembros}): así {@link com.proyecto1.servicio.CargadorClasesZ} puede
     * pre-registrar SOLO las FIRMAS de las clases hermanas (sin verificar sus cuerpos, que no
     * son responsabilidad de este archivo) antes de que este método registre y verifique la
     * clase que sí se está compilando.
     */
    public ManejadorErrores analizar(Clase clase, AmbitoGlobal global) {
        ManejadorErrores errores = new ManejadorErrores();

        Simbolo sClase = registrarFirma(clase, global, errores);
        if (sClase == null) {
            errores.imprimir();
            return errores;
        }
        AmbitoClase ambClase = registrarMiembros(clase, sClase, global, errores);

        // ---- SEGUNDA PASADA: verificar cuerpos ----
        for (Atributo a : clase.getAtributos()) a.verificar(ambClase, errores);
        for (Metodo m : clase.getMetodos())       m.verificar(ambClase, errores);
        for (Constructor c : clase.getConstructores()) c.verificar(ambClase, errores);

        errores.imprimir();
        return errores;
    }

    /**
     * PRIMERA PASADA (parte 1): registra el NOMBRE de la clase (bare, sin miembros) en
     * {@code global}. Es lo mínimo para que otra clase (hermana o esta misma) pueda resolver
     * el nombre como TIPO ("Nodo siguiente;") sin necesitar sus atributos/métodos todavía —
     * por eso {@link com.proyecto1.servicio.CargadorClasesZ} registra los NOMBRES de TODAS
     * las clases hermanas (llamando esto por cada una) antes de registrar los miembros de
     * NINGUNA: así una referencia mutua (Nodo con un campo "Pila", Pila con un campo "Nodo")
     * encuentra el nombre sin importar el orden en que se escanearon los archivos.
     *
     * @return el {@link Simbolo} de la clase ya declarado en {@code global}, o {@code null}
     *         si el nombre ya existía (clase duplicada; ya reportado en {@code errores}).
     */
    public Simbolo registrarFirma(Clase clase, AmbitoGlobal global, ManejadorErrores errores) {
        Simbolo sClase = new Simbolo(clase.getNombre(), CategoriaSimbolo.CLASE,
                null, clase.getLinea(), clase.getColumna());
        if (!global.declarar(sClase)) {
            errores.reportar(clase.getLinea(), clase.getColumna(),
                    "Clase duplicada: '" + clase.getNombre() + "'");
            return null;
        }
        return sClase;
    }

    /**
     * PRIMERA PASADA (parte 2): registra atributos/métodos/constructores de {@code clase}
     * (solo sus FIRMAS -- tipos de retorno, tipos de atributos -- resueltos contra
     * {@code global}; NO se verifican los CUERPOS de los métodos aquí). Requiere que
     * {@link #registrarFirma} ya se haya llamado (para tener {@code sClase}) y que, si
     * {@code clase} referencia otras clases por nombre, esas ya estén registradas en
     * {@code global} (ver el Javadoc de {@link #registrarFirma}).
     *
     * @return el {@link AmbitoClase} recién creado, con los miembros ya declarados en su
     *         propia tabla (necesario para "this" implícito: ver {@link com.proyecto1.semantico.ast.z.Identificador}).
     *         Cuando se llama para pre-registrar una clase HERMANA (no la que se está
     *         compilando), este ámbito se descarta: lo único que importa es el efecto
     *         colateral de {@code declararMiembro}, que además guarda cada miembro en
     *         {@code sClase.agregarMiembro(...)} (ahí es donde después lo encuentra, por
     *         ejemplo, {@code AccesoCampo} con "objeto.miembro").
     */
    public AmbitoClase registrarMiembros(Clase clase, Simbolo sClase, AmbitoGlobal global, ManejadorErrores errores) {
        AmbitoClase ambClase = new AmbitoClase(global, sClase);

        // Registrar atributos (sin verificar inicializadores todavía)
        for (Atributo a : clase.getAtributos()) {
            Simbolo sa = new Simbolo(a.getNombre(), CategoriaSimbolo.ATRIBUTO,
                    a.getTipo().resolver(global, errores),
                    a.getLinea(), a.getColumna());
            ambClase.declararMiembro(sa);
        }

        // Métodos
        for (Metodo m : clase.getMetodos()) {
            Tipo tRet = m.esVoid() ? TipoPrimitivo.VOID : m.getTipoRetorno().resolver(global, errores);
            Simbolo sm = new Simbolo(m.getNombre(), CategoriaSimbolo.METODO, tRet, m.getLinea(), m.getColumna());
            for (Parametro p : m.getParametros()) {
                Tipo tp = p.getTipo().resolver(global, errores);
                sm.agregarParametro(new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO,
                        tp, p.getLinea(), p.getColumna()));
            }
            String clave = m.getNombre() + "#" + m.getParametros().size();
            ambClase.declararMiembroConClave(clave, sm);
        }


        // Constructores
        for (Constructor c : clase.getConstructores()) {
            Simbolo sc = new Simbolo(c.getNombre(), CategoriaSimbolo.CONSTRUCTOR, null, c.getLinea(), c.getColumna());
            for (Parametro p : c.getParametros()) {
                Tipo tp = p.getTipo().resolver(global, errores);
                sc.agregarParametro(new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO,
                        tp, p.getLinea(), p.getColumna()));
            }
            String clave = c.getNombre() + "#" + c.getParametros().size();
            ambClase.declararMiembroConClave(clave, sc);
        }

        return ambClase;
    }

    /**
     * Agrega a {@code simbolo} (un método o constructor) sus parámetros, en orden, con el
     * tipo ya resuelto contra {@code global}. Es PARTE de la firma (junto con el nombre y el
     * tipo de retorno) y por eso vive aquí -- ANTES, esto solo pasaba dentro de
     * {@code Metodo.verificar}/{@code Constructor.verificar}, que es la SEGUNDA pasada; una
     * clase hermana (ver {@link com.proyecto1.servicio.CargadorClasesZ}) nunca llega a esa
     * segunda pasada (no se verifican cuerpos ajenos), así que sus métodos quedaban con la
     * aridad registrada en 0 -- cualquier llamada con argumentos, aunque fuera correcta,
     * reportaba "método espera 0 argumentos". Los nombres de los parámetros NO se declaran
     * aquí como variables locales (eso sigue pasando en verificar(), que sí necesita un
     * ámbito de body real para reportar "parámetro duplicado" con su línea/columna); aquí
     * solo se guarda su TIPO, que es lo único que otra clase puede llegar a necesitar.
     */
    private void registrarParametros(Simbolo simbolo, java.util.List<com.proyecto1.semantico.ast.z.Parametro> parametros,
                                     AmbitoGlobal global, ManejadorErrores errores) {
        for (com.proyecto1.semantico.ast.z.Parametro p : parametros) {
            Tipo t = p.resolverTipo(global, errores);
            simbolo.agregarParametro(new Simbolo(p.getNombre(), CategoriaSimbolo.PARAMETRO, t, p.getLinea(), p.getColumna()));
        }
    }
}