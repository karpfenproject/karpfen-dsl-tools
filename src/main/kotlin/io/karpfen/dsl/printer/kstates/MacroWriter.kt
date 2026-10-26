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
package io.karpfen.dsl.printer.kstates

import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.states.Macro

/**
 * Writes macro definitions: the `TAKES` directives in order, the `RETURNS` directive and the `DEFINITION`
 * block with its `EVAL` code verbatim.
 *
 * @param syntax The shared pieces of KStates syntax.
 */
internal class MacroWriter(private val syntax: KstatesSyntax) {

    /**
     * Writes [macro] as `MACRO "name" { ... }`.
     *
     * @throws IllegalArgumentException If a name or a type cannot be written as a string literal.
     */
    fun write(macro: Macro, writer: CodeWriter) {
        writer.block("MACRO ${syntax.quote(macro.name)}") {
            macro.takes.forEach {
                writer.line("TAKES(${syntax.quote(it.paramName)}, ${syntax.typeExpression(it.paramType)})")
            }
            writer.line("RETURNS(${syntax.typeExpression(macro.returns.returnType)})")
            writer.block("DEFINITION") { writer.line(syntax.eval(macro.definition.codeBlock.code, writer)) }
        }
    }
}
