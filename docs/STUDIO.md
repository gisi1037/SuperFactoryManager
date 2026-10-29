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
Reference-only execution disks are under qualification and unavailable by default.
The existing embedded-source disk workflow remains available.

## Publish a saved build

Open **Builds** (or **Tools → Saved builds** on a compact screen). **Publish saved**
compiles the exact saved server revision and stores an immutable source bundle.
Save name and source changes first. Invalid drafts can still be saved, but cannot
be published. Publishing does not run the program or change any manager.

Use **Refresh** to list published revisions. Click a row to select that exact
build, then use **Copy digest** for its SHA-256 identity. Identical source bundles can share storage; each project's publication
receipt records its owner, project, saved revision and name. Later draft edits
cannot alter a published bundle. A lost acknowledgement can be reconciled by
refreshing or retrying publication of that saved revision.

Builds currently contain root SFML source only, with no imported dependencies.
Storage and compilation run on the bounded project worker. The world's
`sfm/projects` directory holds drafts/receipts, and `sfm/programs` holds bundles;
back up these and `sfm/execution-grants` with the world. Atomic file visibility is not a power-loss guarantee.
Private project access is checked by the server; a digest alone grants no access.

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

## Factory Manager inventory

The separate Factory Manager uses an operational dashboard with Overview,
Program and Network tabs. Its disk slot is manager equipment. Player storage
appears below, with a fixed hotbar and Main/Left/Right storage views. Compact
screens show one storage view at a time; wide screens show all sections.

Use **Layout**, or hold Shift, to reveal row/column controls. Select Left or Right
before resizing that side; slots are borrowed from or returned to Main. **Split**
and **All** select layout presets, and **Undo** restores a recent layout. These
controls move slot presentation only; they do not transfer items or create extra
capacity. Layout preferences are temporary client memory. The live connected
inventory browser remains planned.


## Performance and execution activity

The Factory Manager's **Overview** shows a cost-history chart and the current
runtime state. **Expand** opens the activity view with latest update cost, window
mean/max and recorded peak. Charts use at most twenty chronological update samples
and scale to the visible window, so bar height is not a percentage of the server's
tick budget. Updates include idle checks, recovery, compilation and discovery;
these values do not measure item/fluid throughput.

The scheduler-clock marker advances with the manager's execution clock. It can
advance when no timed trigger is due or nothing can move. Loading, paused,
recovery-blocked and unavailable programs have distinct states. This is an
operational view; statement/branch tracing, node highlighting and breakpoints
remain planned. No private source or execution-grant token is sent by the chart.

## Reference-disk qualification

The guarded reference workflow is currently enabled only in opted-in development
test clients. In that workflow, select an immutable build in **Builds**, hold one
fresh blank SFM disk before opening the station, then use **Write held disk**.
The disk must have no existing program, labels, STATE, recovery or custom metadata.
The selected build may be an older published revision; unsaved draft edits are
not written to it. Changing the medium or station session during the operation
rejects the write without replacing the held item. After an uncertain reply,
inspect the disk before retrying; the editor does not automatically write again.

The physical disk stores a reference and independent STATE, while source remains
in world storage. A manager prepares its own program in memory asynchronously.
Pause intent survives loading and reloads. Missing or invalid builds keep the disk
and its saved values intact; **Start** retries a failed preparation. Runtime source
editing and reset controls do not rewrite an immutable reference. Automatic
source-bearing debug logs are suppressed for references; deliberate `print()`
output remains available when capture is enabled.

Normal running ticks do not reread program files. Reload/reactivation validates
the stored grant and exact bundle again. Reference disks are copyable execution
capabilities, with independent mutable STATE for each physical copy; they do not
grant private project editing or source-export access. Real two-player workflows
and cold-start queue behavior remain qualification gates before general enablement.
