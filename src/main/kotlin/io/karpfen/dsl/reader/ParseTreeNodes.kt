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
package io.karpfen.dsl.reader

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Returns the child tokens of type [tokenType] that the text really contains, in text order.
 *
 * ANTLR adds the tokens it conjures up for a missing token, and the superfluous tokens it skips, as error
 * nodes to the context; the generated accessors return them like any other token, this function leaves them
 * out.
 */
internal fun ParserRuleContext.presentTokens(tokenType: Int): List<TerminalNode> =
    children.orEmpty()
        .filterIsInstance<TerminalNode>()
        .filter { it !is ErrorNode && it.symbol.type == tokenType }

/**
 * Returns the first child token of type [tokenType] that the text really contains, see [presentTokens].
 */
internal fun ParserRuleContext.presentToken(tokenType: Int): TerminalNode? = presentTokens(tokenType).firstOrNull()

/**
 * Returns true if the parser met a syntax error in this context or in a context nested in it: a failed
 * alternative, a conjured missing token or a skipped superfluous one.
 */
internal fun ParserRuleContext.containsSyntaxError(): Boolean =
    exception != null || children.orEmpty().any {
        it is ErrorNode || (it is ParserRuleContext && it.containsSyntaxError())
    }
