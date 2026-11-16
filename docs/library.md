# Using the library from Kotlin

karpfen-dsl-tools is the Kotlin library that reads, checks and writes the three karpfen languages. The runtime and the
IDE use it, and any other JVM program can use it as well. This document is for programmers who call it from code. It
describes the classes and functions they call, what these take and return, and shows each with a Kotlin example and
its output. The reference chapter [2.5 Using the library](reference/2.5-library.md) gives a short overview.

The examples read four files from one folder. `lamp.kmeta`, `lamp.kmodel` and `lamp.kstates` hold the lamp texts of
[2 Modeling languages](reference/2-modeling-languages.md), and `lamp-events.kmeta` holds the event metamodel of
[2.1.7 Event definitions](reference/2.1-kmeta.md#217-event-definitions). The example of a composition reads one more
file, the bulb test fragment.

## Adding the library to a build

| Item | Value |
|---|---|
| Group and artifact | `io.karpfen:karpfen-dsl-tools` |
| Version | `2.0.0-SNAPSHOT` |
| Repository | `https://maven.pkg.github.com/karpfenproject/karpfen-dsl-tools` (GitHub Packages) |
| JDK | 21 |
| Dependencies a consumer gets | ANTLR runtime 4.13.1, `org.json`, SLF4J API |

GitHub Packages asks for credentials even for reading: a GitHub user and a token that may read packages. When the
library publishes itself, its build takes these credentials from the Gradle properties `GitHubPackagesUsername` and
`GitHubPackagesPassword`.

A build may also take the library from a git submodule. The runtime includes the checkout as a subproject with
`include 'karpfen-dsl-tools'` in its `settings.gradle`, and the IDE includes it as an included build. To build the
library on its own, run `./gradlew build` in its folder with JDK 21.

## Reading a text

A reader reads one text and never throws. `KmetaReader`, `KmodelReader` and `KstatesReader` each have the method
`read(text)`, and it returns a `ParseResult`. The result holds the model the reader built, the problems it found and a
source map that tells where each element of the model is written. A byte order mark at the start of the text is
ignored by the reader. The positions in the result still count it as the first char of the first line, because the
text holds it there.

| Member of `ParseResult` | Content |
|---|---|
| `model` | The metamodel, model or state machine. With errors it is partial, because the reader leaves out what it cannot read. It is null only if nothing could be built, for example for a KMeta text without a type. |
| `diagnostics` | Every problem, ordered by its position in the text. |
| `errors` | The problems of severity `ERROR`. |
| `hasErrors` | True if there is at least one error. |
| `sourceMap` | The ranges of the elements of `model` in the text. |

`KmodelReader` takes the metamodel in its constructor. `KstatesReader` takes the optional features that are switched on,
as a set of the `KstatesFeature` values of [Optional features](#optional-features). `KmetaReader` may be given a list of
types that are defined elsewhere, and it adds them to every metamodel it reads. An event metamodel is read by a
`KmetaReader` that has been given the types of the domain metamodel. Since a reader keeps no state between reads, one
reader may serve several threads.

`readLamp` reads the metamodel and the model with their readers and reports the problems of both. The function `report`
defined here is used by the later examples as well.

```kotlin
import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import java.io.File

fun report(fileName: String, problems: List<Diagnostic>) {
    for (problem in problems) {
        val start = problem.range.start
        println("$fileName:${start.line}:${start.column}: ${problem.severity} ${problem.code.id}: ${problem.message}")
    }
}

fun readLamp(folder: File) {
    val metamodelRead = KmetaReader().read(File(folder, "lamp.kmeta").readText())
    report("lamp.kmeta", metamodelRead.diagnostics)
    val metamodel = metamodelRead.model ?: return
    val modelRead = KmodelReader(metamodel).read(File(folder, "lamp.kmodel").readText())
    report("lamp.kmodel", modelRead.diagnostics)
    val room = modelRead.model?.objects?.singleOrNull() ?: return
    val bulb = room.getRel("lamp").single().getRel("bulb").single()
    println(bulb.getProp("maxWatts"))
    println(modelRead.sourceMap.nameRangeOf(bulb))
}
```

For the lamp files `readLamp` reports no problem. It prints the maximum watts of the bulb and the place where the key
of the bulb is written.

```text
[40.0]
SourceRange(start=SourcePosition(offset=148, line=4, column=37), end=SourcePosition(offset=152, line=4, column=41))
```

A diagnostic has a `range`, a `SourceRange` with a `start` and an `end`. Each of them is a `SourcePosition` with an
`offset`, a `line` and a `column`, and lines and columns count from 1. The `code` of a diagnostic is a
`DiagnosticCode` whose `id` is the code that [2.4 Checks](reference/2.4-checks.md) lists. The `severity` is `ERROR` or
`WARNING`. The code is stable, and the `message` is meant for people. For a metamodel with `prop("watts", "Number")`
in line 2, `report` writes this line.

```text
lamp.kmeta:2:20: ERROR kmeta.unknown-property-type: Unknown type 'Number' of prop 'watts'; a prop is of type 'string', 'number', 'boolean'
```

The models that the readers build consist of these classes.

| Class | Members |
|---|---|
| `Metamodel` | The types in `types`, each a `ClassType`, and the root type in `rootClass`. |
| `ClassType` | `simpleProperties` and `objectProperties`. A type with `behaves after` holds the clause in `behavesAfter`, a `BehavesAfter` with the `stateMachineName`. |
| `Model` | The root object in `objects`. |
| `DataObject` | Its `id` and its type `ofType`. `getProp(key)` gives the values of a property and `getRel(key)` the objects of a relation, both as lists. |
| `StateMachine` | `attachedToClass`, `states`, `transitions` and `macros`. |

`readLampMachine` reads the state machine of the lamp and prints the type it is attached to and the line of each state.

```kotlin
import io.karpfen.dsl.reader.kstates.KstatesReader
import java.io.File

fun readLampMachine(folder: File) {
    val read = KstatesReader().read(File(folder, "lamp.kstates").readText())
    report("lamp.kstates", read.diagnostics)
    val machine = read.model ?: return
    println(machine.attachedToClass)
    for (state in machine.states) {
        println("${state.name} ${read.sourceMap.rangeOf(state)?.start?.line}")
    }
}
```

```text
Lamp
off 3
on 10
```

Of the three forms of [2.3.1 The state machine](reference/2.3-kstates.md#231-the-state-machine), an attachable state
machine has `isAttachable` true, its `name`, its types in `attachableTo` and an empty `attachedToClass`. An attached
state machine has the `name` null. In both forms `types` holds the types. A fragment of
[2.3.12 Pages](reference/2.3-kstates.md#2312-pages) has `isFragment` true, its `name`, no types and `isAttachable`
false.

A `State` tells its kind through `isContainer`, with the name of its fragment in `fragment`, and through `isFinal`. The
condition `DONE` is a `DoneCondition`. An action rule with an `ATTACH` clause holds the clause in `attach`, as an
`AttachClause` whose `stateMachineName` is the value of the clause. `EXCHANGE` is the operation
`ActionOperationType.EXCHANGE`.

The source map gives the ranges of the elements of the model in the text.

| Function | Range |
|---|---|
| `rangeOf(element)` | The whole element. |
| `nameRangeOf(element)` | The name or key of the element. |
| `codeRangeOf(element)` | The code of an `EVAL`. |
| `operandRangesOf(element)` | The operands of an action rule. |

The first three functions return null for an element that the source map does not know, and `operandRangesOf` returns
an empty list for it. The source map knows the elements of the model by identity. The name range of an attached state
machine is that of its type. For an attachable state machine it is that of its name, and its operands are its types.
An `AttachClause` and a `BehavesAfter` range over the whole clause, keyword included, and the name of a `BehavesAfter`
is the name of the state machine.

## Throwing converters

The converters are the throwing form of the readers and are meant for a text that is expected to be right. They are
`KmetaDSLConverter`, `KmodelDSLConverter` and `KstatesDSLConverter` in the package `io.karpfen.dsl.dsl.textual`, each
with one function for a string and one for a file. Because each converter reads through its reader, every rule
of the language holds for the converter exactly as for the reader. A converter returns the model of a text without
errors and throws for a text with at least one error. Warnings do not make it throw. `KstatesDSLConverter` reads with
every optional feature switched on.

| Language | Function for a string | Function for a file |
|---|---|---|
| KMeta | `parseKmetaString` | `parseKmetaFile` |
| KModel | `parseKmodelString` | `parseKmodelFile` |
| KStates | `parseKstatesString` | `parseKstatesFile` |

`convertLamp` reads all four files with the converters. At the end it builds the payload of the event `dim` from JSON.

```kotlin
import io.karpfen.dsl.dsl.textual.JsonToModelConverter
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import java.io.File

fun convertLamp(folder: File) {
    val metamodel = KmetaDSLConverter.parseKmetaFile(File(folder, "lamp.kmeta").path)
    val events = KmetaDSLConverter.parseKmetaString(File(folder, "lamp-events.kmeta").readText(), metamodel.types)
    val model = KmodelDSLConverter.parseKmodelFile(File(folder, "lamp.kmodel").path, metamodel)
    val machine = KstatesDSLConverter.parseKstatesFile(File(folder, "lamp.kstates").path)
    println("${metamodel.rootClass.name} ${model.objects.single().id} ${machine.states.size}")
    val payload = JsonToModelConverter(events).toDataObject("""{"bright": true}""", "dim")
    println(payload.getProp("bright"))
}
```

It prints the root type, the id of the root object and the number of states, and in the second line the value of the
payload.

```text
Room living room 2
[true]
```

The functions of `KmodelDSLConverter` take the metamodel as their second argument. A program that reads an event
metamodel with a converter uses `parseKmetaString`, because of the two functions of `KmetaDSLConverter` only this one
takes the types of the domain metamodel, as an optional second argument.
`KmodelDSLConverter.parseSingleObject` reads a KModel text such as an event payload and returns its root object. Since
a KModel text is always one object, `parseSingleObject` reads the text exactly as `parseKmodelString` does and reports
the same errors.

`JsonToModelConverter` turns a JSON object into an object of a type. It is made with the metamodel that holds the
type, and its function `toDataObject` takes the JSON object, as a string or as an `org.json` object, together with the
name of the type. This is how a payload of an event that arrives as JSON becomes an object of the event metamodel.

A converter throws an `InvalidTextException` from the package `io.karpfen.dsl.reader`, and the exception type is the
same for all three languages. It is an `IllegalArgumentException`. Its `errors` hold every error the reader found, as
diagnostics like those of [Reading a text](#reading-a-text). Its `source` holds the path of the file, or null when a
string was read. The message of the exception names each error on a line of its own, with the position, the code and
the message of the error. If the state `off` of `lamp.kstates` lacks its `INITIAL`, `parseKstatesFile("lamp.kstates")`
throws with this message.

```text
lamp.kstates has 1 error:
lamp.kstates:2:5: kstates.initial-state-count: Exactly one state must be marked as INITIAL, but found 0.
```

For a string the first line reads `The text has 1 error:`, and the lines of the errors start with the line and the
column. The converters take this step with `modelOrThrow()` of `ParseResult`, and a program can call it on the result
of a reader too. It returns the model of a text without errors and throws the same exception otherwise.

Use a converter where a wrong text is a failure, such as in a runtime that loads a deployed project or in a test. Use a
reader where a wrong text is normal and its problems are shown, such as in an editor, or where a partial model is
needed.

`collectAllObjects` of `Model`, in the package `io.karpfen.dsl.instance`, takes a list of objects and returns them
together with every object embedded in them. Each object is in the result once, and before the objects it embeds.
Since a model read from text holds only its root object in `objects`, the function returns all objects of the model
when it is given that list.

## Printing back to text

The printers `KmetaPrinter`, `KmodelPrinter` and `KstatesPrinter`, in the packages below `io.karpfen.dsl.printer`, write
a metamodel, a model or a state machine as text. `print` writes a whole text. The printers can also write a single
element as a piece of text that can replace that element in an existing text.

| Language | Functions for a single element |
|---|---|
| KMeta | `printType`, `printRule` |
| KModel | `printObject` |
| KStates | `printState`, `printTransition`, `printMacro` |

The `convertTo...String` functions of the converters call the same printers. `printLamp` prints a whole model and then
two single elements, the bulb of the model and the first state of the state machine.

```kotlin
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.states.StateMachine

fun printLamp(model: Model, machine: StateMachine) {
    print(KmodelPrinter().print(model))
    val bulb = model.objects.single().getRel("lamp").single().getRel("bulb").single()
    println(KmodelPrinter().printObject(bulb))
    println(KstatesPrinter().printState(machine.states.first()))
}
```

For the lamp files the bulb and the state `off` come out as follows.

```text
make object "bulb":"Bulb" {
    prop("maxWatts") -> "40.0"
    prop("watts") -> "0.0"
    prop("lit") -> "false"
}
INITIAL STATE "off" {
    ENTRY {
        SET("mode", "off")
        SET("bulb->lit", "false")
        SET("bulb->watts", "0")
    }
}
```

The printers use one layout, with four spaces per level, one statement per line and an empty block written as `{ }`. A
text that is read and printed again loses its comments, except the comments inside `EVAL` code. A number of a model is
written as the library holds it, as a double: `"40"` comes back as `"40.0"`. The KMeta printer writes the properties
of a type before its relations, and the root type last. Given the domain types as a second argument, `print` of
`KmetaPrinter` writes an event metamodel without the domain types it was read with.

A printer throws an `IllegalArgumentException` for an element that its language cannot write, such as a name with a
line break that no backslash escapes.

## Building models in code

`MetamodelBuilder` and `ModelBuilder` in the package `io.karpfen.dsl.dsl.functional` build a metamodel and a model
without text. A metamodel is built with the following functions of `MetamodelBuilder`.

| Function | Makes |
|---|---|
| `makeClass` | A type, from its name, a description and the lists of its properties and relations. |
| `makeNumber`, `makeString`, `makeBool` and their `...List` forms | Properties. |
| `makeClassProp`, `makeClassPropList` | Relations, with `EMBEDDED` for `has` and `LINK` for `knows`. |
| `setRoot` | The root type, given by its name. |
| `build` | The metamodel. |

A relation can only lead to a type that is already defined. `buildLampMetamodel` builds the metamodel of the lamp
texts and makes the types in such an order.

```kotlin
import io.karpfen.dsl.dsl.functional.MetamodelBuilder
import io.karpfen.dsl.meta.Metamodel

fun buildLampMetamodel(): Metamodel {
    val m = MetamodelBuilder()
    m.makeClass("Bulb", "A light bulb", props = listOf(m.makeNumber("maxWatts"), m.makeNumber("watts"), m.makeBool("lit")))
    m.makeClass("Switch", "A wall switch", props = listOf(m.makeString("position"), m.makeStringList("presses")))
    m.makeClass(
        "Lamp", "A lamp with a bulb, wired to a switch",
        props = listOf(m.makeString("mode")),
        rels = listOf(m.makeClassProp("bulb", "Bulb", m.EMBEDDED), m.makeClassProp("control", "Switch", m.LINK))
    )
    m.makeClass(
        "Room", "The room the lamp stands in",
        rels = listOf(m.makeClassProp("lamp", "Lamp", m.EMBEDDED), m.makeClassProp("switch", "Switch", m.EMBEDDED))
    )
    m.setRoot("Room")
    return m.build()
}
```

`ModelBuilder` is made with a metamodel. `ModelBuilder.makeObject(type, id)` makes an object. It gives every property a
default value according to the type of the property: `false`, the empty string, `Double.MIN_VALUE` or an empty list.
Every relation to one object gets a new default object without an id. `assignProps` and `assignRels` of the object
replace these values. Each takes a map from the keys to the new values, with a list for a list property or relation,
and ignores a key that the type does not have. `build` returns the model. `buildLampModel` builds the lamp model on the
metamodel of `buildLampMetamodel`.

```kotlin
import io.karpfen.dsl.dsl.functional.ModelBuilder
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.meta.Metamodel

fun buildLampModel(metamodel: Metamodel): Model {
    val b = ModelBuilder(metamodel)
    val wallSwitch = b.makeObject("Switch", "wallSwitch")
    wallSwitch.assignProps(mapOf("position" to "down"))
    val bulb = b.makeObject("Bulb", "bulb")
    bulb.assignProps(mapOf("maxWatts" to 40.0, "watts" to 0.0, "lit" to false))
    val lamp = b.makeObject("Lamp", "lamp")
    lamp.assignProps(mapOf("mode" to "off"))
    lamp.assignRels(mapOf("bulb" to bulb, "control" to wallSwitch))
    val room = b.makeObject("Room", "living room")
    room.assignRels(mapOf("lamp" to lamp, "switch" to wallSwitch))
    return b.build()
}
```

Because the model of a builder holds every object the builder made, the default objects included,
`KmodelPrinter().print` starts at the object of the root type that has an id and that no other object embeds. For this
model it writes the same text as the lamp model of [2 Modeling languages](reference/2-modeling-languages.md), with
numbers such as `"40.0"`.

## Optional features

`KstatesReader` takes the optional features of
[2.3.10 Optional features](reference/2.3-kstates.md#2310-optional-features) that a text may use. The default is
`KstatesFeature.ALL`. Each feature has a stable `id`. `KstatesFeature.byId` finds a feature by its id and returns null
for an id that names no feature. With it a program turns the ids it reads from a settings file into the set that the
constructor of `KstatesReader` takes, as `readWithFeatures` does.

```kotlin
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.KstatesFeature
import io.karpfen.dsl.states.StateMachine

fun readWithFeatures(text: String, featureIds: List<String>): ParseResult<StateMachine> {
    val switchedOn = featureIds.mapNotNull { KstatesFeature.byId(it) }.toSet()
    return KstatesReader(switchedOn).read(text)
}
```

The converter `KstatesDSLConverter` always accepts every feature.

## Verifying a project

`ProjectVerifier` in the package `io.karpfen.dsl.verification` checks the state machines of a project against the
metamodels and the model. The problems it reports are listed in [2.4.6 Project](reference/2.4-checks.md#246-project).
Its function `verify` takes the results of the readers in a `ProjectInput` with these four members.

- `metamodel`, the domain metamodel
- `events`, the event metamodel, or null
- `model`, the model, or null
- `stateMachines`, the state machines

Each state machine is a `StateMachineInput` with the result of its reader and the key of the object it runs on, or
null if that key is not known yet. The list holds every state machine of the project: the attached ones, the
attachable ones and the fragments. An attachable state machine and a fragment have no key. The verifier needs all of
them because it checks the names that `behaves after`, `ATTACH` and `EXCHANGE` use. The event metamodel is read with
the domain types as base types, by a `KmetaReader` as in [Reading a text](#reading-a-text).

`verifyLamp` verifies the four files as one project in which the state machine of the lamp runs on the object `lamp`,
and it reports the problems the verifier finds in the text of that state machine.

```kotlin
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.verification.ProjectInput
import io.karpfen.dsl.verification.ProjectVerifier
import io.karpfen.dsl.verification.StateMachineInput
import java.io.File

fun verifyLamp(folder: File) {
    val metamodel = KmetaReader().read(File(folder, "lamp.kmeta").readText())
    val domainTypes = metamodel.model?.types.orEmpty()
    val events = KmetaReader(domainTypes).read(File(folder, "lamp-events.kmeta").readText())
    val model = metamodel.model?.let { KmodelReader(it).read(File(folder, "lamp.kmodel").readText()) }
    val lamp = StateMachineInput(KstatesReader().read(File(folder, "lamp.kstates").readText()), "lamp")
    val findings = ProjectVerifier().verify(ProjectInput(metamodel, events, model, listOf(lamp)))
    report("lamp.kstates", findings.of(lamp))
}
```

`verify` returns a `ProjectFindings` with these members.

| Member | Problems |
|---|---|
| `of(stateMachine)` | Those in the text of that state machine, ordered by position. Because it finds the state machine by identity, it takes the same `StateMachineInput` that the `ProjectInput` holds. |
| `metamodel`, `events` | Those in the text of the domain metamodel and of the event metamodel. They concern `behaves after`. |
| `all`, `errors`, `warnings` | Those of all texts. |

A tool shows the diagnostics of the readers and those of the verifier together, because the verifier adds to the
diagnostics of the readers and never repeats them.

## Flattening a composition

`StateMachineFlattener.flatten(machine, available)` in the package `io.karpfen.dsl.composition` flattens the
composition that starts from `machine`. Compositions are described in
[2.3.12 Pages](reference/2.3-kstates.md#2312-pages). `available` holds the state machines that the function may use,
usually every state machine of the project. It uses only the fragments among them and finds them by name. The function
is pure: it changes neither its input nor anything else, and it throws for no problem of the texts.

The function returns a `FlatteningResult`. It is either a `Flattened` or a `NotFlattened`.

A `Flattened` holds the flat state machine in `machine`. The flat state machine has the header of `machine`, and no
container state is left to resolve, because each container state holds copies of the states of its fragment. A copy
is named with the name of its container state, a slash and its own name. `sources` lists the state machines of the
composition, the starting one first. `originOf(name)` and `origins` tell where each state was written, as a
`StateOrigin` with these members.

- `fragment`, the name of the fragment the state was written in, or null for the starting state machine
- `localName`, the name of the state in that text
- `containers`, the flattened names of the container states above it

A `NotFlattened` holds the `problems` that prevent the flattening. Each is a `FlatteningProblem` with the state machine
it stands in, the element it concerns and a `message`. These are the kinds of problems.

- `UnknownFragment`
- `NotAFragment`
- `FragmentCycle`
- `NameClash`
- `ConflictingMacros`
- `UnknownMacro`
- `MacroArgumentMismatch`, a call whose number of arguments does not fit the macro it names in another file of the
  composition

`flattenLamp` flattens the lamp state machine with the container state `self test`, which uses the bulb test fragment
of [2.3.12.2 Container states and DONE](reference/2.3-kstates.md#23122-container-states-and-done). The fragment is read
from the file `bulb test.frag.kstates`. For each state of the flat state machine the function prints its flattened
name, the fragment it was written in and its name there. If the composition cannot be flattened, it prints the
messages of the problems instead.

```kotlin
import io.karpfen.dsl.composition.FlatteningResult
import io.karpfen.dsl.composition.StateMachineFlattener
import io.karpfen.dsl.reader.kstates.KstatesReader
import java.io.File

fun flattenLamp(folder: File) {
    val lamp = KstatesReader().read(File(folder, "lamp.kstates").readText()).model ?: return
    val bulbTest = KstatesReader().read(File(folder, "bulb test.frag.kstates").readText()).model ?: return
    when (val result = StateMachineFlattener.flatten(lamp, listOf(bulbTest))) {
        is FlatteningResult.Flattened -> {
            for ((name, origin) in result.origins) {
                println("$name ${origin.fragment} ${origin.localName}")
            }
        }
        is FlatteningResult.NotFlattened -> result.problems.forEach { println(it.message) }
    }
}
```

For a state of the starting state machine the fragment is null.

```text
off null off
self test null self test
self test/powered bulb test powered
self test/passed bulb test passed
self test/failed bulb test failed
on null on
```

In the flat state machine, `isContainer` and `isFinal` stay as they were written. `InitialStates.startPath(machine)`
in the package `io.karpfen.dsl.states` returns the states a state machine starts in. `InitialStates.enteredBelow(state)`
returns the states that are entered below a container state when the container state is entered. Both put the
outermost state first and follow the rule of
[2.3.2.2 The initial state](reference/2.3-kstates.md#2322-the-initial-state).
`initialInnerState` of a state is the first state of that path below the state.

The verifier flattens each state machine of a project in the same way for the checks of
[2.4.6.5 Pages](reference/2.4-checks.md#2465-pages), and the runtime flattens a state machine before it runs it.

## Logging

The library logs through the SLF4J API and ships no logging backend. A program that uses the library picks a backend,
and without one it gets no log output.

| Logger | Level | What |
|---|---|---|
| Below `io.karpfen.dsl.reader` | error | A reader that failed through a defect, with its stack trace. |
| Below `io.karpfen.dsl.reader` | warn | A reader that failed on nesting deeper than its parser can follow. |

A reader reports the problems of a text only as diagnostics and never in the log. A converter reports them only in its
exception. When a reader fails, it reports the diagnostic `reader.failure` in addition to the log entry, and a
converter throws with that diagnostic.
