package com.proyecto1.semantico.ast.y;

import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * Un literal (#primariaEntero, #primariaFlotante, #primariaCaracter, #primariaCadena,
 * #primariaVerdadero, #primariaFalso; también usado por la regla independiente
 * {@code literal} dentro de los "caso" de un elegir: #litEntero, #litCaracter,
 * #litCadena). El valor ya viene "parseado" a su tipo Java correspondiente
 * (Long/Double/Character/String/Boolean), no como texto crudo así los nodos de más
 * arriba no tienen que volver a parsear números ni desescapar cadenas.
 */
public final class Literal extends NodoY implements ExpresionY {

    private final Object valor;
    private final CategoriaLiteral categoria;

    public Literal(Object valor, CategoriaLiteral categoria, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.categoria = categoria;
    }

    public Object getValor() {
        return valor;
    }

    public CategoriaLiteral getCategoria() {
        return categoria;
    }

    @Override
    public Tipo verificar(Ambito ambito, ManejadorErrores errores) {
        return switch (categoria) {
            case ENTERO    -> TipoPrimitivo.ENTERO;
            case FLOTANTE  -> TipoPrimitivo.FLOTANTE;
            case CARACTER  -> TipoPrimitivo.CARACTER;
            case CADENA    -> TipoPrimitivo.CADENA;
            case BOOLEANO  -> TipoPrimitivo.BOOL;
        };
    }
}
