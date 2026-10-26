# KMeta Grammar Guide

## Overview

KMeta is a Domain-Specific Language (DSL) for defining data metamodels within the *karpfen ecosystem*. It lets you declare type hierarchies with properties, embedded relationships (`has`), and references (`knows`).

## Grammar Specification

The KMeta grammar is defined using ANTLR v4 in `src/main/antlr/io/karpfen/dsl/grammar/kmeta/Kmeta.g4`. The build generates `KmetaLexer`, `KmetaParser` and the visitor from it into the package `io.karpfen.dsl.grammar.kmeta`, which its `@header` declares. The grammar follows this structure:

### ANTLR Grammar (Kmeta.g4)

```antlr
grammar Kmeta;

@header {
package io.karpfen.dsl.grammar.kmeta;
}

// Parser Rules
kmeta_file
    : type_definition* EOF
    ;

type_definition
    : TYPE STRING STRING LBRACE rule_list? RBRACE
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

LBRACE   : '{' ;
RBRACE   : '}' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
COMMA    : ',' ;

STRING   : '"' ( '\\' . | ~["\r\n] )* '"' ;

WS       : [ \t\r\n]+ -> skip ;
COMMENT  : '//' ~[\r\n]* -> skip ;
```

## KMeta Syntax

### Basic Structure

A KMeta metamodel file consists of one or more type definitions:

```
type "<TypeName>" "<Description>" {
    [prop_statement]*
    [has_statement]*
    [knows_statement]*
}
```

### Comments

Single-line comments start with `//` and run to the end of the line. They are ignored by the parser and
may appear anywhere whitespace is allowed, including at the end of a line:

```
// Domain types for the cleaning-robot example
type "Vector" "A vector in 2D space" { // inline comment
    prop("x", "number") // the x coordinate
    prop("y", "number")
}
```

### Type Definition

Each type definition starts with the `type` keyword followed by:
1. **Type Name** (string): The identifier for the type
2. **Description** (string): Documentation describing the type's purpose
3. **Body**: An optional set of statements enclosed in curly braces

```
type "Point" "A point in 2D space" {
    ...
}
```

### Properties (prop)

Properties define atomic data attributes of a type using the `prop` statement:

```
prop("<propertyName>", "<propertyType>")
```

- `<propertyName>`: The identifier for the property
- `<propertyType>`: The data type (e.g., "number", "string", "boolean")

Example:
```
type "Person" "A person entity" {
    prop("name", "string")
    prop("age", "number")
}
```

### Relationships (has)

The `has` statement defines embedded or linked relationships to other types:

```
has("<relationName>", "<TypeName>")
```

or for collections:

```
has("<relationName>", list("<TypeName>"))
```

- `<relationName>`: The identifier for the relationship
- `<TypeName>`: The name of the related type
- `list()`: Optional wrapper indicating a collection of the type

Example:
```
type "Room" "A room containing objects" {
    has("robot", "Robot")
    has("obstacles", list("Obstacle"))
}
```

### Knowledge References (knows)

The `knows` statement defines references or links to other entities without embedding them:

```
knows("<referenceName>", "<TypeName>")
```

or for collections:

```
knows("<referenceName>", list("<TypeName>"))
```

Example:
```
type "Robot" "A cleaning robot" {
    knows("obstacles", list("Obstacle"))
}
```

## Complete Example

Here is a full metamodel for the cleaning robot demo:

```
type "Point" "A point in 2D space" {
    prop("x", "number")
    prop("y", "number")
}

type "TwoDObject" "A two-dimensional object with a position and diameter" {
    prop("diameter", "number")
    has("position", "Point")
}

type "Obstacle" "An obstacle in the room" {
    has("boundingBox", "TwoDObject")
}

type "Robot" "A cleaning robot that can move around the room and log its actions" {
    prop("speed", "number")
    prop("log", list("string"))
    has("boundingBox", "TwoDObject")
    knows("obstacles", list("Obstacle"))
}

type "Room" "A room that contains a robot and obstacles" {
    has("robot", "Robot")
    has("obstacles", list("Obstacle"))
    has("top_left", "Point")
    has("top_right", "Point")
    has("bottom_left", "Point")
    has("bottom_right", "Point")
}
```

