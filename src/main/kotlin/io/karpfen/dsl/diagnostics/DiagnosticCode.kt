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
package io.karpfen.dsl.diagnostics

/**
 * The kinds of [Diagnostic]s the readers and the verification of a project report.
 *
 * Both the constant names and the [id]s are stable, so that tools can rely on them, for example to offer a
 * fix for a kind of problem. The readers report the kinds prefixed with a language, `syntax` or `reader`; the
 * verification of the files of a project together, [io.karpfen.dsl.verification.ProjectVerifier], reports the
 * kinds prefixed with `project`.
 *
 * @property id A stable identifier of the kind, prefixed with the language it belongs to, with `syntax`
 * and `reader` for kinds every language shares, or with `project` for the kinds that need several files.
 * @property severity The severity of diagnostics of this kind.
 */
enum class DiagnosticCode(val id: String, val severity: Severity) {

    /** The lexer met characters that start no token of the language and skipped them. */
    UNRECOGNIZED_INPUT("syntax.unrecognized-input", Severity.ERROR),

    /** The parser met a token the grammar does not allow at this place, or misses a token it requires. */
    SYNTAX_ERROR("syntax.error", Severity.ERROR),

    /** A KMeta text defines no type, so the metamodel has no root type. */
    NO_TYPES("kmeta.no-types", Severity.ERROR),

    /** A KMeta `prop` names a type other than `string`, `number` and `boolean`. */
    UNKNOWN_PROPERTY_TYPE("kmeta.unknown-property-type", Severity.ERROR),

    /** A KMeta `has` or `knows` refers to a class type that neither the text nor the base types define. */
    UNKNOWN_TARGET_TYPE("kmeta.unknown-target-type", Severity.ERROR),

    /** A KModel object is made of a class type the metamodel does not define. */
    UNKNOWN_CLASS_TYPE("kmodel.unknown-class-type", Severity.ERROR),

    /** A KModel `prop` names a property the class type of its object does not define. */
    UNKNOWN_PROPERTY("kmodel.unknown-property", Severity.ERROR),

    /** A KModel `has` or `knows` names a relation the class type of its object does not define. */
    UNKNOWN_RELATION("kmodel.unknown-relation", Severity.ERROR),

    /** A KModel `has` names a `knows` relation, or a `knows` names a `has` relation. */
    WRONG_RELATION_KIND("kmodel.wrong-relation-kind", Severity.ERROR),

    /** A KModel `prop` assigns a value that is not a number to a number property. */
    INVALID_NUMBER("kmodel.invalid-number", Severity.ERROR),

    /** A KModel `has` embeds an object of another class type than the relation declares. */
    EMBEDDED_TYPE_MISMATCH("kmodel.embedded-type-mismatch", Severity.WARNING),

    /** A KModel `knows` names an object id that no object of the text has. */
    UNRESOLVED_REFERENCE("kmodel.unresolved-reference", Severity.ERROR),

    /** A KModel `knows` names an object of another class type than the relation declares. */
    REFERENCE_TYPE_MISMATCH("kmodel.reference-type-mismatch", Severity.ERROR),

    /** A KStates state machine marks no state or several states as `INITIAL`. */
    INITIAL_STATE_COUNT("kstates.initial-state-count", Severity.ERROR),

    /** A KStates action rule is written with another number of operands than its operation expects. */
    ACTION_ARITY("kstates.action-arity", Severity.ERROR),

    /** A KStates `DELAY` or `TIMEOUT` counts its ticks with a number that is not whole. */
    TICK_COUNT("kstates.tick-count", Severity.ERROR),

    /**
     * A KStates text uses a construct of a [io.karpfen.dsl.states.KstatesFeature] that the reader was
     * told is switched off, such as a history while histories are off.
     */
    FEATURE_OFF("kstates.feature-off", Severity.ERROR),

    /** A KMeta text defines two types of the same name. */
    DUPLICATE_TYPE("kmeta.duplicate-type", Severity.ERROR),

    /** A KMeta type has two properties or relations, or a property and a relation, with the same key. */
    DUPLICATE_KEY("kmeta.duplicate-key", Severity.ERROR),

    /** Two objects of a KModel text have the same id. */
    DUPLICATE_OBJECT_ID("kmodel.duplicate-id", Severity.ERROR),

    /** A KModel object gives several values to a property or relation that its class type does not declare as a list. */
    SEVERAL_VALUES("kmodel.several-values", Severity.ERROR),

