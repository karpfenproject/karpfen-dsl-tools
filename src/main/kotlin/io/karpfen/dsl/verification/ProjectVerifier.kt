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

import io.karpfen.dsl.composition.FlatteningResult
import io.karpfen.dsl.composition.StateMachineFlattener
import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.source.SourceLocation
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.StateMachineContents
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ActionRule

/**
 * Checks the state machines of a project against its metamodels, its model and themselves, beyond what the readers
 * check in a single text: whether the paths of the machines exist in the types, whether their operations fit what the
 * paths lead to, whether the macros are called with values of the types they take, whether the events read are in
 * scope and carry the fields read, whether every state, join and split can be reached, whether the model holds the
 * values the machines read, whether the attachable state machines that `behaves after`, `ATTACH` and `EXCHANGE`
 * name exist, carry names of their own and may run on the objects in question, and whether every composition of
 * fragments flattens.
 *
 * An attachable state machine may run on objects of each type it lists, so its paths must be valid from each of them:
 * the checks that depend on the type run once per listed type, and a problem found for some types but not for all
 * names the type in its message, as in `For type 'Bulb': ...`. The problems of the `behaves after` clauses belong to
 * the texts of the metamodels, see [ProjectFindings.metamodel] and [ProjectFindings.events].
 *
 * Fragments are state machines of the project like the others. Each state machine that is no fragment is flattened
 * with the fragments of the project, see [StateMachineFlattener], exactly as the runtime flattens it. A problem that
 * prevents the flattening is reported in the text where it stands: an unknown fragment or one that is no fragment at
 * the name after `USE`, a cycle at the container state that closes it, a name clash at the state, a macro problem at the
 * call or at the later definition. A problem the reader of that text reports already, such as a fragment that uses
 * itself or an unknown macro in a text without container states, is not repeated. The checks of a single state machine
 * then run on the flattened state machine, so that the content of a fragment is checked where it runs, with the type of
 * the state machine that uses it; a finding inside a fragment is reported in the text of the fragment, and once per
 * kind and place, however often the fragment is used. A macro call that only another file of the composition resolves
 * is a warning at the call. A fragment that no flattened composition uses is checked on its own, flattened with the
 * fragments it uses, as far as it can be without the type of a host. A composition that does not flatten is checked
 * as the single state machine it starts from, as written.
 *
 * The checks never report what they cannot be sure of. A state machine whose text has a syntax error is not checked,
 * and a name that no attachable state machine or fragment has is reported only if every state machine of the project
 * could be read. The checks of types need a domain metamodel without errors, those of events an event metamodel without
 * errors, those of values a model without errors, those of states and events machines whose state names are unique and
 * known in every file of the composition, and those of `behaves after` metamodels without syntax errors.
 *
 * ```
 * val metamodel = KmetaReader().read(kmetaText)
 * val events = KmetaReader(metamodel.model!!.types).read(eventsText)
 * val model = KmodelReader(metamodel.model!!).read(kmodelText)
 * val robot = StateMachineInput(KstatesReader().read(kstatesText), attachedObjectId = "turtle")
 * val blinking = StateMachineInput(KstatesReader().read(blinkingText))
 * val selfTest = StateMachineInput(KstatesReader().read(selfTestText))
 * val findings = ProjectVerifier().verify(ProjectInput(metamodel, events, model, listOf(robot, blinking, selfTest)))
 * findings.of(robot).forEach { println("${it.range.start.line}: ${it.message}") }
 * ```
 */
class ProjectVerifier {

    /** The checks run once on every state machine as written, whatever type it runs on. */
    private val machineChecks: List<MachineCheck> = listOf(AttachmentCheck(), DuplicateNameCheck())

