---
navigation:
  title: "Super Factory Manager"
  position: 200
  icon: "superfactorymanager:manager"
  keywords: ["sfm", "sfml", "factory", "manual", "help"]
  recommend: 1
  required_mod: superfactorymanager
categories: ["sfm"]
updated: "2026-10-28"
item_ids: ["superfactorymanager:manager"]
---

# Super Factory Manager

Super Factory Manager (SFM) runs small programs that route resources through a
cable network. SFML is the language used by both the Text and Visual editors.
Start with a single chest-to-chest transfer, then add limits, conditions and data
as your factory needs them.

This book describes this GTNH port. Examples use its current SFML syntax; other
versions of SFM may have different features or spellings.

## Choose a starting point

- [Getting started](/start/index.md): hardware, your first transfer and labels.
- [Automation](/automation/index.md): items, fluids, queries and redstone.
- [The SFML language](/language/index.md): variables, arrays, structs, loops and functions.
- [Reference and troubleshooting](/reference/index.md): defaults, examples, diagnostics and limits.

## Reading and copying examples

GuideNH's search and navigation sidebar find topics throughout the book. Hold
your configured guide key over an indexed SFM item to jump to its page. The
default key is **G**; check Controls if another mod uses it.

Code blocks marked **sfml** are complete programs. Use the copy button in the
block, open the manager's Text editor and paste. Code blocks marked **text** are
syntax fragments or terminal commands and need their surrounding program.
Assign every example label to real connected blocks before running it.

## The two programming styles

`INPUT` and `OUTPUT` make concise routing programs. `INPUT` registers eligible
sources; each later `OUTPUT` attempts transfers using those registrations.

Typed values provide more detailed control. `LABEL("storage")` is an inventory
selection, `.contents()` captures exact item variants and origins, and `TRANSFER`
requests a self-contained item move with a result record. Ordinary `FUNCTION`
calls calculate values and perform read-only observations.

You can combine the styles in Text. The Visual editor opens only programs it can
represent completely. See [editor compatibility](/start/editors.md) before
switching an advanced program back to nodes.

## Available now and planned later

Local and persistent variables, strings, fixed arrays/structs, arithmetic,
bitwise operations, bounded loops, typed player functions, PRINT, item queries,
grouped contents, value-driven item transfers and Redstone Ports are available.
The Programming Station saves and validates private drafts and publishes immutable
root-source builds. The Factory Manager has separate split player-storage views.

Reusable published libraries, reference-only execution disks, effectful player
actions, recipe-query/NEI APIs and Lua are still planned. The
[feature boundary](/reference/status.md) explains the distinctions.
