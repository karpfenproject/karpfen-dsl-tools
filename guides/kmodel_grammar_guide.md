# KModel Grammar Guide

## Overview

KModel is a Domain-Specific Language (DSL) for defining data model instances within the *karpfen ecosystem*. Where KMeta defines the type schema, KModel instantiates it with concrete objects and values.

## Grammar Specification

The KModel grammar is defined using ANTLR v4 and follows this structure:

### ANTLR Grammar (Kmodel.g4)

```antlr
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
```

## KModel Syntax

### Basic Structure

A KModel instance file starts with a single root object definition using the `make object` keyword:

```
make object "<key>":"<ClassName>" {
    [prop_statement]*
    [has_statement]*
    [knows_statement]*
}
```

### Object Creation

Each object is created with the `make object` keyword followed by an object signature:

```
make object "<key>":"<ClassName>" {
    ...
}
```

- `<key>`: A unique identifier for this object instance (can be empty for anonymous objects using just `:ClassName`)
- `<ClassName>`: The type of the object, as defined in the metamodel
- `{ ... }`: The object body containing property and relationship assignments

Example:
```
make object "APB 2101":"Room" {
    ...
}
```

### Property Assignment (prop)

Properties assign concrete values to properties defined in the metamodel:

```
prop("<propertyName>") -> "<value>"
```

- `<propertyName>`: The property name as defined in the metamodel
- `<value>`: The concrete value (as a string)

Example:
```
prop("x") -> "5.0"
prop("diameter") -> "1.0"
prop("speed") -> "2.5"
```

### Embedded Objects (has)

The `has` statement assigns child objects to embedded relationships:

```
has("<relationName>") -> make object "<key>":"<ClassName>" {
    ...
}
```

Example:
```
has("position") -> make object "pointA":"Point" {
    prop("x") -> "0.0"
    prop("y") -> "10.0"
}
```

### References (knows)

The `knows` statement creates references to other objects by their key:

```
knows("<relationName>") -> "<objectKey>"
```

Example:
```
knows("obstacles") -> "chair"
knows("obstacles") -> "table"
```

## Complete Example

Here is a full model for the cleaning robot demo:

```
make object "APB 2101":"Room" {
    has("top_left") -> make object "topLeftPoint":"Point" {
        prop("x") -> "0.0"
        prop("y") -> "10.0"
    }
    has("top_right") -> make object "topRightPoint":"Point" {
        prop("x") -> "10.0"
        prop("y") -> "10.0"
    }
    has("bottom_left") -> make object "bottomLeftPoint":"Point" {
        prop("x") -> "0.0"
        prop("y") -> "0.0"
    }
    has("bottom_right") -> make object "bottomRightPoint":"Point" {
        prop("x") -> "0.0"
        prop("y") -> "0.0"
    }
    has("obstacles") -> make object "chair":"Obstacle" {
        has("boundingBox") -> make object "chairBoundingBox":"TwoDObject" {
            prop("diameter") -> "1.0"
            has("position") -> make object "chairPosition":"Point" {
                prop("x") -> "2.0"
                prop("y") -> "3.0"
            }
        }
    }
    has("obstacles") -> make object "table":"Obstacle" {
        has("boundingBox") -> make object "tableBoundingBox":"TwoDObject" {
            prop("diameter") -> "3.0"
            has("position") -> make object "tablePosition":"Point" {
                prop("x") -> "5.0"
                prop("y") -> "7.0"
            }
        }
    }
    has("robot") -> make object "turtle":"Robot" {
        prop("speed") -> "1.0"
        has("boundingBox") -> make object "turtleBoundingBox":"TwoDObject" {
            prop("diameter") -> "1.0"
            has("position") -> make object "turtlePosition":"Point" {
                prop("x") -> "5.0"
                prop("y") -> "5.0"
            }
        }
        knows("obstacles") -> "chair"
        knows("obstacles") -> "table"
    }
}
```

The root object `"APB 2101"` owns four corner points and two obstacles via `has`, and the robot references those obstacles via `knows`. Note how the same relationship name (`"obstacles"`) appears twice to build a collection.

## Key Concepts

### Object Keys and Identifiers

