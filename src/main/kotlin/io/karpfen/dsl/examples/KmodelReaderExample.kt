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

import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader

private const val EXAMPLE_KMETA = """
    type "Point" "A point in 2D space" {
        prop("x", "number")
        prop("y", "number")
    }

    type "Robot" "A cleaning robot" {
        prop("speed", "number")
        has("position", "Point")
        knows("home", "Point")
    }

    type "Room" "A room with a robot and its charging station" {
        prop("room_number", "string")
        has("charging_station", "Point")
        has("robot", "Robot")
    }
"""

private const val EXAMPLE_KMODEL = """
    make object "APB 2101":"Room" {
        prop("room_number") -> "2101"

        has("charging_station") -> make object "dock":"Point" {
            prop("x") -> "0.0"
            prop("y") -> "10.0"
        }

        has("robot") -> make object "turtle":"Robot" {
            prop("speed") -> "1.0"

            has("position") -> make object "_":"Point" {
                prop("x") -> "5.0"
                prop("y") -> "5.0"
            }

            knows("home") -> "dock"
        }
    }
"""

/**
 * Minimal KModel example: reads a small metamodel with [KmetaReader] and a model of it with [KmodelReader], and prints
 * the problems the readers found and the objects of the model to standard output, which is the output of this
 * program; the library itself logs and never prints.
 *
 * A program that expects the texts to be right may use [io.karpfen.dsl.dsl.textual.KmetaDSLConverter] and
 * [io.karpfen.dsl.dsl.textual.KmodelDSLConverter] instead, which return the metamodel and the model or throw with every
 * error of the text.
 */
fun main() {
    val metamodelRead = KmetaReader().read(EXAMPLE_KMETA)
    print(renderProblems(metamodelRead.diagnostics))
    val metamodel = metamodelRead.model ?: return
    val modelRead = KmodelReader(metamodel).read(EXAMPLE_KMODEL)
    print(renderProblems(modelRead.diagnostics))
    val model = modelRead.model ?: return
    println("Read ${model.objects.size} root object(s)")
    for (obj in model.objects) {
        print(renderObject(obj, indent = 0))
    }
}

/**
 * Returns [obj] with its property values, the objects embedded in it, recursively, and the ids of the objects it
 * knows.
 *
 * @param indent The nesting depth the object starts at, two spaces per level.
 */
private fun renderObject(obj: DataObject, indent: Int): String = buildString {
    val prefix = "  ".repeat(indent)
    appendLine("${prefix}Object \"${obj.id}\" of type ${obj.ofType.name}")
    for (property in obj.ofType.simpleProperties) {
        appendLine("$prefix  - ${property.key}: ${obj.getProp(property.key).joinToString()}")
    }
    for (relation in obj.ofType.objectProperties) {
        val related = obj.getRel(relation.key)
        if (relation.associationType == AssociationType.EMBEDDED) {
            appendLine("$prefix  - ${relation.key}:")
            related.forEach { append(renderObject(it, indent + 2)) }
        } else {
            appendLine("$prefix  - ${relation.key} knows ${related.joinToString { "\"${it.id}\"" }}")
        }
    }
}
