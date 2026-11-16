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
grammar Kstates;

@header {
package io.karpfen.dsl.grammar.kstates;
}

kstates_file
    : statemachine EOF
    ;

// A fragment, `STATEMACHINE "name" FRAGMENT`, is read with an entry rule of its own. A tool chooses the rule by the
// tokens in front of the first brace, see io.karpfen.dsl.parser.KstatesEntryRule, so that the attached and the
// attachable form keep their rule and recover from errors exactly as before.
fragment_file
    : fragment_machine EOF
    ;

// Two forms share one header: `STATEMACHINE ATTACHED TO "T"` and `STATEMACHINE "name" ATTACHABLE TO "A", "B"`.
// The grammar accepts a name and further types with either keyword; the reader reports a name or a second type of
// an attached state machine and an attachable one without a name. The optional parts stand in rules of their own,
// so that a missing keyword or brace of the header is recovered from exactly as in a grammar without them.
statemachine
    : STATEMACHINE statemachine_name (ATTACHED | ATTACHABLE) TO STRING further_types LBRACE states_block transitions_block macros_block? RBRACE
    ;

fragment_machine
    : STATEMACHINE statemachine_name FRAGMENT LBRACE states_block transitions_block macros_block? RBRACE
    ;

statemachine_name
    : STRING?
    ;

further_types
    : (COMMA STRING)*
    ;

states_block
    : STATES LBRACE state_definition* RBRACE
    ;

// A container state, `CONTAINER STATE "name" USE "fragment"`, and a final state, `FINAL STATE "name"`, share the rule
// of a state. The kind and the fragment stand in rules of their own that may be empty, so that a state without them
// recovers from errors exactly as before. The grammar takes every part in every kind of state; the reader reports
// the parts a kind does not have.
state_definition
    : INITIAL? history_definition? state_kind STATE STRING fragment_use LBRACE state_breakpoint_block? tick_limit_block? entry_block? do_block? state_definition* RBRACE
    ;

state_kind
    : (CONTAINER | FINAL)?
    ;

fragment_use
    : (USE STRING)?
    ;

history_definition
    : shallow_history_definition
    | deep_history_definition
    ;

shallow_history_definition
    : SHALLOW HISTORY
    ;

deep_history_definition
    : DEEP HISTORY
    ;

state_breakpoint_block
    : BREAKPOINT LBRACE state_breakpoint_definition? RBRACE
    ;

state_breakpoint_definition
    : ENTRY EXIT?
    | EXIT ENTRY?
    ;

tick_limit_block
    : TICK LIMIT LBRACE tick_limit_definition? RBRACE
    ;

tick_limit_definition
    : delay_definition timeout_definition?
    | timeout_definition delay_definition?
    ;

delay_definition
    : DELAY COLON NUMBER TICK
    ;

timeout_definition
    : TIMEOUT COLON AFTER NUMBER TICK TRANSITION TO STRING
    ;

entry_block
    : ENTRY action_block
    ;

do_block
    : DO action_block
    ;

action_block
    : LBRACE action_item* RBRACE
    ;

action_item
    : action_rule
    | in_scope_block
    | with_block
    ;

in_scope_block
    : IF IN SCOPE LPAREN STRING (COMMA STRING)* RPAREN action_block
    ;

with_block
    : WITH macro_call AS STRING action_block
    ;

// Three arities, distinguished by the hand-written parser:
//   unary   OP("a")              -> DROPLIST, DROPOBJ
//   binary  OP("a", rhs)         -> SET, APPEND, SETLIST, EVENT, EXCHANGE
//   ternary OP("a", "b", rhs)    -> SETOBJ, APPENDOBJ   (source, relation, target)
// Only the ternary form takes an ATTACH clause, so ATTACH after any other form is a syntax error.
action_rule
    : action_operation LPAREN STRING RPAREN
    | action_operation LPAREN STRING COMMA action_right_side RPAREN
    | action_operation LPAREN STRING COMMA STRING COMMA action_right_side RPAREN attach_clause?
    ;

attach_clause
    : ATTACH action_right_side
    ;

action_operation
    : SET
    | APPEND
    | SETLIST
    | DROPLIST
    | SETOBJ
    | APPENDOBJ
    | DROPOBJ
    | EVENT
    | EXCHANGE
    ;

action_right_side
    : macro_call
    | eval_statement
    | STRING
    ;

macro_call
    : MACRO LPAREN STRING (COMMA STRING)* RPAREN
    ;

eval_statement
    : EVAL LBRACE eval_code_block RBRACE
    ;

transitions_block
    : TRANSITIONS LBRACE transition_definition* RBRACE
    ;

transition_definition
    : normal_transition
    | split_transition
    | join_transition
    ;

normal_transition
    : TRANSITION STRING ARROW STRING not_looping? LBRACE transition_breakpoint_block? condition_block? RBRACE
    ;

split_transition
    : SPLIT STRING ARROW STRING (COMMA STRING)+ not_looping? LBRACE transition_breakpoint_block? condition_block? RBRACE
    ;

