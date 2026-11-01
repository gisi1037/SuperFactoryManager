---
navigation:
  title: "Studio and published builds"
  position: 60
  icon: "superfactorymanager:programmingStation"
  parent: /advanced/index.md
  keywords: ["programming station", "project", "draft", "private", "save"]
categories: ["sfm"]
updated: "2026-10-31"
---

# Studio and published builds

The Programming Station is a separate place to keep editable source drafts.
Its full-screen SFM Studio interface uses **Projects** to create, browse, edit,
save and validate your projects. Another station can access your own projects even if the first
station is broken.

<ItemImage id="superfactorymanager:programmingStation" />

## Save, then validate

Create a named project, open it, write SFML and choose **Save**. An unfinished
program can be saved even if it does not compile. **Check** checks the exact
saved revision, so save changed text or names before checking. Validation
does not execute a program or change a manager.

The editor has the same text tools as the manager: syntax highlighting, line
numbers, undo/redo, indentation, wrapping and snippets. Copy valuable drafts
before leaving if a save is unconfirmed. A rejected or stale edit does not
silently replace a newer saved project.

## Restore an older build

Open **Build revisions**, select the published revision and choose **Restore**
(or **Restore to draft** in the builds window). Keep or discard unsaved work when
prompted. Kept work also has a separate local recovery copy for this session.

The recovered code becomes one undoable local edit. The project name and saved
revision stay current. Choose **Save** to create a new revision, then check and
publish normally. Running managers and disks are unchanged. If you edit while
loading, the late reply keeps your newer work. Only the owner can recover source;
an execution disk or digest alone does not grant access.

## Code, Visual and Split

**Code** supports the complete current language. **Visual** edits the supported
Timer, Pulse, Port edge, Input, Output, If, Forget, Redstone output, Transfer,
Print, variable/state declarations, assignments and counted/array/while loops. **Split** shows
both surfaces when the GUI has at least 640 logical pixels of width. Drag the
divider to change their balance. The full-width **Insert** bar sits above the
Projects column and the Visual/Split/Code buttons. It opens the searchable
statement and example catalog.

Select a node and use **Fields**. **Settings** chooses Essentials or Advanced;
Advanced also exposes the raw SFML clause. Both modes support the same language
and keep the full insert/API catalog. Structured fields cover
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

**Ctrl+Space** completes function, method or variable names (with optional `$`) at the caret,
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
shows diagnostic details; **Check** still checks the exact saved server revision.

## Live manager terminal

The bottom **Debug terminal** dock connects to an explicitly
chosen nearby loaded manager using its X/Y/Z. Use `help`, `status`, `error`, `state`,
`labels` or `logs`. PRINT capture starts off; enable it explicitly to collect
output from that manager. PRINT and command results have separate views with
paging and Copy. Tab completes commands; Up/Down recall history.

**Errors** reads the connected manager's retained runtime failure into the same
dock. It does not check or run the open draft; use **Problems** for draft compiler
errors. Runtime locations refer to the installed program revision. Reference-disk
failure details are visible only to the verified source author.
Use `error clear` to remove the retained failure without clearing PRINT or
ordinary logs. Pause/Run retains it; source edits/resets, disk changes and manager
unload clear it.

The **Watch** dock reads initialized program-wide LET and STATE values from that
manager. Trigger and function locals are unavailable after execution. Reference
variable inspection is restricted to the verified source author; deliberate
PRINT output remains available to execution-disk holders.

The server checks the station session and exact manager on every request.
Replacing/unloading the target ends the connection. The terminal does not run
the open project, evaluate arbitrary SFML, simulate a factory or provide stepping.
Inventory slots, hotbar actions and NEI are unavailable throughout Studio.

## Ownership and backup

Projects belong to your authenticated player UUID. Someone else's station or a
copied project identifier does not grant access to their source. Saved projects
live in the world save, separately from the station block. Include the world's
`sfm/projects`, `sfm/programs` and `sfm/execution-grants` directories in backups.

