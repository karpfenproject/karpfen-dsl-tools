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
 * The kinds of [Diagnostic]s the readers report.
 *
 * Both the constant names and the [id]s are stable, so that tools can rely on them, for example to offer a
 * fix for a kind of problem.
 *
 * @property id A stable identifier of the kind, prefixed with the language it belongs to, or with `syntax`
 * and `reader` for kinds every language shares.
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

    /**
     * The reader failed and could not build a model: on nesting deeper than the parser can recurse, or through a
     * defect of the reader.
     */
    READER_FAILURE("reader.failure", Severity.ERROR)
}
