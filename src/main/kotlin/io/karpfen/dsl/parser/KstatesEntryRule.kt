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

import io.karpfen.dsl.grammar.kstates.KstatesParser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.TokenStream

/**
 * Chooses the entry rule of the KStates grammar for a text: `fragment_file` for a fragment, `kstates_file` for an
 * attached or an attachable state machine.
 *
 * A fragment starts with `STATEMACHINE "name" FRAGMENT` and has a rule of its own, so that the rule of the two other
 * forms stays as it was and recovers from errors exactly as before: a single rule for all three forms would have to
 * decide between them at the keyword after the name, and on a text that lacks the keyword ANTLR then gives up the whole
 * header instead of supplying the missing word. A text is a fragment when its first tokens, comments and whitespace
 * left out, are `STATEMACHINE`, an optional string and `FRAGMENT`. Every other text, also an empty or broken one, is
 * read with `kstates_file`.
 *
 * Every tool that parses KStates text with the generated [KstatesParser] chooses the rule this way; the reader of the
 * library does.
 */
object KstatesEntryRule {

    /**
     * Returns true if [tokens], a stream positioned at the start of a KStates text, starts with the header of a
     * fragment. The stream is only looked ahead, not consumed.
     */
    fun isFragment(tokens: TokenStream): Boolean {
        if (tokens.LT(1)?.type != KstatesParser.STATEMACHINE) {
            return false
        }
        val afterName = if (tokens.LT(2)?.type == KstatesParser.STRING) 3 else 2
        return tokens.LT(afterName)?.type == KstatesParser.FRAGMENT
    }

    /**
     * Parses the text of [parser] with the entry rule its first tokens call for and returns the tree: a
     * [KstatesParser.Fragment_fileContext] for a fragment, a [KstatesParser.Kstates_fileContext] otherwise.
     */
    fun parse(parser: KstatesParser): ParserRuleContext =
        if (isFragment(parser.inputStream)) parser.fragment_file() else parser.kstates_file()

    /**
     * Returns the state machine of [file], a tree that [parse] returned: the [KstatesParser.StatemachineContext] or
     * the [KstatesParser.Fragment_machineContext]; null for a tree of another rule or one without a state machine.
     */
    fun machineOf(file: ParserRuleContext): ParserRuleContext? = when (file) {
        is KstatesParser.Kstates_fileContext -> file.statemachine()
        is KstatesParser.Fragment_fileContext -> file.fragment_machine()
        else -> null
    }
}
