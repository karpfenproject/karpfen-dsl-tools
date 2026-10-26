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
package io.karpfen.dsl.states.actions

/**
 * The operations an [ActionRule] can perform.
 *
 * Simple-property operations work on primitive scalars and lists only, object operations work on the `has`
 * and `knows` relations of the model.
 *
 * @property operandCount the number of operands a rule of this operation is written with: one for a path alone, two
 *   for a path and a value, three for a source, a relation and a target.
 */
enum class ActionOperationType(val operandCount: Int) {
    /** Overwrites a single scalar property. */
    SET(2),

    /** Appends one element to a simple list property. */
    APPEND(2),

    /** Overwrites a whole simple list property. */
    SETLIST(2),

    /** Clears a simple list property. */
    DROPLIST(1),

    /** Replaces the target of a single-object relation. */
    SETOBJ(3),

    /** Adds an object to a list relation. */
    APPENDOBJ(3),

    /** Removes an object and all relations pointing to it from the model. */
    DROPOBJ(1),

    /** Sends an event to an event domain. */
    EVENT(2)
}
