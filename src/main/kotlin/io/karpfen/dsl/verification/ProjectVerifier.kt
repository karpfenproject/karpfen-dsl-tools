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

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.StateMachineContents
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ActionRule

/**
 * Checks the state machines of a project against its metamodels, its model and themselves, beyond what the readers
 * check in a single text: whether the paths of the machines exist in the types, whether their operations fit what the
 * paths lead to, whether the macros are called with values of the types they take, whether the events read are in
 * scope and carry the fields read, whether every state, join and split can be reached, and whether the model holds the
 * values the machines read.
 *
 * The checks never report what they cannot be sure of. A state machine whose text has a syntax error is not checked.
 * The checks of types need a domain metamodel without errors, those of events an event metamodel without errors, those
 * of values a model without errors, and those of states and events a machine whose state names are unique and known.
 *
 * ```
 * val metamodel = KmetaReader().read(kmetaText)
 * val events = KmetaReader(metamodel.model!!.types).read(eventsText)
 * val model = KmodelReader(metamodel.model!!).read(kmodelText)
 * val robot = StateMachineInput(KstatesReader().read(kstatesText), attachedObjectId = "turtle")
 * val findings = ProjectVerifier().verify(ProjectInput(metamodel, events, model, listOf(robot)))
 * findings.of(robot).forEach { println("${it.range.start.line}: ${it.message}") }
 * ```
 */
class ProjectVerifier {

    /** The checks run on every state machine, in the order they report. */
    private val checks: List<MachineCheck> = listOf(
        AttachmentCheck(),
        ActionOperationCheck(),
        LiteralCheck(),
        ValueConditionCheck(),
        CodeReferenceCheck(),
        MacroSignatureCheck(),
        MacroArgumentCheck(),
        EventScopeCheck(),
        UnreachableStateCheck(),
        ShadowedTransitionCheck(),
        ParallelRegionCheck(),
        UnsetPropertyCheck()
    )

    /** Checks the state machines of [input] and returns the problems found in each. */
    fun verify(input: ProjectInput): ProjectFindings {
        val types = typesOf(input)
        val objects = input.model?.takeIf { !it.hasErrors }?.model?.let { KmodelDSLConverter.collectAllObjects(it.objects).toList() }
        val writtenKeys = input.stateMachines.mapNotNull { it.read.model }.flatMap { writtenKeysOf(it) }.toSet()
        val eventFieldCheck = EventFieldCheck(hasEventMetamodel = input.events != null)
        val findings = input.stateMachines.map { machine ->
            verifyMachine(machine, types, objects, writtenKeys, checks + eventFieldCheck)
        }
        return ProjectFindings(input.stateMachines, findings)
    }

    /** Returns the class types of [input], or null if its domain metamodel has errors. */
    private fun typesOf(input: ProjectInput): ProjectTypes? {
        val domain = input.metamodel.takeIf { !it.hasErrors }?.model ?: return null
        val events = input.events
        val eventsKnown = events == null || (!events.hasErrors && events.model != null)
        return ProjectTypes(domain, events?.model?.takeIf { eventsKnown }, eventsKnown)
    }

    /** Checks [input] with [checks] and returns its problems. */
    private fun verifyMachine(
        input: StateMachineInput,
        types: ProjectTypes?,
        objects: List<DataObject>?,
        writtenKeys: Set<String>,
        checks: List<MachineCheck>
    ): List<Diagnostic> {
        val machine = input.read.model ?: return emptyList()
        if (input.read.diagnostics.any { it.code in UNREADABLE }) {
            return emptyList()
        }
        val report = MachineReport(input.read.sourceMap)
        val graph = graphOf(input.read, machine)
        val contextType = types?.domainType(machine.attachedToClass)
        val context = MachineContext(
            machine = machine,
            report = report,
            types = types,
            contextType = contextType,
            attachedObjectId = input.attachedObjectId,
            objects = objects,
            graph = graph,
            sites = MachineSites(machine, report, graph?.eventScopes, types?.let { MacroTypes(it) }),
            resolver = ReadResolver(contextType),
            writtenKeys = writtenKeys
        )
        checks.forEach { it.check(context) }
        return report.diagnostics
    }

    /** Returns the views of the states of [machine], or null if its read reports repeated or unknown state names. */
    private fun graphOf(read: ParseResult<StateMachine>, machine: StateMachine): MachineGraph? {
        if (read.diagnostics.any { it.code in UNSURE_STATES }) {
            return null
        }
        val tree = StateTree(machine)
        return MachineGraph(tree, Reachability(machine, tree), EventScopes(machine, tree))
    }

    /** Returns the keys the action rules of [machine] write. */
    private fun writtenKeysOf(machine: StateMachine): List<String> =
        StateMachineContents(machine).actions.mapNotNull { (it.item as? ActionRule)?.let { rule -> writtenKeyOf(rule) } }

    /** Returns the key [rule] writes, or null for a rule that writes none. */
    private fun writtenKeyOf(rule: ActionRule): String? = when (rule.operationType) {
        ActionOperationType.SETOBJ, ActionOperationType.APPENDOBJ -> rule.secondSide
        ActionOperationType.EVENT -> null
        else -> TypePaths.segmentsOf(rule.leftSide).last()
    }

    private companion object {

        /** The codes after which the state machine of a read may lack parts of its text. */
        val UNREADABLE = setOf(DiagnosticCode.SYNTAX_ERROR, DiagnosticCode.UNRECOGNIZED_INPUT, DiagnosticCode.READER_FAILURE)

        /** The codes after which the states of a state machine cannot be told apart or found. */
        val UNSURE_STATES = setOf(DiagnosticCode.DUPLICATE_STATE, DiagnosticCode.UNKNOWN_STATE)
    }
}
