package com.proyecto1;

import com.proyecto1.semantico.ast.Cuadrupla;
import com.proyecto1.semantico.ast.GeneradorC3D;

import com.proyecto1.semantico.ast.y.Programa;

import com.proyecto1.semantico.tabla.AmbitoGlobal;
import com.proyecto1.semantico.y.ASTBuilderY;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.io.IOException;

/**
 * Prueba de consola del C3D de Y?. Toma un archivo .y, lo parsea, construye
 * el AST, genera las cuádruplas y las imprime por stdout.
 *
 * Uso:
 *   java com.proyecto1.MainC3DPrueba ruta/al/archivo.y
 * o dejar la ruta quemada en el código.
 */
public class MainC3DPrueba {

    public static void main(String[] args) throws IOException {

        String ruta = (args.length > 0) ? args[0] : "/home/andy/Escritorio/Proyectos pig/Pruebas/prueba1.y";

        // 1) Leer el archivo
        CharStream input = CharStreams.fromFileName(ruta);

        // 2) Lexer
        LenguajeLexer lexer = new LenguajeLexer(input);

        // 3) IndentTokenStream (necesario para Y? porque usa indentación)
        IndentTokenStream tokens = new IndentTokenStream(lexer);

        // 4) Parser
        GramaticaY parser = new GramaticaY(tokens);
        parser.removeErrorListeners(); // silenciar los "line X:Y ..." feos
        // (Opcional: agregar tu ListenerErroresANTLR aquí)
        GramaticaY.ProgramaContext arbol = parser.programa();

        // 5) AST
        ASTBuilderY builder = new ASTBuilderY();
        Programa ast = builder.construir(arbol);

        // 6) Generar C3D
        // Placeholder: aún no se corre el análisis semántico en este pipeline, así que el
        // ámbito global está vacío y los tipos de los identificadores saldrán DESCONOCIDO.
        // Cuando se integre el analizador, pasar aquí el ámbito que este produzca.
        GeneradorC3D generador = new GeneradorC3D(new AmbitoGlobal());
        ast.generarC3D(generador);

        // 7) Imprimir tabla
        System.out.println("=== Cuádruplas generadas ===");
        int i = 0;
        for (Cuadrupla c : generador.getCuadruplas()) {
            System.out.printf("%3d: %s%n", i++, c.toStringLegible());
        }
        System.out.println("=== Fin (" + generador.getCuadruplas().size() + " cuádruplas) ===");
    }
}