join_transition
    : JOIN STRING (COMMA STRING)+ ARROW STRING not_looping? LBRACE transition_breakpoint_block? condition_block? RBRACE
    ;

not_looping
    : NOT LOOPING
    ;

transition_breakpoint_block
    : BREAKPOINT LBRACE transition_breakpoint_definition? RBRACE
    ;

transition_breakpoint_definition
    : START END?
    | END START?
    ;

condition_block
    : CONDITION LBRACE condition_clause+ RBRACE
    ;

condition_clause
    : event_condition
    | eval_statement
    | value_condition
    | done_condition
    ;

event_condition
    : EVENT LPAREN STRING COMMA STRING RPAREN
    ;

value_condition
    : VALUE LPAREN STRING RPAREN
    ;

done_condition
    : DONE
    ;

macros_block
    : MACROS LBRACE macro_definition* RBRACE
    ;

macro_definition
    : MACRO STRING LBRACE takes_directive* returns_directive definition_block RBRACE
    ;

takes_directive
    : TAKES LPAREN STRING COMMA type_expression RPAREN
    ;

returns_directive
    : RETURNS LPAREN return_expression RPAREN
    ;

return_expression
    : type_expression
    | reference_expression
    ;

type_expression
    : STRING
    | typed_call_expression
    ;

typed_call_expression
    : IDENT LPAREN STRING RPAREN
    ;

reference_expression
    : REFERENCE LPAREN STRING RPAREN
    ;

definition_block
    : DEFINITION LBRACE eval_statement RBRACE
    ;

eval_code_block
    : code_content*
    ;

code_content
    : LBRACE eval_code_block RBRACE
    | ~(LBRACE | RBRACE)
    ;

STATEMACHINE    : 'STATEMACHINE' ;
ATTACHED        : 'ATTACHED' ;
TO              : 'TO' ;
STATES          : 'STATES' ;
STATE           : 'STATE' ;
INITIAL         : 'INITIAL' ;
SHALLOW         : 'SHALLOW' ;
DEEP            : 'DEEP' ;
HISTORY         : 'HISTORY' ;
BREAKPOINT      : 'BREAKPOINT' ;
TICK            : 'TICK' | 'TICKS' ;
LIMIT           : 'LIMIT' ;
DELAY           : 'DELAY' ;
TIMEOUT         : 'TIMEOUT' ;
AFTER           : 'AFTER' ;
ENTRY           : 'ENTRY' ;
EXIT            : 'EXIT' ;
DO              : 'DO' ;
SET             : 'SET' | 'set' ;
SETLIST         : 'SETLIST' | 'setlist' ;
DROPLIST        : 'DROPLIST' | 'droplist' ;
APPEND          : 'APPEND' | 'append' ;
SETOBJ          : 'SETOBJ' | 'setobj' ;
APPENDOBJ       : 'APPENDOBJ' | 'appendobj' ;
DROPOBJ         : 'DROPOBJ' | 'dropobj' ;
EVENT           : 'EVENT' ;
WITH            : 'WITH' | 'with' ;
AS              : 'AS' | 'as' ;
MACRO           : 'MACRO' ;
EVAL            : 'EVAL' ;
TRANSITIONS     : 'TRANSITIONS' ;
TRANSITION      : 'TRANSITION' ;
SPLIT           : 'SPLIT' ;
JOIN            : 'JOIN' ;
NOT             : 'NOT' ;
LOOPING         : 'LOOPING' ;
START           : 'START' ;
END             : 'END' ;
CONDITION       : 'CONDITION' ;
VALUE           : 'VALUE' ;
MACROS          : 'MACROS' ;
TAKES           : 'TAKES' ;
RETURNS         : 'RETURNS' ;
DEFINITION      : 'DEFINITION' ;
REFERENCE       : 'reference' ;
IF              : 'IF' ;
IN              : 'IN' ;
SCOPE           : 'SCOPE' ;
ATTACHABLE      : 'ATTACHABLE' ;
ATTACH          : 'ATTACH' | 'attach' ;
EXCHANGE        : 'EXCHANGE' | 'exchange' ;
FRAGMENT        : 'FRAGMENT' ;
CONTAINER       : 'CONTAINER' ;
USE             : 'USE' ;
FINAL           : 'FINAL' ;
DONE            : 'DONE' ;

ARROW           : '->' ;
LBRACE          : '{' ;
RBRACE          : '}' ;
LPAREN          : '(' ;
RPAREN          : ')' ;
COMMA           : ',' ;
COLON           : ':' ;

STRING       : '"' ( '\\' . | ~["\\\r\n] )* '"' ;
SSTRING      : '\'' ( '\\' . | ~['\\\r\n] )* '\'' ;
IDENT        : [a-zA-Z_][a-zA-Z0-9_]* ;
NUMBER       : [0-9]+ ('.' [0-9]+)? ;

WS           : [ \t\r\n]+ -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~[\r\n]* -> channel(HIDDEN) ;
SYMBOL       : . ;

