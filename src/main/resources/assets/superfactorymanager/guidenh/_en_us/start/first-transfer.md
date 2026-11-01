---
navigation:
  title: "Your first transfer"
  position: 80
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["first", "beginner", "tutorial", "chest", "retain"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Your first transfer

**Goal:** move one item into an empty destination, then keep that destination
stocked at one. You need a manager, disk, Label Gun, cable and two single chests.
First [build the network](hardware.md) and [name the chests](labels.md).

## Prepare the chests

Put eight of one item in `source`. Leave `dest` empty.
The arrow shows the intended transfer; the preview does not move real items.

<GameScene width="260" height="150" interactive={true}>
  <Block id="superfactorymanager:manager" x="0" y="0" z="0" />
  <Block id="superfactorymanager:cable" x="1" y="0" z="0" />
  <Block id="minecraft:chest" x="2" y="0" z="0" />
  <Block id="superfactorymanager:cable" x="1" y="0" z="1" />
  <Block id="minecraft:chest" x="1" y="0" z="2" />
  <TextAnnotation pos="2.5 1.2 0.5" text="source: 8" color="#66CCFF" />
  <TextAnnotation pos="1.5 1.2 2.5" text="dest: empty" color="#FFCC66" />
  <LineAnnotation from="2.5 1.1 0.5" to="1.5 1.1 2.5" arrow="end" color="#66CCFF" />
</GameScene>

## Save the program

Open the manager, choose **Program > Edit code**, and paste this complete program:

```sfml
name "First transfer"
every 20 ticks {
  input from "source";
  output retain 1 to "dest";
}
```

Choose **Save & Close**, wait for confirmation, then press **Start** if paused.

**Expected:** after the timer runs, `source` has **7**, and `dest` has **1**.
Remove the destination item. The next due run replaces it, leaving **6** in source.

## Understand the result

`input` selects the source; it leaves the items there until `output`
attempts the move. `retain 1` keeps the destination's total at **one**. If it already has one,
nothing moves. It also leaves any existing excess in place.

`every 20 ticks` normally runs once per game second. Lag can make a game second
take longer in real time.

## If nothing moves

Follow [Nothing moving?](troubleshooting.md).

**Next: [change your route](change-route.md)** with a small batch, an item filter
or a different stock target. You can also [choose an editor](editors.md).