Note how `Room` is defined last - the last type in a KMeta file is the root type. All other types must be reachable from the root through `has` embeddings.

The `list()` wrapper marks one-to-many relationships. Forward references between types are allowed.

## Alternative: Functional API

If you need to build metamodels programmatically (e.g. generating them at runtime), use the `MetamodelBuilder` instead of writing `.kmeta` text files:

```kotlin
import io.karpfen.dsl.dsl.functional.MetamodelBuilder

val m = MetamodelBuilder()

m.makeClass(
    name = "Point",
    comment = "A point in 2D space",
    props = listOf(
        m.makeNumber(key = "x"),
        m.makeNumber(key = "y")
    ))

m.makeClass(
    name = "TwoDObject",
    comment = "A two-dimensional object with a position and diameter",
    props = listOf(m.makeNumber(key = "diameter")),
    rels = listOf(m.makeClassProp(key = "position", classT = "Point", relT = m.EMBEDDED))
)

m.makeClass(
    name = "Obstacle",
    comment = "An obstacle in the room that the robot should avoid",
    rels = listOf(m.makeClassProp(key = "boundingBox", classT = "TwoDObject", relT = m.EMBEDDED))
)

m.makeClass(
    name = "Robot",
    comment = "A cleaning robot that can move around the room and log its actions",
    props = listOf(
        m.makeNumber(key = "speed"),
        m.makeStringList(key = "log")),
    rels = listOf(
        m.makeClassProp(key = "boundingBox", classT = "TwoDObject", relT = m.EMBEDDED),
        m.makeClassPropList(key = "obstacles", classT = "Obstacle", relT = m.LINK)
    )
)

m.makeClass(
    name = "Room",
    comment = "A room that contains a robot and some obstacles",
    rels = listOf(
        m.makeClassProp(key = "topLeftCorner", classT = "Point", relT = m.EMBEDDED),
        m.makeClassProp(key = "topRightCorner", classT = "Point", relT = m.EMBEDDED),
        m.makeClassProp(key = "bottomLeftCorner", classT = "Point", relT = m.EMBEDDED),
        m.makeClassProp(key = "bottomRightCorner", classT = "Point", relT = m.EMBEDDED),
        m.makeClassProp(key = "robot", classT = "Robot", relT = m.EMBEDDED),
        m.makeClassPropList(key = "obstacles", classT = "Obstacle", relT = m.EMBEDDED)
    )
)

m.setRoot("Room")
val metamodel = m.build()
```

### MetamodelBuilder API

**Properties:** `makeNumber(key)`, `makeString(key)`, `makeBool(key)` and the list variants `makeNumberList(key)`, `makeStringList(key)`, `makeBoolList(key)`

**Relationships:** `makeClassProp(key, classT, relT)` for single, `makeClassPropList(key, classT, relT)` for lists. `relT` is either `m.EMBEDDED` (`has`) or `m.LINK` (`knows`).

**Class & build:** `makeClass(name, comment, props, rels)` defines a type, `setRoot(name)` picks the root, `build()` produces the metamodel.


## Parsing KMeta Files

To parse a `.kmeta` file, use `KmetaDSLConverter` from the package `io.karpfen.dsl.dsl.textual`. It parses with `KmetaFileParser` (package `io.karpfen.dsl.parser`) and builds the metamodel classes of the package `io.karpfen.dsl.meta`. The test `KmetaStringParserTest` shows the whole flow on the example file `example/metamodel_dsl_example.kmeta`.

For text that may contain errors, such as text being typed in an editor, `KmetaReader` from the package `io.karpfen.dsl.reader.kmeta` reads without throwing: it reports every problem as a diagnostic with a range, builds a partial metamodel and locates its types, properties and relation targets in a source map. The test `ExampleSourceMapTest` shows it on every example.

The way back is `KmetaPrinter` from the package `io.karpfen.dsl.printer.kmeta`, to which `KmetaDSLConverter.convertToKmetaString` delegates: it writes a metamodel, a single type or a single rule as KMeta text in the canonical layout, with the root type last, and can leave out the base types of an event metamodel.

