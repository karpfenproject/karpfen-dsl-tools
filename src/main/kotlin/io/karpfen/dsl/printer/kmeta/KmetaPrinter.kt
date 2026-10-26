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
package io.karpfen.dsl.printer.kmeta

import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.meta.SimpleProperty
import io.karpfen.dsl.parser.KmetaVocabulary
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.printer.DocumentPrinter

/**
 * Writes [Metamodel]s, and single class types and rules of them, as KMeta text in the canonical layout.
 *
 * The layout indents by four spaces, writes one rule per line and separates type definitions by a blank
 * line. The types follow the order of [Metamodel.types], except that the root class type comes last,
 * because KMeta reads the last type of a file as the root. Within a type the `prop` rules come first, in the
 * order of [ClassType.simpleProperties], followed by the `has` and `knows` rules in the order of
 * [ClassType.objectProperties]. Comments are not part of a metamodel and are not written, so reading a KMeta
 * text and writing it again drops its comments. A single type is written as a fragment, see [DocumentPrinter].
 *
 * The printer holds no state; one instance serves any number of calls, also concurrent ones on metamodels
 * that are not changed meanwhile.
 */
class KmetaPrinter : DocumentPrinter<Metamodel> {

    /** The literal syntax of KMeta. */
    private val literals = StringLiteralSyntax.KMETA

    /**
     * Returns [document] as a KMeta document with all its class types, see [print] with base types.
     *
     * @throws IllegalArgumentException If a name or a description cannot be written as a KMeta string
     * literal.
     */
    override fun print(document: Metamodel): String = print(document, emptyList())

    /**
     * Returns [metamodel] as a KMeta document without the class types in [baseTypes].
     *
     * This mirrors reading KMeta text with base types, as an event metamodel is read with the types of the
     * domain metamodel: those types belong to the metamodel but are defined in another document. A base type is
     * left out as that very object, not by its name, so a type of the document that shares its name with a base
     * type, which reading resolves before the base type, is written. The root class type is always written, as
     * the last type.
     *
     * @param baseTypes Class types that are defined elsewhere.
     * @throws IllegalArgumentException If a name or a description cannot be written as a KMeta string
     * literal.
     */
    fun print(metamodel: Metamodel, baseTypes: Collection<ClassType>): String {
        val root = metamodel.rootClass
        val types = metamodel.types.filter { type -> type !== root && baseTypes.none { it === type } } + root
        return CodeWriter.document { writer -> writer.paragraphs(types) { writeType(it, writer) } }
    }

    /**
     * Returns the type definition `type "Name" "Description" { ... }` of [type] with its rules, as a fragment
     * whose first line stands at [indentLevel].
     *
     * @param sharedIndentation The number of whitespace chars that every line of the document the fragment stands in
     * starts with, at least, as [String.trimIndent] counts them; the fragment adds as many spaces in front of the
     * continuation lines of its string literals, see [DocumentPrinter].
     * @throws IllegalArgumentException If a name or the description cannot be written as a KMeta string
     * literal, or [indentLevel] or [sharedIndentation] is negative.
     */
    fun printType(type: ClassType, indentLevel: Int = 0, sharedIndentation: Int = 0): String =
        CodeWriter.fragment(indentLevel, sharedIndentation) { writeType(type, it) }

    /**
     * Returns the `prop` rule of [property] as a fragment on one line, unless a string literal continues on the next
     * line, for example `prop("log", list("string"))`.
     *
     * @param sharedIndentation The indentation that every line of the document shares, see [printType].
     * @throws IllegalArgumentException If the property name cannot be written as a KMeta string literal, or
     * [sharedIndentation] is negative.
     */
    fun printRule(property: SimpleProperty, sharedIndentation: Int = 0): String =
        CodeWriter.fragment(0, sharedIndentation) { it.line(propRule(property)) }

    /**
     * Returns the `has` rule of an embedded or the `knows` rule of a linked [property] as a fragment on one line,
     * unless a string literal continues on the next line, for example `has("position", "Point")`.
     *
     * @param sharedIndentation The indentation that every line of the document shares, see [printType].
     * @throws IllegalArgumentException If a name cannot be written as a KMeta string literal, or [sharedIndentation]
     * is negative.
     */
    fun printRule(property: ClassTypeProperty, sharedIndentation: Int = 0): String =
        CodeWriter.fragment(0, sharedIndentation) { it.line(relationRule(property)) }

    /**
     * Writes the definition of [type] with its `prop` rules first and then its `has` and `knows` rules.
     */
    private fun writeType(type: ClassType, writer: CodeWriter) {
        writer.block("type ${literals.quote(type.name)} ${literals.quote(type.comment)}") {
            type.simpleProperties.forEach { writer.line(propRule(it)) }
            type.objectProperties.forEach { writer.line(relationRule(it)) }
        }
    }

    /**
     * Returns the rule `prop("key", type)` of [property].
     */
    private fun propRule(property: SimpleProperty): String {
        val type = typeValue(property.propertyType.keyword, property.isList)
        return "prop(${literals.quote(property.key)}, $type)"
    }

    /**
     * Returns the rule `has("key", type)` or `knows("key", type)` of [property].
     */
    private fun relationRule(property: ClassTypeProperty): String {
        val keyword = KmetaVocabulary.relationKeyword(property.associationType)
        val target = typeValue(property.reference.classTypeName, property.isList)
        return "$keyword(${literals.quote(property.key)}, $target)"
    }

    /**
     * Returns the type part of a rule: `"T"` for a single value and `list("T")` for a list.
     */
    private fun typeValue(typeName: String, isList: Boolean): String {
        val quoted = literals.quote(typeName)
        return if (isList) "list($quoted)" else quoted
    }
}
