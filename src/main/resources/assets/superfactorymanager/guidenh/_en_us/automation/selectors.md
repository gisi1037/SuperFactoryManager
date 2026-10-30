---
navigation:
  title: "Resource selectors"
  parent: /automation/index.md
  position: 110
  keywords: ["sfm", "item", "fluid", "ore", "selector", "wildcard"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Resource selectors

Resource selectors choose what INPUT, OUTPUT and HAS consider. Use registered identifiers rather than translated item names. Labels remain case-sensitive. The legacy resource-name and ore-name glob matchers ignore ASCII letter case, while the `item:` and `fluid:` type prefixes must be lower-case. Typed ITEM/ORE selectors described below instead compare their identifiers exactly.

| Fragment | Selects |
| --- | --- |
| `item:minecraft:iron_ingot` | That registered item, all metadata/NBT variants. |
| `minecraft:iron_ingot` | That namespace/name without an item-only type restriction. |
| `item:*:*` | Every item. |
| `fluid:water` | The Forge fluid named water. |
| `"item:minecraft:wool:14"` | Wool with metadata 14. |
| `"item:some_mod:part_*"` | Items whose registered names match this glob. |
| `"#ore:ingotIron"` | Items in the named OreDictionary category. |

`*` matches any number of characters and `?` matches one. Other characters are literal; patterns are not Java regular expressions. Quote punctuation and numeric metadata forms that cannot be read as ordinary identifiers. Metadata must be a nonnegative signed-32-bit integer or a supported wildcard spelling; malformed metadata fails rather than silently broadening the selector. Forge's wildcard metadata value also matches every damage variant.

Omitting a filter matches both supported item and fluid domains. Fluid matching uses the Forge fluid's registered name; use `fluid:water` rather than assuming modern namespaced-fluid or fluid-NBT semantics. Fluid quantities are millibuckets. Access modifiers do not select individual tanks.

OR puts alternatives in one allowance. Commas introduce separate limits:

```sfml
NAME "Two allowances"
EVERY 20 TICKS DO
  INPUT 32 item:minecraft:iron_ingot OR item:minecraft:gold_ingot FROM metals
  OUTPUT TO dest
  FORGET
  INPUT 8 item:minecraft:iron_ingot, 4 item:minecraft:gold_ingot FROM metals
  OUTPUT TO reserve
END
```

The first stage allows thirty-two ingots combined. The second allows eight iron and four gold. Overlapping limits use the first matching limit, so keep them disjoint unless that priority is intentional. [Quantity EACH](/automation/quantities.md) is another way to give resources independent allowances.

EXCEPT removes matches after the normal limits, before FROM or TO:

```sfml
NAME "Exclude cobblestone"
EVERY 20 TICKS DO
  INPUT 8 item:*:* EXCEPT item:minecraft:cobblestone FROM miscellaneous
  OUTPUT TO overflow
END
```

WITH TAG and WITHOUT TAG refer to the 1.7.10 OreDictionary. They are not arbitrary NBT predicates or modern datapack tags:

```sfml
NAME "Ore membership"
EVERY 20 TICKS DO
  INPUT 16 item:*:* WITH TAG ingotIron FROM supply
  OUTPUT TO reserve
END
```

`WITHOUT TAG ingotIron` selects the complement. Use one tag condition: compound or parenthesized WITH expressions are rejected. Quoted `"#ore:..."` resource selectors strip the optional lower-case `ore:` prefix and support ore-name glob matching. WITH TAG also uses a glob matcher, but takes the supplied name directly without stripping that prefix. Both legacy tag forms ignore ASCII letter case; typed ORE does not.

Typed data constructors use a narrower interface. `ITEM("minecraft:iron_ingot")` and `ORE("ingotIron")` create RESOURCE_SELECTOR values for queries and TRANSFER. ITEM accepts an exact ID or `"*"`; it does not accept `item:`, metadata, glob patterns or NBT filters. ORE accepts one exact ore name. Both take quoted source literals, not computed strings. [Item queries](/automation/queries.md) describes those types. An exact [ITEM_KEY](/automation/contents.md) additionally distinguishes metadata and NBT.
