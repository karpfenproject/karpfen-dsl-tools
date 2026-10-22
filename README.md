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
| `io.karpfen.dsl.parser` | File parsers that turn text into a syntax-level form (`KmetaFile`, `KmodelFile`, `KstatesFile`), and `LoggingSyntaxErrorListener` |
| `io.karpfen.dsl.dsl.textual` | The converters `KmetaDSLConverter`, `KmodelDSLConverter`, `KstatesDSLConverter` and `JsonToModelConverter`, the usual entry points |
| `io.karpfen.dsl.dsl.functional` | `MetamodelBuilder` and `ModelBuilder` for building metamodels and models in code |
| `io.karpfen.dsl.meta` | The metamodel: `Metamodel`, class types and their properties |
| `io.karpfen.dsl.instance` | The model: `Model`, `DataObject` and its property and relation values |
| `io.karpfen.dsl.states` | The state machine: `StateMachine`, states, transitions, and the sub-packages for actions, conditions and macros |
| `io.karpfen.dsl.examples` | Two small programs that show the syntax-level parsers |

The tests mirror these packages; the example documents they read are in `example/`.

## Logging

The library logs through the SLF4J API and ships no logging backend; the application chooses one.
The converters log the types and objects they convert at debug level.
Syntax errors are logged as warnings by `LoggingSyntaxErrorListener` while the parsers recover and continue,
nothing is printed to the console. The file parsers accept another ANTLR error listener in their constructor.

## Building

The build needs JDK 21 and uses the Gradle wrapper:

```
./gradlew build
```

The lexers and parsers are not checked in: the build generates them from the grammars into `build/generated-src/antlr`,
and the package of each grammar comes from the `@header` in its `.g4` file.
The build is configuration-cache compatible and can also be included as a subproject or as a composite build of another Gradle build.
The workflow in `.github/workflows/build.yml` builds and tests every push and pull request on JDK 21.

## Using the library

The library is published as `io.karpfen:karpfen-dsl-tools`, currently in version `2.0.0-SNAPSHOT`.
Its API dependencies are the ANTLR runtime 4.13.1, because the generated lexers and parsers are public, and `org.json`,
because `JsonToModelConverter` accepts JSON objects; the ANTLR tool stays inside the build.

To publish the jar, the sources jar and the POM to the local Maven repository (`~/.m2`):

```
./gradlew publishToMavenLocal
```

The build also defines the repository `GitHubPackages` (`https://maven.pkg.github.com/<owner>/<repository>`).
It takes the repository from the environment variable `GITHUB_REPOSITORY` (default `karpfenproject/karpfen-dsl-tools`)
and the credentials from `GITHUB_ACTOR` and `GITHUB_TOKEN`; `./gradlew publish` publishes there.

Version 2.0.0 moved every package below `io.karpfen.dsl`: a package `P` of version 1.0 is now `io.karpfen.dsl.P`,
and the generated packages `kmeta`, `kmodel` and `kstates` are now `io.karpfen.dsl.grammar.kmeta`, `.kmodel` and `.kstates`.
Apart from the packages and the console output, the behaviour is the same, so migrating means changing imports.

## License

Licensed under the Apache License, Version 2.0. See `LICENSE` for details.

---

*Parts of this project have been developed with the help of generative AI*.
