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

import io.karpfen.dsl.parser.Arity
import io.karpfen.dsl.states.actions.ActionOperationType

/**
 * The arity rules of KStates action rules, the same the throwing converter checks.
 */
internal object ActionArity {

    /**
     * Returns the arity a rule of [operation] must be written with: `DROPLIST` and `DROPOBJ` take a path,
     * `SETOBJ` and `APPENDOBJ` a source, a relation and a target, every other operation a path and a value.
     */
    fun expectedFor(operation: ActionOperationType): Arity = when (operation) {
        ActionOperationType.DROPLIST, ActionOperationType.DROPOBJ -> Arity.UNARY
        ActionOperationType.SETOBJ, ActionOperationType.APPENDOBJ -> Arity.TERNARY
        else -> Arity.BINARY
    }

    /**
     * Returns the arity a rule is written with: without a right side it is unary, with one string before the
     * right side binary, with more ternary.
     */
    fun writtenWith(stringCount: Int, hasRightSide: Boolean): Arity = when {
        !hasRightSide -> Arity.UNARY
        stringCount == 1 -> Arity.BINARY
        else -> Arity.TERNARY
    }
}
