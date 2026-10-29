---
navigation:
  title: "FORGET and input stages"
  parent: /automation/index.md
  position: 20
  keywords: ["sfm", "forget", "input", "stage", "quota"]
categories: ["sfm", "automation"]
updated: "2026-10-28"
---
# FORGET and input stages

FORGET clears the active INPUT declarations in the current trigger. Use it when a later output must not draw from an earlier stage. It does not delete disk labels, remove blocks, discard resources, clear variables, reset STATE or change destination stock.

```sfml
NAME "Independent iron and gold stages"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM iron_source
  OUTPUT TO iron_dest
  FORGET
  INPUT 4 item:minecraft:gold_ingot FROM gold_source
  OUTPUT TO gold_dest
END
```

Without FORGET, the iron input would still be active when gold_dest is processed. Here the second stage can use only its new gold declaration. There is no hidden inventory to empty: INPUT registered access, and previous outputs already performed any real movement.

`FORGET source_a` removes that label from active source accesses while retaining their other labels. A comma-separated label list is accepted. Label spelling and case must match the active access:

```sfml
NAME "Remove one source label"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM source_a, source_b
  OUTPUT 2 TO priority
  FORGET source_a
  OUTPUT TO overflow
END
```

The final output can still draw from source_b, using the original declaration's remaining allowance. Removing one label does not renew that allowance. If priority moved two ingots, at most six remain under this input's quota. A new INPUT creates its own allowance.

Only executed statements matter: FORGET inside a branch runs when that branch is chosen. Branch exit does not automatically clear declarations. All active inputs are cleared at the end of each trigger invocation, so separate triggers do not need FORGET to isolate one another.

FORGET does not control [TRANSFER](/automation/transfers.md). That statement uses its own explicit source, destination, selector and amount, independently of active input registrations. See [INPUT and OUTPUT](/automation/input-output.md) for declaration lifetime and [quantities](/automation/quantities.md) for shared quotas.
