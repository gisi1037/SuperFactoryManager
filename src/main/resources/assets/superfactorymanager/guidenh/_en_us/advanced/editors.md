---
navigation:
  title: "Text and Visual editors"
  position: 50
  icon: "superfactorymanager:manager"
  parent: /advanced/index.md
  keywords: ["editor", "visual", "undo", "save", "text", "code"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Text and Visual editors

Use **Manager > Program > Edit code** to edit an ordinary source disk.
Use **Program > Visual** to build a supported graph. Both views write SFML.
Save the program, wait for confirmation, then choose **Start** separately.

A published reference disk runs an immutable build. Change its project in the
[Programming Station](studio.md), publish and imprint a fresh disk to deploy it.

## Choose your task

- [Build and wire a graph](visual-editing.md): pins, conditions, loops and
  moving between nodes and source.
- [Edit routing fields](routing-fields.md): labels, resources, limits, exclusions,
  Transfer arguments and named port triggers.
- [Keep a Studio project](studio.md): saving, publishing, imprinting and recovery.

The Manager keeps your normal inventory and hotbar below its pages, with NEI
around the machine window. Studio is a separate full-screen workspace without
inventory, hotbar actions or NEI. Optional expanded storage belongs to Dynamic Inventory.

## Write and save code

The Text editor offers line numbers, syntax colors, undo/redo, wrapping,
scrollbars, indentation and categorized snippets. Literal labels need quotes:
`output 2 to "dest"`. Bare `dest` means a declared variable.

| Control | Result |
| --- | --- |
| Save / Ctrl+S | Submit the draft and keep editing. |
| Save & Close | Return after the server confirms the save. |
| Cancel | Leave the installed program unchanged; retain a session recovery draft. |
| Copy | Copy the draft for backup or reconciliation. |
| Ctrl+Z / Ctrl+Y | Undo / redo on the active editing surface. |
| Ctrl+Space | Request completion or a snippet. |
| F4 / diagnostic status | Jump to the first current compile error. |

Snippets supply syntax; assign their labels and test machine access yourself.
Studio also has Find/Replace, API help and diagnostics; see its [workflow](studio.md).

## Know when to use Code

Visual supports timer and pulse triggers, named Port edge triggers, Input,
Output, If, Forget, Signal, Transfer and Print. It also supports program-wide
LET, STATE, locals, assignments, counted/array/while loops, Java-style FOR and
indexed array loops.

Supported expressions can refer to variables in scope and use arithmetic,
constructors, item/fluid receiver queries and known pure built-ins such as LEN,
ORE, MIN and SHORTAGE. ARRAY and pure collection readers are supported too.
Compiler checks still determine valid names, types and scope.

Use **Text/Code** for player functions and calls, WAIT, BREAK, ghost-circuit
actions, collection mutation calls, transfer result capture with INTO and direct
ELSE IF chains. A program containing an unsupported construct stays intact in
Code; Visual refuses the whole conversion. Deep or complex expressions may also
exceed the editor's bounds.

## Keep source and saved state safe

Moving graph nodes preserves source formatting. Editing their clauses can
regenerate formatting. Node positions are layout metadata in a comment; source
instructions remain authoritative. Small edits can retain matching positions;
major or ambiguous changes can reset layout.

Comment, spacing and layout edits can preserve the running compiled program and
round-robin position. Executable changes recompile and reconcile persistent
[STATE](/language/variables.md). Check its rules before renaming or changing a saved value's type.

A stale editor cannot overwrite a replacement disk or newer source revision.
If a save times out, copy your draft, reopen the current disk and reconcile the
actual saved version. Save before leaving the world: session recovery is temporary
memory and does not survive world unload or a client restart.

Next: [build a graph](visual-editing.md) or [edit routing fields](routing-fields.md).
