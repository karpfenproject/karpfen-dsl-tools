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
import io.karpfen.dsl.meta.SimplePropertyType

/**
 * Reports the `VALUE` conditions whose path leads nowhere from the object the state machine is attached to, and those
 * whose path leads to something the runtime never reads as true: a number, a list or a relation. A boolean property
 * and a string property, which counts as true if it holds `true`, fit.
 *
 * A path on the event in scope is left to [EventFieldCheck].
 */
internal class ValueConditionCheck : MachineCheck {

    /** Checks the `VALUE` conditions of the state machine of [context]. */
    override fun check(context: MachineContext) {
        for (site in context.sites.reads.filter { it.kind == ReadKind.VALUE_CONDITION }) {
            val read = context.resolver.resolve(site.path, site.scope) as? Read.OfContext ?: continue
            when (val resolution = read.resolution) {
                is Resolution.Failed -> context.report.report(DiagnosticCode.UNKNOWN_PATH, site.range, resolution.message)
                is Resolution.Resolved -> checkEnd(context, site, resolution.end)
                Resolution.Undecided -> Unit
            }
        }
    }

    /** Reports the condition of [site] if its path leads to [end], a value that never reads as true. */
    private fun checkEnd(context: MachineContext, site: ReadSite, end: PathEnd) {
        val fits = end is PathEnd.Property && !end.property.isList &&
            end.property.propertyType in setOf(SimplePropertyType.BOOLEAN, SimplePropertyType.STRING)
        if (!fits) {
            context.report.report(
                DiagnosticCode.OPERATION_MISMATCH,
                site.range,
                "'${site.path}' leads to ${end.asMacroType().describe()}, but a VALUE condition needs a boolean"
            )
        }
    }
}

/**
 * Reports the `$(path)` references in the code of `EVAL` bodies that lead nowhere: from the object the state machine is
 * attached to, from a binding of a `WITH` block, or in the code of a macro from one of its parameters, each segment by
 * segment. A reference that an `IF IN SCOPE` block around the code guards is left out, and so is a local variable of
 * the code of a macro. A reference on the event in scope is left to [EventFieldCheck].
 */
internal class CodeReferenceCheck : MachineCheck {

    /** Checks the references of the code of the state machine of [context]. */
    override fun check(context: MachineContext) {
        for (site in context.sites.reads.filter { it.kind == ReadKind.CODE && !it.scope.isGuarded(it.path) }) {
            val failure = failureOf(context.resolver.resolve(site.path, site.scope)) ?: continue
            context.report.report(
                DiagnosticCode.UNRESOLVED_CODE_REFERENCE,
                site.range,
                "\$(${site.path}) cannot be resolved: ${failure.message}"
            )
        }
    }
}

/**
 * Reports the arguments of `MACRO(...)` calls that lead nowhere, and those that lead to a value of another type than
 * the parameter they are passed to takes, both at the argument. An argument that an `IF IN SCOPE` block around the call
 * guards is not reported as leading nowhere; an argument on the event in scope is left to [EventFieldCheck].
 */
internal class MacroArgumentCheck : MachineCheck {

    /** Checks the arguments of the macro calls of the state machine of [context]. */
    override fun check(context: MachineContext) {
        for (site in context.sites.reads.filter { it.kind == ReadKind.MACRO_ARGUMENT }) {
            val read = context.resolver.resolve(site.path, site.scope)
            val resolution = when (read) {
                is Read.OfContext -> read.resolution
                is Read.OfBinding -> read.resolution
                else -> continue
            }
            when (resolution) {
                is Resolution.Failed -> if (!site.scope.isGuarded(site.path)) {
                    context.report.report(
                        DiagnosticCode.UNRESOLVED_MACRO_ARGUMENT,
                        site.range,
                        "The argument '${site.path}' cannot be resolved: ${resolution.message}"
                    )
                }
                is Resolution.Resolved -> checkType(context, site, resolution.end)
                Resolution.Undecided -> Unit
            }
        }
    }

    /** Reports the argument of [site] if [end], what it leads to, does not fit the type of its parameter. */
    private fun checkType(context: MachineContext, site: ReadSite, end: PathEnd) {
        val (name, expected) = site.parameter ?: return
        expected ?: return
        val actual = end.asMacroType()
        if (!expected.accepts(actual)) {
            context.report.report(
                DiagnosticCode.MACRO_ARGUMENT_TYPE,
                site.range,
                "Parameter '$name' takes ${expected.describe()}, but '${site.path}' leads to ${actual.describe()}"
            )
        }
    }
}

/**
 * Returns the failure [read] ends in if it is a path from the context, a binding or a parameter that leads nowhere,
 * otherwise null.
 */
internal fun failureOf(read: Read): Resolution.Failed? = when (read) {
    is Read.OfContext -> read.resolution as? Resolution.Failed
    is Read.OfBinding -> read.resolution as? Resolution.Failed
    is Read.OfParameter -> read.resolution as? Resolution.Failed
    is Read.OfEvent, Read.Local -> null
}
