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
import io.karpfen.dsl.meta.SimplePropertyType
import io.karpfen.dsl.reader.ReadingSession

/**
 * Turns the checked `prop` statements of one object into its [SimplePropertyObject]s, the way the throwing
 * converter does: the statements of one key form a single value, or a list if there are several of them or
 * the property is a list.
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
        val propertyObject = if (valued.size == 1 && !property.isList) {
            SimpleAtomicPropertyObject(property, key, valued.single().second)
        } else {
            SimpleListPropertyObject(property, key, valued.map { it.second }.toMutableList())
        }
        for ((statement, _) in valued) {
            session.record(propertyObject, statement.context, statement.keyToken)
        }
        return propertyObject
    }

    private fun valueOf(statement: PropStatement): Any? = when (statement.property.propertyType) {
        SimplePropertyType.STRING -> statement.value
        SimplePropertyType.NUMBER -> numberOf(statement)
        SimplePropertyType.BOOLEAN -> statement.value.toBoolean()
    }

    /**
     * Parses the value of [statement] the way the throwing converter does, with [String.toDouble], and reports
     * a value it cannot parse.
     */
    private fun numberOf(statement: PropStatement): Double? = try {
        statement.value.toDouble()
    } catch (notANumber: NumberFormatException) {
        session.report(
            DiagnosticCode.INVALID_NUMBER,
            session.locator.problemRangeOf(statement.valueToken),
            "'${statement.value}' is not a number, but prop '${statement.key}' is of type 'number'"
        )
        null
    }
}
