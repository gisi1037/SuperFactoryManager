---
navigation:
  title: "Choose an editor"
  position: 70
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["editor", "visual", "code", "undo", "save", "text"]
categories: ["sfm"]
updated: "2026-10-30"
---

# Choose an editor

All SFM programs use **SFML**. Choose the view that helps you write it.

## Copy or write code

Open **Manager > Program > Edit code** for an ordinary source disk.
Paste a program, then choose **Save & Close** and wait for confirmation.

**Ctrl+Z** undoes an edit. **F4** jumps to the first compile error.
Saving and pressing **Start** are separate steps.

## Build a simple graph

Open **Program > Visual**. Add a Timer, Input and Output, then connect their
execution pins in that order. Edit each node's clause and apply it.

A basic route is **Timer → Input → Output**. Every node must connect to a
trigger before saving. The detailed [Visual instructions](/advanced/editors.md)
explain pins, node fields and controls.

Variables, loops and other advanced programs may require text.
If Visual cannot represent the whole program, it keeps the source intact.

## Keep projects in Studio

<BlockImage id="superfactorymanager:programmingStation" scale="3" />

The [Programming Station](station.md) has **Visual**, **Split** and **Code**
views and saves named projects. It has no player inventory while open.

A published reference disk runs an immutable build. Change its project in
Studio and imprint a fresh disk to deploy a different build.

More: [editor controls and save recovery](/advanced/editors.md).
