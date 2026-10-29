---
navigation:
  title: "Your first transfer"
  position: 90
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["first", "beginner", "tutorial", "chest", "retain"]
categories: ["sfm"]
updated: "2026-10-28"
---

# Your first transfer

**Goal:** move one item into an empty destination, then keep that destination
stocked at one. You need a manager, disk, Label Gun, cable and two single chests.
See the [hardware layout](hardware.md) if you have not connected them yet.

## Assign the labels

1. Put a small countable stack in the source chest. Leave the other chest empty.
2. Insert the disk into the manager.
3. Right-click the Label Gun in air. Create/select the active label `source`.
4. Right-click the source chest with the gun to add that position.
5. Select `dest` in the gun and right-click the destination chest.
6. Right-click the manager with the gun **without sneaking** to copy the gun's
   label table onto its disk. Read the confirmation in chat.

Gun labels do not update the disk automatically after this copy. The program
reads the disk's saved table.

## Save the program

Open the manager, choose **Text**, and paste this complete program:

```sfml
NAME "First transfer"
EVERY 20 TICKS DO
  INPUT FROM source
  OUTPUT RETAIN 1 TO dest
END
```

Choose **Save & Close**, wait for the saved result, and enable the manager if it
is paused. Once the timer is due, the destination should contain one item and
the source should contain the remainder. Count both containers.

## Understand the result

`INPUT FROM source` registers the source inventory for this trigger. It does not
extract everything immediately. `OUTPUT ... TO dest` attempts the actual move.

`RETAIN 1` on an output is a stock target. When the destination already has one
matching item, further timer executions add none. Remove that item and the next
due execution can replace it. Change the output to `OUTPUT TO dest` if you want
to move all eligible stock the destination can accept.

`EVERY 20 TICKS` normally runs once per game second. Lag can make a game second
take longer in real time. Paused managers do not keep advancing their ordinary
running-tick timer.

## If nothing moves

Check the manager status and running state; confirm the disk has `source` and
`dest` in its saved labels; check cable connectivity and loaded chunks. The
destination may already meet the target. See
[troubleshooting](/reference/debugging.md) for a longer checklist.

Next: [labels](labels.md), then [INPUT and OUTPUT](/automation/input-output.md).
