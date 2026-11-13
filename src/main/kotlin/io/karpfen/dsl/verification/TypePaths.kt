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
package io.karpfen.dsl.verification

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.SimpleProperty
import io.karpfen.dsl.meta.SimplePropertyType

/**
 * What a path leads to, as far as the types tell.
 */
internal sealed interface PathEnd {

    /** Returns the type of the value the path leads to, as a macro type. */
    fun asMacroType(): MacroType

    /** The simple property [property]. */
    class Property(val property: SimpleProperty) : PathEnd {

        /** Returns the primitive of the property, or a list of it. */
        override fun asMacroType(): MacroType {
            val primitive = MacroType.Primitive(property.propertyType)
            return if (property.isList) MacroType.ListOf(primitive) else primitive
        }
    }

    /** The relation [relation] to objects of the type [target]. */
    class Relation(val relation: ClassTypeProperty, val target: ClassType) : PathEnd {

        /** Returns the target type, referenced for a `knows` relation, or a list of it. */
        override fun asMacroType(): MacroType {
            val single = MacroType.Object(target, isReference = relation.associationType == AssociationType.LINK)
            return if (relation.isList) MacroType.ListOf(single) else single
        }
    }

    /** A binding of a `WITH` block or a parameter of a macro itself, of the type [type]. */
    class Value(val type: MacroType) : PathEnd {

        /** Returns the type of the binding or the parameter. */
        override fun asMacroType(): MacroType = type
    }

    /** The id or the type name that the runtime adds to an object a macro receives. */
    object ObjectMeta : PathEnd {

        /** Returns `string`, the type of an id and of a type name. */
        override fun asMacroType(): MacroType = MacroType.Primitive(SimplePropertyType.STRING)
    }
}

/**
 * The outcome of following a path through the types.
 */
internal sealed interface Resolution {

    /** The path leads to [end]. */
    class Resolved(val end: PathEnd) : Resolution

    /** The path leads nowhere at the segment [segment], explained by [message]. */
    class Failed(val segment: Int, val message: String) : Resolution

    /** The types cannot tell, such as for a relation to a type that is not defined. */
    object Undecided : Resolution
}

/**
 * How the runtime follows a path.
 */
internal enum class Navigation {

    /** Through the objects of the model or of a payload: a path goes on through the first object of a list relation. */
    MODEL,

    /** Through the Python dicts a macro receives: a path cannot go on through a list, but may end at `__id__` or `__type__`. */
    DICT
}

/**
 * Follows paths, segments joined by `->`, through the class types of a project, segment by segment, the way the runtime
 * follows them through the values: each segment names a relation or a property of the type reached so far, a path
 * cannot go on through a property, and a relation to an undefined type leaves the outcome open.
 */
internal object TypePaths {

    /** The keys the runtime adds to every object a macro receives. */
    private val OBJECT_META_KEYS = setOf("__id__", "__type__")

    /** Returns the segments of [path], each without the whitespace around it, as the runtime splits it. */
    fun segmentsOf(path: String): List<String> = path.split("->").map { it.trim() }

    /**
     * Follows [segments] from an object of [type], numbering the segments from [offset] on in a failure.
     */
    fun resolve(type: ClassType, segments: List<String>, navigation: Navigation, offset: Int = 0): Resolution {
        var current = type
        for ((index, segment) in segments.withIndex()) {
            val isLast = index == segments.lastIndex
            val relation = current.objectProperties.firstOrNull { it.key == segment }
            if (relation != null) {
                val target = relation.reference.classType ?: return Resolution.Undecided
                if (isLast) {
                    return Resolution.Resolved(PathEnd.Relation(relation, target))
                }
                if (relation.isList && navigation == Navigation.DICT) {
                    return Resolution.Failed(
                        offset + index,
                        "'$segment' of type '${current.name}' is a list of objects, so the path cannot go on through it"
                    )
                }
                current = target
                continue
            }
            val property = current.simpleProperties.firstOrNull { it.key == segment }
            if (property != null) {
                if (isLast) {
                    return Resolution.Resolved(PathEnd.Property(property))
                }
                return Resolution.Failed(
                    offset + index,
                    "'$segment' of type '${current.name}' is a property, so the path cannot go on through it"
                )
            }
            if (isLast && navigation == Navigation.DICT && segment in OBJECT_META_KEYS) {
                return Resolution.Resolved(PathEnd.ObjectMeta)
            }
            return Resolution.Failed(offset + index, "Type '${current.name}' has no property or relation '$segment'")
        }
        return Resolution.Undecided
    }

    /**
     * Follows [rest], the segments after the root, from a value of [type] that the root named [root] stands for.
     */
    fun resolveFrom(root: String, type: MacroType, rest: List<String>, navigation: Navigation): Resolution = when {
        rest.isEmpty() -> Resolution.Resolved(PathEnd.Value(type))
        type is MacroType.Object -> resolve(type.type, rest, navigation, offset = 1)
        type is MacroType.ListOf -> Resolution.Failed(0, "'$root' is a list, so the path cannot go on through it")
        else -> Resolution.Failed(0, "'$root' is a ${type.describe()}, so the path cannot go on through it")
    }
}
