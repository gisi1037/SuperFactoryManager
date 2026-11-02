---
navigation:
  title: "Copy program disks"
  position: 90
  icon: "superfactorymanager:diskCopier"
  parent: /advanced/index.md
  keywords: ["copy", "copier", "disk", "duplicate", "blank"]
categories: ["sfm"]
updated: "2026-11-01"
item_ids: ["superfactorymanager:diskCopier"]
---

# Copy program disks

Use a **Disk Copier** when several managers should run the same program.
NEI shows its late-HV recipe. It needs no operating EU.

<ItemImage id="superfactorymanager:diskCopier" />
<ItemImage id="superfactorymanager:disk" />

1. Put the programmed disk in **Source** in the center.
2. Put up to **eight fresh blank disks** in the left grid.
3. Wait **one second** while the copier is loaded, with default server settings.
4. Take the new disks from **Copies** on the right.

**Expected:** the source stays in place. One blank becomes one copy.
Each cycle makes up to eight independent copies. When the output grid fills up,
remove copies to continue. Partial space uses only as many blanks as can fit.

## What carries over?

The copy keeps the exact program or published build reference and **all saved
labels**. It starts with fresh runtime STATE and its own execution clock.
Changing labels on the copy does not change the source.

A reference copy can execute the same published revision. It does not grant
access to the author's private project or library source. Copying does not
repair a missing published build. Source disks copy their text verbatim.

## Automate it

| Face | Action |
| --- | --- |
| Top | Insert source disk. |
| Horizontal sides | Insert fresh blank media. |
| Bottom | Extract completed copies. |

The source is only removable manually. Disks do not stack.

Server owners can change `diskCopierTicksPerCycle` (default 20) and
`diskCopierCopiesPerCycle` (default 8) in the `machines` category of
`config/superfactorymanager.cfg`, then restart the server. A smaller batch limit
automatically processes the queued blanks over several cycles. The GUI shows
the server's timing and batch limit.

## If copying stops

Read the status in the machine. Use a fresh blank, clear the output, and resolve
any recovery record on the source first. Malformed or oversized disk metadata
is refused without consuming the blank. Correct and reinsert the source to retry.

Changing slots or unloading the copier cancels progress without consuming media.
A new full cycle starts after loading again. Custom names, foreign metadata,
recovery evidence and private project ownership are not copied.

Return to [programming tools](index.md).