    /** A KModel `prop` assigns a value other than `true` or `false` to a boolean property. */
    INVALID_BOOLEAN("kmodel.invalid-boolean", Severity.ERROR),

    /** Two states of a KStates state machine have the same name, at any depth. */
    DUPLICATE_STATE("kstates.duplicate-state", Severity.ERROR),

    /** A KStates transition, split, join or timeout names a state that the state machine does not define. */
    UNKNOWN_STATE("kstates.unknown-state", Severity.ERROR),

    /** A KStates `SPLIT` names the same target state twice. */
    REPEATED_SPLIT_TARGET("kstates.repeated-split-target", Severity.ERROR),

    /** A KStates `JOIN` names the same source state twice. */
    REPEATED_JOIN_SOURCE("kstates.repeated-join-source", Severity.ERROR),

    /** A KStates text defines two macros of the same name. */
    DUPLICATE_MACRO("kstates.duplicate-macro", Severity.ERROR),

    /** A KStates macro has two `TAKES` parameters of the same name. */
    DUPLICATE_PARAMETER("kstates.duplicate-parameter", Severity.ERROR),

    /** A KStates `MACRO(...)` call, or an `@name(...)` call in the code of an `EVAL`, names a macro the text does not define. */
    UNKNOWN_MACRO("kstates.unknown-macro", Severity.ERROR),

    /**
     * A KStates macro call passes more arguments than the macro takes, or an `@name(...)` call in the code of an
     * `EVAL`, which Python runs as a function call, passes another number.
     */
    MACRO_ARGUMENT_COUNT("kstates.macro-argument-count", Severity.ERROR),

    /**
     * A KStates `MACRO(...)` call passes fewer arguments than the macro takes; the runtime reads each missing argument
     * from the path named like its parameter.
     */
    MISSING_MACRO_ARGUMENTS("kstates.missing-macro-arguments", Severity.WARNING),

    /** A KStates `CONDITION` has more than one `EVENT` clause. */
    SEVERAL_EVENT_CLAUSES("kstates.several-event-clauses", Severity.ERROR),

    /**
     * A KStates `CONDITION` has an `EVENT` clause that is not its first clause; the runtime finds it in any place, so
     * the clause only reads better first.
     */
    EVENT_CLAUSE_NOT_FIRST("kstates.event-clause-not-first", Severity.WARNING),

    /** A KStates state has a history but no inner states, so there is nothing it could remember. */
    HISTORY_WITHOUT_INNER_STATES("kstates.history-without-inner-states", Severity.WARNING),

    /** A KStates `BREAKPOINT` block names neither of its two words and so marks nothing. */
    EMPTY_BREAKPOINT("kstates.empty-breakpoint", Severity.WARNING),

    /** A KStates `TICK LIMIT` block has neither a `DELAY` nor a `TIMEOUT` and so limits nothing. */
    EMPTY_TICK_LIMIT("kstates.empty-tick-limit", Severity.WARNING),

    /** A KStates `TICK LIMIT` has a `DELAY` longer than its `TIMEOUT`, which holds the timeout back. */
    DELAY_BEYOND_TIMEOUT("kstates.delay-beyond-timeout", Severity.WARNING),

    /** The code of a KStates `EVAL` holds `//`, which starts a comment of KStates there, so a brace after it on its line does not count. */
    COMMENT_IN_EVAL("kstates.comment-in-eval", Severity.WARNING),

    /** The code of a KStates `EVAL` has no `return`, so it yields no value. */
    EVAL_WITHOUT_RETURN("kstates.eval-without-return", Severity.WARNING),

    /** A KStates state machine is `ATTACHABLE TO` types but has no name, which `behaves after`, `ATTACH` and `EXCHANGE` need. */
    MISSING_STATE_MACHINE_NAME("kstates.missing-state-machine-name", Severity.ERROR),

    /**
     * A KStates state machine `ATTACHED TO` a type has a name or names more than one type; only the attachable form,
     * `ATTACHABLE TO`, has them.
     */
    ATTACHED_HEADER("kstates.attached-header", Severity.ERROR),

    /** A KStates state machine names the same type twice after `ATTACHABLE TO`. */
    REPEATED_ATTACHABLE_TYPE("kstates.repeated-attachable-type", Severity.ERROR),

