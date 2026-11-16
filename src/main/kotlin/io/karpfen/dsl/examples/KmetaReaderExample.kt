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

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.reader.kmeta.KmetaReader

private const val EXAMPLE_KMETA = """
    type "Address" "An address entity" {
        prop("street", "string")
        prop("city", "string")
        prop("zipCode", "string")
    }

    type "Person" "A person entity" {
        prop("name", "string")
        prop("age", "number")
        has("address", list("Address"))
        knows("friends", list("Person"))
    }
"""

/**
 * Minimal KMeta example: reads a small KMeta text with [KmetaReader] and prints the problems the reader found and the
 * types of the metamodel to standard output, which is the output of this program; the library itself logs and never
 * prints.
 *
 * A program that expects the text to be right may use [io.karpfen.dsl.dsl.textual.KmetaDSLConverter] instead, which
 * returns the metamodel or throws with every error of the text.
 */
fun main() {
    val result = KmetaReader().read(EXAMPLE_KMETA)
    print(renderProblems(result.diagnostics))
    val metamodel = result.model ?: return
    println("Read ${metamodel.types.size} type(s), root type ${metamodel.rootClass.name}:")
    print(renderTypes(metamodel))
}

/**
 * Returns the types of [metamodel] with their descriptions, properties and relations, one line each.
 */
private fun renderTypes(metamodel: Metamodel): String = buildString {
    for (type in metamodel.types) {
        appendLine("Type: ${type.name}")
        appendLine("   Description: ${type.comment}")
        appendRules("Properties", type.simpleProperties.map { it.key to listed(it.propertyType.keyword, it.isList) })
        appendRules("Has", relationsOf(type, AssociationType.EMBEDDED))
        appendRules("Knows", relationsOf(type, AssociationType.LINK))
    }
}

/**
 * Returns the relations of [type] of [kind], each as its key and its target type.
 */
private fun relationsOf(type: ClassType, kind: AssociationType): List<Pair<String, String>> =
    type.objectProperties.filter { it.associationType == kind }
        .map { it.key to listed(it.reference.classTypeName, it.isList) }

/**
 * Returns [typeName] as KMeta writes it for a list, `list("T")`, if [isList], otherwise as it is.
 */
private fun listed(typeName: String, isList: Boolean): String = if (isList) "list($typeName)" else typeName

/**
 * Appends the [rules] under [heading], unless there are none.
 */
private fun StringBuilder.appendRules(heading: String, rules: List<Pair<String, String>>) {
    if (rules.isEmpty()) {
        return
    }
    appendLine("   $heading:")
    for ((key, value) in rules) {
        appendLine("      - $key: $value")
    }
}
