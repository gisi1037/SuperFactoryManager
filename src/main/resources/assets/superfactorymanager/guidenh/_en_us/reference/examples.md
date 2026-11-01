---
navigation:
  title: "Complete examples"
  position: 90
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["examples", "logistics", "iron", "stock", "program", "copy"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Complete examples

These advanced experiments combine features from several chapters. For your
next step after the first transfer, try [Change your route](/start/change-route.md).

Try **one complete program at a time**. Before each experiment, pause the manager,
connect and label the listed blocks, push the Label Gun table to the disk, and
prepare the contents. Save the program, wait for confirmation, then press Start.
For printed results, open **Manager > Terminal** and enable **PRINT capture**
before starting. Turn capture off after inspecting the result.

These examples use lowercase keywords, braces and plain variable names. The
[language overview](/language/index.md) explains compatible spellings.

## Maintain an output stock target

**Setup:** use two single chests: `logistics_inputs` holds forty iron ingots;
`assembly` is empty. The function calculates the shortage; TRANSFER moves items.

```sfml
name "Maintain assembly stock"
integer logistics.missing(integer target, label_ref destination, resource_selector selector) {
  return shortage(target, destination.count(selector))
}
every 20 ticks {
  let source = label("logistics_inputs")
  let destination = label("assembly")
  let selector = item("minecraft:iron_ingot")
  if (source.available() and destination.available()) {
    let missing = logistics.missing(32, destination, selector)
    transfer(source, destination, selector, missing) into result
    print("moved", result.moved, "remaining", result.remaining, result.status)
  } else {
    print("Inventory group unavailable")
  }
}
```

**Expected:** the first run leaves **8 source / 32 assembly** and prints
`moved 32 remaining 0 complete`. Later runs move zero and leave those counts
unchanged. Remove four ingots from assembly; the next due run restores thirty-two,
leaving **4 / 32**.

**Try a change:** pause and reset the contents to **40 / 0**. Change the function
call's target from `32` to `16`, save and start. Counts settle at **24 / 16**.

A target never removes excess stock. The request is shared across a source group;
capacity or machine restrictions can reduce actual movement. Check `moved`,
`remaining` and `status` instead of assuming the requested amount arrived.

## Move one exact observed variant

**Setup:** in `logistics_inputs`, put one stack of four iron ingots renamed
**Sample** using an anvil, plus four cobblestone. Empty `assembly`.
The iron filter finds one group; its captured ITEM_KEY preserves the name's NBT.

```sfml
name "Move one exact observed variant"
every 20 ticks {
  let source = label("logistics_inputs")
  let destination = label("assembly")
  if (source.available() and destination.available()) {
    let snapshot = source.contents(item("minecraft:iron_ingot"))
    print("observed matching amount", total(snapshot))
    if (len(snapshot) > 0) {
      let requested = min(8, stock(snapshot, snapshot[0].item))
      transfer(source, destination, snapshot[0].item, requested) into result
      print(result.moved, result.remaining, result.status)
    }
  }
}
```

**Expected:** all **4 Sample ingots** reach assembly with their name intact;
all **4 cobblestone** stay in source. The first run prints matching amount four,
then `4 0 complete`. Later runs find no matching iron.

**Try a change:** pause and restore the starting contents. Add a separate stack
of four ordinary iron ingots. Now there are two exact iron groups. The first
request moves only one group's four ingots; the next due run can move the other.
Do not depend on which group appears first in a contents snapshot.

Metadata and NBT distinguish exact keys even when registry IDs match. Captured
quantities are observations; TRANSFER rechecks current stock. For a broad ore
category, use `ore("ingotIron")`; printing an ITEM_KEY does not dump its full NBT.

## Inspect grouped contents and their locations

**Setup:** give two single chests the label `logistics_inputs`. Put one stack of
three iron ingots in the first chest. In the second, put one stack of five iron
ingots and a separate stack of two cobblestone. Leave all other slots empty.

```sfml
name "Where is our stock?"
every 100 ticks {
  let source = label("logistics_inputs")
  if (source.available()) {
    let snapshot = source.contents()
    print("groups", len(snapshot), "items", total(snapshot))
    for group in snapshot {
      print(group.id, group.metadata, group.total)
      for location in group.locations {
        print(location.dimension, location.x, location.y, location.z, location.slot, location.amount)
      }
    }
  }
}
```

**Expected:** each run prints `groups 2 items 10`. The iron group totals **8**
across two locations; cobblestone totals **2** at one location. Location rows
identify the chests' real coordinates and slot indices. Group and location order
are not part of this experiment's expected result. Nothing moves.

**Try a change:** merge the five iron ingots into the first chest's three-ingot
stack. The next snapshot still has **2 groups / 10 items**, but iron now has one
location. This contrasts grouped identity with physical storage.

Use this on a small view. PRINT allows sixteen statements per invocation; this
setup uses six. Larger output can be capped, while scan or loop-budget exhaustion
faults execution. Locations report physical slots, not labels or reservations.
Reuse one captured snapshot instead of rescanning for every group.

## A persistent controller with a reset pulse

**Setup:** put eight cobblestone in `source`; empty `dest`. Attach an off lever
to the manager. Use a fresh source disk, or save this program and choose
**Program > Reset state** before starting, so `lock` begins false.

```sfml
name "One attempt per reset"
state lock = false
every redstone pulse {
  lock = false
}
every 20 ticks {
  if (not lock) {
    input 1 from "source"
    output to "dest"
    lock = true
  }
}
```

**Expected:** the first due timer leaves **7 source / 1 dest**, then repeated
runs move nothing. Switch the lever on: the next scheduled attempt leaves
**6 / 2**. Holding the lever on permits no further attempts. Switch it off,
allow a game tick for observation, then switch it on for another attempt.

**Try a change:** pause, reset the contents to **8 / 0**, and change `input 1`
to `input 2`. Save, Reset state, then start with the lever off. The first attempt
leaves **6 / 2**; the next rising pulse permits another attempt, leaving **4 / 4**.

The lock records an attempted output, even if zero items move. Use TRANSFER's
result when confirmed movement must decide whether to lock. STATE survives reload;
changing its default alone does not reset an existing value. A detector elsewhere
on the network uses a labelled native [Redstone Port](/automation/redstone.md)
and named rising/falling triggers instead of the manager's own pulse.

Continue with [automation](/automation/index.md), [functions](/language/functions.md)
or [debugging](debugging.md).
