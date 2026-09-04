package com.proyecto1;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {
    public static void main(String[] args) throws Exception {
        String archivo = "/home/andy/Descargas/deepz.z";
        // String archivo = "/home/andy/Descargas/Demo.z";

        CharStream input = CharStreams.fromFileName(archivo);

        LenguajeLexer lexer = new LenguajeLexer(input);

        // Zetariano NO es sensible a indentación, así que NO se usa
        // IndentTokenStream aquí (eso es solo para Y?). Un
        // CommonTokenStream normal alcanza: NEWLINE y ESPACIO ya
        // están en canal oculto dentro de LenguajeLexer.g4.
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GramaticaZ parser = new GramaticaZ(tokens);

        // El punto de entrada de Zetariano es "compilationUnit",
        // no "programa" (ese es el de Y?).
        ParseTree arbol = parser.compilationUnit();

        System.out.println(arbol.toStringTree(parser));
    }
}