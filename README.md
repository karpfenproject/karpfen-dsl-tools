![Logo](https://avatars.githubusercontent.com/u/264260364?s=200&v=4)

# karpfen-dsl-tools

karpfen-dsl-tools is the Kotlin library of the three karpfen modeling languages: KMeta for metamodels, KModel for models
and KStates for state machines. It reads the texts into models, reports the problems it finds in them, checks the state
machines of a project against its metamodels and model, and writes models back as text. The karpfen runtime and the
karpfen IDE are built on it.

## Documentation

The reference of the languages and of the library is chapter 2 of the karpfen reference, in `docs/reference`.

| Chapter | Content |
|---|---|
| [2 Modeling languages](docs/reference/2-modeling-languages.md) | The three languages, their files and lexical rules, and the lamp example. |
| [2.1 KMeta](docs/reference/2.1-kmeta.md) | Types, properties, relations and event definitions. |
| [2.2 KModel](docs/reference/2.2-kmodel.md) | Objects, keys, values, embedded objects and references. |
| [2.3 KStates](docs/reference/2.3-kstates.md) | States, actions, transitions, events, macros and the optional features. |
| [2.4 Checks](docs/reference/2.4-checks.md) | Every problem the readers and the verifier report, with its code. |
| [2.5 Using the library](docs/reference/2.5-library.md) | Reading, checking, printing and building models from Kotlin code. |

The table of all chapters, with the runtime and the IDE, is in the
[README of the reference in karpfen-IDE](https://github.com/karpfenproject/karpfen-IDE/blob/main/docs/reference/README.md).
The grammars are in `src/main/antlr`, and the KDoc on the declarations describes the API in detail.

## Building

The build needs JDK 21 and uses the Gradle wrapper:

```bash
./gradlew build
```

The build generates the lexers and parsers from the grammars into `build/generated-src/antlr`; they are not checked in.
`build` runs the tests and two more checks. The task `compatibilityTest` compares the converters with those of version
1.0, which it compiles from the history of this repository, so a shallow clone has to fetch the full history first
(`git fetch --unshallow`). The task `checkPublishedDependencies` checks the dependencies that the publication declares.
The tests also read every example of `docs/reference` with the readers of the library.

The library can also be built as a subproject or an included build of another Gradle build, as karpfen-runtime and the
karpfen IDE do. There `check` runs the tests only and needs neither git nor the history of this repository.

## Publishing

The library is published as `io.karpfen:karpfen-dsl-tools`, at present in version `2.0.0-SNAPSHOT`, to GitHub Packages
at `https://maven.pkg.github.com/<owner>/<repository>`. The repository comes from the environment variable
`GITHUB_REPOSITORY` and is `karpfenproject/karpfen-dsl-tools` by default. `./gradlew publish` publishes the jar, the
sources jar and the POM. It reads the credentials from the Gradle properties `GitHubPackagesUsername` and
`GitHubPackagesPassword`, for example from the environment variables `ORG_GRADLE_PROJECT_GitHubPackagesUsername` and
`ORG_GRADLE_PROJECT_GitHubPackagesPassword`. Consumers read the published versions with the same credentials.
`./gradlew publishToMavenLocal` publishes the same files to the local Maven repository in `~/.m2`.

The API dependencies are the ANTLR runtime 4.13.1, because the generated lexers and parsers are public, `org.json`,
because `JsonToModelConverter` takes JSON objects, and the SLF4J API, because `LoggingSyntaxErrorListener` takes a
logger.

Version 2.0.0 moved every package below `io.karpfen.dsl`: a package `P` of version 1.0 is now `io.karpfen.dsl.P`, and
the generated packages `kmeta`, `kmodel` and `kstates` are now `io.karpfen.dsl.grammar.kmeta`, `.kmodel` and `.kstates`.
Apart from the packages and the console output, the behaviour is the same, so moving to version 2.0.0 means changing the
imports.

## License

Licensed under the Apache License, Version 2.0. See `LICENSE` for details.

Parts of this project have been developed with the help of generative AI.
