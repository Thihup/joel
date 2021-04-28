parser grammar ExpressionLanguageParser;
options { tokenVocab=ExpressionLanguageLexer; }

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
