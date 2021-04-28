grammar ExpressionLanguageGrammar;
prog: compositeExpression;

compositeExpression
    : (dynamicExpression | deferredExpression | literalExpression)*
    ;
dynamicExpression
    : DYNAMIC_START expression RCURL
    ;
deferredExpression
    : DEFERRED_START expression RCURL
    ;
literalExpression
    : literal
    ;


literal
    : booleanLiteralExpression
    | floatingPointLiteralExpression
    | integerLiteralExpression
    | stringLiteralExpression
    | nullLiteralExpression
    ;

booleanLiteralExpression
    : BOOL_LITERAL
    ;
floatingPointLiteralExpression
    : FLOATING_POINT_LITERAL
    ;
integerLiteralExpression
    : INTEGER_LITERAL
    ;
stringLiteralExpression
    : STRING_LITERAL
    ;
nullLiteralExpression
    : NULL
    ;

arguments
    : LPAREN expressionList? RPAREN
    ;
expressionList
    : (expression ((COMMA expression)*))
    ;

lambdaParameters
    : IDENTIFIER
    | (LPAREN (IDENTIFIER ((COMMA IDENTIFIER)*))? RPAREN)
    ;

mapEntry
    : expression COLON expression
    ;
mapEntries
    : mapEntry (COMMA mapEntry)*
    ;

expression
    : expression (LBRACK expression RBRACK) #memberIndexExpression
    | expression bop=DOT (IDENTIFIER) #memberDotExpression
    | expression arguments #callExpression
    | prefix=(MINUS | NOT | EMPTY) expression #unaryExpression
    | expression bop=(MULT | DIV | MOD ) expression #infixExpression
    | expression bop=(PLUS | MINUS) expression #infixExpression
    | expression bop=(LE | GE | LT | GT) expression #relationalExpression
    | expression bop=INSTANCEOF IDENTIFIER #infixExpression
    | expression bop=(EQ | NE) expression #relationalExpression
    | expression bop=AND expression #logicalExpression
    | expression bop=OR expression #logicalExpression
    | <assoc=right> expression bop=QUESTIONMARK expression bop=COLON expression #ternaryExpression
    | <assoc=right> expression bop=(ASSIGN | CONCAT) expression #assignExpression
    | lambdaParameters ARROW expression #lambdaExpression
    | expression SEMICOLON expression #semicolonExpression
    | IDENTIFIER #identifierExpression
    | literal #literalExpr
    | LBRACK expressionList? RBRACK  #listExpression
    | LCURL expressionList? RCURL #setExpression
    | LCURL mapEntries? RCURL #mapExpression
    | LPAREN expression RPAREN #parenExpression
    ;

// LEXER
LCURL: '{';
RCURL: '}';
BOOL_LITERAL: TRUE | FALSE;
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

INTEGER_LITERAL: [0-9]+;
FLOATING_POINT_LITERAL: [0-9]+ '.' [0-9]* EXPONENT? | '.' [0-9]+ EXPONENT? | [0-9]+ EXPONENT?;
fragment EXPONENT: ('e'|'E') ('+'|'-')? [0-9]+;

STRING_LITERAL
    : ('"' ((~["\\])
    | ('\\' ( ["\\] )))* '"')
    | ('\'' ((~['\\])
    | ('\\' ( [\\'] )))* '\'')
    ;

IDENTIFIER: LETTER ( LETTER | DIGIT )*;

LETTER
    : '\u0024'
    | '\u0041'..'\u005a'
    | '\u005f'
    | '\u0061'..'\u007a'
    | '\u00c0'..'\u00d6'
    | '\u00d8'..'\u00f6'
    | '\u00f8'..'\u00ff'
    | '\u0100'..'\u1fff'
    | '\u3040'..'\u318f'
    | '\u3300'..'\u337f'
    | '\u3400'..'\u3d2d'
    | '\u4e00'..'\u9fff'
    | '\uf900'..'\ufaff'
    ;

DIGIT
    : '\u0030'..'\u0039'
    | '\u0660'..'\u0669'
    | '\u06f0'..'\u06f9'
    | '\u0966'..'\u096f'
    | '\u09e6'..'\u09ef'
    | '\u0a66'..'\u0a6f'
    | '\u0ae6'..'\u0aef'
    | '\u0b66'..'\u0b6f'
    | '\u0be7'..'\u0bef'
    | '\u0c66'..'\u0c6f'
    | '\u0ce6'..'\u0cef'
    | '\u0d66'..'\u0d6f'
    | '\u0e50'..'\u0e59'
    | '\u0ed0'..'\u0ed9'
    | '\u1040'..'\u1049'
    ;

WS: [ \t\r\n]+ -> skip;

ANY: .;
