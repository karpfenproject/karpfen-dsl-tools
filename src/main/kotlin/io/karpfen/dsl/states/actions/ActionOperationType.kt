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
 */
enum class ActionOperationType {
    /** Overwrites a single scalar property. */
    SET,

    /** Appends one element to a simple list property. */
    APPEND,

    /** Overwrites a whole simple list property. */
    SETLIST,

    /** Clears a simple list property (unary). */
    DROPLIST,

    /** Replaces the target of a single-object relation (ternary: source, relation, target). */
    SETOBJ,

    /** Adds an object to a list relation (ternary: source, relation, target). */
    APPENDOBJ,

    /** Removes an object and all relations pointing to it from the model (unary). */
    DROPOBJ,

    /** Sends an event to an event domain. */
    EVENT
}
