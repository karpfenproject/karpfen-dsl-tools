# Open points

What is known to be missing, unfinished or undecided in karpfen-dsl-tools.

## Pages

- A container state goes down to the initial state of its fragment only when that state stands at the top level of the
  fragment. `State.initialInnerState` takes the inner state marked `INITIAL`, or else the first one, as the runtime does
  for every level. If the `INITIAL` state of a fragment stands inside another state of the fragment, the container
  enters the first state at the top level instead, and no check reports it. Either a reader check that the initial state
  of a fragment stands at its top level, or a way down to a nested initial state in the library and the runtime, is
  undecided.
- The throwing converter `KstatesDSLConverter` reads fragments, container states, final states and `DONE`, but it does
  not enforce their rules, such as a final state outside a fragment or `DONE` on a state that is no container. Only the
  reader reports them.
- Two macros of one name in two files of a composition count as the same macro when their parameters, their result
  type and their code without the surrounding blank space are equal. Code that differs only in its indentation inside a
  line, or in its comments, counts as a different definition.
