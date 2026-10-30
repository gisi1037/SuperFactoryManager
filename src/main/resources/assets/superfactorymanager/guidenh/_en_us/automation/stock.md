---
navigation:
  title: "Stock and shortage helpers"
  parent: /automation/index.md
  position: 40
  keywords: ["sfm", "stock", "total", "shortage", "target"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Stock and shortage helpers

Use TOTAL and STOCK to calculate with a captured [contents snapshot](/automation/contents.md). SHORTAGE turns a target and an observed amount into the additional quantity needed.

| Call | Result |
| --- | --- |
| `TOTAL(contents)` | Sum of every group's total. |
| `TOTAL(locations)` | Sum of the physical location amounts. |
| `STOCK(contents, itemKey)` | Captured quantity for that exact key, or zero if absent. |
| `SHORTAGE(required, available)` | `MAX(required - available, 0)` for nonnegative integers. |

```sfml
NAME "Inspect ore stock"
EVERY 20 TICKS DO
  LET $stock = LABEL("inputs").contents(ORE("ingotIron"))
  PRINT("all matching variants", TOTAL($stock))
  IF (LEN($stock) > 0) THEN
    PRINT("first exact variant", STOCK($stock, $stock[0].item))
    PRINT("origins agree", TOTAL($stock[0].locations))
  END
END
```

STOCK requires an ITEM_KEY, not ITEM or ORE. Filter the original scan when you need the total for a broad item/category selector. TOTAL over an empty sequence returns zero. Negative SHORTAGE arguments fault; an available amount already above the target returns zero. All totals use checked signed-64-bit arithmetic and fault on overflow.

These helpers read materialized values. They do not contact providers, refresh a snapshot or reserve the quantity they report. TOTAL and STOCK walk their captured rows under shared work limits. Repeated STOCK calls are linear searches, so a loop of repeated lookups can be much more expensive than one pass over the groups.

To fill a destination toward a broad item target, a fresh count often suffices:

```sfml
NAME "Target stock"
EVERY 20 TICKS DO
  LET $source = LABEL("inputs")
  LET $target = LABEL("assembly")
  LET $iron = ITEM("minecraft:iron_ingot")
  IF ($source.available() AND $target.available()) THEN
    LET $needed = SHORTAGE(32, $target.count($iron))
    TRANSFER ($source, $target, $iron, $needed) INTO $result
    PRINT($result.status, $result.moved)
  END
END
```

The request aims for thirty-two visible ingots, including variants selected by ITEM. A refusing/full destination can still move less. The count is an observation rather than a transaction: external changes or earlier effects can change stock before the request finishes. For one exact variant, capture a key and use STOCK on a destination snapshot before transferring that key.

TOTAL, STOCK and SHORTAGE can be called inside read-only [player functions](/language/functions.md). SHORTAGE can initialize STATE when its arguments are state-safe. Live keys, contents and locations cannot be stored in STATE. Keep a snapshot local to the invocation and persist only ordinary totals or decisions when needed.
