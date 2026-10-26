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

import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.meta.SimplePropertyType

/**
 * How the statements of a KModel text become the values of a model, which the converter and the reader share: when
 * the statements of one key form a list, how a primitive value is read, and which objects a `knows` statement can
 * refer to by id.
 */
internal object KmodelSemantics {

    /**
     * Returns true if the [statementCount] `prop`, `has` or `knows` statements of one key form a list, false if they
     * form a single value: they form a list if there are several of them or the property or relation is declared as
     * a list, [declaredAsList].
     */
    fun formsList(statementCount: Int, declaredAsList: Boolean): Boolean = statementCount != 1 || declaredAsList

    /**
     * Returns the value of a property of the primitive [type] that a `prop` statement writes as [text]: the text
     * itself for a string, a [Double] for a number and a [Boolean] for a boolean, which is true only for the text
     * `true` in any case.
     *
     * @throws NumberFormatException If [type] is a number and [text] is not one.
     */
    fun valueOf(type: SimplePropertyType, text: String): Any = when (type) {
        SimplePropertyType.STRING -> text
        SimplePropertyType.NUMBER -> text.toDouble()
        SimplePropertyType.BOOLEAN -> text.toBoolean()
    }

    /**
     * Returns the objects among [objects] that a `knows` statement can refer to, by their ids: objects with an empty
     * id cannot be referred to, and of several objects with the same id the last one wins.
     */
    fun referableById(objects: Collection<DataObject>): Map<String, DataObject> =
        objects.filter { it.id.isNotEmpty() }.associateBy { it.id }
}
