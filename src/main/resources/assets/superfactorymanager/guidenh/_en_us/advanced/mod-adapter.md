---
navigation:
  title: "ME network adapter"
  position: 28
  icon: "superfactorymanager:modAdapter"
  parent: /advanced/index.md
  keywords: ["AE2", "ME", "interface", "adapter", "network", "storage", "fluid"]
categories: ["sfm"]
updated: "2026-11-08"
item_ids: ["superfactorymanager:modAdapter"]
---

# Access an ME network

The **Mod Storage Adapter** lets SFM read and move stored items and fluids through
an adjacent ME Interface. Its recipe targets late HV. AE2 is optional.

<BlockImage id="superfactorymanager:modAdapter" scale="2" />

## Set up the adapter

1. Place the adapter against an **unconfigured ME Interface**. It faces the block
   you placed it against. Block and multipart interfaces are supported.
2. Connect SFM cable to the adapter. Use the Label Gun to name the adapter
   `me`, and copy that label to the manager's disk.
3. Keep the ME network powered and the Interface's channel active. Leave the
   Interface's stocking configuration empty so it exposes network storage.
4. Give the player who placed the adapter ME **Build, Extract and Inject**
   permissions. That player's identity is saved on the adapter for automation.

Sneak-right-click the adapter with an empty hand to cycle its target face.
The chat message names the new face. Each adapter targets one neighbor.
Right-click without sneaking to see its facing and ME access status in chat.
If the access check passes but a manager query fails, check the manager's cable
connection and every block assigned to the label on its disk.

## Read stock and move resources

Name ordinary chests `inputs` and `outputs`, and ordinary tanks `water_source`
and `water_destination`. Then try the operations below one at a time.

```sfml
name "ME storage"
every 20 ticks {
  let me = label("me")
  print(me.available(), me.count(item("minecraft:iron_ingot")))
  let stored = me.contents(item("minecraft:iron_ingot"))
  print(stored)
  transfer(label("inputs"), me, 16) into inserted
  transfer(me, label("outputs"), item("minecraft:iron_ingot"), 8) into extracted
  print(inserted.status, extracted.moved)

  print(me.favailable(), me.fcount(fluid("water")))
  transferf(label("water_source"), me, fluid("water"), 1000)
  transferf(me, label("water_destination"), fluid("water"), 250)
}
```

Item amounts are items. Fluid amounts are millibuckets. The adjacent Interface
must expose AE's fluid storage monitor for the fluid operations.

The adapter uses ME's native simulation, extraction and insertion APIs, including
ME power costs. A full destination can accept only part of a request. Check
`into` results when a program needs confirmed progress.

Queries are fresh, so a later query in the same tick sees an earlier successful
move. Empty and unavailable remain distinct. Two adapters on the same ME network
are counted once within a label group, and transfers between them move nothing.

## Limits

Use `transfer()`, `transferf()` and the query methods with this block.
Legacy `input`/`output` statements and the exact-transfer helpers currently
require other supported storage.

ME entries have no physical inventory slot. Adapter `.slots()` selections are
refused. Contents location indexes identify snapshot rows and must not be used
as persistent machine slots. Use the captured item or fluid key for a transfer.

A network scan is bounded to 1024 stored variants per resource type, with the
normal shared program work limits. Item `.contents()` also retains its
256-group result limit. Oversized scans fail explicitly. Counts use 64-bit
integers. Craftable-only entries are excluded from stored stock.

Keep the adapter and Interface loaded. Removing the Interface, changing its
stocking configuration, losing power/channel access or revoking permissions
invalidates access. SFM's transfer recovery rules apply if a provider fails
during an operation.
