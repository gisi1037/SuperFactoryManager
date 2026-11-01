---
navigation:
  title: "Choose an editor"
  position: 70
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["editor", "visual", "code", "undo", "save", "text"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Choose an editor

All SFM programs use **SFML**. You can keep using the manager's Text editor;
a Programming Station is optional.

## Text: paste or write a program

Open **Manager > Program > Edit code** for an ordinary source disk.
Paste a program, then choose **Save & Close** and wait for confirmation.

**Ctrl+Z** undoes an edit. **F4** jumps to the first compile error.
Saving and pressing **Start** are separate steps.

## Visual: connect a simple route

Start with the [first transfer](first-transfer.md) program installed. Open
**Manager > Program > Visual** to see it as nodes in this execution order:

**Timer → Input → Output**

The timer starts the route, Input selects the source, and Output attempts the
move. Select a node to edit its clause and apply the change. Keep the execution
pins connected in that order, then save.

**Expected:** the saved route still stocks `dest` at one, just as the Text
program did. If Visual cannot represent a program, it preserves the source;
continue in Text. See [Visual controls](/advanced/editors.md) when you need them.

## Studio: save named projects

<BlockImage id="superfactorymanager:programmingStation" scale="3" />

The [Programming Station](station.md) keeps named projects and offers
**Visual / Split / Code** views. Choose it when you want to keep reusable source.
It opens a separate full-screen editor without player inventory slots.

Next: [change your route](change-route.md), or [save a Studio project](station.md).
Need help? [Nothing moving?](troubleshooting.md).
