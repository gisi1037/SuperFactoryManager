---
navigation:
  title: "Variables and state"
  parent: /language/index.md
  position: 70
  keywords: ["sfml", "variables", "state", "let", "set"]
categories: ["sfm", "language"]
updated: "2026-10-28"
---

# Variables and persistent state

`LET` declares and initializes a mutable variable. Inside a trigger/function it is local; at program scope it is shared by that manager's triggers. `$name = value` assigns an existing variable; `SET $name = value` remains accepted. Direct assignment also supports paths such as `$jobs[0].amount = 8`. Every variable reference starts with `$`.

```sfml
NAME "Computed local batch"
EVERY 20 TICKS DO
  LET $batch = 8
  $batch = $batch * 2 + 4
  IF ($batch > 0) THEN
    INPUT ($batch) item:minecraft:iron_ingot FROM source
    OUTPUT TO dest
  END
END
```

The input allowance is twenty. The compiler infers `$batch` as `INTEGER` from its initializer, so later assignments must also be integers. There is no declaration such as `LET $batch: INTEGER`; explicit type annotations belong to function signatures.

The scalar types are signed 64-bit `INTEGER`, `BOOLEAN` and immutable `STRING`. `TRUE` is a boolean, `1` is an integer, and `"1"` is a string. They do not convert automatically. Arrays, structs and typed query values also have fixed types. See [Collections](collections.md) and [Typed queries](/automation/queries.md).

Names are case-sensitive: `$Batch` and `$batch` differ. Names start with an ASCII letter or underscore and continue with letters, digits or underscores. Keywords are case-insensitive. Newlines help readability, but the grammar determines where statements end.

## Local scope

A local is fresh for every trigger invocation. It becomes visible after its initializer and remains visible until the end of its enclosing `DO`, `THEN` or `ELSE` block. An inner block may `SET` an outer variable. A variable declared inside a branch is unavailable after that branch.

Unknown names, use before declaration, self-reference in an initializer and shadowing a visible name fail compilation. Separate triggers and sibling branches may reuse a local name. `FORGET` clears registered inputs; it does not clear variables.

## Program-wide memory

Place LET after NAME and before functions/triggers to share a value across scheduled runs:

```sfml
name "In-memory counter"
let $runs = 0
let $source = label("inputs")
every 20 ticks do
  $runs = $runs + 1
  print("runs", $runs)
end
```

These values initialize once at first active execution, before checking due triggers. Initializers may read earlier globals and STATE or call pure/query functions. Queries do not run while compiling or saving. Failed initialization publishes no partial globals; resume retries it. Functions must receive globals through explicit arguments.

Globals stay in RAM. Pause/resume and formatting/comment-only saves preserve them; executable edits, recompile, disk removal/replacement, chunk unload, world reload and a successful State reset discard them. They are independent between managers, even for identical programs. Handles and exact item/contents snapshots may live here; snapshots do not automatically refresh. CONST remains planned.

The store permits 256 globals, 4,096 retained cells, 16,384 characters and 256 KiB of opaque handle/key data. Exact NBT includes conservative retained array-capacity accounting. Failed assignments preserve the old value. Use STATE when values must survive reloads.

## Disk state

Declare `STATE` at the top level, after `NAME` and before functions and triggers. Defaults must be pure constant expressions: they cannot read variables, call player functions or observe the world. Pure default functions such as `CLAMP` may be used.

```sfml
NAME "Persistent invocation counter"
STATE $runs = 0
STATE $config = { batch: 8, reserve: 16 }

EVERY 20 TICKS DO
  SET $runs = $runs + 1
  INPUT ($config.batch) RETAIN ($config.reserve) item:minecraft:iron_ingot FROM source
  OUTPUT TO dest
END
```

State names are visible to all triggers and cannot be shadowed by their locals. Completed writes are visible to later statements and other due triggers in source order. Player functions receive state through explicit arguments and return values; they cannot capture it.

State belongs to the program disk. It survives pause/resume, moving the disk between managers and ordinary world save/reload. Copying a disk creates an independent snapshot. Updates are kept in memory and saved at persistence or disk movement boundaries; each `SET` does not write the disk immediately. State and external inventories do not share an atomic save transaction, so a counter cannot guarantee exactly-once delivery after a process crash.

Only integers, booleans, strings and collections made entirely from those values persist. `LABEL_REF`, `RESOURCE_SELECTOR`, `ITEM_KEY`, `ITEM_CONTENTS` and `ITEM_LOCATIONS` values cannot go in `STATE`, including inside arrays or structs. Keep handles and query snapshots in local or program-wide LET bindings.

## Editing state declarations

State identity is its case-sensitive name and complete type/shape.

| Edit | Result when activated |
| --- | --- |
| Change formatting, comments or layout | Keep existing values. |
| Change a default without changing the type | Keep the existing value; use the new default for new state or a reset. |
| Add a declaration | Initialize it from its default. |
| Remove a declaration | Drop its value. Adding that name later starts from its default. |
| Rename a declaration | Remove the old name and initialize the new one, with a warning. |
| Change type, array length or struct shape | Reject activation and require an explicit state reset. |

Invalid source or incompatible state is not executed. Preserve a copy of important source before restructuring state. Use the manager's **State** control to reset variables to current defaults. This separate reset keeps source, labels, layout and recovery records, pauses the manager, and leaves it paused. Inspect the result before choosing **Run**. The general **Reset** control clears program/labels and is a different operation.

## Size and work limits

There are at most 256 persistent declarations, 256 local declarations per trigger, and 4,096 expanded persistent value cells, counting container nodes. Collections have at most 256 elements or fields and data nesting is limited to 32. The whole disk also has a 30,000-byte uncompressed NBT limit shared by source, labels, state, layout and recovery data.

Due triggers share 16,384 expression-work units and 16,384 constructed/copied collection cells per manager invocation. Failed assignments publish no replacement value; earlier completed statements remain completed. These bounds limit work and storage, rather than promising a fixed execution time.

Next: [Arrays and structs](collections.md). Related: [Functions](functions.md) and [Expressions](expressions.md).


For a loaded immutable reference disk, **Program > Reset state** in the native
Factory Manager console resets STATE from the loaded schema and leaves the
reference/labels unchanged. It pauses execution and clears globals/redstone edge
history. Pending loading, unavailable bindings and transfer recovery reject reset
without discarding saved values. Use this only when you intend to lose STATE.
