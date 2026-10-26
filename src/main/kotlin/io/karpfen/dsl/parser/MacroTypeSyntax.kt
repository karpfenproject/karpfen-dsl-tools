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
 * How a KStates macro holds the type of a `TAKES` or `RETURNS` directive as a string, which the parse visitor and
 * the reader build and the printer takes apart again.
 *
 * A type written as a string literal, `"number"`, is held as its value, `number`. A type written as a typed call,
 * `list("Obstacle")`, or as a reference, `reference("Obstacle")`, is held as the call with the value of its type
 * name between double quotes.
 */
internal object MacroTypeSyntax {

    /** The function of a reference type, which the grammar spells as a keyword of its own. */
    const val REFERENCE_FUNCTION = "reference"

    /**
     * A typed call as [typedCall] builds it: an identifier, `("`, the type name and `")`. The type name is taken as it
     * stands between them, whatever chars it holds, line breaks and backslashes included.
     */
    private val TYPED_CALL = Regex("""([A-Za-z_][A-Za-z0-9_]*)\("([\s\S]*)"\)""")

    /**
     * Returns the type of a typed call of [function] on the type [typeName].
     */
    fun typedCall(function: String, typeName: String): String = "$function(\"$typeName\")"

    /**
     * Returns the type of a reference to [typeName].
     */
    fun reference(typeName: String): String = typedCall(REFERENCE_FUNCTION, typeName)

    /**
     * Returns the function and the type name of [type] if it is held as a typed call, or null otherwise.
     */
    fun typedCallOf(type: String): Pair<String, String>? =
        TYPED_CALL.matchEntire(type)?.destructured?.let { (function, typeName) -> function to typeName }
}
