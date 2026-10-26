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
package io.karpfen.dsl.reader.kstates

import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.containsSyntaxError
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.macros.CodeBlock
import io.karpfen.dsl.states.macros.Definition
import io.karpfen.dsl.states.macros.ReturnsDirective
import io.karpfen.dsl.states.macros.TakesDirective

/**
 * Converts the macro definitions of a KStates parse tree into [Macro]s and records where each macro, directive,
 * definition and code block is written.
 *
 * A macro without a name is left out, as is a takes or returns directive that contains a syntax error; a macro
 * without a readable returns directive or definition gets an empty one, which has no location.
 *
 * @param session The read the conversion belongs to.
 * @param evalBodies Reads the code of the definitions.
 */
internal class MacroConverter(
    private val session: ReadingSession,
    private val evalBodies: EvalBodies
) {

    /**
     * Converts [definitions] in text order.
     */
    fun convert(definitions: List<KstatesParser.Macro_definitionContext>): MutableList<Macro> =
        definitions.mapNotNull { convertMacro(it) }.toMutableList()

    private fun convertMacro(definition: KstatesParser.Macro_definitionContext): Macro? {
        val nameToken = definition.presentToken(KstatesParser.STRING) ?: return null
        val macro = Macro(
            name = unquoted(nameToken),
            takes = definition.takes_directive().mapNotNull { convertTakes(it) },
            returns = definition.returns_directive()?.let { convertReturns(it) } ?: ReturnsDirective(""),
            definition = definition.definition_block()?.let { convertDefinition(it) } ?: Definition(CodeBlock(""))
        )
        session.record(macro, definition, nameToken)
        return macro
    }

    private fun convertTakes(directive: KstatesParser.Takes_directiveContext): TakesDirective? {
        if (directive.containsSyntaxError()) {
            return null
        }
        val nameToken = directive.presentToken(KstatesParser.STRING) ?: return null
        val type = directive.type_expression()?.let { typeOf(it) } ?: return null
        return TakesDirective(unquoted(nameToken), type).also { session.record(it, directive, nameToken) }
    }

    private fun convertReturns(directive: KstatesParser.Returns_directiveContext): ReturnsDirective? {
        if (directive.containsSyntaxError()) {
            return null
        }
        val expression = directive.return_expression() ?: return null
        val reference = expression.reference_expression()
        val type = if (reference != null) {
            reference.presentToken(KstatesParser.STRING)?.let { "reference(\"${unquoted(it)}\")" }
        } else {
            expression.type_expression()?.let { typeOf(it) }
        }
        return type?.let { ReturnsDirective(it) }?.also { session.record(it, directive) }
    }

    /**
     * Writes a type expression the way the throwing converter does: `"T"` as `T`, `list("T")` as `list("T")`.
     */
    private fun typeOf(expression: KstatesParser.Type_expressionContext): String? {
        expression.presentToken(KstatesParser.STRING)?.let { return unquoted(it) }
        val call = expression.typed_call_expression() ?: return null
        val function = call.presentToken(KstatesParser.IDENT) ?: return null
        val typeName = call.presentToken(KstatesParser.STRING) ?: return null
        return "${function.text}(\"${unquoted(typeName)}\")"
    }

    private fun convertDefinition(block: KstatesParser.Definition_blockContext): Definition? {
        val statement = block.eval_statement() ?: return null
        val body = evalBodies.read(statement)
        val codeBlock = CodeBlock(body.code)
        session.record(codeBlock, statement, codeRange = body.codeRange)
        return Definition(codeBlock).also { session.record(it, block) }
    }
}
