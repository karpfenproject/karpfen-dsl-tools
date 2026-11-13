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
package io.karpfen.dsl.reader.kmodel

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.reader.ReadingSession

/**
 * Reports the statements of one object that give a further value to a property or relation that is not declared as a
 * list.
 *
 * Several statements of one key form a list, see [io.karpfen.dsl.parser.KmodelSemantics.formsList], also for a
 * property or relation that holds a single value; every statement after the first one of such a key is reported at
 * its key. The check only reports and leaves the values as they are.
 *
 * @param session The read the statements belong to; it receives the problems.
 */
internal class MultiplicityCheck(private val session: ReadingSession) {

    /**
     * Checks [statements], the matched `prop`, `has` and `knows` statements of the object [objectKey], unless they may
     * belong to other objects, [belongToObject] false, see [StatementOwnership].
     */
    fun check(statements: List<KeyedStatement>, objectKey: String, belongToObject: Boolean) {
        if (!belongToObject) {
            return
        }
        val seen = mutableSetOf<String>()
        for (statement in statements.sortedBy { it.keyToken.symbol.tokenIndex }) {
            if (!seen.add(statement.key) && !statement.declaredAsList) {
                session.report(
                    DiagnosticCode.SEVERAL_VALUES,
                    session.valueProblemRangeOf(statement.keyToken),
                    "'${statement.key}' of object '$objectKey' holds a single value, but is given several; " +
                        "declare it as a list in the metamodel or keep one value"
                )
            }
        }
    }
}
