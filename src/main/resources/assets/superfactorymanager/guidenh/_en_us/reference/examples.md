---
navigation:
  title: "Complete examples"
  position: 90
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["examples", "logistics", "iron", "stock", "program", "copy"]
categories: ["sfm"]
updated: "2026-10-30"
---

# Complete examples

Each SFML block here is a complete program. Copy one program at a time, assign
the named labels to connected containers, save to a disk and deliberately enable
the manager. Keep PRINT capture enabled only while inspecting results.

## Maintain an output stock target

Assign all eligible source chests to `logistics_inputs` and the desired output
to `assembly`. This program tops up to 32 iron ingots without draining outputs
when they already contain more. The read-only function computes the request;
the TRANSFER statement performs the move.

```sfml
NAME "Maintain assembly stock"
FUNCTION logistics.missing($target: INTEGER, $destination: LABEL_REF, $item: RESOURCE_SELECTOR) RETURNS INTEGER DO
  RETURN SHORTAGE($target, $destination.count($item))
END
EVERY 20 TICKS DO
  LET $source = LABEL("logistics_inputs")
  LET $destination = LABEL("assembly")
  LET $item = ITEM("minecraft:iron_ingot")
  IF ($source.available() AND $destination.available()) THEN
    LET $missing = logistics.missing(32, $destination, $item)
    TRANSFER ($source, $destination, $item, $missing) INTO $result
    PRINT("moved", $result.moved, "remaining", $result.remaining, $result.status)
  ELSE
    PRINT("Inventory group unavailable")
  END
END
```

The request is aggregate across the source group. If the output has capacity
for fewer items, inspect `moved`, `remaining` and `status`. A missing/unsupported
view is different from a complete scan with zero matching stock.

## Move one exact observed variant

Metadata and NBT can distinguish stacks with the same item ID. The first
captured group's ITEM_KEY selects that exact variant. Its quantity is advisory;
the action still checks current stock.

```sfml
NAME "Move one exact observed variant"
EVERY 20 TICKS DO
  LET $source = LABEL("logistics_inputs")
  LET $destination = LABEL("assembly")
  IF ($source.available() AND $destination.available()) THEN
    LET $stock = $source.contents(ITEM("minecraft:iron_ingot"))
    PRINT("observed matching amount", TOTAL($stock))
    IF (LEN($stock) > 0) THEN
      LET $requested = MIN(8, STOCK($stock, $stock[0].item))
      TRANSFER ($source, $destination, $stock[0].item, $requested) INTO $result
      PRINT($result.moved, $result.remaining, $result.status)
    END
  END
END
```

If you want a broad ore category rather than an exact variant, use an
`ORE("ingotIron")` selector. Equality between two ITEM_KEY values is exact;
printing a key does not display its entire NBT payload.

## Inspect grouped contents and their locations

```sfml
NAME "Where is our stock?"
EVERY 100 TICKS DO
  LET $source = LABEL("logistics_inputs")
  IF ($source.available()) THEN
    LET $stock = $source.contents()
    PRINT("groups", LEN($stock), "items", TOTAL($stock))
    FOR $group IN $stock DO
      PRINT($group.id, $group.metadata, $group.total)
      FOR $where IN $group.locations DO
        PRINT($where.dimension, $where.x, $where.y, $where.z, $where.slot, $where.amount)
      END
    END
  END
END
```

This is an inspection example for a small selected view. After sixteen printed
statements, later PRINT arguments/output are skipped. Exhausting a scan or loop
budget instead faults execution; disabled/capped PRINT still has a statement-work
charge. Locations report physical slots and block
coordinates, not labels or a reservation. Reuse the captured snapshot instead
of scanning once per discovered item.

## A persistent controller with a reset pulse

This demonstrates state lifetime and manager-level pulse detection. The first
scheduled pass attempts to transfer up to one item, then locks until a new pulse reaches the
manager. The lock changes after the output statement runs, not after a proven
item count change; use TRANSFER results if that distinction matters.

```sfml
NAME "One attempt per reset"
STATE $lock = FALSE
EVERY REDSTONE PULSE DO
  SET $lock = FALSE
END
EVERY 20 TICKS DO
  IF (NOT $lock) THEN
    INPUT 1 FROM source
    OUTPUT TO dest
    SET $lock = TRUE
  END
END
```

For a detector elsewhere on the cable network, use a labelled native
[Redstone Port](/automation/redstone.md) and a named rising/falling trigger.

Continue with [automation](/automation/index.md), [functions](/language/functions.md)
or [debugging](debugging.md).
