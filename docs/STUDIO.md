# SFM Studio

Open a Programming Station to use the full-screen MUI2 editor. The station keeps
named project drafts in the world's project storage, outside the block's NBT.
Editing a project does not change a running manager program. Inventory slots,
hotbar actions and NEI overlays are unavailable while the editor is open.

## Start a project

Choose **New**, give the draft a name, then use **Create**. **Projects** lists
your saved projects. **Save** waits for a server acknowledgement and uses the
revision you loaded; another writer cannot silently overwrite it.

The diagnostic below the workspace describes the current draft. **Validate**
checks the exact saved server revision, so save first when you want that check
to include your latest edits. Invalid text can be saved as a draft.

**Copy** copies source without hidden visual-layout metadata. To run it with the
current disk workflow, paste it into the manager's Text editor and save its disk.
Project build publication and reference-only execution disks are a later stage;
Studio does not offer an installation button without that backend.

## Write code

SFML keywords accept lowercase and uppercase. Variable, function and label names
retain their own case rules; the editor never lowercases source identifiers.
Comments start with `--`.

| Control | Action |
| --- | --- |
| Ctrl+S | Create or save the current project |
| Ctrl+Space | Complete a function, receiver method, or `$variable` at the caret |
| Tab / Shift+Tab | Move through inserted template placeholders |
| Ctrl+Z / Ctrl+Y | Undo / redo in the active authoring pane |
| Ctrl+F / Ctrl+H | Open literal Find/Replace |
| Ctrl+I | Indent the source |
| F4 | Navigate to the first compiler diagnostic |
| Wrap | Toggle visual wrapping without changing source |

The categorized Insert browser provides statements and examples. **API** shows
built-in functions, label methods and declarations in the current program, with
signatures and help. Completion replaces the current name instead of duplicating
its receiver. Its suggestions are assistance, not proof that a variable has the
right type or scope; the SFML compiler checks that.
Use Up/Down, Page Up/Page Down, or Home/End to choose a completion, then Enter
to insert it. The list keeps the selected row visible. Escape cancels the popup.

After `$`, variable completion offers earlier declarations in the current scope,
including function parameters and loop variables. Closed blocks and other triggers
are excluded; function bodies cannot capture program variables. Suggestions show
the declaration's line and role, and preserve its exact case. This scope lookup
runs only when requested and needs a parseable surrounding draft. It does not
evaluate initializers or guess an inferred type. STATE defaults cannot read variables.

Find/Replace treats punctuation literally and can match or ignore case. Replace
All is one undo operation and refuses an oversized result before changing text.
Compiler checks run after typing settles; edits invalidate old error markers.

## Use Visual and Split

**Visual** edits a supported program as execution nodes. **Split** shows nodes
and source together when at least 640 logical pixels are available. Small windows
use one surface at a time; compact Insert and Tools menus keep working space free.
Drag the divider to adjust the balance between graph and code. Both panes keep a
minimum usable width; resizing does not change source or saved node positions.

Supported nodes are Timer, Pulse, Input, Output, If, Forget and Redstone output.
Source containing unsupported statements remains available in Code; conversion
refuses before replacing it with a partial graph.

| Gesture | Action |
| --- | --- |
| Right-click empty canvas | Add a node or inspect the selection |
| Drag a node header | Move it; one drag is one undo step |
| Click an output pin, then an input pin | Connect execution order |
| Right-click a pin | Disconnect it |
| Middle-drag | Pan |
| Wheel over canvas | Zoom |
| F | Fit the graph |
| Delete | Delete the selected node |
| Esc while connecting | Cancel the pending wire |

Select a node to edit its SFML clause or choose **Fields** for structured settings.
The dialog resizes with the screen, keeps pending values, and its fields list
scrolls. It provides supported timer and routing settings while preserving
unrelated filters and modifiers. Ambiguous clauses keep the full-clause
fallback. Apply or reset pending edits before changing views or saving.

Supported visual edits preserve source comments and reuse node layout identities
where their structure can still be matched. Disconnected nodes stay in an
incomplete visual draft; saving is blocked until they are connected or undone.
The last safe source remains available for preview.

## Recover unfinished work

Switching away from unsaved work offers **Keep draft & continue**. Returning to
that project restores its own source, graph and unapplied fields. A rejected or
uncertain write also keeps the local draft; reconcile the saved revision before
trying another write.

Local recovery is bounded temporary client memory, not a backup. It does not
survive a client restart or world unload. Save important text drafts explicitly;
finish incomplete graphs before ending the session. Back up the world's project
storage along with the world.

## Inspect a running manager

**Terminal** opens a diagnostic console. On compact screens it is also under
Tools or Problems. Enter a nearby loaded manager's X/Y/Z and choose **Connect**.
The server checks the station session, distance and exact target for each request.
Replacing or unloading that manager ends the connection.

Use `help`, `status`, `state`, `state $name`, `labels`, or `logs`. **Capture**
explicitly enables or disables PRINT output. Connecting does not enable capture.
PRINT and command results have separate views, with paging, horizontal movement
and Copy. Tab completes console commands; Up/Down recall command history.

The terminal inspects the connected manager. It does not run the open project,
evaluate arbitrary SFML, simulate transfers, or provide breakpoints and stepping.

See the [SFML guide](SFML_GUIDE.md) for language behavior and the
[roadmap](STUDIO_ROADMAP.md) for remaining integration and qualification work.
