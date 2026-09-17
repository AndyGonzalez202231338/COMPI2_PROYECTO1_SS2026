package com.proyecto1.semantico.ast.z;
import com.proyecto1.semantico.errores.ManejadorErrores;
import com.proyecto1.semantico.tabla.Ambito;
import com.proyecto1.semantico.tipos.Tipo;
import com.proyecto1.semantico.tipos.TipoPrimitivo;

/**
 * Un literal (#primarioEntero, #primarioFlotante, #primarioCaracter, #primarioCadena,
 * #primarioTrue, #primarioFalse, #primarioNull). Igual que en Y?, el valor ya viene
 * parseado a su tipo Java (Long/Double/Character/String/Boolean/null), no como texto
 * crudo. Para NULO, {@code valor} es simplemente {@code null}.
 */
public final class Literal extends NodoZ implements ExpresionZ {

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
            case NULO      -> TipoPrimitivo.NULO;
        };
    }
}
