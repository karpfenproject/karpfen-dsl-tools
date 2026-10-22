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

import io.karpfen.dsl.grammar.kmeta.KmetaBaseVisitor
import io.karpfen.dsl.grammar.kmeta.KmetaParser

/**
 * Turns a KMeta parse tree into a [KmetaFile].
 *
 * The visitor collects the type definitions it has visited; use a new visitor for every parse tree.
 */
class KmetaParseVisitor : KmetaBaseVisitor<KmetaFile>() {
    private val typeDefinitions = mutableListOf<TypeDefinition>()

    /**
     * Reads every type definition of the file, in definition order.
     */
    override fun visitKmeta_file(ctx: KmetaParser.Kmeta_fileContext): KmetaFile {
        for (typeDefCtx in ctx.type_definition()) {
            typeDefinitions.add(visitTypeDefinition(typeDefCtx))
        }
        return KmetaFile(typeDefinitions)
    }

    private fun visitTypeDefinition(ctx: KmetaParser.Type_definitionContext): TypeDefinition {
        val typeName = ctx.STRING(0).text.trim('"')
        val typeDescription = ctx.STRING(1).text.trim('"')
        val typeDef = TypeDefinition(typeName, typeDescription)

        val ruleListCtx = ctx.rule_list()
        if (ruleListCtx != null) {
            for (ruleCtx in ruleListCtx.rule_()) {
                when {
                    ruleCtx.prop_rule() != null -> {
                        val propCtx = ruleCtx.prop_rule()
                        val key = propCtx.STRING().text.trim('"')
                        val value = parseRuleValue(propCtx.rule_value())
                        typeDef.addProp(key, value)
                    }
                    ruleCtx.has_rule() != null -> {
                        val hasCtx = ruleCtx.has_rule()
                        val key = hasCtx.STRING().text.trim('"')
                        val value = parseRuleValue(hasCtx.rule_value())
                        typeDef.addHas(key, value)
                    }
                    ruleCtx.knows_rule() != null -> {
                        val knowsCtx = ruleCtx.knows_rule()
                        val key = knowsCtx.STRING().text.trim('"')
                        val value = parseRuleValue(knowsCtx.rule_value())
                        typeDef.addKnows(key, value)
                    }
                }
            }
        }

        return typeDef
    }

    /**
     * Reads a rule value: `list("T")` becomes a [ListValue], a plain `"T"` a [StringValue].
     */
    private fun parseRuleValue(ctx: KmetaParser.Rule_valueContext): RuleValue {
        return when {
            ctx.LIST() != null -> {
                val listValue = ctx.STRING().text.trim('"')
                ListValue(listValue)
            }
            ctx.STRING() != null -> {
                val stringValue = ctx.STRING().text.trim('"')
                StringValue(stringValue)
            }
            else -> StringValue("")
        }
    }
}
