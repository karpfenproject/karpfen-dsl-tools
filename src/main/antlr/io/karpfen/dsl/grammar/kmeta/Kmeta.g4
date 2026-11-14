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
grammar Kmeta;

@header {
package io.karpfen.dsl.grammar.kmeta;
}

@lexer::members {
    /**
     * Reports characters that start no token the way the grammar did before `behaves` and `after` were keywords. A
     * text such as `a "x"` lets the lexer read `a` as the start of `after` and fail at the quote, which it would then
     * skip together with the `a`; without the keywords it skipped the `a` alone and read the string. So when the
     * failed characters start with the first letter of one of the two keywords, which no other token starts with, the
     * lexer goes back to that letter, reports it alone and skips only it.
     */
    @Override
    public void notifyListeners(LexerNoViableAltException e) {
        int start = _tokenStartCharIndex;
        if (_input.index() > start) {
            int first = _input.getText(org.antlr.v4.runtime.misc.Interval.of(start, start)).codePointAt(0);
            if (first == 'a' || first == 'b') {
                _input.seek(start);
                setCharPositionInLine(_tokenStartCharPositionInLine);
            }
        }
        super.notifyListeners(e);
    }
}

// Parser Rules
kmeta_file
    : type_definition* EOF
    ;

// The behaviour clause is a rule of its own that may be empty, so that a missing brace after the description is
// recovered from exactly as in a grammar without the clause.
type_definition
    : TYPE STRING STRING behaviour_clause LBRACE rule_list? RBRACE
    ;

behaviour_clause
    : (BEHAVES AFTER STRING)?
    ;

rule_list
    : rule+
    ;

rule
    : prop_rule
    | has_rule
    | knows_rule
    ;

prop_rule
    : PROP LPAREN STRING COMMA rule_value RPAREN
    ;

has_rule
    : HAS LPAREN STRING COMMA rule_value RPAREN
    ;

knows_rule
    : KNOWS LPAREN STRING COMMA rule_value RPAREN
    ;

rule_value
    : STRING
    | LIST LPAREN STRING RPAREN
    ;

// Lexer Rules
TYPE     : 'type' ;
PROP     : 'prop' ;
HAS      : 'has' ;
KNOWS    : 'knows' ;
LIST     : 'list' ;
BEHAVES  : 'behaves' ;
AFTER    : 'after' ;

LBRACE   : '{' ;
RBRACE   : '}' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
COMMA    : ',' ;

STRING   : '"' ( '\\' . | ~["\r\n] )* '"' ;

WS       : [ \t\r\n]+ -> skip ;
COMMENT  : '//' ~[\r\n]* -> skip ;



