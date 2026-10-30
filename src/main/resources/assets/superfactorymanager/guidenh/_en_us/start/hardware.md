---
navigation:
  title: "Hardware and the manager"
  position: 100
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["manager", "disk", "cable", "inventory", "hardware"]
categories: ["sfm"]
updated: "2026-10-28"
item_ids: ["superfactorymanager:disk", "superfactorymanager:cable", "superfactorymanager:fancyCable"]
---

# Hardware and the manager

## What each part does

| Part | Purpose |
| --- | --- |
| Super Factory Manager | Holds the execution disk, runs SFML and exposes program status. |
| Program disk | Carries source, saved labels and persistent STATE. |
| Label Gun | Names positions and copies a label table to or from a manager's disk. |
| Cable / Fancy Cable | Connects the manager to eligible blocks; both participate in its cable network. |
| Programming Station | Saves, validates and publishes private project builds. Imprints exact published builds onto fresh held disks; managers load their code from world storage. |
| Redstone Port | A combined signal detector and emitter with per-face settings. |

<ItemImage id="superfactorymanager:disk" />
<ItemImage id="superfactorymanager:labelgun" />
<ItemImage id="superfactorymanager:cable" />
<ItemImage id="superfactorymanager:fancyCable" />

Use NEI's recipe/usage views for the recipes installed by your pack or server.
Do not assume another modpack uses the same crafting ingredients.

## A small network

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
</GameScene>

The chest at the end of the short branch can be `source`; the other can be
`dest`. Each needs a saved label and cable connectivity. A label by itself is
not a connection. The manager is not a chunk loader.

## Manager workflow

Insert a disk, synchronize labels, then choose **Text** or **Visual**. Saving a
program and enabling its running state are separate steps. Keep a production
manager paused while changing its routing, then check the first transfer.

The manager also provides **Terminal**, **View logs**, network rebuilding and
STATE inspection. Read its status when automation stops. Recovery or invalid
state can block execution even when the source text compiles.

With Dynamic Inventory installed, the manager uses the actual expanded player
inventory. Its player-storage area is scrollable and the hotbar remains separate.
Changing GUI scale changes layout, not the meaning of SFML slot numbers: those
numbers select the attached container, not your player inventory.

Continue with [your first transfer](first-transfer.md).
