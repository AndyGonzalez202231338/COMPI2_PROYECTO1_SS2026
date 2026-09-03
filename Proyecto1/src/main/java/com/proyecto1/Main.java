package com.proyecto1;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {
    public static void main(String[] args) throws Exception {
        String archivo = "/home/andy/Descargas/deep.y";
        // String archivo = "Descargas/02_completo.y";

        CharStream input = CharStreams.fromFileName(archivo);

        LenguajeLexer lexer = new LenguajeLexer(input);
        IndentTokenStream tokens = new IndentTokenStream(lexer);
        GramaticaY parser = new GramaticaY(tokens);

        ParseTree arbol = parser.programa();

        // Muestra el árbol de parseo en formato LISP (paréntesis anidados)
        System.out.println(arbol.toStringTree(parser));
    }
}