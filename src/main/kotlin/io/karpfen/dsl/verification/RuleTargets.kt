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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty

/**
 * The object an action rule works on, as far as the types tell.
 */
internal sealed interface RuleTarget {

    /** An object of the type [type]; [relation] is the relation the path ends with, or null for the context itself. */
    class Found(val type: ClassType, val relation: ClassTypeProperty?) : RuleTarget

    /** No single object, a problem of the kind [code] explained by [message]. */
    class Problem(val code: DiagnosticCode, val message: String) : RuleTarget

    /** The types cannot tell. */
    object Undecided : RuleTarget
}

/**
 * Finds the objects the action rules work on, the way the runtime finds them: a path from the object the state machine
 * is attached to that must lead to one object, not to a list and not to a property.
 */
internal object RuleTargets {

    /** The words that name the object the state machine is attached to as the source of `SETOBJ` and `APPENDOBJ`. */
    private val SELF = setOf("", "self")

    /**
     * Returns the object that owns the key [path] ends with, for `SET`, `APPEND`, `SETLIST` and `DROPLIST`: the
     * object the path leads to without its last segment. A path that goes on through a list or a property there leads
     * nowhere, as for the runtime.
     */
    fun ownerOf(contextType: ClassType, path: String): RuleTarget {
        val segments = TypePaths.segmentsOf(path)
        if (segments.size == 1) {
            return RuleTarget.Found(contextType, null)
        }
        return objectAt(contextType, segments.dropLast(1), goesOn = true)
    }

    /** Returns the object [path] leads to, the source of `SETOBJ` and `APPENDOBJ`, where `self` names the context. */
    fun sourceOf(contextType: ClassType, path: String): RuleTarget =
        if (path.trim() in SELF) RuleTarget.Found(contextType, null) else objectAt(contextType, TypePaths.segmentsOf(path))

    /** Returns the object [path] leads to from the context. */
    fun objectAt(contextType: ClassType, path: String): RuleTarget = objectAt(contextType, TypePaths.segmentsOf(path))

    /**
     * Returns the object [segments] lead to from the context; if [goesOn], the path goes on after them, so that a list or
     * a property there makes it lead nowhere.
     */
    private fun objectAt(contextType: ClassType, segments: List<String>, goesOn: Boolean = false): RuleTarget {
        val path = segments.joinToString("->")
        val endCode = if (goesOn) DiagnosticCode.UNKNOWN_PATH else DiagnosticCode.OPERATION_MISMATCH
        return when (val resolution = TypePaths.resolve(contextType, segments, Navigation.MODEL)) {
            is Resolution.Failed -> RuleTarget.Problem(DiagnosticCode.UNKNOWN_PATH, resolution.message)
            Resolution.Undecided -> RuleTarget.Undecided
            is Resolution.Resolved -> when (val end = resolution.end) {
                is PathEnd.Relation -> if (end.relation.isList) {
                    RuleTarget.Problem(endCode, "'$path' leads to a list of objects, but the rule needs one object")
                } else {
                    RuleTarget.Found(end.target, end.relation)
                }
                is PathEnd.Property -> RuleTarget.Problem(
                    endCode,
                    "'$path' leads to the property '${end.property.key}', but the rule needs an object"
                )
                else -> RuleTarget.Undecided
            }
        }
    }
}
