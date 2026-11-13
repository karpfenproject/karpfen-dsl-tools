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

import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.SimplePropertyType
import io.karpfen.dsl.parser.MacroTypeSyntax

/**
 * The type of a `TAKES` parameter or of the `RETURNS` of a macro, as the runtime reads it: a primitive, a class type,
 * a reference to an object of a class type, or a list of one of these.
 */
internal sealed interface MacroType {

    /** Returns true if a value of [other] fits where this type is expected. */
    fun accepts(other: MacroType): Boolean

    /** Returns the type as KStates writes it. */
    fun describe(): String

    /**
     * The primitive [type], `string`, `number` or `boolean`; a reference to a primitive reads as the primitive.
     */
    class Primitive(val type: SimplePropertyType) : MacroType {

        /** Accepts the same primitive. */
        override fun accepts(other: MacroType): Boolean = other is Primitive && other.type == type

        /** Returns the keyword of the primitive. */
        override fun describe(): String = type.keyword
    }

    /**
     * An object of the class type [type], a new one or, if [isReference], an object of the model.
     */
    class Object(val type: ClassType, val isReference: Boolean) : MacroType {

        /** Accepts an object of the same type, a new one or one of the model. */
        override fun accepts(other: MacroType): Boolean = other is Object && other.type.name == type.name

        /** Returns the type name, or the reference to it. */
        override fun describe(): String =
            if (isReference) MacroTypeSyntax.reference(type.name) else type.name
    }

    /**
     * A list of values of the type [element].
     */
    class ListOf(val element: MacroType) : MacroType {

        /** Accepts a list whose elements this list accepts. */
        override fun accepts(other: MacroType): Boolean = other is ListOf && element.accepts(other.element)

        /** Returns the list call around the element type. */
        override fun describe(): String = MacroTypeSyntax.typedCall("list", element.describe())
    }
}

/**
 * The result of reading the type of a macro: the type, or why it is not one.
 */
internal sealed interface MacroTypeReading {

    /** The type read, [type]. */
    class Known(val type: MacroType) : MacroTypeReading

    /** A type that names no primitive and no class type of the project, explained by [message]. */
    class Unknown(val message: String) : MacroTypeReading
}

/**
 * Reads the types of macros as [MacroTypeSyntax] holds them, against the class types of a project.
 *
 * A primitive is matched without regard to case, as the runtime matches it. A class type is looked up among the domain
 * types and the event types.
 *
 * @param types The class types of the project.
 */
internal class MacroTypes(private val types: ProjectTypes) {

    /** Returns the type [text] stands for. */
    fun read(text: String): MacroTypeReading {
        val typedCall = MacroTypeSyntax.typedCallOf(text) ?: return readName(text)
        val (function, name) = typedCall
        return when (function) {
            LIST_FUNCTION -> elementOf(name)?.let { MacroTypeReading.Known(MacroType.ListOf(it)) } ?: unknownName(name)
            MacroTypeSyntax.REFERENCE_FUNCTION -> elementOf(name)?.let { MacroTypeReading.Known(referenceTo(it)) }
                ?: unknownName(name)
            else -> MacroTypeReading.Unknown(
                "'$function' is no type function; write list(\"...\") or reference(\"...\")"
            )
        }
    }

    /** Returns the type [text] stands for, or null if it is unknown. */
    fun typeOf(text: String): MacroType? = (read(text) as? MacroTypeReading.Known)?.type

    /** Reads [name], a primitive or the name of a class type. */
    private fun readName(name: String): MacroTypeReading =
        elementOf(name)?.let { MacroTypeReading.Known(it) } ?: unknownName(name)

    /** Returns the primitive or the class type [name] stands for, or null. */
    private fun elementOf(name: String): MacroType? {
        SimplePropertyType.entries.firstOrNull { it.keyword.equals(name, ignoreCase = true) }?.let {
            return MacroType.Primitive(it)
        }
        return types.anyType(name)?.let { MacroType.Object(it, isReference = false) }
    }

    /** Returns the type of a reference to [element]; a reference to a primitive stays the primitive. */
    private fun referenceTo(element: MacroType): MacroType =
        if (element is MacroType.Object) MacroType.Object(element.type, isReference = true) else element

    /** Explains that [name] is no type. */
    private fun unknownName(name: String): MacroTypeReading.Unknown = MacroTypeReading.Unknown(
        "'$name' is neither string, number nor boolean, nor a type of the metamodel or the events"
    )

    private companion object {

        /** The function of a list type. */
        const val LIST_FUNCTION = "list"
    }
}
