---
navigation:
  title: "Contents and exact item keys"
  parent: /automation/index.md
  position: 60
  keywords: ["sfm", "contents", "item key", "nbt", "locations", "snapshot"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Contents and exact item keys

`LABEL("inputs").contents()` captures visible items into a read-only ITEM_CONTENTS sequence. Pass ITEM or ORE to filter the scan, for example `.contents(ORE("ingotIron"))`. Access uses the same direct-inventory, loaded-chunk, chest-pair, side and slot rules as [count](/automation/queries.md).

```sfml
NAME "Where is our stock?"
EVERY 20 TICKS DO
  LET $stock = LABEL("inputs").contents()
  PRINT("distinct variants", LEN($stock))
  FOR $group IN $stock DO
    PRINT($group.id, $group.metadata, $group.total)
    FOR $where IN $group.locations DO
      PRINT($where.dimension, $where.x, $where.y, $where.z,
            $where.slot, $where.amount)
    END
  END
END
```

A complete available empty view produces zero groups. Missing, incomplete, unsupported or over-budget views fault the whole query. No partial stock is returned. The scan moves nothing and reserves nothing.

Every group has these exact, case-sensitive fields:

| Field | Type and meaning |
| --- | --- |
| `item` | ITEM_KEY, the exact variant identity. |
| `id` | STRING, the registered item ID. |
| `metadata` | INTEGER, the item damage/metadata. |
| `total` | INTEGER, summed observed quantity of this exact variant. |
| `locations` | ITEM_LOCATIONS, its physical origin rows. |

Each location contains integer fields `dimension`, `x`, `y`, `z`, `slot`, `sideMask` and `amount`. Coordinates identify the physical inventory. For a double chest, slot and coordinates identify the appropriate physical half, although a handle's slot selection uses the stable combined chest order. Amount is the quantity observed in that physical slot.

sideMask records the sides selected for the observation, not extraction permission or a per-slot proof that every selected face exposes that slot. Its bit values are down=1, up=2, north=4, south=8, west=16 and east=32. Default all-face selection is 63. Do not use a captured mask or coordinate as authority to bypass a later access check.

Groups appear in first-encounter order from label positions and ascending eligible slots. Repeated physical inventories/chest halves are deduplicated. Treat this as scan order, not a sort by item name or a permanently stable row index.

An ITEM_KEY includes exact registry ID, metadata and canonical NBT. Quantity is separate. Compound entry order does not change a key; NBT types, list order and payload do. Absent NBT and an empty NBT compound are distinct keys. `=` and `==` compare keys exactly; `!=` detects a difference, including an NBT-only change. Printed key summaries omit the NBT payload, so identical-looking summaries do not prove equal variants. There is no public key constructor or general NBT inspector.

Program-wide LET can retain a baseline between runs. Compare `$current[0].item != $original[0].item` after checking both sequence lengths. This is automatic full-NBT comparison, not a universal recipe-completion signal: some machines change NBT during processing or return an identical final key. RAM baselines reset on reload/recompile; STATE still cannot store exact keys.

Guard indices using LEN before indexing:

```sfml
NAME "Inspect the first group"
EVERY 20 TICKS DO
  LET $stock = LABEL("inputs").contents(ITEM("minecraft:iron_ingot"))
  IF (LEN($stock) > 0) THEN
    PRINT($stock[0].total, LEN($stock[0].locations))
  END
END
```

LEN and FOR IN use actual sequence lengths, including zero; different groups may have different location counts. Snapshot sequences reject indexed SET updates. A group or location record copied to an ordinary local can be edited with normal value-copy rules without changing the snapshot. Iteration variables remain read-only.

The captured values contain no live inventory or mutable item stack. Later indexing, printing and loops read materialized data. Earlier snapshots keep their original amounts after transfers; query again to observe new stock. Keys can select exact variants for [TRANSFER](/automation/transfers.md), which rechecks live access and stock.

Keys and sequences can pass through typed [player functions](/language/functions.md), but cannot be stored in STATE, even inside arrays or structs. Provisional per-scan caps are 256 exact groups and 1,024 locations. Each key's NBT has limits of depth 16, 1,024 nodes and 8,192 canonical bytes. Shared query/data limits may fail earlier. Oversized, unsupported or corrupt NBT faults instead of producing a broadened key or truncated snapshot.
