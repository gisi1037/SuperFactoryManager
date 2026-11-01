---
navigation:
  title: "Arrays and structs"
  parent: /language/index.md
  position: 60
  keywords: ["sfml", "arrays", "structs", "collections"]
categories: ["sfm", "language"]
updated: "2026-10-30"
---

# Arrays and structs

Array literals hold a fixed number of values with one identical type and shape.
Explicit dynamic arrays can grow and shrink in RAM; see below. Structs group values under fixed field names. Combine them to describe batches, settings or jobs.

```sfml
NAME "Independent collection values"
STATE $last = { original: 0, updated: 0 }
EVERY 20 TICKS DO
  LET $jobs = [{ amount: 4, enabled: TRUE }, { amount: 8, enabled: FALSE }]
  LET $edited = $jobs
  SET $edited[0].amount = $jobs[1].amount * 3 + 2
  SET $last.original = $jobs[0].amount
  SET $last.updated = $edited[0].amount
  IF ($edited[0].enabled) THEN
    INPUT ($edited[0].amount) item:minecraft:iron_ingot FROM "source"
    OUTPUT TO "dest"
  END
END
```

`$last.original` becomes four and `$last.updated` becomes twenty-six. Assigning `$jobs` to `$edited` copies the value: editing one does not change the other. Collections are immutable values with replacement updates, so they do not create mutable aliases or cycles.

## Array indexes and length

Use brackets for zero-based indexes. In `[4, 8, 16]`, index `0` is four and index `2` is sixteen. Use `LEN($array)` to count outer elements. A nested array's outer length does not count every value inside it.

Indexes must be integers in range. Negative or out-of-range indexes fault; arrays do not clamp, wrap or grow. An array initialized with three entries must always hold exactly three entries of the same element type. `[1, TRUE]` is invalid. An empty array has no inferable element type and is rejected.

```text
LET $batches = [4, 8, 16]
SET $batches[1] = 12
LET $count = LEN($batches)
```

Here the length remains three. There are no append, remove or resize operations on fixed arrays. [Array loops](loops.md) visit entries in index order.

## Fields and nested updates

Read a struct member with `.field`, and use the same path in `SET`. Field names are case-sensitive. Their source order does not define the type; their names and types do. Duplicate or unknown fields fail compilation.

```sfml
NAME "Nested settings"
STATE $settings = { target: { amount: 64, enabled: TRUE }, batches: [4, 8] }
EVERY 20 TICKS DO
  SET $settings.target.amount = 32
  SET $settings.batches[0] = 12
  PRINT("target", $settings.target.amount, "first", $settings.batches[0])
END
```

You may replace a complete struct with another of the same fields and types. You cannot add a field or turn its integer into a boolean through assignment. Empty structs are allowed. Array and struct literals permit one trailing comma.

For a nested assignment, indexes and the path are evaluated and validated before the right-hand side is published. If the assignment fails, that binding retains its old complete value. This protection does not undo earlier statements in the trigger.

## Records and query values

A record such as `{item: "minecraft:iron_ingot", count: 8}` is ordinary data. Editing it does not create or modify a Minecraft item stack. Its string field cannot substitute for a static resource selector in `INPUT` or `OUTPUT`.

Typed query handles and snapshots are also immutable. They may be held in locals, passed to functions and nested inside local collections. Collections containing any handle or snapshot cannot be persistent `STATE`. Use the supported query methods to inspect them; their types and capabilities differ from ordinary arrays and structs.

Next: [Strings](strings.md). Related: [Variables and state](variables.md), [Loops](loops.md) and [Typed queries](/automation/queries.md).


## Dynamic arrays (RAM only)

`integer[]` is a growable integer array; `integer[3]` still means exactly three
integers. Construct an empty array explicitly with `array("integer")`,
`array("label_ref")`, or another constant type string. `array(sequence)` converts
an existing fixed array, dynamic array or contents snapshot explicitly. Bare `[]`
still cannot infer an element type. No implicit fixed/dynamic conversion occurs.

```sfml
integer[] makeValues() {
  let values = array("integer")
  push(ref values, 2)
  extend(ref values, [4, 6])
  return values
}

every 20 ticks {
  let values = makeValues()
  insert(ref values, 0, 1)
  let last = pop(ref values)
  let first = removeAt(ref values, 0)
  print(first, last, peek(values), contains(values, 4), indexOf(values, 4))
  clear(ref values)
}
```

| Function | Result |
| --- | --- |
| `push(ref values, value)` | Append one element; no return value. |
| `extend(ref values, sequence)` | Append same-type elements; no return value. |
| `insert(ref values, index, value)` | Insert at index `0..len(values)`; no return value. |
| `pop(ref values)` | Remove and return the last element. |
| `removeAt(ref values, index)` | Remove and return the indexed element. |
| `clear(ref values)` | Remove all elements; no return value. |
| `peek(sequence)` | Read the last element without removing it. |
| `contains(sequence, value)` | Exact typed membership. |
| `indexOf(sequence, value)` | First zero-based match, or `-1`. |

Mutation requires a writable dynamic array and explicit REF. Pure readers accept
fixed/dynamic arrays and native sequences. Element types must match exactly.
Membership compares nested values, label names plus side/slot restrictions, and
exact metadata/NBT item/fluid keys. This does not add `==` for labels/collections.
Empty POP/PEEK and invalid indexes fault without changing the target. CLEAR on an
empty array and EXTEND by an empty sequence are no-ops. Other already evaluated
argument effects remain committed if a later check fails.

Arrays have at most 256 outer elements and 4,096 retained cells, with existing
character/opaque/work/allocation/WAIT limits. They use immutable replacement:
append/insert/remove/extend are O(n), not amortized constant time. Cumulative work
can fault before the size limit. Ordinary assignment/parameters keep value-copy
semantics; REF is the explicit way to change another binding.

Structural changes invalidate captured indexed references and assignment paths,
including across WAIT, same-length replacement and remove/reinsert cycles.
Invalidation is conservatively per root, so sibling indexed references may also
fault. Whole-array references remain usable; the successful indexed writer is
refreshed, while forwarded aliases can become stale. Ordinary scalar element
updates preserve other indexed references. FOREACH retains its original snapshot.

Dynamic arrays are RAM-only in LET/parameters/returns, including nested arrays
and structs; STATE rejects them until its variable-length codec/budgets exist.
Native mutation calls and wrappers with REF effects cannot run in STATE defaults,
program-wide LET initializers or named-redstone subscriptions. Pure ARRAY
construction is allowed in program-wide LET. Mutations belong in triggers or
function execution. Disabled PRINT still skips argument evaluation, so perform
required POP/remove operations in assignments before printing.

Visual supports ARRAY and the pure readers in expressions; collection mutation
calls currently require Code. Fixed arrays, ordinary Visual variables and loops
retain their existing behavior. `reserve`/capacity and persistent dynamic arrays
remain future work.
