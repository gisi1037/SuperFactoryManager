---
navigation:
  title: "Default functions"
  position: 100
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["api", "builtin", "min", "max", "len", "total", "stock", "shortage"]
categories: ["sfm"]
updated: "2026-10-28"
---

# Default functions

These tables list the current typed data API. `INTEGER` is signed 64-bit,
`BOOLEAN` is true/false, and `STRING` is text. Built-in names ignore case.
Exact-name player functions take precedence over ordinary built-in calls; avoid
reusing built-in names so a program remains easy to read. LABEL, ITEM, ORE and
TRANSFER are reserved boundaries.

## Constructors and selections

| Call | Returns | Meaning |
| --- | --- | --- |
| `LABEL("name")` | LABEL_REF | Select the exact saved label group. |
| `ITEM("namespace:name")` | RESOURCE_SELECTOR | Match every metadata/NBT variant of one exact item ID; `ITEM("*")` matches all items. |
| `ORE("ingotIron")` | RESOURCE_SELECTOR | Match one exact ore-dictionary name, including its registered alternatives. |
| `$label.side("up")` | LABEL_REF | Restrict observed/used faces. |
| `$label.slots(0, 8)` | LABEL_REF | Select an inclusive logical slot range. |

Constructor strings must be literals. A label is a selection, an ITEM/ORE value
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
| `LEN(array)` | Number of outer fixed-array entries. |
| `LEN(string)` | Unicode code-point count, not UTF-8 bytes or displayed grapheme clusters. |
| `LEN(contents)` / `LEN(locations)` | Actual captured sequence length, including zero. |

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

## Statements are separate

`PRINT(...)` is a statement, not a function returning data. Arguments are only
evaluated while capture is enabled and allowance remains.

`TRANSFER (source, destination[, keyOrSelector[, amount]]) [INTO $result]` is an
item action statement. Missing selector means all items; missing amount means
matching visible source stock observed during preflight. INTO is optional;
its result has `requested`, `moved`, `remaining` and `status`.
It cannot be called inside a data expression or a current FUNCTION body.

Item/fluid `INPUT` and `OUTPUT`, redstone `OUTPUT` and `FORGET` also remain action
statements. Read-only player functions can use the data/query calls above,
bounded loops and local/parameter assignments; they cannot perform world actions
or mutate persistent STATE. There are no current recipe-query,
dynamic interval, append, substring or implicit string-conversion defaults.

Next: [player functions](/language/functions.md) or [complete examples](examples.md).
