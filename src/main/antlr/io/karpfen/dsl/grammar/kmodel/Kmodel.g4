/**
 * Copyright 2026 Karl Kegel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar Kmodel;

@header {
package io.karpfen.dsl.grammar.kmodel;
}

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

