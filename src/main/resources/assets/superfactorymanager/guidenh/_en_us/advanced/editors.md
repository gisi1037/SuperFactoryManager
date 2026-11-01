---
navigation:
  title: "Text and Visual editors"
  position: 50
  icon: "superfactorymanager:manager"
  parent: /advanced/index.md
  keywords: ["editor", "visual", "retro", "undo", "save", "text"]
categories: ["sfm"]
updated: "2026-10-31"
---

# Text and Visual editors

The manager's Text and Visual editors save SFML to an ordinary source disk.
Use **Visual** for supported node graphs and **Text** for the complete current
language. Immutable reference disks use published builds from the
[Programming Station](/start/station.md); edit the project and imprint a new build
onto a fresh disk to change their program.

## Text controls

The Factory Manager opens on an operational dashboard. **Program > Edit code**
opens the Text editor; **Program > Visual** opens its graph editor. Program contains its
disk editing controls. The normal nine-column player inventory and hotbar stay
below the manager's pages, with NEI available around the machine window.
Optional expanded storage and scrolling are provided by Dynamic Inventory.
Inventory is unavailable throughout the separate full-screen Programming Station.

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

The palette includes Timer, Pulse, Port edge, Input, Output, If, Forget, Signal,
Transfer and Print. The Studio list scrolls above its fixed footer. IO node
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

Visual opens only source it can represent without dropping instructions.
Transfer supports the existing two-, three- and four-argument item actions
without `INTO`; Print supports one to eight expressions. Studio Fields has
explicit Transfer suffix controls and Print Add/Remove rows. Typing into an
  existing field selects the overload by type: a third integer limits all items;
  a third item filter selects matching stock. Four arguments require a filter
  followed by an integer amount.

Transfer, Print and If predicates support self-contained expressions using
literals, arithmetic, `LABEL`/`ITEM` constructors and receiver methods such as
`.slots()` or `.count()`.
Other function calls, including `LEN` and `ORE`, still require Text. Variables, STATE, assignments, result capture, loops, player functions
and direct `ELSE IF` chains remain text-only. Data
expressions in other statement positions retain their existing restrictions. A mixed program
with any unsupported construct is refused intact.

Node positions are saved as layout metadata in a comment. Small edits to numbers
or item IDs can keep matching positions when you return to Visual. Large
restructures or ambiguous matches can reset layout. Instructions always come
from the current source, never from an old layout.

Comment-, spacing- and layout-only saves can preserve the running compiled
program and round-robin position. Changes to executable tokens recompile and
reconcile persistent STATE. See [STATE](/language/variables.md) before changing
the types or names of saved variables.

### Typed If conditions

Select an If node and open **Fields**. Simple literal HAS conditions keep their
label, comparison, amount and resource rows. Newly supported typed predicates
use one **Condition** field instead.

Examples: `(LABEL("source").available() AND NOT LABEL("source").empty())`,
`source HAS >= (2 + 3)`, or `REDSTONE >= (2 + 3)`. Keep those parentheses and leave
out `IF`, `THEN` and `END`. **Apply fields** checks the entire condition once;
editing does not query containers or run the program. **Cancel** discards pending
changes. Variables and other function calls still require Code, as do unsupported
statements anywhere in the program. A refused conversion keeps all source.

### Structured fields and suggestions

Input/Output Fields supports 1–32 ordered literal labels: edit/Pick a row,
Add label, or Remove while retaining one. Duplicates, case, legacy quoting and
order stay intact; this edits program references, not block names or bindings.
Larger groups remain fully manual. Every row and other field stays pending until
one Apply; Cancel preserves source/Undo. Pick can repair blank/invalid text,
while Apply still requires every row to be valid. Unchanged lexemes and gaps
stay exact; append uses comma-space, removal consumes the following delimiter
(preceding for the final row). Retained drafts keep row identities and pending
text; stale/structurally changed picker sessions refuse safely.
If Escape or Done closes a pending inspector, use **Go > Reopen Fields** or
**Go > Reopen clause**. This also works at compact sizes and does not apply edits.

Fields errors describe the last attempted action. Editing a pending value clears
its old error and disables stale **Details**; Apply checks the new value. This is
not live validation: new text can still be invalid. Unchanged or rejected edits
keep the current error. Cancel/Apply or rebuilt Fields retire only that form's
feedback; unrelated status messages and compiler Problems stay separate.

Editing text (including code Undo/Redo) clears visual selection and its clean
cached inspector. Select the node again in Visual/Split to inspect current code.
Genuine pending fields/raw text still blocks editing; finish or cancel it first.

Eligible Input/Output Fields also offers one **OreDictionary filter**:
choose **Off**, **With** or **Without**, and enter a name such as `ingotIron` or
`ingot*`. Only Off removes it; enabled blank/invalid patterns block Apply.
Mode and pattern stay pending with other fields, even while Off, and survive
reopening/resizing/recovery. Unchanged WITH/TAG/# spelling and spacing stay exact.

