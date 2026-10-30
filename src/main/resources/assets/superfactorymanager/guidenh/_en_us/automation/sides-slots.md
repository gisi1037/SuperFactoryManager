---
navigation:
  title: "Sides, slots and round robin"
  parent: /automation/index.md
  position: 90
  keywords: ["sfm", "side", "slot", "round robin", "label access"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Sides, slots and round robin

The side is the accessed block's physical face. It does not refer to the manager's facing. Ordinary statement access lists labels, then optional round robin, sides and slots in that order:

```sfml
NAME "Explicit access"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM source TOP SIDE SLOTS 0-8
  OUTPUT TO dest BOTTOM SIDE SLOT 1
END
```

Statement-side names are TOP, BOTTOM, NORTH, SOUTH, EAST, WEST and NULL, followed by SIDE. Select several with `NORTH, WEST SIDE`. EACH SIDE selects the face views without multiplying identical physical stock or quotas; an omitted side also uses the default all-face selection. NULL SIDE means unsided/unknown access and does not bypass a sided machine's rules. LEFT, RIGHT, FRONT and BACK are rejected because relative orientation is not implemented.

Slot indices start at zero and refer to backend inventory indices, which may differ from GUI order. Ranges include both endpoints. `SLOTS 0-8, 12, 20-22` selects a set. Selected slots must still be exposed by the chosen face, and extraction/insertion permissions still apply. Check circuit slots, reserved slots and covers explicitly. Slot selection does not select individual fluid tanks.

TO EMPTY SLOTS IN prevents an item output from topping up occupied slots:

```sfml
NAME "Empty slots only"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM source
  OUTPUT TO EMPTY SLOTS IN dest TOP SIDE SLOTS 0-8
END
```

This is an insertion restriction, not a fullness test. Empty slots may still refuse the item.

ROUND ROBIN BY BLOCK chooses one unique block from the supplied labels whenever that access resolves. BY LABEL chooses one label, which may contain many blocks:

```sfml
NAME "Rotate blocks"
EVERY 20 TICKS DO
  INPUT 4 item:minecraft:cobblestone FROM source
  OUTPUT TO machines ROUND ROBIN BY BLOCK
END
```

The cursor advances on resolution, not successful movement. A disconnected, full or otherwise unusable selection can receive nothing. Round robin does not search for the next available machine. Its compiled expression owns the cursor; it is not a persistent fairness ledger across recompilation or restart.

Typed label values use methods instead of these statement modifiers:

```sfml
NAME "Observe a selected view"
EVERY 20 TICKS DO
  LET $whole = LABEL("source")
  LET $selected = $whole.side("north").slots(0, 8)
  IF ($selected.available()) THEN
    PRINT($selected.count(ITEM("minecraft:iron_ingot")))
  END
END
```

`.side()` takes one quoted absolute direction: down/up/north/south/west/east, case-insensitive; bottom/top are aliases. It accepts no relative direction, NULL or computed string. `.slots(first, last)` takes inclusive integer expressions satisfying `0 <= first <= last <= 2147483647`. Invalid evaluated bounds fault. A combined chest uses a stable order with the west/north half first.

Each modifier returns an immutable new LABEL_REF; `$whole` remains unrestricted. Repeating side or slots replaces that restriction while preserving the other restriction: the last call of each kind wins. There is no typed round-robin modifier. A typed handle does not change a separate INPUT/OUTPUT/HAS statement with the same label. [Queries](/automation/queries.md) and [TRANSFER](/automation/transfers.md) use these handles.
