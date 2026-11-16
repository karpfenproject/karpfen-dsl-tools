# Open points

What is known to be missing, unfinished or undecided in karpfen-dsl-tools.

## Pages

- The throwing converter `KstatesDSLConverter` reads fragments, container states, final states and `DONE`, but it does
  not enforce their rules, such as a final state outside a fragment or `DONE` on a state that is no container. Only the
  reader reports them.
- Two macros of one name in two files of a composition count as the same macro when their parameters, their result
  type and their code without the surrounding blank space are equal. Code that differs only in its indentation inside a
  line, or in its comments, counts as a different definition.