    /** The checks run on every flattened state machine once per type it runs on, in the order they report. */
    private val typeChecks: List<MachineCheck> = listOf(
        ActionOperationCheck(),
        AttachmentNameCheck(),
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

    /** Checks the state machines and the metamodels of [input] and returns the problems found in each text. */
    fun verify(input: ProjectInput): ProjectFindings {
        val types = typesOf(input)
        val objects = input.model?.takeIf { !it.hasErrors }?.model?.let { Model.collectAllObjects(it.objects).toList() }
        val writtenKeys = input.stateMachines.mapNotNull { it.read.model }.flatMap { writtenKeysOf(it) }.toSet()
        val attachables = attachablesOf(input.stateMachines)
        val eventFieldCheck = EventFieldCheck(hasEventMetamodel = input.events != null)
        val project = ProjectContext(types, objects, writtenKeys, attachables, typeChecks + eventFieldCheck)
        val findings = MachineFindings(input.stateMachines)
        val readable = findings.readable
        readable.forEach { verifyAsWritten(it, project, findings) }
        verifyCompositions(readable, project, findings)
        val behaviour = BehaviourCheck(attachables)
        val metamodelFindings = input.metamodel.takeIf { isReadable(it) }?.let { behaviour.checkDomain(it) }.orEmpty()
        val eventFindings = input.events?.takeIf { isReadable(it) }?.let { behaviour.checkEvents(it) }.orEmpty()
        return ProjectFindings(input.stateMachines, findings.all(), metamodelFindings, eventFindings)
    }

    /** Returns the class types of [input], or null if its domain metamodel has errors. */
    private fun typesOf(input: ProjectInput): ProjectTypes? {
        val domain = input.metamodel.takeIf { !it.hasErrors }?.model ?: return null
        val events = input.events
        val eventsKnown = events == null || (!events.hasErrors && events.model != null)
        return ProjectTypes(domain, events?.model?.takeIf { eventsKnown }, eventsKnown)
    }

    /** Returns the attachable state machines and fragments among [machines] whose reads can be trusted. */
    private fun attachablesOf(machines: List<StateMachineInput>): AttachableMachines {
        val readable = machines.filter { isReadable(it.read) }.mapNotNull { it.read.model }
        return AttachableMachines(readable, isComplete = readable.size == machines.size)
    }

    /** Runs the checks of the state machine of [input] as written, which do not depend on its composition. */
    private fun verifyAsWritten(input: StateMachineInput, project: ProjectContext, findings: MachineFindings) {
        val machine = input.read.model ?: return
        val report = MachineReport(input.read.sourceMap)
        val context = contextOf(input, machine, machine.types.firstOrNull() ?: "", report, graph = null, project)
        machineChecks.forEach { it.check(context) }
        findings.add(input, report.diagnostics)
    }

    /**
     * Verifies the compositions of the state machines of [readable]: first those that start from a state machine that
     * is no fragment, then those that start from a fragment that no container state of the project uses, and last each
     * fragment that none of these covered, since the compositions that use it did not flatten. The unknown macros of
     * such a used fragment are left to the compositions of the state machines that use it, see [FlatteningProblems].
     */
    private fun verifyCompositions(readable: List<StateMachineInput>, project: ProjectContext, findings: MachineFindings) {
        val pool = readable.mapNotNull { it.read.model }
        val problems = FlatteningProblems(project.attachables.isAllRead)
        val covered = mutableListOf<StateMachine>()
        val (fragments, others) = readable.partition { it.read.model?.isFragment == true }
        others.forEach { covered.addAll(verifyComposition(it, pool, project, findings, problems, withMacros = true)) }
        val usedNames = pool.flatMap { StateMachineContents(it).states }.mapNotNull { it.fragment }.toSet()
        val (used, unused) = fragments.partition { it.read.model?.name in usedNames }
        for (fragment in unused + used) {
            if (covered.none { it === fragment.read.model }) {
                val withMacros = fragment in unused
                covered.addAll(verifyComposition(fragment, pool, project, findings, problems, withMacros))
            }
        }
    }

    /**
     * Flattens the composition that starts from the state machine of [input] with the fragments of [pool], reports
     * its problems with [problems], with its unknown macros if [withMacros], or checks the flattened state machine, and
     * returns the state machines of the composition if it flattened, otherwise nothing.
     */
    private fun verifyComposition(
        input: StateMachineInput,
        pool: List<StateMachine>,
        project: ProjectContext,
        findings: MachineFindings,
        problems: FlatteningProblems,
        withMacros: Boolean
    ): List<StateMachine> {
        val machine = input.read.model ?: return emptyList()
        return when (val result = StateMachineFlattener.flatten(machine, pool)) {
            is FlatteningResult.Flattened -> {
                verifyFlattened(input, result, project, findings)
                result.sources
            }
            is FlatteningResult.NotFlattened -> {
                problems.report(result.problems, findings, withMacros)
                val report = typeReports(input, machine, input.read.sourceMap::locationOf, graphOf(listOf(input.read), machine), project)
                findings.add(input, report)
                emptyList()
            }
        }
    }

    /**
     * Checks the flattened state machine of [result], which starts from the state machine of [input], and reports
     * each finding in the file it stands in.
     */
    private fun verifyFlattened(
        input: StateMachineInput,
        result: FlatteningResult.Flattened,
        project: ProjectContext,
        findings: MachineFindings
    ) {
        val reads = result.sources.map { source -> findings.inputOf(source).read }
        val text = CompositionText(result, reads.map { it.sourceMap })
        val diagnostics = typeReports(input, result.machine, text::locationOf, graphOf(reads, result.machine), project)
        text.split(diagnostics).forEach { (source, diagnostic) -> findings.add(findings.inputOf(source), listOf(diagnostic)) }
        ForeignMacroCheck(result).check(findings)
    }

    /**
     * Runs the checks of [project] on [machine], which runs with the object of [input], once per type it runs on, finds
     * the elements with [locationOf] and returns the problems, see [mergedByType].
     */
    private fun typeReports(
        input: StateMachineInput,
        machine: StateMachine,
        locationOf: (Any) -> SourceLocation?,
        graph: MachineGraph?,
        project: ProjectContext
    ): List<Diagnostic> {
        val typeNames = machine.types.distinct().ifEmpty { listOf("") }
        val byType = typeNames.map { typeName ->
            val report = MachineReport(locationOf)
            project.typeChecks.forEach { it.check(contextOf(input, machine, typeName, report, graph, project)) }
            typeName to report.diagnostics
        }
        return mergedByType(byType).distinct()
    }

    /**
     * Returns the problems the checks found for each type of a state machine, in [byType]: a problem found for every
     * type once, as it is, and a problem found for some types only once per such type, with the type named in front of
     * its message.
     */
    private fun mergedByType(byType: List<Pair<String, List<Diagnostic>>>): List<Diagnostic> {
        if (byType.size == 1) {
            return byType.single().second
        }
        val common = byType.map { it.second.toSet() }.reduce { found, next -> found intersect next }
        val particular = byType.flatMap { (typeName, diagnostics) ->
            diagnostics.filter { it !in common }.map { it.copy(message = "For type '$typeName': ${it.message}") }
        }
        return common.toList() + particular
    }

    /** Returns what the checks of [machine], which runs with the object of [input], work on while it runs on [typeName]. */
    private fun contextOf(
        input: StateMachineInput,
        machine: StateMachine,
        typeName: String,
        report: MachineReport,
        graph: MachineGraph?,
        project: ProjectContext
    ): MachineContext {
        val contextType = project.types?.domainType(typeName)
        return MachineContext(
            machine = machine,
            report = report,
            types = project.types,
            contextType = contextType,
            attachedObjectId = input.attachedObjectId,
            objects = project.objects,
            graph = graph,
            sites = MachineSites(machine, report, graph?.eventScopes, project.types?.let { MacroTypes(it) }),
            resolver = ReadResolver(contextType),
            writtenKeys = project.writtenKeys,
            attachables = project.attachables
        )
    }

    /**
     * Returns the views of the states of [machine], or null if one of [reads], the reads of the files it is made of,
     * reports repeated or unknown state names.
     */
    private fun graphOf(reads: List<ParseResult<StateMachine>>, machine: StateMachine): MachineGraph? {
        if (reads.any { read -> read.diagnostics.any { it.code in UNSURE_STATES } }) {
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
        ActionOperationType.EVENT, ActionOperationType.EXCHANGE -> null
        else -> TypePaths.segmentsOf(rule.leftSide).last()
    }

    /**
     * What the verification of each state machine of a project shares.
     *
     * @property types The class types of the project, or null if the domain metamodel has errors.
     * @property objects Every object of the model, or null if there is no model or it has errors.
     * @property writtenKeys The keys that some action of some state machine of the project writes.
     * @property attachables The attachable state machines and fragments of the project.
     * @property typeChecks The checks run once per type a state machine runs on.
     */
    private class ProjectContext(
        val types: ProjectTypes?,
        val objects: List<DataObject>?,
        val writtenKeys: Set<String>,
        val attachables: AttachableMachines,
        val typeChecks: List<MachineCheck>
    )

    internal companion object {

        /** Returns true if [read] has no problem after which its text may lack parts. */
        fun isReadable(read: ParseResult<*>): Boolean = read.diagnostics.none { it.code in UNREADABLE }

        /** The codes after which the model of a read may lack parts of its text. */
        private val UNREADABLE = setOf(DiagnosticCode.SYNTAX_ERROR, DiagnosticCode.UNRECOGNIZED_INPUT, DiagnosticCode.READER_FAILURE)

        /** The codes after which the states of a state machine cannot be told apart or found. */
        private val UNSURE_STATES = setOf(DiagnosticCode.DUPLICATE_STATE, DiagnosticCode.UNKNOWN_STATE)
    }
}
