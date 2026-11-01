---
navigation:
  title: "Language"
  parent: /advanced/index.md
  position: 90
  keywords: ["sfml", "language"]
categories: ["sfm", "language"]
updated: "2026-10-31"
---

# Language

SFML combines scheduled automation with typed data. Use variables to calculate a batch, keep counters on the program disk, or build a reusable function that decides what a trigger should do.

Use the **Text** editor for the language features in this chapter. See [Editors](/start/editors.md) for choosing an editor. Every `sfml` code block here is a complete program; `text` blocks show fragments.

Read these pages in order, or jump to the feature you need:

- [Variables and persistent state](variables.md): `LET`, `SET`, fixed types, scope and disk state.
- [Arrays and structs](collections.md): fixed shapes, member access and independent values.
- [Strings](strings.md): text values, escaping, concatenation and comparisons.
- [Expressions](expressions.md): checked arithmetic, boolean conditions and computed quantities.
- [Bitwise operators](bitwise.md): masks, shifts and precedence.
- [Loops](loops.md): counted ranges, array iteration and bounded `WHILE`.
- [Functions](functions.md): typed player functions and default functions.

The program order is optional NAME, top-level STATE/LET declarations, functions,
then triggers. Use braces for statement blocks; DO/THEN/ELSE/END remains supported.
Semicolons are optional. `--` starts a comment. Braces also construct struct
values, depending on context. Plain variable names and `$names` are equivalent.
Literal labels always need quotes; an unquoted name is a variable reference.

```sfml
state batches = 0;

void moveBatch(label_ref source, label_ref destination) {
  transfer(source, destination, 8) into result;
  if (result.moved > 0) {
    batches = batches + 1;
    wait(5);
    print("Batches completed", batches);
  }
}

every 20 ticks {
  moveBatch(label("source"), label("dest"));
}
```

`void` means this procedure performs statements without returning a value.
`result.moved` reports actual movement, so an empty source/full destination does
not count as a successful batch. Earlier transfers and STATE writes are not
undone if later work fails. WAIT suspends this trigger only; it never sleeps the
server thread. See [scheduling](/automation/scheduling.md) for the full WAIT rules.

Data calculations run within shared work and size limits. A runtime fault stops the invocation and pauses execution. Earlier completed assignments and transfers remain completed; a trigger is not a rollback transaction.

Next: [Variables and persistent state](variables.md). Related: [Typed queries](/automation/queries.md) and the [function reference](/reference/functions.md).
