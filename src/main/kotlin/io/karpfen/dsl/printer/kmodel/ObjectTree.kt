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
import io.karpfen.dsl.meta.AssociationType

/**
 * Reads the containment tree of a [Model]: which objects an object embeds, which objects it links to, and
 * which object a KModel document starts from.
 *
 * Unlike the accessors of the relation values, it tolerates references that are not resolved: an embedded
 * reference without an object is skipped, and a link falls back to the identifier it names.
 */
internal class ObjectTree {

    /**
     * Returns the object a KModel document of [model] starts from.
     *
     * A KModel document holds exactly one root object. That is the only object of [Model.objects] that no
     * other one of them embeds; models made with a builder also hold the default objects the builder
     * replaced, so among several such objects it is the only one whose class type is the root class type of
     * the metamodel.
     *
     * @throws IllegalArgumentException If the model has no such object or several.
     */
    fun rootOf(model: Model): DataObject {
        val embedded = model.objects.flatMapTo(HashSet()) { embeddedObjectsOf(it) }
        val topLevel = model.objects.filter { it !in embedded }
        topLevel.singleOrNull()?.let { return it }
        val rootClassName = model.metamodel?.rootClass?.name
        return topLevel.singleOrNull { it.ofType.name == rootClassName }
            ?: throw IllegalArgumentException(
                "A KModel document holds exactly one root object, but the model has ${topLevel.size} objects " +
                    "that no other object embeds, and not exactly one of them has the root class type $rootClassName."
            )
    }

    /**
     * Returns the objects [owner] embeds through all its `has` relations, in relation order.
     */
    fun embeddedObjectsOf(owner: DataObject): List<DataObject> =
        owner.relations.filter { it.isEmbedding() }.flatMap { embeddedObjectsOf(it) }

    /**
     * Returns the objects the embedding [relation] holds, skipping references without an object.
     */
    fun embeddedObjectsOf(relation: ClassTypePropertyObject): List<DataObject> =
        referencesOf(relation).mapNotNull { it.obj }

    /**
     * Returns the identifiers of the objects the linking [relation] refers to, skipping empty references.
     */
    fun linkedIdsOf(relation: ClassTypePropertyObject): List<String> =
        referencesOf(relation).map { it.obj?.id ?: it.objectId }.filter { it.isNotEmpty() }

    private fun ClassTypePropertyObject.isEmbedding(): Boolean =
        getPropertyType().associationType == AssociationType.EMBEDDED

    /**
     * Returns the references of [relation], resolved or not.
     *
     * @throws IllegalArgumentException If [relation] is neither a single-object nor a list relation.
     */
    private fun referencesOf(relation: ClassTypePropertyObject): List<ObjectReference> = when (relation) {
        is ClassTypeAtomicPropertyObject -> listOf(relation.getValueRef())
        is ClassTypeListPropertyObject -> relation.getValueRefs()
        else -> throw IllegalArgumentException("Unsupported kind of relation value: ${relation::class.simpleName}")
    }
}
