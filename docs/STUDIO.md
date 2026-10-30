# SFM Studio

Open a Programming Station to use the full-screen MUI2 editor. The station keeps
named project drafts in the world's project storage, outside the block's NBT.
Editing a project does not change a running manager program. Inventory slots,
hotbar actions and NEI overlays are unavailable while the editor is open.

## Start a project

Use the **Projects** column on the left. Choose **+** for an empty draft or a
template for a working starting point. Name the project and **Save** to create it.
Saved projects stay in the column; the top selector provides another route.
Reopening the station restores your last selected project for this world/server
and player through an authenticated load. Dirty local recovery still takes priority.
**Save** waits for a server acknowledgement and uses the revision you loaded;
another writer cannot silently overwrite it.

The bottom **Problems** dock describes the current draft. The top **Check** button
checks the exact saved server revision, so save first when you want that check
to include your latest edits. Invalid text can be saved as a draft.

The top bar shows the selected project, draft status, SAFE authoring status,
Essentials/Advanced tool mode, Save, Check and Imprint disk. Essentials uses typed
node fields; Advanced also exposes a selected node's raw SFML clause. Both modes
support the same language and retain the full searchable insert/API catalog.
The full-width Insert bar sits above Projects and the larger Visual/Split/Code
buttons, which sit directly above the authoring surface. Drag the project-column
divider or the horizontal dock divider to resize those regions. Studio and its
dialogs stay fixed in place; their title bars do not move the interface.

Open **Essentials/Advanced > Studio scale** to change the editor's GUI scale with
**-** and **+**. **Game** restores following Minecraft's GUI scale. The choice is
saved on this client and applies to Studio and its dialogs only; the HUD, Manager
and other screens keep their game scale. If the window becomes too small, Studio
temporarily fits the chosen scale to its minimum workspace without forgetting it.
Changing scale keeps the current draft, undo history and pending node fields.

**Copy** copies source without hidden visual-layout metadata. The ordinary
embedded-source manager-disk workflow remains available. To use world-stored
programs instead, save, publish and imprint a reference disk in the station.

## Publish a saved build

Open the bottom **Build revisions** dock, or the top **Imprint disk** entry. **Publish saved**
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
| Shift-drag a node header | Snap its position to the canvas grid |
| Click an output pin, then an input pin | Connect execution order |
| Right-click a pin | Disconnect it |
| Middle-drag | Pan |
| Wheel over canvas | Zoom |
| F | Fit the graph |
| Delete | Delete the selected node |
| Esc while connecting | Cancel the pending wire |

Select a node to edit its SFML clause or choose **Fields** for structured settings.
On larger workspaces, drag the Inspector's left edge to resize it. Node movement
and pane resizing follow rendered frames; source regeneration waits until the
node drag ends.
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

The bottom dock has **Problems**, **Debug terminal**, **Watch** and **Build
revisions**. Tabs preserve their session and results; resizing the dock changes
available output space. At the smallest GUI scale, shorter tab names save space.
The terminal is a bounded diagnostic console, not an arbitrary SFML evaluator.

In **Debug terminal**, enter a nearby loaded manager's X/Y/Z and choose **Connect**.
The server checks the station session, distance and exact target on every request.
Switch to **Watch** for live read-only program-wide values. Trigger-local variables
end with their invocation and are not retained for inspection.

The Factory Manager itself is a centered machine window with the installed disk,
Start/Pause, Overview/Program/Network and inline Terminal/Logs/Profile/Watch pages.
The ordinary 9-column player inventory and hotbar stay visible below every page;
NEI remains available around the machine. SFM uses stock MUI2 inventory binding
and widgets. Dynamic Inventory owns optional expanded-storage behavior, including
its three-row scrolling viewport; SFM has no dependency on that mod's API.

**Logs** separates the server recording level from the local severity/search
filter. Use wheel/scrollbars, Wrap, Follow, Live/Frozen and Copy to inspect bounded
retained output. Opening Logs does not enable recording. **Terminal** replaces
the body in the same machine window; it no longer opens a second panel. PRINT
capture starts off and must be enabled explicitly.

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

## Imprint a program disk

Reference-disk imprinting is available in the normal station UI. Hold one fresh
blank SFM disk before opening the station. Save and Check the project, open
**Build revisions**, choose **Publish saved**, select the exact immutable build,
then press the dock's **Imprint disk** button. The station imprints that published
program's identity onto the held disk; installing it in a manager loads its code
from world storage into private runtime memory.
The disk must have no existing program, labels, STATE, recovery or custom metadata.
The selected build may be an older published revision; unsaved draft edits are
not written to it. Changing the medium or station session during the operation
rejects the write without replacing the held item. After an uncertain reply,
inspect the disk before retrying; the editor does not automatically write again.

The physical disk stores a reference and independent STATE, while source remains
in world storage. A manager prepares its own program in memory asynchronously.
Pause intent survives loading and reloads. Temporary capacity waits retry automatically;
missing or invalid builds keep the disk
and its saved values intact; **Start** retries a failed preparation. Runtime source
editing and reset controls do not rewrite an immutable reference. Automatic
source-bearing debug logs are suppressed for references; deliberate `print()`
output remains available when capture is enabled.

Normal running ticks do not reread program files. Reload/reactivation validates
the stored grant and exact bundle again. Reference disks are copyable execution
capabilities, with independent mutable STATE for each physical copy; they do not
grant private project editing or source-export access. Cooperative two-player
handoff, privacy, copied STATE isolation, pause/reset, retry and clean reloads
passed. Broader adversarial races, hardware input and provider/churn coverage
remain separate qualification; see [the report](MULTIPLAYER_QUALIFICATION.md).


Use the native manager console's **Program > Reset state** only to deliberately
discard persistent values. A loaded reference uses its exact in-memory schema;
its immutable program reference, labels and other disk metadata stay unchanged.
Reset pauses the manager and clears global values/redstone edge history. It does
not recompile or redefine round-robin cursor behavior. Loading, unavailable,
replaced or recovery-blocked disks reject reset without destroying saved STATE.
The old legacy screen cannot authorize reference reset; use the native console.

See [reference performance](REFERENCE_PERFORMANCE.md) for loading limits,
correctness-checked measurements and remaining qualification gates.


## Player profiler and live values

**Profile** records a bounded sample of complete server manager updates, including
idle checks, recovery and discovery. Record defaults to 1,200 updates and stops
automatically; Stop preserves the sample and Reset clears it. Results show count,
updates with a due trigger, average, p50/p95/p99 and maximum milliseconds. This is
not per-instruction, compiler-worker, manual-run or whole-server TPS profiling.
The collector is off by default and allocates no samples while off.

The same controls are available through terminal commands:

```text
profile start 1200
profile status
profile stop
profile reset
watch
watch $counter
state $counter
```

`profile start` accepts 1–4,096 updates. `watch` shows a bounded list of initialized
program-wide globals and persistent values; an exact `$name` selects one.
Inspection never runs expressions, initializes globals or flushes STATE to disk.
Legacy source disks retain existing access. On a reference disk, only its verified
source author can inspect variable names/values; other execution-disk holders can
still use numeric profiling and deliberate PRINT output. Local trigger variables
are not available after their trigger completes.
