lexer grammar LenguajeLexer;

tokens { INDENT, DEDENT }

/** PALABRAS RESERVADAS **/
ESTRUCTURA : 'estructura';
ENTERO     : 'entero';
FLOTANTE   : 'flotante';
CARACTER   : 'caracter';
CADENA     : 'cadena';
BOOL       : 'bool';
VERDADERO  : 'verdadero';
FALSO      : 'falso';

DEFINIR    : 'definir';
RETORNAR   : 'retornar';

SI         : 'si';
ENTONCES   : 'entonces';
SINO       : 'sino';
CONTRARIO  : 'contrario';

ELEGIR     : 'elegir';
CASO       : 'caso';
SIEMPRE    : 'siempre';

PARA       : 'para';
MIENTRAS   : 'mientras';
HACER      : 'hacer';
CONTINUAR  : 'continuar';
ROMPER     : 'romper';

IMPRIMIR   : 'imprimir';
LEER       : 'leer';

// Secciones del programa
SEC_ESTRUCTURAS : '%estructuras';
SEC_FUNCIONES   : '%funciones';

/** SÍMBOLOS **/
DOSPUNTOS  : ':';
PUNTOYCOMA : ';';
LPAREN     : '(';
RPAREN     : ')';
LLAVEIZQ   : '{';
LLAVEDER   : '}';
CORIZQ     : '[';
CORDER     : ']';
COMA       : ',';
PUNTO      : '.';

/** OPERADORES **/
IGUALIGUAL : '==';
DISTINTO   : '!=';
Y_LOGICO   : '&&';
O_LOGICO   : '||';
INCREMENTO : '++';
DECREMENTO : '--';
MENORIGUAL : '<='; //posibles
MAYORIGUAL : '>='; //posibles
MENORQUE   : '<';
MAYORQUE   : '>';
NEGACION   : '!';

/** ASIGNACIONES COMPUESTAS **/
MAS_ASIGNA   : '+=';
MENOS_ASIGNA : '-=';
MULT_ASIGNA  : '*=';
DIV_ASIGNA   : '/=';
ASIGNAR      : '=';

/** Flecha para tipo de retorno de función **/
FLECHA     : '->';

/** OPERADORES SIMPLES **/
MAS        : '+';
MENOS      : '-';
MULT       : '*';
DIV        : '/';
MODULO     : '%';

/** COMENTARIOS LINEA **/
COMENTARIO_LINEA
    : '//' ~[\r\n]* -> channel(HIDDEN)
    ;

COMENTARIO_BLOQUE
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;


/** LITERALES **/

FLOTANTE_LIT : DIGITO+ '.' DIGITO+; //22.8
ENTERO_LIT   : DIGITO+; //182
CARACTER_LIT : '\'' (ESCAPE | ~['\\\r\n]) '\'';
CADENA_LIT   : '"' (ESCAPE | ~["\\\r\n])* '"';

/** IDENTIFICADORES **/
//  Letras, dígitos y guión bajo; no pueden iniciar con dígito.
ID : LETRA (LETRA | DIGITO | '_')*;

/** SALTO DE LÍNEA / INDENTACIÓN **/
/** NEWLINE captura el salto de línea junto con los espacios/tabs
 *  que lo siguen; LenguajeLexerBase usa ese texto para calcular
 *  el nivel de indentación y emitir INDENT/DEDENT.
**/
NEWLINE : ('\r'? '\n' | '\r');

/** Espacios y tabs -- tanto los que abren una línea (indentación)
 * como los que separan tokens dentro de una línea -- van al canal
 * oculto. IndentTokenStream es quien distingue unos de otros según
 * su posición respecto al NEWLINE anterior.
**/
ESPACIO : [ \t]+ -> channel(HIDDEN);

fragment LETRA  : [a-zA-Z];
fragment DIGITO : [0-9];
fragment ESCAPE : '\\' [ntr'"\\];