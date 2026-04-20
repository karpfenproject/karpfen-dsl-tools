![Logo](https://avatars.githubusercontent.com/u/264260364?s=200&v=4)

# karpfen DSL TOOLS

This repository contains tools and libraries for working with the *karpfen ecosystem* domain-specific languages (karpfen DSLs).
The language tools are based on ANTLR v4 grammars and the ANTLR runtime.
Each DSL comes with its own runtime representation (dynamic object models), parsers, and a functional language construction API.

As of right now, the following languages and language tools are available:
- **KMeta**: A DSL for defining data metamodels. The KMeta grammar is documented in [guides/kmeta_grammar_guide.md](guides/kmeta_grammar_guide.md).
- **KModel**: A DSL for defining data models that conform to KMeta metamodels. The KModel grammar is documented in [guides/kmodel_grammar_guide.md](guides/kmodel_grammar_guide.md).
- **KStates**: A DSL for defining hierarchical state machines. The KStates grammar is documented in [guides/kstates_grammar_guide.md](guides/kstates_grammar_guide.md).

## About the karpfen DSLs

This repository serves as the functional base for all remaining karpfen tools, specifically the dynamic statechart interpreter.
This repository is included in the application-level karpfen tools provided. 
Thus, directly using this module is not necessary for most users. However, users interested in the inner workings of the karpfen DSLs, 
or those looking to extend the functionality of the karpfen ecosystem, may find it useful to explore this repository.

## License

Licensed under the Apache License, Version 2.0. See `LICENSE` for details.

---

*Parts of this poject have been developed with the help of generative AI*.