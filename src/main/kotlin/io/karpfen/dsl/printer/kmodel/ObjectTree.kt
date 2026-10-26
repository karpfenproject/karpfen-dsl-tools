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
 * reference without an object is skipped, and a link falls back to the identifier it names. It rejects the trees
 * KModel cannot write: a KModel document writes each embedded object where it is embedded and refers to a linked
 * object by its id.
 */
internal class ObjectTree {

    /**
     * Returns the object a KModel document of [model] starts from.
     *
     * A KModel document holds exactly one root object. That is the only object of [Model.objects] that no
     * other one of them embeds. Models made with a builder also hold the default objects the builder made for the
     * single-valued relations of every object, which have no id and stay in the model when they are replaced. Among
     * several objects that no other one embeds, the root is therefore the only one whose class type is the root class
     * type of the metamodel, and among several of those the only one with an id.
     *
     * @throws IllegalArgumentException If the model has no such object or several.
     */
    fun rootOf(model: Model): DataObject {
        val embedded = model.objects.flatMapTo(HashSet()) { embeddedObjectsOf(it) }
        val topLevel = model.objects.filter { it !in embedded }
        topLevel.singleOrNull()?.let { return it }
        val rootClassName = model.metamodel?.rootClass?.name
        val ofRootType = topLevel.filter { it.ofType.name == rootClassName }
        return ofRootType.singleOrNull()
            ?: ofRootType.singleOrNull { it.id.isNotEmpty() }
            ?: throw IllegalArgumentException(
                "A KModel document holds exactly one root object, but the model has ${topLevel.size} objects " +
                    "that no other object embeds, and not exactly one of them has the root class type " +
                    "$rootClassName and an id."
            )
    }

    /**
     * Returns [root] and every object it embeds, directly or through others: the objects that a KModel document, for
     * [isDocument], or a fragment that starts from [root] writes.
     *
     * @throws IllegalArgumentException If an object embeds itself, or an object is embedded more than once, under
     * two relations or twice in one list, which the text would turn into several objects.
     */
    fun objectsWrittenFrom(root: DataObject, isDocument: Boolean): WrittenObjects {
        val written = LinkedHashSet<DataObject>()
        collectWritten(root, HashSet(), written)
        return WrittenObjects(written, isDocument)
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
     * Returns the identifiers of the objects the linking [relation] refers to in the text that writes [written],
     * skipping empty references, see [WrittenObjects].
     *
     * An object without an id cannot be referred to. A link to such an object among [written] is rejected; a link to
     * one outside the text is taken as empty, since it is the default object that a
     * [io.karpfen.dsl.dsl.functional.ModelBuilder] puts into a link nobody set.
     *
     * @throws IllegalArgumentException If [relation] links to an object of [written] that has no id or whose id a later
     * object of [written] has too, or, in a whole document, to an object with an id that the document does not write.
     */
    fun linkedIdsOf(relation: ClassTypePropertyObject, written: WrittenObjects): List<String> =
        referencesOf(relation).mapNotNull { reference ->
            val target = reference.obj
            when {
                target == null -> reference.objectId.takeIf { it.isNotEmpty() }
                target in written -> idOfWritten(relation, target, written)
                target.id.isEmpty() -> null
                else -> idOfUnwritten(relation, target, written)
            }
        }

    /**
     * Returns the id of [target], an object of [written] that [relation] links to.
     *
     * @throws IllegalArgumentException If [target] has no id, or a later object of [written] has its id, so that the
     * link would read back as a link to that object.
     */
    private fun idOfWritten(relation: ClassTypePropertyObject, target: DataObject, written: WrittenObjects): String {
        require(target.id.isNotEmpty()) {
            "The relation '${relation.key}' links to an object of class type ${target.ofType.name} that has no id, " +
                "but KModel refers to a linked object by its id."
        }
        require(written.isReferableById(target)) {
            "The relation '${relation.key}' links to an object '${target.id}' of class type ${target.ofType.name}, " +
                "but a later object has the same id, and KModel refers to the last object of an id."
        }
        return target.id
    }

    /**
     * Returns the id of [target], an object with an id that [relation] links to and the text of [written] does not
     * write, which only a fragment may refer to.
     *
     * @throws IllegalArgumentException If [written] is a whole document: no object of it embeds [target], so the
     * link would refer to no object.
     */
    private fun idOfUnwritten(relation: ClassTypePropertyObject, target: DataObject, written: WrittenObjects): String {
        require(!written.isDocument) {
            "The relation '${relation.key}' links to the object '${target.id}' of class type ${target.ofType.name}, " +
                "which no object of the document embeds, so the document does not hold it."
        }
        return target.id
    }

    /**
     * Adds [obj] and the objects it embeds to [written], checking that none of them embeds itself or is already
     * written.
     *
     * @param enclosing The objects that embed [obj], which holds them again when the call returns.
     */
    private fun collectWritten(obj: DataObject, enclosing: MutableSet<DataObject>, written: MutableSet<DataObject>) {
        require(obj !in enclosing) { "The object '${obj.id}' of class type ${obj.ofType.name} embeds itself." }
        require(written.add(obj)) {
            "The object '${obj.id}' of class type ${obj.ofType.name} is embedded more than once, but KModel writes an " +
                "embedded object where it is embedded, so the text would read back as several objects."
        }
        enclosing.add(obj)
        embeddedObjectsOf(obj).forEach { collectWritten(it, enclosing, written) }
        enclosing.remove(obj)
    }

    /**
     * Returns true if this relation embeds its objects.
     */
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
