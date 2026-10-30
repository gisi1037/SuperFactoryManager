---
navigation:
  title: "Typed item queries"
  parent: /automation/index.md
  position: 70
  keywords: ["sfm", "label", "item", "ore", "count", "available", "empty"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Typed item queries

Typed queries read current item stock without moving or reserving it. LABEL creates a LABEL_REF for a disk label group; ITEM and ORE create RESOURCE_SELECTOR values. Constructor and method names ignore case, while label names and registry/ore identifiers match exactly.

```sfml
NAME "Read before moving"
EVERY 20 TICKS DO
  LET $src = LABEL("chest_in")
  LET $iron = ITEM("minecraft:iron_ingot")
  IF ($src.available($iron) AND $src.count($iron) >= 8) THEN
    INPUT 8 item:minecraft:iron_ingot FROM chest_in
    OUTPUT TO chest_out
  END
END
```

Constructors require one quoted source literal. Computed strings and string variables are not accepted. LABEL names must be nonempty, have no surrounding whitespace and fit within 256 characters. ITEM accepts `"*"` or an exact `namespace:name` up to 256 characters. It includes every metadata/NBT variant of that item. ITEM does not support the `item:` prefix, metadata suffixes, glob patterns, NBT filters or fluids. A valid unknown item ID matches nothing.

ORE accepts an exact case-sensitive OreDictionary name of 1–256 ASCII letters, digits or underscores, for example `ORE("ingotIron")`. No `ore:` prefix, wildcard or computed name is accepted. Forge wildcard-metadata ore registrations work. An unknown name matches nothing and does not create a registry entry.

| Query | Result |
| --- | --- |
| `$src.count($iron)` | Matching visible-item total as a checked signed-64-bit integer. An incomplete/unavailable group faults. |
| `$src.available($iron)` | TRUE if the nonempty group is completely connected, loaded, supported and has visible permitted slots in every distinct inventory view. It does not inspect matching stock. |
| `$src.empty($iron)` | TRUE if the complete available group has no matching visible items. Missing or incomplete groups return FALSE. |
| `$src.available()` | Same access-only test with the all-items selector. |
| `$src.empty()` | Same item-only test as `empty(ITEM("*"))`. |
| `$src.contents()` | A captured sequence of exact item groups; see [contents](/automation/contents.md). |

Count requires one RESOURCE_SELECTOR. Available, empty and contents accept zero or one selector. There is no implicit string-to-selector conversion. The no-argument forms refer only to visible items: they do not inspect fluids, empty fluid tanks, free space or machine insertion capacity. Supplying a filter to available does not change its access-only meaning; an available group may have zero matching stock.

Default access is all ordinary slots, or the deduplicated union of valid slots exposed by the six faces of an ISidedInventory. Duplicate faces/indices count once; invalid indices are ignored. Visible stock is counted even when extraction is refused. Zero-sized inventories and entirely hidden selections are unavailable, rather than confirmed empty. There are no current `canExtract`, `canInsert`, accepted-amount or capacity methods. Those permissions are checked by an actual transfer; available and empty do not substitute for them.

Restrict observations using immutable handle methods:

```sfml
NAME "Restricted observation"
EVERY 20 TICKS DO
  LET $whole = LABEL("chest_in")
  LET $selected = $whole.side("north").slots(0, 8)
  IF ($selected.available() AND NOT $selected.empty()) THEN
    PRINT($selected.count(ITEM("minecraft:iron_ingot")))
  END
END
```

These methods affect this handle only. They do not alter an ordinary INPUT/OUTPUT statement naming chest_in. [Sides and slots](/automation/sides-slots.md) explains literal side names, inclusive bounds, replacement behavior and combined chest ordering.

Each query resolves current loaded providers and membership afresh, so it can see an earlier transfer in the same trigger. Available is one observation; a subsequent count can still fault if the provider changes. Arguments evaluate once, receiver first. Short-circuiting can avoid a count when availability is false, but does not reserve access for later.

Direct IInventory tiles and canonical vanilla chest pairs are supported. Repeated direct provider objects and labelled chest halves are counted once. Uncertain unloaded chest adjacency makes access unavailable without loading chunks. Proxy/cluster adapters and distinct proxy tiles hiding shared storage need separate support; do not assume their combined counts represent unique physical stock.

LABEL_REF and RESOURCE_SELECTOR values may live in locals, arrays and structs and pass through typed player functions. They cannot live in STATE, even nested in a container. Save an ordinary integer or boolean result if you need persistent observations.

Queries, contents, transfers and typed redstone share per-manager-invocation limits across all due triggers: 1,024 label-position visits, 8,192 provider calls, 16,384 slot-work units and 16,384 collection/allocation reservations. Returned sided indices and deduplication also cost work. Provider exceptions, invalid stacks, overflow and exhausted budgets fault instead of appearing empty or unavailable. These limits cannot interrupt one external callback and do not guarantee a fixed tick duration. Ordinary HAS/INPUT/OUTPUT retain their own behavior.

Typed queries currently require Text. Use [PRINT and debugging](/reference/debugging.md) to inspect results.
