---
navigation:
  title: "Change your route"
  position: 75
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["beginner", "batch", "filter", "stock", "iron", "example"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Change your route

Use the same connected single chests labelled `source` and `dest` from
[your first transfer](first-transfer.md). Try **one program at a time**.

Before each exercise, **pause the manager**, replace its code through
**Program > Edit code**, and reset the chest contents as described.
Choose **Save & Close**, wait for confirmation, then press **Start**.
These timers run every **100 game ticks**, normally five seconds.

## Move two each run

**Setup:** put eight cobblestone in `source`; empty `dest`.

```sfml
name "Two each run"
every 100 ticks {
  input 2 from "source";
  output to "dest";
}
```

**Expected:** after one due run, the counts are **6 source / 2 dest**.
After two, they are **4 / 4**. Eventually they settle at **0 / 8**.
The `2` limits each run; it does not limit the final destination stock.

More: [quantities and retention](/automation/quantities.md).

## Move only iron

**Setup:** put four iron ingots and four cobblestone in `source`; empty `dest`.

<ItemImage id="minecraft:iron_ingot" />

```sfml
name "Iron only"
every 100 ticks {
  input item:minecraft:iron_ingot from "source";
  output to "dest";
}
```

**Expected:** all **4 iron ingots** move to `dest`; all **4 cobblestone** stay
in `source`. Later runs leave those counts unchanged.
The selector `item:minecraft:iron_ingot` chooses the item by its registered ID.

More: [resource selectors](/automation/selectors.md).

## Keep three in stock

**Setup:** put eight cobblestone in `source`; empty `dest`.

```sfml
name "Keep three ready"
every 100 ticks {
  input from "source";
  output retain 3 to "dest";
}
```

**Expected:** the counts settle at **5 source / 3 dest**. Remove one from `dest`.
After the next due run, they settle at **4 / 3**.
Unlike the batch limit, `retain 3` counts what is already in the destination.
It fills a shortage and leaves any existing excess alone.

More: [stock targets and reserves](/automation/quantities.md).

Next: [choose an editor](editors.md), or explore [Advanced](/advanced/index.md).
Need help? [Nothing moving?](troubleshooting.md).
