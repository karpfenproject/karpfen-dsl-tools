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
package io.karpfen.dsl.parser

/**
 * The syntax-level content of a KMeta file, before type names are resolved into a metamodel.
 *
 * @property types The type definitions, in definition order.
 */
data class KmetaFile(val types: List<TypeDefinition>)

/**
 * A `type "Name" "Description" { ... }` definition with its rules.
 *
 * @property name The type name.
 * @property description The type description.
 */
data class TypeDefinition(
    val name: String,
    val description: String
) {

    /** The `prop` rules, in definition order. */
    val props = mutableListOf<Rule>()

    /** The `has` rules, in definition order. */
    val has = mutableListOf<Rule>()

    /** The `knows` rules, in definition order. */
    val knows = mutableListOf<Rule>()

    /**
     * Adds a `prop` rule.
     */
    fun addProp(key: String, value: RuleValue) {
        props.add(Rule(key, value))
    }

    /**
     * Adds a `has` rule.
     */
    fun addHas(key: String, value: RuleValue) {
        has.add(Rule(key, value))
    }

    /**
     * Adds a `knows` rule.
     */
    fun addKnows(key: String, value: RuleValue) {
        knows.add(Rule(key, value))
    }
}

/**
 * A `prop`, `has` or `knows` rule.
 *
 * @property key The property name.
 * @property value The property's type, single or list.
 */
data class Rule(val key: String, val value: RuleValue)

/**
 * The type part of a rule.
 */
sealed class RuleValue

/**
 * A single-valued type, written `"T"`.
 *
 * @property value The type name.
 */
data class StringValue(val value: String) : RuleValue()

/**
 * A list type, written `list("T")`.
 *
 * @property value The element type name.
 */
data class ListValue(val value: String) : RuleValue()
