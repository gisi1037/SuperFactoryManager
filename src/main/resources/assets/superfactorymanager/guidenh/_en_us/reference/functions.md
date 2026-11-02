---
navigation:
  title: "Default functions"
  position: 100
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["api", "builtin", "min", "max", "len", "total", "stock", "shortage"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Default functions

Look up a callable name here, then follow its topic link for setup and examples.
`INTEGER` is signed 64-bit,
`BOOLEAN` is true/false, and `STRING` is text. Built-in names ignore case.
Exact-name player functions take precedence over ordinary built-in calls; avoid
reusing built-in names so a program remains easy to read. LABEL, ITEM, ORE and
TRANSFER are reserved boundaries.

- [Choose labels and resources](#constructors-and-selections): LABEL, ITEM, ORE and FLUID.
- [Calculate an amount or length](#arithmetic-and-size): MIN, MAX, CLAMP, ABS and LEN.
- [Observe items](#inventory-observations) or [fluids](#fluid-observations).
- [Calculate stock and shortages](#stock-calculations): TOTAL, STOCK and SHORTAGE.
- [Build or search arrays](#arrays-and-sequences), or [read captured NBT](#captured-item-nbt).
- [Read signals](#signal-observations), [wait between steps](#wait) or [set a ghost circuit](#ghost-circuits).

## Constructors and selections

| Call | Returns | Meaning |
| --- | --- | --- |
| `LABEL(name)` | LABEL_REF | Select the exact saved label group from a STRING expression. |
| `FLUID("water")` | RESOURCE_SELECTOR | Match a fluid registry name; `FLUID("*")` matches all fluids. |
| `ITEM("namespace:name")` | RESOURCE_SELECTOR | Match every metadata/NBT variant of one exact item ID; `ITEM("*")` matches all items. |
| `ORE("ingotIron")` | RESOURCE_SELECTOR | Match one exact ore-dictionary name, including its registered alternatives. |
| `$label.side("up")` | LABEL_REF | Restrict observed/used faces. |
| `$label.slots(0, 8)` | LABEL_REF | Select an inclusive logical slot range. |

LABEL accepts a STRING expression, including `label("chest" + index)`.
ITEM, ORE and FLUID strings still must be source literals. A label is a selection, an ITEM/ORE value
is a filter, and an ITEM_KEY from `.contents()` is one exact registry/metadata/NBT
identity. These are different types. Unknown ore names match no item without
registering a new ore category. See [selectors](/automation/selectors.md) and
[side/slot selection](/automation/sides-slots.md) for accepted spellings.

## Arithmetic and size

| Call | Result |
| --- | --- |
| `MIN(a, b)` | Smaller of two integers. |
| `MAX(a, b)` | Larger of two integers. |
| `CLAMP(value, low, high)` | Value limited to inclusive integer bounds; reversed bounds fault. |
| `ABS(value)` | Absolute integer value; the minimum signed value faults because its positive counterpart cannot fit. |
| `LEN(array)` | Number of outer fixed or dynamic array entries. |
| `LEN(string)` | Unicode code-point count, not UTF-8 bytes or displayed grapheme clusters. |
| `LEN(contents)` / `LEN(locations)` | Actual item/fluid snapshot or location sequence length, including zero. |

## Inventory observations

| Call | Returns | Meaning |
| --- | --- | --- |
| `$label.available()` | BOOLEAN | Whether the whole selected loaded inventory view is supported and accessible for observation. |
| `$label.available($selector)` | BOOLEAN | Availability with an explicit item filter. |
| `$label.empty()` | BOOLEAN | Whether an available selected view contains no visible items. |
| `$label.empty($selector)` | BOOLEAN | Whether no visible items match the filter. |
| `$label.count($selector)` | INTEGER | Visible matching item count. |
| `$label.contents()` | ITEM_CONTENTS | Immutable exact groups and physical origins for all visible items. |
| `$label.contents($selector)` | ITEM_CONTENTS | The same snapshot restricted by an ITEM/ORE filter. |

Visibility is separate from extraction permission or insertion capacity. There
is no current `.full()`, `.canInsert()` or `.canExtract()` method. An unavailable
view does not prove emptiness; use the [query contract](/automation/queries.md).
Snapshots are observations rather than reservations.

## Fluid observations

| Call | Returns | Meaning |
| --- | --- | --- |
| `$label.favailable([selector])` | BOOLEAN | Whether the complete selected tank group can be observed. |
| `$label.fempty([selector])` | BOOLEAN | Whether an available group has no matching fluid. |
| `$label.fcount([selector])` | INTEGER | Matching visible amount in mB; unavailable groups fault. |
| `$label.fcontents([selector])` | FLUID_CONTENTS | Immutable nonempty tank rows, including exact fluid/NBT keys. |

Square brackets here mean an optional argument. Omit the selector for all fluids,
or pass `FLUID("water")`. **1000 mB = one bucket.** Missing groups make
favailable/fempty false. These methods accept fluid selectors, not ITEM/ORE.
See [fluid methods and TRANSFERF](/automation/fluid-methods.md).

## Arrays and sequences

| Call | Result |
| --- | --- |
| `ARRAY("integer")` | Empty typed dynamic array; other supported type strings work too. |
| `ARRAY(sequence)` | Explicit dynamic-array copy of a fixed/dynamic array or native sequence. |
| `PUSH(REF values, value)` | Append one element. |
| `EXTEND(REF values, sequence)` | Append same-type elements. |
| `INSERT(REF values, index, value)` | Insert at an index from zero through LEN. |
| `POP(REF values)` | Remove and return the last element. |
| `REMOVEAT(REF values, index)` | Remove and return one indexed element. |
| `CLEAR(REF values)` | Remove all elements. |
| `PEEK(sequence)` | Read the last element. |
| `CONTAINS(sequence, value)` | Test exact typed membership. |
| `INDEXOF(sequence, value)` | First zero-based match, or -1. |

PUSH/EXTEND/INSERT/CLEAR return no value. Mutations require an explicit writable
dynamic-array REF; fixed arrays and native snapshots stay read-only in size.
PEEK/CONTAINS/INDEXOF accept fixed/dynamic arrays and native sequences. Empty
POP/PEEK and invalid indices fault. All arrays remain bounded. See
[arrays, mutation and saved queues](/language/collections.md).

## Captured item NBT

Use an exact `ITEM_KEY`, such as `group.item` from `.contents()`.
An ITEM selector has no captured tags. All paths below are optional JSON Pointers;
omitting the path selects the root.

| Method on a key | Result |
| --- | --- |
| `.hasnbt(path)` | Whether a captured tag exists. |
| `.nbttype(path)` | Exact type name, or `"missing"`. |
| `.nbtinteger(path)` | Signed byte/short/int/long value. |
| `.nbtstring(path)` | String tag value. |
| `.nbttext(path)` | Scalar display text, including float/double. |
| `.nbtkeys(path)` | Sorted immediate compound keys as dynamic string[]. |
| `.nbtlength(path)` | Container entries, or UTF-16 code units for a string. |

Wrong-type and missing typed reads fault. These methods inspect the immutable
snapshot, without rescanning or writing a live stack. See
[read item NBT](/advanced/nbt.md) for paths, bounds and discovery.

## Stock calculations

| Call | Returns | Meaning |
| --- | --- | --- |
| `TOTAL(contents)` | INTEGER | Sum the captured group totals. |
| `TOTAL(locations)` | INTEGER | Sum captured physical-location amounts. |
| `STOCK(contents, itemKey)` | INTEGER | Captured count of one exact ITEM_KEY, or zero. |
| `SHORTAGE(required, available)` | INTEGER | Extra nonnegative amount needed, clamped to zero. Both arguments must be nonnegative. |

TOTAL and STOCK read the captured rows without rescanning the containers. STOCK
accepts an exact key, not a broad ITEM/ORE selector. Arithmetic uses checked
integer aggregation. See [contents](/automation/contents.md) and [stock helpers](/automation/stock.md).

## Signal observations

| Call | Returns | Meaning |
| --- | --- | --- |
| `$label.redstoneAvailable()` | BOOLEAN | Whether the selected native Redstone Ports can be read. |
| `$label.redstone()` | INTEGER | Aggregated detected signal level for the selected ports/faces. |
| `$label.powered()` | BOOLEAN | Whether the detected level is nonzero. |

The signal methods use their redstone endpoint contract, not the item-inventory
availability method. A port's faces have their own input/output settings. See
[redstone](/automation/redstone.md) for aggregation, edge and feedback behavior.

## Recipe inspection

| Call | Result |
| --- | --- |
| `itemkey(id [, metadata])` | Exact ITEM_KEY with null NBT; metadata defaults to zero. |
| `recipes(outputKey, map)` | Candidate summaries, status, coverageComplete and ambiguous. |
| `recipe(candidate.reference)` | One candidate's detailed requirements or an explicit unavailable/stale status. |

See [Inspect machine recipes](/advanced/recipes.md) for the three supported maps,
ordered ingredients, catalysts, chance fields and fluid quantities. Recipe calls
inspect data; they do not craft items or infer machine feasibility.

## Statements are separate

`PRINT(...)` is a statement, not a function returning data. Arguments are only
evaluated while capture is enabled and allowance remains.

`TRANSFER (source, destination[, keyOrSelector[, amount]]) [INTO $result]` is an
item action statement. Missing selector means all items; missing amount means
matching visible source stock observed during preflight. INTO is optional;
its result has `requested`, `moved`, `remaining` and `status`.
Use it in triggers or void procedures, never inside a data expression or a
value-returning function body. TRANSFERF is the corresponding fluid action in mB.

Item/fluid `INPUT` and `OUTPUT`, redstone `OUTPUT` and `FORGET` also remain action
statements for triggers and void procedures. Value functions can use data/query
calls, bounded loops, local/parameter assignments, direct STATE reads and WAIT;
they cannot perform world actions or directly assign STATE. Void procedures can update
STATE directly. Program-wide LET and caller locals still need explicit arguments.
Parameters are values unless a value function or void procedure explicitly
declares REF and its caller supplies a matching REF target. Those writes update
the caller immediately. A trigger or void procedure can explicitly pass STATE
by REF; a value function cannot directly acquire a STATE reference.
There are no current dynamic interval or substring defaults. String plus integer
concatenation uses signed decimal text; it does not provide general conversion.

## WAIT

`wait(n)` suspends the current trigger and active function stack for exactly n
game ticks while the manager remains loaded and running. Zero continues now;
negative values/deadline overflow fault. It resumes independently of the original
EVERY interval, on the server main thread, without blocking that thread or
creating a worker. Other triggers continue; new firings of this busy trigger
are not queued. It also works inside value-returning functions.

Completed operands, locals and loop progress survive; later queries see current
providers. Continuations are bounded RAM only: program/disk reload or replacement,
unload, STATE reset and runtime faults cancel them. No call stack is persisted.
See [scheduling](/automation/scheduling.md) for the full rules and examples.

## Ghost circuits

`setcircuit(machine, configuration)` configures the native ghost circuit on a
compatible GregTech machine. `machine` must be a whole-machine LABEL_REF, such
as `label("assembler")`, with no `.side(...)` or `.slots(...)` restriction.
Values 1 through 24 select a circuit; **0 or -1 clears it**. Other values fault.
This is a configuration action, not a physical item transfer into a guessed slot.

**Setup:** connect and label one supported GregTech machine `assembler`, with its
ghost circuit initially cleared. Enable PRINT capture to inspect the result.

```sfml
every 20 ticks {
  setcircuit(label("assembler"), 5) into configured;
  print(configured.matched, configured.changed, configured.status);
}
```

**Expected:** the first successful run selects configuration 5 and reports one
changed machine. Later successful runs report one matched and zero changed.
Unsupported machines report a status rather than receiving a physical circuit.

INTO is optional. Its record has integer `matched` and `changed` fields and a
string `status`: `complete`, `partial`, `unavailable` or `unsupported`. Counts
report confirmed results, not every attempted callback. A machine already at the
requested setting can be matched without being changed. A partial result may
have unconfirmed side effects; zero confirmed changes is not a rollback guarantee.

The group is checked for connected, loaded, supported endpoints before any
writes. A missing/disconnected endpoint gives unavailable; an unsupported host
gives unsupported. Later replacement or callback failures can interrupt commits.
Earlier accepted changes remain; work/provider failures can fault and pause the
manager. SETCIRCUIT shares the bounded action/query budgets and runs only on the
server thread. Use it in triggers or void procedures, not data expressions.

GregTech integration is optional and deliberately limited to supported ghost
configuration interfaces. It does not assume every machine, addon or inventory
supports this operation, and never edits a physical circuit stack as a fallback.

Next: [player functions](/language/functions.md) or [complete examples](examples.md).
