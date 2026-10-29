---
navigation:
  title: "Programming Station projects"
  position: 60
  icon: "superfactorymanager:programmingStation"
  parent: /start/index.md
  keywords: ["programming station", "project", "draft", "private", "save"]
categories: ["sfm"]
updated: "2026-10-29"
item_ids: ["superfactorymanager:programmingStation"]
---

# Programming Station projects

The Programming Station is a separate place to keep editable source drafts.
Its full-screen SFM Studio interface uses **Projects** to create, browse, edit,
save and validate your projects. Another station can access your own projects even if the first
station is broken.

<ItemImage id="superfactorymanager:programmingStation" />

## Save, then validate

Create a named project, open it, write SFML and choose **Save**. An unfinished
program can be saved even if it does not compile. **Validate** checks the exact
saved revision, so save changed text or names before validating. Validation
does not execute a program or change a manager.

The editor has the same text tools as the manager: syntax highlighting, line
numbers, undo/redo, indentation, wrapping and snippets. Copy valuable drafts
before leaving if a save is unconfirmed. A rejected or stale edit does not
silently replace a newer saved project.

## Code, Visual and Split

**Code** supports the complete current language. **Visual** edits the supported
Timer, Pulse, Input, Output, If, Forget and Redstone output nodes. **Split** shows
both surfaces when the GUI has at least 640 logical pixels of width. Drag the
divider to change their balance. Smaller screens use compact **Insert** and
**Tools** menus so toolbar rows do not consume the workspace.

Select a node and use its full clause or **Fields**. Structured fields cover
supported labels, amounts, retention, separate per-item/per-container EACH,
round robin, sides, slots and empty-slot output. The fields dialog resizes and
scrolls; ambiguous clauses retain the full-clause fallback. Apply or cancel
unapplied fields before changing the document. Right-click empty canvas to add
nodes, click pins to connect, right-click a pin to disconnect, middle-drag to
pan, and use the wheel to zoom. **F** fits the graph.

Unsupported text stays in Code; Visual refuses conversion instead of dropping
instructions. Disconnected graphs keep their last safe source and block Save
until repaired. Switching projects can retain an incomplete graph and unapplied
fields in temporary client recovery.

## Completion and diagnostics

**Ctrl+Space** completes function, method or `$variable` names at the caret,
preserving the receiver and exact variable spelling. Up/Down and Page keys choose
a result; Enter inserts it and Escape cancels. Variable suggestions use earlier
declarations in the current scope of a parseable draft, excluding closed blocks
and other functions/triggers. STATE defaults cannot read variables.

**API** searches built-ins, label methods and functions declared in the
current program. Hover a call for its signature and help. **Tab/Shift+Tab** moves
through inserted template placeholders. Suggestions do not establish variable
types or scope; compiler diagnostics remain authoritative.

**Ctrl+F/Ctrl+H** opens literal Find/Replace. Replace All is one undo step.
**Ctrl+Z/Ctrl+Y** targets the active authoring surface. Draft compiler checks run
after typing settles, mark source errors and support **F4** navigation. **Problems**
shows diagnostic details; **Validate** still checks the exact saved server revision.

## Live manager terminal

**Terminal**, also available under Tools or Problems, connects to an explicitly
chosen nearby loaded manager using its X/Y/Z. Use `help`, `status`, `state`,
`labels` or `logs`. PRINT capture starts off; enable it explicitly to collect
output from that manager. PRINT and command results have separate views with
paging and Copy. Tab completes commands; Up/Down recall history.

The server checks the station session and exact manager on every request.
Replacing/unloading the target ends the connection. The terminal does not run
the open project, evaluate arbitrary SFML, simulate a factory or provide stepping.
Inventory slots, hotbar actions and NEI are unavailable throughout Studio.

## Ownership and backup

Projects belong to your authenticated player UUID. Someone else's station or a
copied project identifier does not grant access to their source. Saved projects
live in the world save, separately from the station block. Include the world's
`sfm/projects` and `sfm/programs` directories in backups.

Local recovery drafts are temporary client memory. They help after a closed
screen or failed request during the session; an accepted project save is what
survives leaving the game.

## Running the source today

**Builds** (under **Tools** on compact screens) publishes immutable root-source
bundles from an exact saved revision. Save edits first, then choose **Publish saved**.
Invalid drafts cannot be published. **Refresh** lists saved builds; click a row
to copy its digest. A later edit does not change a published build, and publishing
does not change or run any manager. Publication receipts remain private to the
project owner. A digest alone grants no source access.

This station version does **not yet** install reference disks or import libraries.
To run a saved draft now, copy its source into a manager's Text editor, save to
an execution disk, synchronize that disk's labels and enable the manager.

Execution disks still hold their source and persistent STATE. A reference-only
disk workflow is planned separately; the station does not move an existing
disk's data into project storage automatically.

Projects currently allow 30,000 UTF-8 source bytes and 96 name bytes, with up to
128 projects per owner. Names and non-ASCII characters can use several bytes.
If the station reports busy or rejects an old revision, wait for the current
request or reopen the saved project before editing again.

Next: [the SFML language](/language/index.md) and [feature status](/reference/status.md).