    /** A state machine is `ATTACHED TO`, or `ATTACHABLE TO`, a class type that the metamodel of the project does not define. */
    UNKNOWN_ATTACHED_TYPE("project.unknown-attached-type", Severity.ERROR),

    /**
     * The model has no object of the id a state machine is attached to, or it is of another class type than its
     * `ATTACHED TO` names, or than any of the types its `ATTACHABLE TO` lists.
     */
    ATTACHED_OBJECT_TYPE("project.attached-object-type", Severity.ERROR),

    /** A path of an action or of a `VALUE` condition names a property or relation the class type on its way lacks. */
    UNKNOWN_PATH("project.unknown-path", Severity.ERROR),

    /** An operation is applied to something it cannot change, such as `APPEND` to a property that is not a list. */
    OPERATION_MISMATCH("project.operation-mismatch", Severity.ERROR),

    /** The literal right side of a `SET`, `APPEND` or `SETLIST` cannot be read as the type of the property it is written to. */
    LITERAL_MISMATCH("project.literal-mismatch", Severity.ERROR),

    /** A `$(path)` reference in the code of an `EVAL` does not resolve. */
    UNRESOLVED_CODE_REFERENCE("project.unresolved-code-reference", Severity.ERROR),

    /** A `TAKES` or `RETURNS` of a macro names a type that is neither primitive nor defined by the project. */
    UNKNOWN_MACRO_TYPE("project.unknown-macro-type", Severity.ERROR),

    /** An argument path of a macro call does not resolve. */
    UNRESOLVED_MACRO_ARGUMENT("project.unresolved-macro-argument", Severity.ERROR),

    /** An argument path of a macro call leads to something of another type than the parameter takes. */
    MACRO_ARGUMENT_TYPE("project.macro-argument-type", Severity.ERROR),

    /** An `event->field` path reads a field that no event that can be in scope there carries, or an event no event metamodel declares. */
    UNKNOWN_EVENT_FIELD("project.unknown-event-field", Severity.ERROR),

    /** A state can never become active. */
    UNREACHABLE_STATE("project.unreachable-state", Severity.WARNING),

    /** A transition can never fire, because an earlier transition from the same state always fires first. */
    SHADOWED_TRANSITION("project.shadowed-transition", Severity.WARNING),

    /** A `JOIN` can never fire, because its source states can never be active side by side. */
    JOIN_NEVER_FIRES("project.join-never-fires", Severity.WARNING),

    /** A `SPLIT` can never fire, because its source state is only active inside a branch of a split, where no split fires. */
    SPLIT_INSIDE_PARALLEL("project.split-inside-parallel", Severity.WARNING),

    /** Code reads `event` or `event->...` where no event can be in scope. */
    EVENT_NOT_IN_SCOPE("project.event-not-in-scope", Severity.WARNING),

    /** A state machine reads a property that the model gives no value, on the way from the object it runs on, and that no action writes. */
    UNSET_PROPERTY("project.unset-property", Severity.WARNING),

    /** Two attachable state machines of a project have the same name. */
    DUPLICATE_STATE_MACHINE_NAME("project.duplicate-state-machine-name", Severity.ERROR),

    /** A `behaves after`, an `ATTACH` or an `EXCHANGE` names a state machine that no attachable state machine of the project is called. */
    UNKNOWN_STATE_MACHINE("project.unknown-state-machine", Severity.ERROR),

    /**
     * A `behaves after`, an `ATTACH` or an `EXCHANGE` names an attachable state machine for an object of a type that the
     * state machine does not list after `ATTACHABLE TO`.
     */
    ATTACHABLE_TYPE_MISMATCH("project.attachable-type-mismatch", Severity.ERROR),

    /**
     * An `ATTACH` clause follows a `SETOBJ` or `APPENDOBJ` on a `knows` relation, which links an object that exists
     * already instead of creating one; `EXCHANGE` replaces the state machine of such an object.
     */
    ATTACH_ON_KNOWS("project.attach-on-knows", Severity.ERROR),

    /** A type of the event metamodel has `behaves after`, but events have no state machines. */
    BEHAVES_AFTER_IN_EVENTS("project.behaves-after-in-events", Severity.ERROR),

    /**
     * The reader failed and could not build a model: on nesting deeper than the parser can recurse, or through a
     * defect of the reader. The reader also logs the failure, see [io.karpfen.dsl.reader.TextReader].
     */
    READER_FAILURE("reader.failure", Severity.ERROR)
}