These are legacy OreDictionary-name patterns, not modern Minecraft tags.
Matching ignores ASCII case and supports `*`, not `?` or regex. Unknown names
are valid: With matches no items; Without excludes none. **Fluids ignore this
filter.** Eligible OR alternatives share one filter. Independent limits use full
clause rows, without nested filter controls. Single-limit trailing OR, inclusion
commas and over32 alternatives stay manual. Compound filters remain
unsupported by this backport. No registry scan or ore picker is involved.

For an eligible unrestricted node or existing single static limit, switch **Unlimited / Limited** to
set the movement amount. Input **Leave at least** reserves source stock; Output
**Fill up to** caps destination stock. Enabling an omitted number starts blank:
fill it before Apply. Disabled defaults are unlimited movement, zero Input reserve
and no Output stock cap. Explicit `0` is different. Numbers use items or fluid mB;
their EACH switches are independent of each other and container EACH.

Disabled text/EACH stays locally for re-enabling/recovery and is not validated or
emitted. Apply/Cancel or conversion to full rows ends that latent control state.
Unrestricted nodes start with both numbers off and blank latent cells. Leaving
them off preserves the original clause. Edit numbers, resources, ore filters,
exclusions and routing together, then Apply once. On an initially unrestricted
node, **Add independent limit** stays unavailable until Apply/reopen establishes
an explicit limit; it never inserts a hidden match-all first row. Multiple,
computed and unsupported limits keep their old controls/manual editing.

Eligible Input/Output nodes offer **Resources (OR)**: 0–32 ordered selectors in
one limit, in either statement order. OR alternatives share one quantity,
retention and ore filter. Add a row, type a selector or Pick a base item ID.
Blank/invalid rows block Apply; Remove explicitly deletes them. Removing all
rows restores match-all, retaining IO defaults/allowances, EACH scopes, filters,
exclusions and routing. Duplicate/case/quoted OR text and unchanged gaps are kept.
Single-limit trailing OR, inclusion commas and over32 alternatives stay manual
with an inline notice. Other safe fields remain. Fluids/quoted
metadata stay manual; no new NBT filter support.

Existing clauses with **2–32 independent limits** show numbered full SFML rows.
For example: `16 RETAIN 2 item:minecraft:iron_ingot OR item:minecraft:gold_ingot`.
Rows have separate allowances. OR shares only its row; the first matching row
wins, so overlapping selectors are order-sensitive. Edit text horizontally, use
**Add limit** or **Remove**, and keep at least one row. Blank/invalid cells block
the whole Apply, including pending global labels/EACH/exclusions/routing/EMPTY.
No nested controls or pickers are added. For an eligible existing single static
limit, **Add independent limit** keeps validated pending edits in row 1 and adds
a blank row 2. Typed amount/retention/OR/ore controls become full SFML rows; global
fields stay active. Source changes only on Apply; invalid edits refuse conversion
without loss. Cancel discards all pending Fields. Absent/unsupported limits do
not offer conversion. Removing down to one keeps row
mode until Apply; reopening returns the existing single-limit controls. Original
lexemes, duplicate/case/Unicode and unchanged comma gaps stay intact. Top-level
trailing commas, over32 limits and unsupported forms stay manual without
truncation; dynamic limits still require Text.
Comments and overly nested row syntax are refused; quoted resource IDs stay literal.

**Exclusions** edits 0–32 global `EXCEPT` rows in either IO order, even with
complex inclusion limits. Add a row, type one resource or Pick a client item ID;
fluids/metadata stay manual. Blank or invalid rows block Apply: use **Remove**
to delete a row. Removing the last row removes EXCEPT. Order, duplicates, case,
unchanged selectors and separator gaps are kept. Trailing exclusion commas and
groups over 32 remain fully manual. Inclusion limits/ore filters/allowances and
routing stay unchanged. Choose changes only its pending row; Apply commits all
rows and other fields together. Cancel/reopening/recovery preserve the draft.

Manual exclusions show a notice: Apply or Cancel other Fields before editing the
full clause. Shortened errors end in `...`; **Details** opens the complete wrapped,
scrollable message with **Copy error**. **Back to fields** or Escape keeps your
pending edits unchanged.

Simple **If / HAS** conditions offer 1–32 ordered label rows, comparison, a literal
threshold and 0–32 **Resources (OR)** rows. OR selectors form one matching set;
the existing set operator decides how counts are compared to the threshold.
Zero resource rows restores match-all. Blank/invalid rows block Apply; use Remove
to delete a row. Keep at least one label. Parentheses, set operators, sides/slots,
round robin, filters and exclusions are retained. Negated/compound legacy
conditions, trailing OR and groups over 32 use the full clause. Computed
thresholds use the Condition field described above.
**Signal** offers 1–32 ordered destination label rows and a literal level in either
statement order and either quantity position. Add, Pick or Remove references;
keep at least one label. Blank/invalid rows block the entire Apply, including
pending level edits. EACH, round robin, sides/slots and untouched source stay.
Two-quantity, computed-level and over-32-label forms use the full clause.
An omitted signal level keeps default
15; the compiler still clamps static levels to 0–15.

