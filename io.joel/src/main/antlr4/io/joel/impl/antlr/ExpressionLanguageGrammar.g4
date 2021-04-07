grammar ExpressionLanguageGrammar;
prog: compositeExpression;

compositeExpression: (dynamicExpression | deferredExpression | literalExpression)*;
dynamicExpression: DYNAMIC_START expression RCURL;
deferredExpression: DEFERRED_START expression RCURL;
literalExpression: literal;

literal: BOOL_LITERAL | FLOATING_POINT_LITERAL | INTEGER_LITERAL | StringLiteral | NULL;
methodArguments: LPAREN expressionList? RPAREN;
expressionList: (expression ((COMMA expression)*));

lambdaExpressionOrCall: LPAREN lambdaExpression RPAREN methodArguments*;
lambdaExpression: lambdaParameters ARROW expression;
lambdaParameters: IDENTIFIER | (LPAREN (IDENTIFIER ((COMMA IDENTIFIER)*))? RPAREN);

mapEntry: expression COLON expression;
mapEntries: mapEntry (COMMA mapEntry)*;

expression
    : primary
    | LBRACK expressionList? RBRACK
    | LCURL expressionList? RCURL
    | LCURL mapEntries? RCURL
    | expression bop=DOT (IDENTIFIER | IDENTIFIER LPAREN expressionList? RPAREN)
    | expression (LBRACK expression RBRACK)+
    | prefix=(MINUS | NOT | EMPTY) expression
    | expression bop=(MULT | DIV | MOD ) expression
    | expression bop=(PLUS | MINUS) expression
    | expression bop=(LE | GE | LT | GT) expression
    | expression bop=INSTANCEOF IDENTIFIER
    | expression bop=(EQ | NE) expression
    | expression AND expression
    | expression OR expression
    | <assoc=right> expression bop=QUESTIONMARK expression bop=COLON expression
    | <assoc=right> expression bop=(ASSIGN | CONCAT) expression
    | lambdaExpression
    | lambdaExpressionOrCall
    ;

primary
    : '(' expression ')'
    | literal
    | IDENTIFIER
    ;

// LEXER
LCURL: '{';
RCURL: '}';
TRUE: 'true';
FALSE: 'false';
NULL: 'null';
DOT: '.';
LPAREN: '(';
RPAREN: ')';
LBRACK: '[';
RBRACK: ']';
COLON: ':';
COMMA: ',';
SEMICOLON: ';';
GT: ('>' | 'gt');
LT: ('<' | 'lt');
GE: ('>=' | 'ge');
LE: ('<=' | 'le');
EQ: ('==' | 'eq');
NE: ('!=' | 'ne');
NOT: ('!' | 'not');
AND: ('&&' | 'and');
OR: ('||' | 'or');
EMPTY: 'empty';
INSTANCEOF: 'instanceof';
MULT: '*';
PLUS: '+';
MINUS: '-';
QUESTIONMARK: '?';
DIV: ('/' | 'div');
MOD: ('%' | 'mod');
CONCAT: '+=';
ASSIGN: '=';
ARROW: '->';
DYNAMIC_START: DOLLAR LCURL;
DEFERRED_START: HASH LCURL;
DOLLAR: '$';
HASH: '#';
BOOL_LITERAL: TRUE | FALSE;
INTEGER_LITERAL: [0-9]+;
FLOATING_POINT_LITERAL: [0-9]+ '.' [0-9]* EXPONENT? | '.' [0-9]+ EXPONENT? | [0-9]+ EXPONENT?;
fragment EXPONENT: ('e'|'E') ('+'|'-')? [0-9]+;

StringLiteral:                 ('"' DoubleStringCharacter* '"'
             |                  '\'' SingleStringCharacter* '\'') ;

fragment DoubleStringCharacter
    : ~["\\\r\n]
    | '\\' EscapeSequence
    ;

fragment SingleStringCharacter
    : ~['\\\r\n]
    | '\\' EscapeSequence
    ;
fragment EscapeSequence
    : CharacterEscapeSequence
    | '0'
    | HexEscapeSequence
    | UnicodeEscapeSequence
    | ExtendedUnicodeEscapeSequence
    ;
fragment CharacterEscapeSequence
    : SingleEscapeCharacter
    | NonEscapeCharacter
    ;
fragment HexEscapeSequence
    : 'x' HexDigit HexDigit
    ;

fragment UnicodeEscapeSequence
    : 'u' HexDigit HexDigit HexDigit HexDigit
    | 'u' '{' HexDigit HexDigit+ '}'
    ;
fragment ExtendedUnicodeEscapeSequence
    : 'u' '{' HexDigit+ '}'
    ;
fragment SingleEscapeCharacter
    : ['"\\bfnrtv]
    ;

fragment NonEscapeCharacter
    : ~['"\\bfnrtv0-9xu\r\n]
    ;
fragment EscapeCharacter
    : SingleEscapeCharacter
    | [0-9]
    | [xu]
    ;
fragment HexDigit
    : [_0-9a-fA-F]
    ;
fragment DecimalIntegerLiteral
    : '0'
    | [1-9] [0-9_]*
    ;
fragment ExponentPart
    : [eE] [+-]? [0-9_]+
    ;
fragment IdentifierPart
    : IdentifierStart
    | [\p{Mn}]
    | [\p{Nd}]
    | [\p{Pc}]
    | '\u200C'
    | '\u200D'
    ;
fragment IdentifierStart
    : [\p{L}]
    | [$_]
    | '\\' UnicodeEscapeSequence
    ;

IDENTIFIER: LETTER (LETTER|DIGIT)*;

LETTER:  '\u0024' |
                 '\u0041'..'\u005a' |
                 '\u005f' |
                 '\u0061'..'\u007a' |
                 '\u00c0'..'\u00d6' |
                 '\u00d8'..'\u00f6' |
                 '\u00f8'..'\u00ff' |
                 '\u0100'..'\u1fff' |
                 '\u3040'..'\u318f' |
                 '\u3300'..'\u337f' |
                 '\u3400'..'\u3d2d' |
                 '\u4e00'..'\u9fff' |
                 '\uf900'..'\ufaff';
DIGIT: '\u0030'..'\u0039'|
               '\u0660'..'\u0669'|
               '\u06f0'..'\u06f9'|
               '\u0966'..'\u096f'|
               '\u09e6'..'\u09ef'|
               '\u0a66'..'\u0a6f'|
               '\u0ae6'..'\u0aef'|
               '\u0b66'..'\u0b6f'|
               '\u0be7'..'\u0bef'|
               '\u0c66'..'\u0c6f'|
               '\u0ce6'..'\u0cef'|
               '\u0d66'..'\u0d6f'|
               '\u0e50'..'\u0e59'|
               '\u0ed0'..'\u0ed9'|
               '\u1040'..'\u1049';

WS: [ \t\r\n]+ -> skip;

ANY: .;