Each object can have an optional key that uniquely identifies it within the model:

```
make object "robotA":"Robot" { ... }    // Named object
make object "_":"Point" { ... }          // Anonymous object with no key
```

Keys are useful for creating references via `knows` statements.

### Hierarchical Structure

KModel creates hierarchical object trees through `has` statements. Objects can be nested arbitrarily deep:

```
make object "root":"Room" {
    has("robot") -> make object "r1":"Robot" {
        has("position") -> make object "_":"Point" {
            prop("x") -> "5.0"
        }
    }
}
```

### Property Values

Properties are assigned string values, which are interpreted according to the property type defined in the metamodel:

```
prop("x") -> "5.0"           // Will be parsed as number
prop("name") -> "Office A"   // Will be kept as string
prop("active") -> "true"     // Will be parsed as boolean
```

### Multiple Relationships

The same relationship can appear multiple times to create collections:

```
has("obstacles") -> make object "chair":"Obstacle" { ... }
has("obstacles") -> make object "table":"Obstacle" { ... }
```

## Alternative: Functional API

If you need to build models programmatically (e.g. generating them at runtime), use the `ModelBuilder` instead of writing `.kmodel` text files:

```kotlin
import dsl.functional.ModelBuilder
import instance.Model
import meta.Metamodel

fun setupRobotRoomModel(metamodel: Metamodel): Model {
    val m = ModelBuilder(metamodel)

    val obstacleA = m.makeObject("Obstacle", "chair").apply {
        assignRels(
            mapOf("boundingBox" to m.makeObject("TwoDObject").apply {
                assignProps(mapOf("diameter" to 1.0))
                assignRels(
                    mapOf("position" to m.makeObject("Point").apply {
                        assignProps(mapOf("x" to 2.0, "y" to 3.0))}))}))}

    val obstacleB = m.makeObject("Obstacle", "table").apply {
        assignRels(
            mapOf("boundingBox" to m.makeObject("TwoDObject").apply {
                assignProps(mapOf("diameter" to 3.0))
                assignRels(
                    mapOf("position" to m.makeObject("Point").apply {
                        assignProps(mapOf("x" to 5.0, "y" to 7.0))}))}))}

    val robot = m.makeObject("Robot", "turtle").apply {
        assignProps(mapOf("speed" to 1.0))
        assignRels(
            mapOf("boundingBox" to m.makeObject("TwoDObject").apply {
                assignProps(mapOf("diameter" to 1.0))
                assignRels(
                    mapOf(
                        "position" to m.makeObject("Point").apply {
                            assignProps(mapOf("x" to 5.0, "y" to 5.0))
                        },
                        "obstacles" to listOf(obstacleA, obstacleB)))}))}

    val room = m.makeObject("Room", "APB 2101").apply {
        assignRels(
            mapOf(
                "topLeftCorner" to m.makeObject("Point").apply {
                    assignProps(mapOf("x" to 0.0, "y" to 10.0))
                },
                "topRightCorner" to m.makeObject("Point").apply {
                    assignProps(mapOf("x" to 10.0, "y" to 10.0))
                },
                "bottomLeftCorner" to m.makeObject("Point").apply {
                    assignProps(mapOf("x" to 0.0, "y" to 0.0))
                },
                "bottomRightCorner" to m.makeObject("Point").apply {
                    assignProps(mapOf("x" to 10.0, "y" to 0.0))
                },
                "robot" to robot,
                "obstacles" to listOf(obstacleA, obstacleB)))}

    return m.build()
}
```

### ModelBuilder API

- `makeObject(className)` / `makeObject(className, key)` - create objects (anonymous or named)
- `assignProps(mapOf("x" to 5.0, ...))` - set property values (numbers, strings, booleans)
- `assignRels(mapOf("robot" to obj))` - set single relationships or lists of objects
- `build()` - produce the final model

Kotlin's `apply()` is used to chain calls on each object.

## Parsing KModel Files

To parse a `.kmodel` file, use `KmodelDSLConverter` from the `kotlin/dsl/textual` package.

## Relationship to KMeta

Every KModel file must match the types, properties, and relationships declared in its corresponding KMeta metamodel - the parser will reject unknown names.

