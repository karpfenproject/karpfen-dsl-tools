# KStates Grammar Guide

## Overview

KStates is a Domain-Specific Language (DSL) for defining hierarchical state machines in the *karpfen ecosystem*. A `.kstates` file declares states (with `ENTRY`/`DO` action blocks), transitions with conditions, and optional macros for reusable logic.

## Grammar Specification

The KStates grammar is defined with ANTLR v4.
You find the full grammar at the end of this guide.

## KStates Syntax

### Basic Structure

A KStates file starts with the state machine header and contains three major blocks: `STATES`, `TRANSITIONS`, and optional `MACROS`.

```
STATEMACHINE ATTACHED TO "<ClassName>" {
    STATES { ... }
    TRANSITIONS { ... }
    MACROS { ... } // optional
}
```

### State Definitions

States are declared in the `STATES` block. A state can be marked as `INITIAL`.

```
INITIAL STATE "ready" { }

STATE "observe" {
    ENTRY { ... }
    DO { ... }
    STATE "react to obstacle" { ... } // nested state
}
```

- `INITIAL` is optional per state declaration.
- Exactly one state should be initial in a complete machine.
- States can contain nested sub-states.

### Action Blocks (`ENTRY` / `DO`)

`ENTRY` and `DO` both contain action rules:

```
ENTRY {
    SET("x", "1.0")
    APPEND("log", "Robot started")
    EVENT("public", "start")
}
```

Action rule format:

```
<SET|APPEND|EVENT>("leftSide", <rightSide>)
```

Right side can be:
- a literal string: `"value"`
- a macro call: `MACRO("name", "arg1", "arg2")`
- an eval statement: `EVAL { ... }`

### Transitions

Transitions are declared in `TRANSITIONS`:

```
TRANSITION "observe" -> "react to obstacle" NOT LOOPING {
    CONDITION {
        EVAL { return $(d_closest_obstacle) < $(d_closest_wall) }
    }
}
```

- `NOT LOOPING` is optional.
- A transition may include a `CONDITION` block.
- Condition variants:
  - `EVAL { ... }`
  - `EVENT("domain", "value")`
  - `VALUE("variableKey")`

### Macros

Macros are declared in `MACROS`:

```
MACRO "get_closest_obstacle" {
    TAKES("robot_position", "Vector")
    TAKES("obstacles", list("Obstacle"))
    RETURNS(reference("Obstacle"))
    DEFINITION {
        EVAL {
            // arbitrary code
        }
    }
}
```

Macro structure:
- Zero or more `TAKES(name, type)` directives
- One mandatory `RETURNS(...)` directive
- One mandatory `DEFINITION { EVAL { ... } }`

## Complete Example

The file `example/statemachine_full_example/cleaning_robot.kstates` is a complete and parseable reference example.

It contains:
- Top-level states such as `ready`, `observe`, and `drive`
- Nested states like `react to obstacle`, `react to wall`, `drive fast`, `drive slow`
- Ordered transitions (runtime-relevant order)
- Rich macro definitions for geometric and decision logic

## Key Concepts

### Hierarchical States

States can be nested arbitrarily deep - outer states represent high-level modes, inner states handle specific sub-behaviors.

### Executable Snippets via `EVAL`

`EVAL { ... }` blocks are opaque to the parser. ANTLR captures the content as a raw code string without any semantic analysis.

### Macro Calls in Actions

A macro call is written as:

```
MACRO("macroName", "arg1", "arg2", ...)
```

The first parameter is the macro name, all remaining parameters are arguments.

### Return References

Macro return types can express reference semantics:

```
RETURNS(reference("Obstacle"))
```

This differs from plain values such as `RETURNS("number")`.

## Best Practices

1. Keep one clearly identifiable `INITIAL` state in the whole state machine.
2. Use nested states to model complexity instead of writing very long `DO` blocks.
3. Keep transition conditions small and focused; move larger logic into macros.
4. Use meaningful macro names and type hints in `TAKES` and `RETURNS`.
5. Place the most specific transitions first if runtime uses first-match semantics.

## Parsing KStates Files

Use the textual converter in `src/main/kotlin/dsl/textual/KstatesDSLConverter.kt`:

```kotlin
import dsl.textual.KstatesDSLConverter

val sm = KstatesDSLConverter.parseKstatesFile("example/statemachine_full_example/cleaning_robot.kstates")
println(sm.attachedToClass)
println(sm.states.size)
```

Low-level parse access is available via `src/main/kotlin/parser/KStatesFileParser.kt`.

The integration test `src/test/kotlin/KstatesParserIntegrationTest.kt` shows a full parse flow against the example file.


## Full ANTLR KStates Grammar (`Kstates.g4`)

```antlr
grammar Kstates;

kstates_file
    : statemachine EOF
    ;

statemachine
    : STATEMACHINE ATTACHED TO STRING LBRACE states_block transitions_block macros_block? RBRACE
    ;

states_block
    : STATES LBRACE state_definition* RBRACE
    ;

state_definition
    : INITIAL? STATE STRING LBRACE entry_block? do_block? state_definition* RBRACE
    ;

entry_block
    : ENTRY action_block
    ;

do_block
    : DO action_block
    ;

action_block
    : LBRACE action_rule* RBRACE
    ;

action_rule
    : action_operation LPAREN STRING COMMA action_right_side RPAREN
    ;

action_operation
    : SET
    | APPEND
    | EVENT
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
    : TRANSITION STRING ARROW STRING not_looping? LBRACE condition_block? RBRACE
    ;

not_looping
    : NOT LOOPING
    ;

condition_block
    : CONDITION LBRACE transition_condition RBRACE
    ;

transition_condition
    : eval_statement
    | event_condition
    | value_condition
    ;

event_condition
    : EVENT LPAREN STRING COMMA STRING RPAREN
    ;

value_condition
    : VALUE LPAREN STRING RPAREN
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
```