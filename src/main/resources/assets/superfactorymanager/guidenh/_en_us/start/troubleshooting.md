---
navigation:
  title: "Nothing moving?"
  position: 50
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["help", "stopped", "empty", "first transfer", "troubleshooting"]
categories: ["sfm"]
updated: "2026-10-30"
---

# Nothing moving?

Open the manager and read its status first.

1. **Is the disk installed, and is the manager running?** Save the program,
   then press **Start** if it is paused.
2. **Is the destination already at its target?** The first program stops adding
   items when `dest` contains one. Remove that item to try again.
3. **Are the labels on the disk?** Copy the Label Gun table to the manager;
   check the exact names `source` and `dest`.
4. **Are both chests connected and loaded?** Follow the cable path.
   Use **Network > Rebuild network** after fixing a connection.
5. **Does the destination accept the item?** Try an empty ordinary chest first.
   Machines may require a particular face or slot.

**Expected:** after fixing the route, the next due timer moves an item.
The first program checks every twenty game ticks.

A compile error needs a source edit: open **Program > Edit code** and press
**F4** to find it. A recovery-blocked status needs
[recovery help](/reference/recovery.md); keep the disk and its record intact.

More detail: [PRINT, logs and live inspection](/reference/debugging.md).
