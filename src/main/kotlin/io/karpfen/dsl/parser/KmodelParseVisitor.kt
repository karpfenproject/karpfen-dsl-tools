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

import io.karpfen.dsl.grammar.kmodel.KmodelBaseVisitor
import io.karpfen.dsl.grammar.kmodel.KmodelParser

/**
 * Turns a KModel parse tree into a [KmodelFile] holding the root object with its nested objects.
 */
class KmodelParseVisitor : KmodelBaseVisitor<KmodelFile>() {

    /**
     * Reads the root `make object` block of the file.
     */
    override fun visitKmodel_file(ctx: KmodelParser.Kmodel_fileContext): KmodelFile {
        val rootObject = visitMakeObjectBlock(ctx.make_object_block())
        return KmodelFile(listOf(rootObject))
    }

    /** Reads a `make object` block with its statements and the objects it embeds. */
    private fun visitMakeObjectBlock(ctx: KmodelParser.Make_object_blockContext): ModelObject {
        val signature = ctx.object_signature()
        val keyAndClassName = parseObjectSignature(signature)
        val key = keyAndClassName.first
        val className = keyAndClassName.second

        val modelObject = ModelObject(key, className)

        val statements = ctx.statement()
        if (statements != null) {
            for (stmt in statements) {
                when {
                    stmt.prop_statement() != null -> {
                        val propStmt = stmt.prop_statement()
                        val propKey = extractStringFromQuotes(propStmt.STRING(0).text)
                        val propValue = extractStringFromQuotes(propStmt.STRING(1).text)
                        modelObject.addProp(propKey, propValue)
                    }
                    stmt.has_statement() != null -> {
                        val hasStmt = stmt.has_statement()
                        val hasKey = extractStringFromQuotes(hasStmt.STRING().text)
                        val childObject = visitMakeObjectBlock(hasStmt.make_object_block())
                        modelObject.addHas(hasKey, childObject)
                    }
                    stmt.knows_statement() != null -> {
                        val knowsStmt = stmt.knows_statement()
                        val knowsKey = extractStringFromQuotes(knowsStmt.STRING(0).text)
                        val knowsValue = extractStringFromQuotes(knowsStmt.STRING(1).text)
                        modelObject.addKnows(knowsKey, knowsValue)
                    }
                }
            }
        }

        return modelObject
    }

    /**
     * Reads the key and the class name of an object signature `"key":"Class"` from its first and last child.
     */
    private fun parseObjectSignature(ctx: KmodelParser.Object_signatureContext): Pair<String, String> {
        requireLeadingString(ctx)

        val key = StringLiteralSyntax.KMODEL_SIGNATURE.unquote(ctx.children.first().text)
        val className = StringLiteralSyntax.KMODEL_SIGNATURE.unquote(ctx.children.last().text)

        return Pair(key, className)
    }

    /**
     * Fails with a [NullPointerException] when an object signature lacks its leading string token, which
     * only happens for input the parser had to recover from.
     */
    private fun requireLeadingString(ctx: KmodelParser.Object_signatureContext) {
        extractStringFromQuotes(ctx.STRING(0).text)
    }

    /** Returns the value of a string token of a statement. */
    private fun extractStringFromQuotes(text: String): String = StringLiteralSyntax.KMODEL.unquote(text)
}
