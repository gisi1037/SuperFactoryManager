---
navigation:
  title: "Read item NBT"
  position: 45
  icon: "superfactorymanager:disk"
  parent: /advanced/index.md
  keywords: ["nbt", "heat", "coolant", "durability", "tags"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Read item NBT

Use an exact captured item key: **`group.item`**, from a contents snapshot.
`ITEM("namespace:name")` is a filter, not a stack with tags.

For example, a 360k Helium Coolantcell may expose a known `/heat` field separately
from its metadata. Metadata and raw tag values have item-specific meanings;
do not assume metadata is a universal heat or remaining-durability value.

**Setup:** put a coolant cell in a supported connected inventory labelled
`reactor`. Enable PRINT capture. Use the installed item's exact registry ID;
this example targets the GregTech 360k Helium Coolantcell.

```sfml
name "Coolant heat"
every 40 ticks {
  let stock = label("reactor").contents(item("gregtech:gt.360k_Helium_Coolantcell"))
  for group in stock {
    print("metadata", group.metadata, "heat type", group.item.nbttype("/heat"))
    if (group.item.nbttype("/heat") == "int") {
      print("heat", group.item.nbtinteger("/heat"))
    }
  }
}
```

Connect and label the actual accessible inventory first. Read capture output in
the Manager terminal. A new contents call observes new values; an old snapshot
keeps its captured heat even while the reactor changes the live cell.

**Expected:** matching cells print their metadata and captured heat tag type.
An integer `/heat` prints its raw value. A missing or differently typed tag skips
that read. Inspect the type before adapting the program to another item or pack.

## Find the tag

| Method on group.item | Result |
| --- | --- |
| hasnbt(path) | Whether that captured tag exists. |
| nbttype(path) | Exact type name, or missing. |
| nbtinteger(path) | Signed byte/short/int/long as an SFML integer. |
| nbtstring(path) | String tag value. |
| nbttext(path) | Scalar display text, including float/double. |
| nbtkeys(path) | Sorted immediate compound keys as string[]. |
| nbtlength(path) | Container entries/elements, or UTF-16 string code units. |

Omit path, or use an empty string, to select the tag root. Start by printing
`group.item.nbtkeys()` on a compound root and inspect a field's `nbttype` before choosing its getter.
Type names are byte, short, int, long, float, double, byte_array, string, list,
compound and int_array. No tag differs from an empty compound.

## Paths

Paths are JSON Pointers relative to the item tag compound: /heat,
/display/Name or /list/0/value. Lists and byte/int arrays use zero-based indices.
Dots belong to a literal key, so `/GT.Item/heat` selects a `GT.Item` compound.
Escape a slash inside a key as `~1` and a tilde as `~0`. Paths allow at most 512
characters and 16 segments. Leading-zero list indices are not aliases for an index.

Missing typed reads and wrong types fault; they do not invent zero or truncate
floating values. Use hasnbt for optional fields. nbttext displays scalar values;
it is not a complete binary NBT export. To inspect a compound, list its keys and
read children. Key lists refuse more than 256 entries rather than silently truncate.

## Discover an unfamiliar item

Put one item in a connected chest labelled `inspect`. This program does not
assume a registry ID or tag layout:

```sfml
name "Discover captured tags"
every 100 ticks {
  let stock = label("inspect").contents()
  for group in stock {
    print(group.id, group.metadata, "root", group.item.nbttype())
    if (group.item.nbttype() == "compound") {
      print("keys", group.item.nbtkeys())
    }
  }
}
```

**Expected:** the captured ID, metadata and root type appear in PRINT output.
Tagged items also list immediate root keys. An untagged item reports `missing`.
Use a discovered key in a JSON Pointer, then choose a getter matching its type.
This is a small inspection tool; PRINT output remains bounded.

These operations read immutable snapshot bytes under shared work/allocation
limits. They do not load chunks, rescan slots, retain a mutable ItemStack or write
NBT. Exact-key comparison and transfer still use the complete original identity.
Known item-specific tags are inspectable; a universal remaining-durability adapter
and a graphical NBT tree are separate features.

Next: [contents](/automation/contents.md) or [performance](/reference/performance.md).