**Pick** suggests labels referenced by this draft, or base client item IDs. It
does not scan connected inventories. Metadata/NBT and complex expressions stay
manual. Only bare `LABEL("…")`/`ITEM("…")` Transfer arguments have pickers.
Select a row, **Choose (pending)**, then **Apply fields**. Picker Cancel keeps
earlier pending edits; Fields Cancel leaves source and undo history untouched.
Problems refreshes after Cancel/Apply/Reset or a manual revert, without applying
pending work. Real compiler errors and incomplete graphs remain visible.

Suggestions are bounded: 256 draft labels / 4,096 reference visits, or 8,192 item
keys; search has 96 characters and 64 visible matches. The dialog reports capped
snapshots and omitted entries. Reopen after changing the project, node or source.

### Print and Transfer argument fields

**Print Fields** has 1–8 ordered expression rows. Add a row, type an expression,
or Remove a row; keep at least one. Quote literal messages. Nested commas in
strings, arrays, structs and calls stay inside their row. Blank/invalid rows
block the entire Apply. Apply commits all arguments as one undo step; pending
rows do not run. Cancel keeps source untouched. Complex expressions beyond the
editor's nesting/chain budget remain intact for Text editing. Syntax, type and
visual-capability checks still apply when you Apply.

**Transfer Fields** always requires Source and Destination. Use **Add filter /
count** for an item filter or total integer count. **Add amount** adds a fourth
argument when the third is an item filter.
New fields start blank; complete them before Apply. Remove amount before the
item filter. Source and Destination cannot be removed.

Without an amount, Transfer uses the available stock snapshot. With an explicit
amount, **zero moves nothing**. Removing the amount restores automatic stock.
The pending mode is shown above the roles. Apply checks all fields and commits
once; pending edits do not move items. Cancel keeps source untouched.

**Pick** works only for a bare `LABEL("...")` or `ITEM("...")`; blank or complex
expressions give a manual-editing explanation. Side/slot restrictions and exact
item keys stay manual. Choose changes one pending role; Apply checks the whole
list. Result capture with `INTO` and unsupported/over-budget forms stay in Text.

### Port edge trigger

**Port edge** watches named native Redstone Port inputs. **Pulse** remains the
separate manager pulse trigger. Open **Fields** to choose **Edge** (`RISING`,
`PULSE`, or `FALLING`) and edit 1–32 ordered literal label rows.

For example: `every redstone rising from "signal" north side`.

`PULSE` is an alias for `RISING`; it is not an additional event. Add, Pick or
Remove label rows while retaining at least one. Pick suggests references from
the draft, not connected blocks. Label order, duplicates, case and legacy
quoting are kept. Blank/invalid rows or edge values block the entire Apply.
**Apply fields** validates all changes together; **Cancel** preserves source
and undo history. A successful Apply is one undo step.

The **Physical sides** field takes `NORTH`, `EAST`, `SOUTH`, `WEST`, `TOP`,
`BOTTOM` separated by commas. Leave it blank for the default all six faces, or
enter `EACH` for explicit all faces. Leave out `SIDE`; the editor supplies it.
Invalid sides block the entire Apply, including pending edge and label changes.
Untouched spelling and gaps stay intact. Clearing sides removes the whole
qualifier. `UP`, `DOWN`, relative faces and `NULL` are not field values.
Do not use `LABEL(...)`, slots or round robin in this header.

Headers over 32 labels or with unsupported row forms keep the complete manual
**Trigger header** field, without `DO` or `END`. No labels are truncated and no
partial edge controls appear. Initial observations establish a baseline rather
than triggering a false edge. Invalid edits keep the original header/body;
unsupported body statements still require Text.

### Reveal source and nodes

**Go > Reveal node** (F6) finds the instruction/header token at your code caret.
**Go > Reveal source** (Shift+F6) selects the current graph node's source. Wide
layouts also have direct buttons. Split stays open when usable; narrow layouts
reveal the target in Visual or Code. Ordinary typing/selection never switches.

Comments, whitespace, NAME, ELSE and END have no node. Move onto a token.
Duplicate statements use their own branch/trigger identities. Revealing changes
only focus, selection and viewport, retaining source, saved positions and Undo.
Apply/Cancel pending fields or Apply/Reset a raw clause first. Incomplete graphs,
unsupported code, active gestures/wires and project requests refuse navigation.

Next: [Programming Station projects](/start/station.md), or [program structure](/automation/scheduling.md).
