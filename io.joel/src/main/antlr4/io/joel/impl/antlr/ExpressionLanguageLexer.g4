lexer grammar ExpressionLanguageLexer;

LITERAL_EXPRESSION:
   (~[$#\\] * '\\' ('$' | '#') ?
   | (~[$#])* (('$'|'#') ~[{$#\\])
   | (~[$#]))+
   | '$'
   | '#';
START_DYNAMIC_EXPRESSION: '${'  -> pushMode(IN_EXPRESSION);
START_DEFERRED_EXPRESSION: '#{' -> pushMode(IN_EXPRESSION);

WS: [ \t\r\n]+ -> skip;
ANY: .;

mode IN_EXPRESSION;
LCURL: '{' -> pushMode(IN_EXPRESSION);
RCURL: '}' -> popMode;
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
WHITE_SPACE: [ \t\r\n]+ -> skip;
