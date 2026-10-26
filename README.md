![Logo](https://avatars.githubusercontent.com/u/264260364?s=200&v=4)

# karpfen DSL TOOLS

This repository contains tools and libraries for working with the *karpfen ecosystem* domain-specific languages (karpfen DSLs).
The language tools are based on ANTLR v4 grammars and the ANTLR runtime.
Each DSL comes with its own runtime representation (dynamic object models), parsers, and a functional language construction API.

As of right now, the following languages and language tools are available:
- **KMeta**: A DSL for defining data metamodels. The KMeta grammar is documented in [the KMeta grammar guide](guides/kmeta_grammar_guide.md).
- **KModel**: A DSL for defining data models that conform to KMeta metamodels. The KModel grammar is documented in [the KModel grammar guide](guides/kmodel_grammar_guide.md).
- **KStates**: A DSL for defining hierarchical state machines. The KStates grammar is documented in [the KStates grammar guide](guides/kstates_grammar_guide.md).

## About the karpfen DSLs

This repository serves as the functional base for all remaining karpfen tools, specifically the dynamic statechart interpreter and the karpfen IDE.
This repository is included in the application-level karpfen tools provided.
Thus, directly using this module is not necessary for most users. However, users interested in the inner workings of the karpfen DSLs,
or those looking to extend the functionality of the karpfen ecosystem, may find it useful to explore this repository.

## Where to find what

All code lives below the package `io.karpfen.dsl`; the KDoc on the declarations explains the details.

| Package | Content |
| --- | --- |
| `io.karpfen.dsl.grammar.kmeta`, `.kmodel`, `.kstates` | Lexers, parsers and visitors that the build generates from the grammars in `src/main/antlr` |
| `io.karpfen.dsl.parser` | File parsers that turn text into a syntax-level form (`KmetaFile`, `KmodelFile`, `KstatesFile`), `LoggingSyntaxErrorListener`, and the one home of each language rule that the converters, readers and printers share, such as `StringLiteralSyntax`, `ConditionClauses` and `KmodelSemantics` |
| `io.karpfen.dsl.dsl.textual` | The converters `KmetaDSLConverter`, `KmodelDSLConverter`, `KstatesDSLConverter` and `JsonToModelConverter`, the usual entry points |
| `io.karpfen.dsl.reader`, `.kmeta`, `.kmodel`, `.kstates` | The readers `KmetaReader`, `KmodelReader` and `KstatesReader`, which read text without throwing, behind the interface `TextReader`, and their `ParseResult` |
| `io.karpfen.dsl.diagnostics` | `Diagnostic`, the problem a reader reports, with its `Severity` and its stable `DiagnosticCode` |
| `io.karpfen.dsl.source` | `SourcePosition` and `SourceRange`, `SourceText`, which turns offsets into lines and columns, and `SourceMap`, which locates model elements in their text |
| `io.karpfen.dsl.printer`, `.kmeta`, `.kmodel`, `.kstates` | The printers `KmetaPrinter`, `KmodelPrinter` and `KstatesPrinter`, which write metamodels, models and state machines back as text |
| `io.karpfen.dsl.dsl.functional` | `MetamodelBuilder` and `ModelBuilder` for building metamodels and models in code |
| `io.karpfen.dsl.meta` | The metamodel: `Metamodel`, class types and their properties |
| `io.karpfen.dsl.instance` | The model: `Model`, `DataObject` and its property and relation values |
| `io.karpfen.dsl.states` | The state machine: `StateMachine`, states, transitions, and the sub-packages for actions, conditions and macros |
| `io.karpfen.dsl.examples` | Two small programs that show the syntax-level parsers and print what they parsed |

The tests mirror these packages; the example documents they read are in `example/`, and the printer and reader tests
also read the karpfen runtime examples copied to `src/test/resources/examples` (their origin is in the `NOTICE.md` there),
all of them through `ExampleCatalog`. It lives with the other fixtures the tests share, such as `TextLayout` and
`Mutator`, in the test package `io.karpfen.dsl.fixtures`. The comparison with version 1.0 has a source set of its own,
`src/compatibilityTest`.
Every Kotlin source, every Gradle script and the grammars start with the Apache 2.0 license header, which
`LicenseHeaderTest` requires; documentation lives in KDoc on declarations, and `DocumentationStyleTest` rejects any
other comment in the Kotlin sources and the Gradle scripts. Both tests are in `io.karpfen.dsl.conventions`.

## Logging

The library logs through the SLF4J API and ships no logging backend; the application chooses one.
The converters log the types and objects they convert at debug level.
Syntax errors are logged as warnings by `LoggingSyntaxErrorListener` while the parsers recover and continue,
nothing is printed to the console. The file parsers accept another ANTLR error listener in their constructor.
A reader that fails logs the failure besides reporting it, see `TextReader`.

## Reading text with diagnostics

The converters stop at the first problem, which suits documents that are finished. For text that is still being
written, the readers never throw: `read` returns a `ParseResult` with the model, the diagnostics and a source map.
Each diagnostic marks the offending part of the text with a `SourceRange` of offsets, lines and columns, and carries a
severity, a stable `DiagnosticCode` and a message. The reader keeps going after a problem, so that a text in the middle
of an edit still yields a partial model; the KDoc of each reader says what it reports and what a partial model leaves
out. A reader reads the text the way its converter reads it, and leaves out the checks that a syntax error may have
misled. Without errors, the model is the one the converter builds. The `SourceMap` is a side table keyed by identity that
locates the elements of the model at their construct, their name and the code of their `EVAL` bodies; its KDoc lists
the elements it covers. The model classes know nothing of their text.

The reader tests check the source maps on every example, report the problems of broken texts where they occur, and
read thousands of mutated examples, every example in other layouts such as other line breaks and indentation, and
every prefix of an example without throwing, with proper ranges and in agreement with the converters; `TextLayout` and
`Mutator` make these texts for the reader tests, the printer tests and the comparison with version 1.0 alike.

## Printing

The printers turn a metamodel, a model or a state machine back into text: a whole document, or a fragment for a single
element such as a type, a rule, an object, a state, a transition or a macro, laid out to replace that element's text
in an existing document. The `convertTo...String` functions of the converters delegate to them. All output uses one
canonical layout, re-indents `EVAL` code while it keeps its tokens, its comments and the indentation of the lines
after the first relative to each other, and drops the comments outside `EVAL` code; the KDoc of the printers describes
the layout, and the printers reject what their language cannot read back, such as a value no string literal carries,
an object embedded twice or `EVAL` code whose braces do not balance. The printer tests parse every example, print it
and parse the text again; they also print every model that a reader reads without errors from an example in another
layout or from a mutated variant of it, and check for every short string literal that the value it reads back can be
written again.

## Building

The build needs JDK 21 and uses the Gradle wrapper:

```
./gradlew build
```

The lexers and parsers are not checked in: the build generates them from the grammars into
`build/generated-src/antlr`, and the package of each grammar comes from the `@header` in its `.g4` file. The build is
configuration-cache compatible and can also be included as a subproject or as an included build of another Gradle
build, as karpfen-runtime and the karpfen IDE do; there `check` runs the tests only and needs neither git nor the
history of this repository. The workflow in `.github/workflows/build.yml` builds and tests every push and pull request
on JDK 21 and configures publishing without publishing. It also builds, tests and runs the library as a subproject of
a build shaped like that of karpfen-runtime, whose root project applies the Kotlin plugin in version 2.3.0 and whose
sibling project applies it without a version, so that a change that would break karpfen-runtime fails there. The doc
blocks in `build.gradle` and in the scripts it applies from `gradle/` explain how the build is put together.

Two more checks of what this repository delivers are part of `check`, and therefore of `build`, in its own build only.
The task `compatibilityTest` holds the converters to the behaviour of version 1.0: it converts the examples and
thousands of variants of them with the converters of version 1.0 and with the current ones, and requires the same
models and the same exceptions. The build compiles version 1.0 from the history of this repository, so a shallow clone
has to fetch the full history first (`git fetch --unshallow`). The task `checkPublishedDependencies` checks that the
POM and the module metadata of the publication declare the API dependencies to consumers and not the ANTLR tool.

## Using the library

The library is published as `io.karpfen:karpfen-dsl-tools`, currently in version `2.0.0-SNAPSHOT`.
Its API dependencies are the ANTLR runtime 4.13.1, because the generated lexers and parsers are public, `org.json`,
because `JsonToModelConverter` accepts JSON objects, and the SLF4J API, because `LoggingSyntaxErrorListener` takes a
logger; the ANTLR tool stays inside the build.

Versions are published with `./gradlew publish`: the jar, the sources jar and the POM go to the repository
`GitHubPackages` (`https://maven.pkg.github.com/<owner>/<repository>`), named by the environment variable
`GITHUB_REPOSITORY` (default `karpfenproject/karpfen-dsl-tools`). The credentials are the Gradle properties
`GitHubPackagesUsername` and `GitHubPackagesPassword`, for example as the environment variables
`ORG_GRADLE_PROJECT_GitHubPackagesUsername` and `ORG_GRADLE_PROJECT_GitHubPackagesPassword`, set from `GITHUB_ACTOR`
and `GITHUB_TOKEN` in a GitHub workflow. Consumers read published versions from there with the same credentials.

`./gradlew publishToMavenLocal` publishes the same files to the local Maven repository (`~/.m2`) for a build on the
same machine that reads it. The karpfen IDE does not: it builds with this repository as a git submodule, or reads a
published version from GitHub Packages.

Version 2.0.0 moved every package below `io.karpfen.dsl`: a package `P` of version 1.0 is now `io.karpfen.dsl.P`,
and the generated packages `kmeta`, `kmodel` and `kstates` are now `io.karpfen.dsl.grammar.kmeta`, `.kmodel` and `.kstates`.
Apart from the packages and the console output, the behaviour is the same, so migrating means changing imports.

## License

Licensed under the Apache License, Version 2.0. See `LICENSE` for details.

---

*Parts of this project have been developed with the help of generative AI*.
