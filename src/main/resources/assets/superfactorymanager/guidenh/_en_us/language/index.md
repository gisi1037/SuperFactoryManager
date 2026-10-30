---
navigation:
  title: "Language"
  parent: /advanced/index.md
  position: 90
  keywords: ["sfml", "language"]
categories: ["sfm", "language"]
updated: "2026-10-30"
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

The program order is an optional `NAME`, then top-level `STATE` and `LET` declarations, then function definitions, then triggers. Top-level LET keeps shared values in manager memory; STATE persists values on the disk. Statements use words such as `DO`, `THEN`, `ELSE` and `END` to delimit blocks. `--` starts a comment. Braces construct struct values; they do not delimit statement blocks.

```sfml
NAME "Calculated batch"
STATE $runs = 0

EVERY 20 TICKS DO
  LET $batch = 8
  SET $runs = $runs + 1
  INPUT ($batch * 2) item:minecraft:iron_ingot FROM source
  OUTPUT TO dest
END
```

This program increments a disk counter and offers up to sixteen iron ingots for transfer each time the trigger runs. The counter counts executions reaching `SET`, even when nothing can move.

Data calculations run within shared work and size limits. A runtime fault stops the invocation and pauses execution. Earlier completed assignments and transfers remain completed; a trigger is not a rollback transaction.

Next: [Variables and persistent state](variables.md). Related: [Typed queries](/automation/queries.md) and the [function reference](/reference/functions.md).
