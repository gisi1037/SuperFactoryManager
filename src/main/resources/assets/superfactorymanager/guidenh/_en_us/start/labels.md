---
navigation:
  title: "Labels and cable networks"
  position: 80
  icon: "superfactorymanager:labelgun"
  parent: /start/index.md
  keywords: ["labels", "labelgun", "names", "network", "multiple containers"]
categories: ["sfm"]
updated: "2026-10-28"
item_ids: ["superfactorymanager:labelgun"]
---

# Labels and cable networks

A label is an exact name for a group of world positions. It is not an item ID or
a special variable. Several containers can share one label, and a container can
belong to several label groups. A program can therefore pull from a whole group
without listing each chest in its source.

## Label Gun controls

| Action | Result |
| --- | --- |
| Right-click air | Opens the gun's menu. |
| Right-click a normal block | Toggles the active label on that position. |
| Sneak-right-click a labelled normal block | Picks a label when a different one is active; clears that block's gun labels when the selected label is already active. Read chat before repeating. |
| Right-click a manager | Pushes the gun's complete table to the disk. |
| Sneak-right-click a manager | Pulls the disk's table into the gun. |
| Sneak + mouse wheel | Cycles the active label. |

Push and pull copy a table. A different gun can overwrite useful labels, so keep
a deliberate master copy. Editing the gun later does not automatically edit the
disk. If you replace the gun while its menu is open, reopen the menu for the new
gun before changing anything.

## Spelling and group behavior

Labels are case-sensitive. SFML keywords ignoring case does not make `Input`
the same label as `input`. Quote spaces, punctuation and keyword-like names:

```sfml
NAME "Quoted labels"
EVERY 20 TICKS DO
  INPUT FROM "Input chest"
  OUTPUT TO "Finished goods"
END
```

When several containers share a name, they form the selection for that clause.
Amounts normally apply across the selection; [EACH and RETAIN](/automation/quantities.md)
change how limits are divided. A name is not a promise that the group contains
only one container. [Contents snapshots](/automation/contents.md) record physical
origins and deduplicate aliases rather than counting the same chest slot twice.

## Connectivity and replacement

A labelled position must also be reachable through the manager's cable network
and loaded in the world. Labelling a distant chest neither connects nor loads it.
Cables do not bypass machine faces, filters or covers.

Use **Rebuild cable network** if a connection is not visible. The normal refresh
can take up to 21 ticks; this is separate from the program's timer. Repeatedly
rebuilding a large network costs work, so diagnose the cable path first.

Saved labels can remain after a block is replaced. Check the block now occupying
that position before restarting. `FORGET` clears active input registrations;
it does not delete saved labels.

## Labels as typed values

`LABEL("Input chest")` creates a `LABEL_REF` selection for typed queries and
`TRANSFER`. It does not create labels or discover disconnected blocks. A handle
can select sides and slots, but coordinates returned by a snapshot do not grant
permission to access an arbitrary block.

Next: [editors](editors.md) or [typed inventory queries](/automation/queries.md).
