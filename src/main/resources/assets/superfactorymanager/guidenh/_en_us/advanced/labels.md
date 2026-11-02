---
navigation:
  title: "Labels and cable networks"
  position: 40
  icon: "superfactorymanager:labelgun"
  parent: /advanced/index.md
  keywords: ["labels", "labelgun", "names", "network", "multiple containers"]
categories: ["sfm"]
updated: "2026-11-01"
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

## Edit labels from a cable

1. Right-click our full-block or Fancy Cable **with an empty hand**.
2. Choose a Manager. Shared networks can have several; the tool never guesses.
3. Wait for discovery. Select connected blocks, across pages if needed.
4. Type an exact label and choose **Add label** or **Remove**.

These edits go directly to that Manager's installed disk. Other label groups,
program source and runtime STATE stay intact. Several selected blocks can share
one label, and adding a label keeps their other labels. Removing an assignment
can change what a running program finds; it does not rename source references.

**Search** uses cached coordinates, block IDs and every label. Long row text ends
in `...`; shortening the display never removes labels or limits the search.
Select up to 32 blocks per edit. Discovery supports up to 64 candidate Managers
and 8,192 connected blocks, with work spread across server ticks. Unloaded chunks
are not opened. If the disk, labels or network changes, refresh and choose again.

The gun has its own saved table. After cable edits, **sneak-right-click the
Manager with the gun to pull the updated table** before using that gun to push
again. An old gun table can overwrite your newer disk labels.

## Route from a group of chests

Give two single source chests the same label, `supply`, and give an empty
destination the label `dest`. Connect all three, then push the gun's table to
the Manager. The preview illustrates shared label membership and intended item
flow; it does not assign labels or move items in your world.

<GameScene width="260" height="180" interactive={true}>
  <Block id="superfactorymanager:manager" x="0" y="0" z="0" />
  <Block id="superfactorymanager:cable" x="1" y="0" z="0" />
  <Block id="minecraft:chest" x="2" y="0" z="0" />
  <Block id="superfactorymanager:cable" x="1" y="0" z="1" />
  <Block id="minecraft:chest" x="1" y="0" z="2" />
  <Block id="superfactorymanager:cable" x="1" y="0" z="-1" />
  <Block id="minecraft:chest" x="1" y="0" z="-2" />
  <TextAnnotation pos="0.5 1.2 0.5" text="Manager" color="#B4D89B" />
  <TextAnnotation pos="2.5 1.2 0.5" text="supply: A" color="#66CCFF" />
  <TextAnnotation pos="1.5 1.2 2.5" text="supply: B" color="#66CCFF" />
  <TextAnnotation pos="1.5 1.2 -1.5" text="dest" color="#FFCC66" />
  <LineAnnotation from="2.5 1.1 0.5" to="1.5 1.1 -1.5" arrow="end" color="#66CCFF" />
  <LineAnnotation from="1.5 1.1 2.5" to="1.5 1.1 -1.5" arrow="end" color="#66CCFF" />
</GameScene>

Put eight iron ingots in each source chest. Copy this complete program:

```sfml
name "One shared supply group"
every 20 ticks {
  input 8 item:minecraft:iron_ingot from "supply"
  output retain 8 item:minecraft:iron_ingot to "dest"
}
```

**Expected:** after a successful due run, dest has eight iron ingots and supply
has eight remaining in total. The program does not promise an equal draw from
both source chests. Further runs leave dest at eight. The quantity is shared
across the group; [EACH and RETAIN](/automation/quantities.md) changes its scope.

## Spell literal names exactly

Labels are case-sensitive. SFML keywords ignoring case does not make `Input`
the same label as `input`. Quote every literal label, including simple names.
Unquoted names now mean variable references; older programs must convert.
There is no compatibility fallback from an unknown variable to a label:

```text
input from "Input chest"
output to "Finished goods"
```

When several containers share a name, they form the selection for that clause.
Amounts normally apply across the selection. A name is not a promise that the
group contains only one container. [Contents snapshots](/automation/contents.md) record physical
origins and deduplicate aliases rather than counting the same chest slot twice.

## Connectivity and replacement

A labelled position must also be reachable through the manager's cable network
and loaded in the world. Labelling a distant chest neither connects nor loads it.
Cables do not bypass machine faces, filters or covers.

Use **Network > Rebuild network** if a connection is not visible. The normal refresh
can take up to 21 ticks; this is separate from the program's timer. Repeatedly
rebuilding a large network costs work, so diagnose the cable path first.

Saved labels can remain after a block is replaced. Check the block now occupying
that position before restarting. `FORGET` clears active input registrations;
it does not delete saved labels.

## Inspect the network

Open the manager's **Network** tab. Search names, labels or coordinates, and use
**All / Items / Fluids** to filter the list. **Refresh** reads cached discovery;
**Rebuild network** explicitly discovers cable connections again.

The list includes unlabeled connected blocks and saved positions that are now
missing, unloaded or disconnected. An **unconfirmed** row means discovery is
stale. A listed inventory does not guarantee that a particular face allows a
transfer. This view reads metadata, not item contents or fluid amounts.

Use **Next / Previous** to browse. Filtering can produce an empty page with more
candidates still ahead. Large networks may show a partial index. A page request
inspects at most 128 candidates and returns up to eight rows; a snapshot lasts
30 seconds of game time. Refresh after it expires. These limits keep browsing
bounded without loading chunks or repeatedly scanning the whole network.

## Labels as typed values

`LABEL("Input chest")` creates a `LABEL_REF` selection for typed queries and
`TRANSFER`, as well as ordinary IO/HAS label operands. LABEL accepts STRING
expressions, so `label("chest" + index)` can select numbered groups. A plain
STRING variable is not itself a label handle. Resolved names must be nonempty,
valid Unicode, at most 256 characters and have no surrounding whitespace.
Computed names use the invocation's bounded label snapshot.

LABEL does not create labels or discover disconnected blocks. A handle
can select sides and slots, but coordinates returned by a snapshot do not grant
permission to access an arbitrary block.

Next: [editors](/start/editors.md) or [typed inventory queries](/automation/queries.md).
