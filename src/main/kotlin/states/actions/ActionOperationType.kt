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
package states.actions

/**
 * Enumeration of possible action rule operation types.
 *
 * Simple-property operations (operate on primitive scalars / lists only):
 * - SET: Overwrite a single scalar property.
 * - APPEND: Append one element to a simple list property.
 * - SETLIST: Overwrite a whole simple list property.
 * - DROPLIST: Clear a simple list property (unary).
 *
 * Object/relation operations (operate on `has`/`knows` relations):
 * - SETOBJ: Replace the target of an atomic relation (ternary: source, relation, target).
 * - APPENDOBJ: Add an object to a list relation (ternary: source, relation, target).
 * - DROPOBJ: Remove an object and all relations pointing to it from the model (unary).
 *
 * - EVENT: Send an event to the event domain.
 */
enum class ActionOperationType {
    SET,
    APPEND,
    SETLIST,
    DROPLIST,
    SETOBJ,
    APPENDOBJ,
    DROPOBJ,
    EVENT
}