grammar Kmodel;

// Parser Rules
kmodel_file
    : make_object_block EOF
    ;

make_object_block
    : MAKE OBJECT object_signature LBRACE statement* RBRACE
    ;

object_signature
    : STRING COLON STRING
    ;

statement
    : prop_statement
    | has_statement
    | knows_statement
    ;

prop_statement
    : PROP LPAREN STRING RPAREN ARROW STRING
    ;

has_statement
    : HAS LPAREN STRING RPAREN ARROW make_object_block
    ;

knows_statement
    : KNOWS LPAREN STRING RPAREN ARROW STRING
    ;

// Lexer Rules
MAKE     : 'make' ;
OBJECT   : 'object' ;
PROP     : 'prop' ;
HAS      : 'has' ;
KNOWS    : 'knows' ;

LBRACE   : '{' ;
RBRACE   : '}' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
COLON    : ':' ;
ARROW    : '->' ;

STRING   : '"' (~["\r\n] | '\\' .)* '"'
         | '\'' (~['\r\n] | '\\' .)* '\''
         ;

WS       : [ \t\r\n]+ -> skip ;
COMMENT  : '//' ~[\r\n]* -> skip ;

