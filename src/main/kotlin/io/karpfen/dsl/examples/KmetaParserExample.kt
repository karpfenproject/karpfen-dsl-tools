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

import io.karpfen.dsl.parser.KmetaFile
import io.karpfen.dsl.parser.KmetaFileParser
import io.karpfen.dsl.parser.ListValue
import io.karpfen.dsl.parser.Rule
import io.karpfen.dsl.parser.StringValue
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("io.karpfen.dsl.examples.KmetaParserExample")

private const val EXAMPLE_KMETA = """
    type "Person" "A person entity" {
        prop("name", "string")
        prop("age", "number")
        has("address", list("Address"))
        knows("friends", list("Person"))
    }

    type "Address" "An address entity" {
        prop("street", "string")
        prop("city", "string")
        prop("zipCode", "string")
    }
"""

/**
 * Minimal KMeta parser example: parses a small KMeta text with [KmetaFileParser] and logs the parsed types
 * at info level, which is the output of this example.
 *
 * The example works on the syntax level only, without a metamodel or any type checking. Real applications
 * convert KMeta with [io.karpfen.dsl.dsl.textual.KmetaDSLConverter] into a metamodel and work with that.
 */
fun main() {
    val kmetaFile = KmetaFileParser().parseString(EXAMPLE_KMETA.trimIndent())
    logger.info("Parsed {} type(s):\n{}", kmetaFile.types.size, renderTypes(kmetaFile))
}

/**
 * Renders every type of [kmetaFile] with its properties and relations, one line each.
 */
private fun renderTypes(kmetaFile: KmetaFile): String = buildString {
    for (type in kmetaFile.types) {
        appendLine("Type: ${type.name}")
        appendLine("   Description: ${type.description}")
        appendRules("Properties", type.props)
        appendRules("Has", type.has)
        appendRules("Knows", type.knows)
    }
}

private fun StringBuilder.appendRules(heading: String, rules: List<Rule>) {
    if (rules.isEmpty()) {
        return
    }
    appendLine("   $heading:")
    for (rule in rules) {
        val valueStr = when (val v = rule.value) {
            is StringValue -> v.value
            is ListValue -> "list(${v.value})"
        }
        appendLine("      • ${rule.key}: $valueStr")
    }
}
