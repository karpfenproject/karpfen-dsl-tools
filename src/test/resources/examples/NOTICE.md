# Example files: origin and license

The `.kmeta`, `.kmodel` and `.kstates` files in `karpfen-runtime/` are unmodified copies of the examples of the
karpfen runtime. The printer tests in `src/test/kotlin/io/karpfen/dsl/printer`, the reader tests in
`src/test/kotlin/io/karpfen/dsl/reader` and the comparison with version 1.0 in `src/compatibilityTest` read them,
together with the examples of this repository in `example/`, through `ExampleCatalog`.

| Folder | Origin repository | Commit | Copied paths |
|---|---|---|---|
| `karpfen-runtime/` | [karpfenproject/karpfen-runtime](https://github.com/karpfenproject/karpfen-runtime) | `d4c2059` (d4c20591689fb301f2af06f7f2965c86abf83e83) | `examples/single_robot_example`, `examples/delivery_drone_example`, `examples/autonomous_car_example`, `examples/drone_swarm_example` (only `*.kmeta`, `*.kmodel`, `*.kstates`) |

The files were taken from the identical copies in the test fixtures of the karpfen IDE.

Copyright 2026 Karl Kegel. The karpfen runtime, and therefore these files, is licensed under the Apache License,
Version 2.0, the same license as this repository (see `LICENSE` in the repository root). You may obtain a copy of
the license at <http://www.apache.org/licenses/LICENSE-2.0>.

When the upstream examples change, copy them again and update the commit above.
