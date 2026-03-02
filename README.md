# karpfen DSL TOOLS

This repository contains tools and libraries for working with the *karpfen ecosystem* domain-specific languages (karpfen DSLs).
The langauge tools are based on ANTLR v4 grammars and the ANTLR runtime.
Each DSL comes with its own runtime representation (dynamic object models), parsers, and a functional language construction API.

As of right now, the following languages and language tools are available:
- **kMeta**: A DSL for defining data metamodels. The KMeta grammar is documented in `guides/kmeta_grammar_guide.md` [goto](guides/kmeta_grammar_guide.md).
- **kModel**: A DSL for defining data models that conform to KMeta metamodels. The kModel grammar is documented in `guides/kmodel_grammar_guide.md` [goto](guides/kmodel_grammar_guide.md).

## About the karpfen DSLs

This repository severs as the functional bases for all remaining karpfen tools, specifically the dynamic statechart interpreter.
This repository is included in the application-level karpfen tools provided. 
Thus, directly using this module is not necessary for most users. However, users interested in the inner workings of the karpfen DSLs, 
or those looking to extend the functionality of the karpfen ecosystem, may find it useful to explore this repository.
