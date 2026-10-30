---
navigation:
  title: "Name the chests"
  position: 90
  icon: "superfactorymanager:labelgun"
  parent: /start/index.md
  keywords: ["labels", "labelgun", "names", "source", "dest"]
categories: ["sfm"]
updated: "2026-10-30"
item_ids: ["superfactorymanager:labelgun"]
---

# Name the chests

A label tells the program **which block to use**. The first program needs
two names: `source` and `dest`.

<ItemImage id="superfactorymanager:labelgun" scale="3" />

## Label both chests

1. Right-click the Label Gun in air. Create/select `source`.
2. Right-click the chest that will supply items. Read the chat confirmation.
3. Open the gun again and select `dest`.
4. Right-click the empty destination chest.

**Expected:** the gun now has one chest under each name. A normal right-click
toggles a label, so clicking the same chest again can remove it.

## Copy the labels to the disk

<ItemImage id="superfactorymanager:manager" scale="2" />
<ItemImage id="superfactorymanager:disk" scale="2" />

Insert the disk in the manager. Right-click that manager with the Label Gun
**without sneaking**.

**Expected:** chat confirms that the gun's table was copied to the disk.
Editing the gun afterward does not automatically update the disk.

Labels are case-sensitive. Use `source` and `dest` exactly as shown.
Both chests still need a cable connection.

**Next: [run your first transfer](first-transfer.md).**

Later: [multiple containers, gun controls and network rules](/advanced/labels.md).
