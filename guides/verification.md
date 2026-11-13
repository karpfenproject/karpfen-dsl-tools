# Verification

The karpfen DSL tools check texts in two stages.

1. The readers `KmetaReader`, `KmodelReader` and `KstatesReader` check each text on its own while they read it. They
   report their problems in the `ParseResult` of the read and leave the model as they built it.
2. `ProjectVerifier` from the package `io.karpfen.dsl.verification` checks the read state machines of a project against
   the read metamodels and the read model. It reports its problems in the text of the state machine they concern.

Both report a `Diagnostic` with a range, a severity and a stable `DiagnosticCode`, whose `id` is the code below. No
check reports what it cannot be sure of: a check that needs a text without errors stays silent while that text has
errors.

## Calling the verifier

| Step | Call |
| --- | --- |
| Read the domain metamodel | `val metamodel = KmetaReader().read(kmetaText)` |
| Read the event metamodel, if the project has one | `val events = KmetaReader(metamodel.model!!.types).read(eventsText)` |
| Read the model, if the project has one | `val model = KmodelReader(metamodel.model!!).read(kmodelText)` |
| Read each state machine with the id of the object it runs on, if known | `val robot = StateMachineInput(KstatesReader().read(kstatesText), "turtle")` |
| Verify | `val findings = ProjectVerifier().verify(ProjectInput(metamodel, events, model, listOf(robot)))` |
| Take the problems of a state machine | `findings.of(robot)`, sorted by offset; `findings.errors` and `findings.warnings` hold all of them |

The verifier leaves out a state machine with a syntax error. The checks of types need a domain metamodel without
errors, the checks of event fields an event metamodel without errors, the checks of values a model without errors and
the id of the object the machine runs on, and the checks of states and events a machine whose state names are unique
and known.

## Problems of the syntax

| Code | Severity | Meaning |
| --- | --- | --- |
| `syntax.unrecognized-input` | error | The lexer met characters that start no token and skipped them. |
| `syntax.error` | error | The parser met a token the grammar does not allow there, or misses one it requires. |
| `reader.failure` | error | The reader failed and built no model, for example on nesting deeper than the parser can recurse. |

## Problems the KMeta reader reports

| Code | Severity | Meaning |
| --- | --- | --- |
| `kmeta.no-types` | error | The text defines no type, so the metamodel has no root type. |
| `kmeta.unknown-property-type` | error | A `prop` names a type other than `string`, `number` and `boolean`. |
| `kmeta.unknown-target-type` | error | A `has` or `knows` refers to a type that neither the text nor the base types define. |
| `kmeta.duplicate-type` | error | A type has the name of an earlier type; reported at the later name. |
| `kmeta.duplicate-key` | error | A type uses a key twice, for props, relations or both; reported at the later key. |

## Problems the KModel reader reports

| Code | Severity | Meaning |
| --- | --- | --- |
| `kmodel.unknown-class-type` | error | An object is made of a type the metamodel does not define. |
| `kmodel.unknown-property` | error | A `prop` names a property the type of its object does not define. |
| `kmodel.unknown-relation` | error | A `has` or `knows` names a relation the type of its object does not define. |
| `kmodel.wrong-relation-kind` | error | A `has` names a `knows` relation, or a `knows` names a `has` relation. |
| `kmodel.invalid-number` | error | A number property is given a value that is not a number. |
| `kmodel.embedded-type-mismatch` | warning | A `has` embeds an object of another type than the relation declares. |
| `kmodel.unresolved-reference` | error | A `knows` names an id that no object has. |
| `kmodel.reference-type-mismatch` | error | A `knows` names an object of another type than the relation declares. |
| `kmodel.duplicate-id` | error | An object has the id of an earlier object, embedded ones included; reported at the later id. |
| `kmodel.several-values` | error | A property or relation that is not a list is given several values; reported at each further key. |
| `kmodel.invalid-boolean` | error | A boolean property is given a value other than `true` or `false` in any case; it reads as false. |

## Problems the KStates reader reports

