---
navigation:
  title: "Text and Visual editors"
  position: 70
  icon: "superfactorymanager:manager"
  parent: /start/index.md
  keywords: ["editor", "visual", "retro", "undo", "save", "text"]
categories: ["sfm"]
updated: "2026-10-28"
---

# Text and Visual editors

Both editors save SFML to the same execution disk. Use **Visual** for supported
node graphs and **Text** for the complete current language.

## Text controls

The MUI2 editor provides line numbers, syntax colors, undo/redo, wrapping,
scrollbars, indentation and categorized snippets. Snippets help with syntax;
they do not choose your labels or guarantee the machine will accept a resource.

| Action | Use |
| --- | --- |
| Save | Submit the source and keep editing. |
| Save & Close | Save, then return after the server confirms the result. |
| Cancel | Keep the installed program unchanged; retain a session recovery draft. |
| Copy | Copy the draft so you can keep or reconcile it elsewhere. |
| Ctrl+S | Save the current draft. |
| Ctrl+Space | Ask for a suggestion/snippet. |
| F4 / diagnostic status | Jump to the first current compile error. |

Read any save error. An old editor cannot overwrite a replacement disk or newer
source revision. If a save times out, it may still have reached the server:
copy the draft, reopen the current disk, then reconcile instead of repeatedly
submitting an old revision. Save before leaving the game; recovery drafts are
client-session memory, not a permanent source backup.

## Visual workflow

The palette includes Timer, Pulse, Input, Output, If, Forget and Signal. IO node
fields contain ordinary SFML clauses; specialized forms for every modifier are
not available.

1. Select a node and edit its field. Press **Enter** or **Apply**.
2. A trigger field holds `EVERY 20 TICKS`, without `DO`/`END`.
3. An If field holds a condition such as `source HAS >= 1`, without `IF`/`THEN`/`END`.
4. Other statement fields hold a full clause such as `OUTPUT 2 TO dest`.
5. Click an output pin and then an input pin to wire nodes.

If nodes have TRUE, FALSE and AFTER outputs. AFTER runs after the selected
branch. All nodes must connect to a trigger before saving; triggers cannot have
incoming wires and cycles are rejected. Right-click a pin to disconnect it.
Drag headers to move nodes, use the wheel to zoom, and right-drag empty canvas
to pan. Delete removes the selected node when its text field is unfocused.

**Preview** shows the generated SFML. **Copy** exports the program. **Undo**
retains bounded history. Moving nodes without editing clauses preserves source
formatting; editing graph clauses can regenerate the program's formatting.

## Switching between Text and Visual

Visual opens only source it can represent without dropping instructions. Direct
`ELSE IF` chains and advanced data features belong in Text today. Variables,
STATE, player functions, loops, PRINT, typed queries and TRANSFER do not all have
equivalent visual nodes; unsupported programs are refused intact.

Node positions are saved as layout metadata in a comment. Small edits to numbers
or item IDs can keep matching positions when you return to Visual. Large
restructures or ambiguous matches can reset layout. Instructions always come
from the current source, never from an old layout.

Comment-, spacing- and layout-only saves can preserve the running compiled
program and round-robin position. Changes to executable tokens recompile and
reconcile persistent STATE. See [STATE](/language/variables.md) before changing
the types or names of saved variables.

Next: [Programming Station projects](station.md), or [program structure](/automation/scheduling.md).
