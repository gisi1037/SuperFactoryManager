---
navigation:
  title: "Quantities and retention"
  parent: /automation/index.md
  position: 100
  keywords: ["sfm", "each", "retain", "quantity", "reserve"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Quantities and retention

Quantities limit movement; they do not promise that a complete batch will fit. EACH has two different placements: after a quantity it separates resource allowances, while after FROM/TO it separates block allowances.

| Fragment | Scope |
| --- | --- |
| `INPUT 8 FROM source` | Eight matching units shared across selected source blocks. |
| `INPUT 8 FROM EACH source` | Eight per selected source block. |
| `INPUT 8 EACH FROM source` | Eight per resource identity, shared across selected source blocks. |
| `OUTPUT 8 TO dest` | Eight for this output as a whole. |
| `OUTPUT 8 TO EACH dest` | Eight per selected destination block. |
| `OUTPUT 8 EACH TO dest` | Eight per resource identity for this output. |
| `RETAIN 8 EACH` | A separate reserve or target cap for each resource identity. |

You may combine quantity EACH and statement EACH. A block scope does not mean each slot, face or spelling of a label. Item quota identity is item plus metadata; different NBT variants share that allowance. Exact stack compatibility still matters when inserting or merging them. Exact keys from contents have a stricter identity than these quotas.

RETAIN means different things in each direction:

- INPUT RETAIN leaves matching stock in source views. Only stock above the reserve is eligible to move.
- OUTPUT RETAIN fills matching destination stock toward a cap, including items already there. It does not remove an existing excess.

Without statement EACH, selected blocks share the reserve/cap. Without resource EACH, the resources matching that limit share it. Selecting more slots or faces does not multiply retention.

```sfml
NAME "Reserve supply and stock machines"
EVERY 20 TICKS DO
  INPUT 64 RETAIN 16 item:minecraft:iron_ingot FROM supply
  OUTPUT 8 RETAIN 16 item:minecraft:iron_ingot TO EACH machines
END
```

This leaves sixteen ingots across the selected source scope. At most sixty-four additional ingots can pass through that INPUT during the trigger. Each destination may receive at most eight during this OUTPUT and stops at sixteen existing-plus-new ingots. The shared source allowance, actual stock, free space and access rules may reduce those amounts.

```sfml
NAME "Independent resources"
EVERY 20 TICKS DO
  INPUT 8 EACH RETAIN 4 EACH item:minecraft:iron_ingot OR item:minecraft:gold_ingot FROM supply
  OUTPUT 8 EACH TO dest
END
```

Here iron and gold each get an eight-unit movement allowance and a four-unit source reserve. OR without EACH would share the quantity. Two comma-separated resource limits can also express independent allowances; when their selectors overlap, the first matching limit wins.

Computed quantities and RETAIN values use parentheses and must evaluate to nonnegative integers. An input's values are captured on registration and its allowance is shared across all later outputs until forgotten or the trigger ends. A new trigger run starts fresh. [INPUT and OUTPUT](/automation/input-output.md) explains that lifetime.

For fluids the same quantity syntax counts millibuckets:

```sfml
NAME "Water buffer"
EVERY 20 TICKS DO
  INPUT 1000 fluid:water FROM water_source
  OUTPUT RETAIN 4000 fluid:water TO water_buffer
END
```

This attempts at most one thousand millibuckets per run while filling toward four thousand. Verify the particular handler and access face before relying on a tank or cover. [TRANSFER](/automation/transfers.md) has its own explicit amount and does not inherit any of these limits.
