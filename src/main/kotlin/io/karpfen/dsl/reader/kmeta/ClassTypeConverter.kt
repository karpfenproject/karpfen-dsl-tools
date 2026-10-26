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
package io.karpfen.dsl.reader.kmeta

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.grammar.kmeta.KmetaParser
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.ClassTypeReference
import io.karpfen.dsl.meta.SimpleProperty
import io.karpfen.dsl.parser.KmetaVocabulary
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.reader.presentTokens
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Converts the type definitions of a KMeta parse tree into [ClassType]s whose relations are not resolved yet,
 * and records where each type, property and relation target is written.
 *
 * @param session The read the conversion belongs to.
 */
internal class ClassTypeConverter(private val session: ReadingSession) {

    /**
     * Converts [definitions] in text order, leaving out those without a name.
     */
    fun convert(definitions: List<KmetaParser.Type_definitionContext>): List<ClassType> =
        definitions.mapNotNull { convertType(it) }

    /** Converts a type definition with its `prop`, `has` and `knows` rules; null for a definition without a name. */
    private fun convertType(definition: KmetaParser.Type_definitionContext): ClassType? {
        val strings = definition.presentTokens(KmetaParser.STRING)
        val nameToken = strings.firstOrNull() ?: return null
        val description = strings.getOrNull(1)?.let { unquote(it) } ?: ""
        val rules = definition.rule_list()?.rule_().orEmpty()
        val classType = ClassType(
            unquote(nameToken),
            description,
            rules.mapNotNull { it.prop_rule() }.mapNotNull { convertProperty(it) },
            rules.mapNotNull { it.has_rule() }.mapNotNull { convertRelation(it, AssociationType.EMBEDDED) } +
                rules.mapNotNull { it.knows_rule() }.mapNotNull { convertRelation(it, AssociationType.LINK) }
        )
        session.record(classType, definition, nameToken)
        return classType
    }

    /**
     * Converts a `prop` rule; reports a type other than the primitive ones and leaves such a rule out.
     */
    private fun convertProperty(rule: KmetaParser.Prop_ruleContext): SimpleProperty? {
        val typedKey = TypedKey.of(rule) ?: return null
        val typeName = unquote(typedKey.typeToken)
        val propertyType = KmetaVocabulary.primitiveTypeNamed(typeName)
        if (propertyType == null) {
            session.report(
                DiagnosticCode.UNKNOWN_PROPERTY_TYPE,
                session.valueProblemRangeOf(typedKey.typeToken),
                "Unknown type '$typeName' of prop '${unquote(typedKey.keyToken)}'; " +
                    "a prop is of type ${KmetaVocabulary.primitiveTypeNames.joinToString(", ") { "'$it'" }}"
            )
            return null
        }
        val property = SimpleProperty(unquote(typedKey.keyToken), propertyType, typedKey.isList)
        session.record(property, rule, typedKey.keyToken)
        return property
    }

    /**
     * Converts a `has` or `knows` rule into a relation with an unresolved reference to its target type.
     */
    private fun convertRelation(rule: ParserRuleContext, associationType: AssociationType): ClassTypeProperty? {
        val typedKey = TypedKey.of(rule) ?: return null
        val reference = ClassTypeReference(associationType, unquote(typedKey.typeToken))
        val relation = ClassTypeProperty(unquote(typedKey.keyToken), reference, typedKey.isList, associationType)
        session.record(relation, rule, typedKey.keyToken)
        session.record(reference, typedKey.value, typedKey.typeToken)
        return relation
    }

    /**
     * The key and the type of a `prop`, `has` or `knows` rule, which all have the same shape.
     *
     * @property keyToken The quoted key.
     * @property value The `"T"` or `list("T")` part.
     * @property typeToken The quoted type name inside [value].
     * @property isList True for `list("T")`.
     */
    private class TypedKey(
        val keyToken: TerminalNode,
        val value: KmetaParser.Rule_valueContext,
        val typeToken: TerminalNode,
        val isList: Boolean
    ) {

        /** Reads typed keys. */
        companion object {

            /**
             * Reads the key and the type of [rule], or returns null if one of them is missing.
             */
            fun of(rule: ParserRuleContext): TypedKey? {
                val keyToken = rule.presentToken(KmetaParser.STRING) ?: return null
                val value = rule.getRuleContext(KmetaParser.Rule_valueContext::class.java, 0) ?: return null
                val typeToken = value.presentToken(KmetaParser.STRING) ?: return null
                return TypedKey(keyToken, value, typeToken, value.presentToken(KmetaParser.LIST) != null)
            }
        }
    }

    /**
     * Returns the value of a KMeta string token.
     */
    private fun unquote(token: TerminalNode): String = StringLiteralSyntax.KMETA.unquote(token.text)
}
