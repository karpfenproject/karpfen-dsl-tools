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
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.repetitionsBy

/**
 * Reports the objects of a KModel text whose id an object earlier in the text has.
 *
 * Every object after the first one of an id is reported at its id. Objects with an empty id are left out, since no
 * `knows` can refer to them, see [io.karpfen.dsl.parser.KmodelSemantics.referableById]. The check only reports and
 * leaves the model as it is.
 *
 * @param session The read the objects belong to; it holds their locations and receives the problems.
 */
internal class DuplicateIdCheck(private val session: ReadingSession) {

    /**
     * Checks [objects], every object the text makes, in any order.
     */
    fun check(objects: Collection<DataObject>) {
        val inTextOrder = objects.filter { it.id.isNotEmpty() }.sortedBy { offsetOf(it) }
        for (repetition in inTextOrder.repetitionsBy { it.id }) {
            session.report(
                DiagnosticCode.DUPLICATE_OBJECT_ID,
                session.problemRangeOf(repetition.element),
                "The id '${repetition.element.id}' is already the id of the object in line " +
                    "${session.locationOf(repetition.first)?.range?.start?.line ?: 0}; every object needs an id of its own"
            )
        }
    }

    /** Returns the offset at which [dataObject] is written, or the largest offset if it has no location. */
    private fun offsetOf(dataObject: DataObject): Int = session.locationOf(dataObject)?.range?.start?.offset ?: Int.MAX_VALUE
}
