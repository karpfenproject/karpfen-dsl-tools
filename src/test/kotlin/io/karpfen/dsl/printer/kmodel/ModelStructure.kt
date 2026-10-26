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
package io.karpfen.dsl.printer.kmodel

import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.ClassTypePropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.instance.ObjectReference
import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import io.karpfen.dsl.instance.SimpleListPropertyObject
import io.karpfen.dsl.instance.SimplePropertyObject
import io.karpfen.dsl.meta.AssociationType

/**
 * The structure of a [DataObject] and the objects it embeds as plain values, so that two models compare
 * with `equals`; data objects themselves compare by identity.
 *
 * @property typeName The name of the class type.
 * @property id The object identifier.
 * @property properties The simple property values in order.
 * @property relations The relation values in order.
 */
data class ObjectStructure(
    val typeName: String,
    val id: String,
    val properties: List<PropertyStructure>,
    val relations: List<RelationValueStructure>
)

/**
 * The structure of a simple property value.
 *
 * @property key The property name.
 * @property isList True for a list property.
 * @property values The values in order.
 */
data class PropertyStructure(val key: String, val isList: Boolean, val values: List<Any>)

/**
 * The structure of a relation value: the embedded objects of a `has` relation, or the targets of a `knows`
 * relation as `Class:id`, with `unresolved:id` for a reference that did not resolve.
 *
 * @property key The relation name.
 * @property isList True for a list relation.
 * @property associationType Embedded or linked.
 * @property embedded The structures of the embedded objects.
 * @property linked The targets of the links.
 */
data class RelationValueStructure(
    val key: String,
    val isList: Boolean,
    val associationType: AssociationType,
    val embedded: List<ObjectStructure>,
    val linked: List<String>
)

/**
 * Returns the structures of the objects of this model.
 */
fun Model.structure(): List<ObjectStructure> = objects.map { it.structure() }

/**
 * Returns the structure of this object and the objects it embeds.
 */
fun DataObject.structure(): ObjectStructure =
    ObjectStructure(ofType.name, id, properties.map { it.structure() }, relations.map { it.structure() })

private fun SimplePropertyObject.structure(): PropertyStructure = when (this) {
    is SimpleAtomicPropertyObject -> PropertyStructure(key, false, listOf(getValue()))
    is SimpleListPropertyObject -> PropertyStructure(key, true, getValues().toList())
    else -> error("Unknown kind of property value $this")
}

private fun ClassTypePropertyObject.structure(): RelationValueStructure {
    val references = when (this) {
        is ClassTypeAtomicPropertyObject -> listOf(getValueRef())
        is ClassTypeListPropertyObject -> getValueRefs().toList()
        else -> error("Unknown kind of relation value $this")
    }
    val associationType = getPropertyType().associationType
    return if (associationType == AssociationType.EMBEDDED) {
        RelationValueStructure(key, isList(), associationType, references.mapNotNull { it.obj?.structure() }, emptyList())
    } else {
        RelationValueStructure(key, isList(), associationType, emptyList(), references.map { it.target() })
    }
}

private fun ObjectReference.target(): String = obj?.let { "${it.ofType.name}:${it.id}" } ?: "unresolved:$objectId"
