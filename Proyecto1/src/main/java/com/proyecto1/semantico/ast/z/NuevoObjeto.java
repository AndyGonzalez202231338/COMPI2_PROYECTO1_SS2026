package com.proyecto1.semantico.ast.z;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tabla.CategoriaSimbolo;
import com.proyecto1.semantico.tabla.Simbolo;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoClase;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

import java.util.List;

/** {@code NEW ID LPAREN argumentList? RPAREN} (#primarioInstanciaClase): "new Persona(args)". */
public final class NuevoObjeto extends NodoZ implements ExpresionZ {

    private final String nombreClase;
    private final List<ExpresionZ> argumentos;

    public NuevoObjeto(String nombreClase, List<ExpresionZ> argumentos, int linea, int columna) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.argumentos = argumentos;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    public List<ExpresionZ> getArgumentos() {
        return argumentos;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        Simbolo c = ambito.ambitoGlobal().resolverLocal(nombreClase);
        if (c == null || c.getCategoria() != CategoriaSimbolo.CLASE) {
            errores.reportar(linea, columna, "Clase desconocida: '" + nombreClase + "'");
            return TipoPrimitivo.DESCONOCIDO;
        }
        // Buscar constructor compatible por aridad (el curso no exige matching exacto de tipos)
        boolean hayConstructorCompatible = c.getMiembros().valores().stream()
                .anyMatch(m -> m.getCategoria() == CategoriaSimbolo.CONSTRUCTOR
                        && m.getParametros().size() == argumentos.size());
        if (!hayConstructorCompatible)
            errores.reportar(linea, columna,
                    "No existe constructor de '" + nombreClase + "' con " + argumentos.size() + " argumentos");

        for (ExpresionZ a : argumentos) a.verificar(ambito, errores);
        return new TipoClase(c);
    }


}
