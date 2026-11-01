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

Use an exact captured item key: **group.item**, from a contents snapshot.
ITEM("namespace:name") is a filter, not a stack with tags.

For example, a 360k Helium Coolantcell can have metadata15 and separate heat
and tag fields. This coolant uses metadata as coarse heat percent; the heat NBT
field exposes a separate raw value. Other items use metadata for unrelated variants.

```sfml
name "Coolant heat"
every 40 ticks {
  let stock = label("reactor").contents(item("gregtech:gt.360k_Helium_Coolantcell"))
  for group in stock {
    if (group.metadata == 15 and group.item.hasnbt("/heat")) {
      print("heat", group.item.nbtinteger("/heat"))
    }
  }
}
```

Connect and label the actual accessible inventory first. Read capture output in
the Manager terminal. A new contents call observes new values; an old snapshot
keeps its captured heat even while the reactor changes the live cell.

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
group.item.nbtkeys() and inspect a field's nbttype before choosing its getter.
Type names are byte, short, int, long, float, double, byte_array, string, list,
compound and int_array. No tag differs from an empty compound.

## Paths

Paths are JSON Pointers relative to the item tag compound: /heat,
/display/Name or /list/0/value. Lists and byte/int arrays use zero-based indices.
Dots belong to a literal key, so /GT.Item/heat selects a GT.Item compound.
Escape a slash inside a key as ~1 and a tilde as ~0. Paths allow at most512
characters and16 segments. Leading-zero list indices are not aliases for an index.

Missing typed reads and wrong types fault; they do not invent zero or truncate
floating values. Use hasnbt for optional fields. nbttext displays scalar values;
it is not a complete binary NBT export. To inspect a compound, list its keys and
read children. Key lists refuse more than256 entries rather than silently truncate.

These operations read immutable snapshot bytes under shared work/allocation
limits. They do not load chunks, rescan slots, retain a mutable ItemStack or write
NBT. Exact-key comparison and transfer still use the complete original identity.
Known item-specific tags are inspectable; a universal remaining-durability adapter
and a graphical NBT tree are separate features.

Next: [contents](/automation/contents.md) or [performance](/reference/performance.md).
