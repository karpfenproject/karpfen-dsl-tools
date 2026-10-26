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
import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import io.karpfen.dsl.instance.SimpleListPropertyObject
import io.karpfen.dsl.instance.SimplePropertyObject
import io.karpfen.dsl.parser.KmodelSemantics
import io.karpfen.dsl.reader.ReadingSession

/**
 * Turns the checked `prop` statements of one object into its [SimplePropertyObject]s, the way the throwing
 * converter does: the statements of one key form a single value or a list, and each value is read by the type of
 * its property, see [KmodelSemantics].
 *
 * A value that is not a number for a number property is reported and left out.
 *
 * @param session The read the statements belong to.
 */
internal class PropertyAssignments(private val session: ReadingSession) {

    /**
     * Converts [statements] into property objects, one per key in the order the keys first occur.
     */
    fun assign(statements: List<PropStatement>): MutableList<SimplePropertyObject> =
        statements.groupBy { it.key }
            .mapNotNull { (key, group) -> assignKey(key, group) }
            .toMutableList()

    private fun assignKey(key: String, group: List<PropStatement>): SimplePropertyObject? {
        val property = group.first().property
        val valued = group.mapNotNull { statement -> valueOf(statement)?.let { statement to it } }
        if (valued.isEmpty()) {
            return null
        }
        val propertyObject = if (KmodelSemantics.formsList(valued.size, property.isList)) {
            SimpleListPropertyObject(property, key, valued.map { it.second }.toMutableList())
        } else {
            SimpleAtomicPropertyObject(property, key, valued.single().second)
        }
        for ((statement, _) in valued) {
            session.record(propertyObject, statement.context, statement.keyToken)
        }
        return propertyObject
    }

    /**
     * Reads the value of [statement] the way the throwing converter does, and reports a value that is not a number
     * for a number property.
     */
    private fun valueOf(statement: PropStatement): Any? = try {
        KmodelSemantics.valueOf(statement.property.propertyType, statement.value)
    } catch (notANumber: NumberFormatException) {
        session.report(
            DiagnosticCode.INVALID_NUMBER,
            session.locator.problemRangeOf(statement.valueToken),
            "'${statement.value}' is not a number, but prop '${statement.key}' is of type 'number'"
        )
        null
    }
}
