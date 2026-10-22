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

/**
 * The syntax-level content of a KModel file, before it is checked against a metamodel.
 *
 * @property objects The root objects; a KModel file has exactly one.
 */
data class KmodelFile(val objects: List<ModelObject>)

/**
 * A `make object "key":"Class" { ... }` block with its statements.
 *
 * @property key The object key, the identifier of the object; `_` or empty for anonymous objects.
 * @property className The class type name.
 */
data class ModelObject(
    val key: String,
    val className: String
) {

    /** The `prop` statements, in definition order. */
    val props = mutableListOf<ObjectProperty>()

    /** The `has` statements with their embedded objects, in definition order. */
    val has = mutableListOf<HasRelation>()

    /** The `knows` statements, in definition order. */
    val knows = mutableListOf<KnowsRelation>()

    /**
     * Adds a `prop` statement.
     */
    fun addProp(key: String, value: String) {
        props.add(ObjectProperty(key, value))
    }

    /**
     * Adds a `has` statement embedding [childObject].
     */
    fun addHas(key: String, childObject: ModelObject) {
        has.add(HasRelation(key, childObject))
    }

    /**
     * Adds a `knows` statement referring to the object with identifier [value].
     */
    fun addKnows(key: String, value: String) {
        knows.add(KnowsRelation(key, value))
    }

    /**
     * Renders the object header as written in KModel, `make object "key:Class"`.
     */
    override fun toString(): String {
        val keyStr = if (key.isNotEmpty()) "$key:" else ":"
        return "make object \"$keyStr$className\""
    }
}

/**
 * A `prop("key") -> "value"` statement.
 *
 * @property key The property name.
 * @property value The property value as written.
 */
data class ObjectProperty(val key: String, val value: String)

/**
 * A `has("key") -> make object ...` statement.
 *
 * @property key The relation name.
 * @property childObject The embedded object.
 */
data class HasRelation(val key: String, val childObject: ModelObject)

/**
 * A `knows("key") -> "id"` statement.
 *
 * @property key The relation name.
 * @property value The identifier of the referenced object.
 */
data class KnowsRelation(val key: String, val value: String)