| Code | Severity | Meaning |
| --- | --- | --- |
| `kstates.initial-state-count` | error | No state or several states are marked `INITIAL`. |
| `kstates.action-arity` | error | An action rule has another number of operands than its operation expects. |
| `kstates.tick-count` | error | A `DELAY` or `TIMEOUT` counts its ticks with a number that is not whole. |
| `kstates.feature-off` | error | A construct of a feature that is switched off is used. |
| `kstates.duplicate-state` | error | A state has the name of an earlier state, at any depth; reported at the later name. |
| `kstates.unknown-state` | error | A transition, split, join or timeout names a state that does not exist. |
| `kstates.repeated-split-target` | error | A split names the same target twice. |
| `kstates.repeated-join-source` | error | A join names the same source twice. |
| `kstates.duplicate-macro` | error | A macro has the name of an earlier macro, which the runtime calls instead. |
| `kstates.duplicate-parameter` | error | A macro takes two parameters of the same name. |
| `kstates.unknown-macro` | error | A `MACRO(...)` call or an `@name(...)` call in `EVAL` code names a macro that does not exist. |
| `kstates.macro-argument-count` | error | A `MACRO(...)` call passes more arguments than the macro takes, or an `@name(...)` call in `EVAL` code, which Python runs as a function call, passes another number. |
| `kstates.missing-macro-arguments` | warning | A `MACRO(...)` call passes fewer arguments than the macro takes; the runtime reads each missing one from the path named like its parameter. |
| `kstates.several-event-clauses` | error | A condition has more than one `EVENT` clause; the runtime matches only the first and ignores the others. |
| `kstates.event-clause-not-first` | warning | An `EVENT` clause is not the first clause of its condition; the runtime finds it in any place, so it only reads better first. |
| `kstates.history-without-inner-states` | warning | A state without inner states has a history. |
| `kstates.empty-breakpoint` | warning | A `BREAKPOINT` block marks nothing. |
| `kstates.empty-tick-limit` | warning | A `TICK LIMIT` block has neither a `DELAY` nor a `TIMEOUT`. |
| `kstates.delay-beyond-timeout` | warning | A `DELAY` is longer than the `TIMEOUT` of its block, which then fires only after the delay. |
| `kstates.comment-in-eval` | warning | `EVAL` code holds `//`, which starts a KStates comment, so a closing brace after it on its line does not count. |
| `kstates.eval-without-return` | warning | `EVAL` code has no `return`, so it yields no value. |

The checks of names and calls are left out for a text with a syntax error, since the parser may have lost a
definition; the repetitions in splits and joins and the comments in `EVAL` code are reported nevertheless.

## Problems the verifier reports

A path is followed segment by segment, the way the runtime follows it: each segment names a relation or a property, a
path goes on through the first object of a list relation but not through a property, and in the code of a macro it
cannot go on through a list. A path is read from a binding of a `WITH` block if it starts with its name, from the event
in scope if it starts with `event`, in the code of a macro from a parameter, and otherwise from the object the state
machine runs on. The paths of `IF IN SCOPE` blocks are never reported, and a path inside such a block that starts with
one of its paths, or with `event` if one of its paths does, is never reported as leading nowhere.

| Code | Severity | Meaning |
| --- | --- | --- |
| `project.unknown-attached-type` | error | The domain metamodel defines no type of the name after `ATTACHED TO`. |
| `project.attached-object-type` | error | The model has no object of the given id, or it is of another type than the state machine is attached to. |
| `project.unknown-path` | error | The path of an action rule or of a `VALUE` condition leads nowhere. |
| `project.operation-mismatch` | error | An operation does not fit what its path leads to, such as `SET` on a relation, see below. |
| `project.literal-mismatch` | error | A literal value of `SET`, `APPEND` or `SETLIST` cannot be read as the type of its property. |
| `project.unresolved-code-reference` | error | A `$(path)` in `EVAL` code leads nowhere. |
| `project.unknown-macro-type` | error | A `TAKES` or `RETURNS` type names no primitive, domain type or event type, or uses a function other than `list` and `reference`. |
| `project.unresolved-macro-argument` | error | An argument of a `MACRO(...)` call leads nowhere. |
| `project.macro-argument-type` | error | An argument of a `MACRO(...)` call leads to a value of another type than its parameter takes. |
| `project.unknown-event-field` | error | `event->...` reads a field that no event that can be in scope there has, or the event is not declared. |
| `project.unreachable-state` | warning | A state can never be active; only the outermost of such states is reported. |
| `project.shadowed-transition` | warning | An earlier transition of the same state always fires and may loop, so this one never fires. |
| `project.join-never-fires` | warning | No split leads to branches in which each source state of the join is active in a branch of its own. |
| `project.split-inside-parallel` | warning | The source state of a split is active only inside a branch of a split, where no split fires. |
| `project.event-not-in-scope` | warning | Code reads `event` where no event can be in scope. |
| `project.unset-property` | warning | The model gives the object the state machine runs on no value on the way of a read of a property, and no action writes any key of the path. |

The operations fit as follows. `SET` needs a property that is not a list, and `APPEND`, `SETLIST` and `DROPLIST` need
a list property; on a relation, the message names the relation operation to use instead. `SETOBJ` needs a relation to one object and `APPENDOBJ` a
relation to a list; a literal path of either must lead to one object of the type of the relation, and only a `knows`
relation may link it, since a `has` relation takes a new object. `DROPOBJ` needs a path to one object. A `VALUE`
condition needs a boolean or a string property.

An event comes into scope at the target of the transition or split whose `EVENT` clause matched it, and stays while
that state is active. The code of a state sees the events of the transitions into the state, into an outer state and
into an inner state; the conditions of a transition see the event of their own `EVENT` clause, or else those of the
state they start in, and the conditions of a join see none. A state is reachable if a transition, split, join or
timeout leads into it from the initial state, with every condition taken as possibly true; entering a state enters
none of its inner states.
