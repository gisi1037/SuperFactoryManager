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

Direct recipes target late HV: stainless steel, advanced circuits, HV components
and advanced epoxy boards replace the old vanilla-material recipes. Use plain,
unprogrammed ingredients; recipes refuse saved NBT rather than erase it.

You can freely convert one plain Steve's Factory Manager controller or cable
into our matching controller or full-block cable and back. Our full-block and
Fancy cables also convert1:1. With Steve's Addons, its plain Labeler and our
Label Gun convert both ways. Original pack recipes and both cable shapes remain
visible in NEI. These swaps retain the legacy recipes' progression.

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

For cable conversion, put our full cable in the left column of a 3x3 crafting
grid to return a legacy cable, or the right column for Fancy Cable. Either
converts back to our full cable in any slot. Use plain, untagged items.