Switching away from unsaved work offers **Keep draft & continue**, which retains
that project's source, graph and unapplied fields. **Discard draft & continue**
removes those unsaved changes and their local recovery copy; it leaves saved
projects and published builds intact. Discard waits for pending or unconfirmed
requests to be resolved. Copy valuable source before reconciling a failed save.

Local recovery drafts are temporary client memory and do not survive a client
restart or world unload. An accepted project save is what survives leaving the
game; finish incomplete graphs before ending the session.

## Running the source today

The bottom **Build revisions** dock, also opened by the top **Imprint disk**
entry, publishes immutable root-source bundles from an exact saved revision.
Save edits first, then choose **Publish saved**.
Invalid drafts cannot be published. **Refresh** lists saved builds; click a row
to select it, then use **Copy digest** for its identity. A later edit does not change a published build, and publishing
does not change or run any manager. Publication receipts remain private to the
project owner. A digest alone grants no source access.

**Imprint disk** is available normally. Hold one fresh blank disk before opening
the station. Save and Check, publish the saved revision, select that exact build
in the bottom Build revisions dock, then press the dock's **Imprint disk** button.
The held disk must have no program, labels, STATE, recovery or custom metadata.
An older published build may be selected; unsaved changes are never included.
Insert the imprinted disk into a manager and copy its labels from the Label Gun.
After an uncertain imprint reply, inspect the disk before retrying. You can also
[publish reusable functions](libraries.md) from a functions-only project and pin
an exact owned library revision in another program.

Ordinary execution disks still hold source and persistent STATE. The
reference workflow writes only a published-build reference to a fresh disk, then
loads a private program into manager memory on the storage worker. Source stays
in world storage; STATE remains independent on each physical disk. Pause intent
survives loading. Temporary loading-capacity waits retry automatically. Missing
or invalid builds preserve the disk and allow an explicit **Start** retry after
repair. The station never silently converts or erases an existing disk.

In the Factory Manager, **Overview → Expand** shows measured update-cost history,
latest/mean/max/peak and runtime state. The clock marker represents scheduler ticks,
not individual statements or successful transfers. Detailed execution tracing is
not sampled yet. See [performance](/reference/performance.md) for measurement limits.

Projects currently allow 30,000 UTF-8 source bytes and 96 name bytes, with up to
128 projects per owner. Names and non-ASCII characters can use several bytes.
If the station reports busy or rejects an old revision, wait for the current
request or reopen the saved project before editing again.

## Workspace and diagnostics

Projects and templates live in the left column; reopening restores your last
selected project for this world/server and player. A retained dirty draft takes
priority. Visual/Split/Code are directly above the editor. Drag the
project divider or bottom dock divider to resize the workspace. The bottom tabs
contain Problems, Debug terminal, Watch and Build revisions. Inventory slots and
NEI stay unavailable throughout this full-screen authoring surface. Studio stays
fixed in place; Settings and unsaved-draft dialogs open centered and can be moved
by dragging their title bars.

Open **Settings > Studio scale** and use **-**/**+** to change Studio's GUI scale.
**Game** restores following Minecraft's scale. The client remembers this choice;
it applies to Studio and its dialogs, while the Manager and HUD keep their game
scale. Small windows temporarily fit the chosen scale without forgetting it.
Changing scale preserves your draft, undo history and pending node fields.

The Factory Manager uses a normal centered machine GUI with player inventory
and NEI. Terminal, Logs, Profile and Watch replace its main content directly.
Logs have search, severity filtering, wrapping and follow/freeze controls.
Profile records bounded full-update timings; watch reads initialized globals and
persistent values. Reference variable inspection is restricted to its source
author. Neither tool evaluates arbitrary expressions or changes variable values.

Next: [the SFML language](/language/index.md) and [feature status](/reference/status.md).
