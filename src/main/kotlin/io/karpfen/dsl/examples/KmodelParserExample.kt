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
package io.karpfen.dsl.examples

import io.karpfen.dsl.parser.KmodelFileParser
import io.karpfen.dsl.parser.ModelObject
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("io.karpfen.dsl.examples.KmodelParserExample")

private const val EXAMPLE_KMODEL = """
    make object "APB 2101":"Room" {
        prop("room_number") -> "2101"
        prop("floor") -> "2"

        has("top_left") -> make object "_":"Point" {
            prop("x") -> "0.0"
            prop("y") -> "10.0"
        }

        has("top_right") -> make object "_":"Point" {
            prop("x") -> "10.0"
            prop("y") -> "10.0"
        }

        has("bottom_left") -> make object "_":"Point" {
            prop("x") -> "0.0"
            prop("y") -> "0.0"
        }

        has("robot") -> make object "turtle":"Robot" {
            prop("speed") -> "1.0"
            prop("battery_level") -> "85"

            has("position") -> make object "_":"Point" {
                prop("x") -> "5.0"
                prop("y") -> "5.0"
            }

            knows("home_position") -> "charging_station"
            knows("current_task") -> "exploring"
        }

        has("obstacles") -> make object "chair":"Obstacle" {
            prop("type") -> "furniture"

            has("position") -> make object "_":"Point" {
                prop("x") -> "2.0"
                prop("y") -> "3.0"
            }
        }
    }
"""

/**
 * Minimal KModel parser example: parses a small KModel text with [KmodelFileParser] and logs the parsed
 * objects at info level, which is the output of this example.
 *
 * The example works on the syntax level only, without a metamodel or any type checking. Real applications
 * convert KModel with [io.karpfen.dsl.dsl.textual.KmodelDSLConverter] against a metamodel and work with
 * the resulting model.
 */
fun main() {
    val kmodelFile = KmodelFileParser().parseString(EXAMPLE_KMODEL.trimIndent())
    logger.info("Parsed {} object(s)", kmodelFile.objects.size)
    for (obj in kmodelFile.objects) {
        printObject(obj, indent = 0)
    }
}

/**
 * Logs [obj] at info level as a block with headed sections for its properties, its embedded objects
 * (rendered recursively) and its references.
 *
 * @param indent The nesting depth the block starts at, two spaces per level.
 */
fun printObject(obj: ModelObject, indent: Int = 0) {
    logger.info("\n{}", renderObject(obj, indent))
}

/**
 * Logs [obj] at info level in the compact form used for nested objects, without section headings.
 *
 * @param indent The nesting depth the object starts at, two spaces per level.
 */
fun printObjectInline(obj: ModelObject, indent: Int = 0) {
    logger.info("\n{}", renderObjectInline(obj, indent))
}

private fun renderObject(obj: ModelObject, indent: Int): String = buildString {
    val prefix = "  ".repeat(indent)
    val nextPrefix = "  ".repeat(indent + 1)

    appendLine("${prefix}Object: $obj")
    appendLine("$prefix{")
    if (obj.props.isNotEmpty()) {
        appendLine("${nextPrefix}Properties:")
        for (prop in obj.props) {
            appendLine("$nextPrefix• ${prop.key}: ${prop.value}")
        }
    }
    if (obj.has.isNotEmpty()) {
        appendLine("${nextPrefix}Has relations:")
        for (hasRel in obj.has) {
            appendLine("$nextPrefix• ${hasRel.key}:")
            append(renderObjectInline(hasRel.childObject, indent + 2))
        }
    }
    if (obj.knows.isNotEmpty()) {
        appendLine("${nextPrefix}Knows relations:")
        for (knowsRel in obj.knows) {
            appendLine("$nextPrefix• ${knowsRel.key}: ${knowsRel.value}")
        }
    }
    appendLine("$prefix}")
}

private fun renderObjectInline(obj: ModelObject, indent: Int): String = buildString {
    val prefix = "  ".repeat(indent)
    val nextPrefix = "  ".repeat(indent + 1)

    appendLine("$prefix$obj {")
    for (prop in obj.props) {
        appendLine("$nextPrefix• ${prop.key}: ${prop.value}")
    }
    for (hasRel in obj.has) {
        appendLine("$nextPrefix• ${hasRel.key}:")
        append(renderObjectInline(hasRel.childObject, indent + 1))
    }
    for (knowsRel in obj.knows) {
        appendLine("$nextPrefix• ${knowsRel.key}: ${knowsRel.value}")
    }
    appendLine("$prefix}")
}
