---
navigation:
  title: "Build the network"
  position: 100
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["manager", "disk", "cable", "inventory", "hardware"]
categories: ["sfm"]
updated: "2026-10-30"
item_ids: ["superfactorymanager:disk", "superfactorymanager:cable", "superfactorymanager:fancyCable"]
---

# Build the network

Gather **one manager, one blank disk, a Label Gun, two cables and two single
chests**. NEI shows the recipes installed by your pack.

## What you need

| Part | Purpose |
| --- | --- |
| Super Factory Manager | Holds the execution disk, runs SFML and exposes program status. |
| Program disk | Holds the program and its saved labels. |
| Label Gun | Names positions and copies a label table to or from a manager's disk. |
| Cable / Fancy Cable | Connects the manager to eligible blocks; both participate in its cable network. |

<ItemImage id="superfactorymanager:disk" />
<ItemImage id="superfactorymanager:labelgun" />
<ItemImage id="superfactorymanager:cable" />
<ItemImage id="superfactorymanager:fancyCable" />

## Connect the blocks

Place a manager, run cable from it, and connect two chests to the cable. Give each
chest its own branch so a route never depends on passing through an inventory.
The demonstration below shows the layout; it does not place blocks or assign
labels in your world.

<GameScene width="260" height="150" interactive={true}>
  <Block id="superfactorymanager:manager" x="0" y="0" z="0" />
  <Block id="superfactorymanager:cable" x="1" y="0" z="0" />
  <Block id="minecraft:chest" x="2" y="0" z="0" />
  <Block id="superfactorymanager:cable" x="1" y="0" z="1" />
  <Block id="minecraft:chest" x="1" y="0" z="2" />
  <TextAnnotation pos="0.5 1.2 0.5" text="Manager" color="#B4D89B" />
  <TextAnnotation pos="2.5 1.2 0.5" text="Source" color="#66CCFF" />
  <TextAnnotation pos="1.5 1.2 2.5" text="Destination" color="#FFCC66" />
</GameScene>

**Expected:** both chests touch cable, and that cable reaches the manager.
The manager does not load distant chunks.

## Insert the disk

Open the manager and place the blank disk in its disk slot.
Leave it paused while setting up this first route.

**Expected:** the manager shows an installed disk. **Program** contains its
editing controls. Your normal inventory and hotbar stay below the machine pages.

**Next: [name the chests](labels.md).**
