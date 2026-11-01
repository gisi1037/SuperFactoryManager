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
Settings, Save, Check and Imprint disk. Settings includes the tool mode. Essentials uses typed
node fields; Advanced also exposes a selected node's raw SFML clause. Both modes
support the same language and retain the full searchable insert/API catalog.
The full-width Insert bar sits above Projects and the larger Visual/Split/Code
buttons, which sit directly above the authoring surface. Drag the project-column
divider or the horizontal dock divider to resize those regions. Studio stays fixed
in place. Popups open centered on the screen. The **Settings** and unsaved-draft
dialogs can be moved by dragging their title bars.

**Settings > Publish library** publishes the exact saved functions-only project.
**Insert library** browses your own projects and their immutable library revisions
without changing the open program. Choose an alias to insert one exact import as
one undoable edit. Imports follow an optional NAME and precede other declarations.
Save, then **Check** the linked server revision; local checking cannot resolve
private libraries. Program builds freeze the selected source closure, so later
library edits do not change installed programs. Libraries cannot implicitly read
root STATE/globals or call root-private functions. See the advanced GuideNH
[reusable functions](../src/main/resources/assets/superfactorymanager/guidenh/_en_us/advanced/libraries.md)
page for limits and the complete workflow.

Open **Settings > Studio scale** to change the editor's GUI scale with
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

To recover code from an older build, select its row and choose **Restore** in the
Build revisions dock, or **Restore to draft** in the builds window. The source is
loaded into your current local draft as one undoable text edit. Keep or discard
unsaved work when prompted; kept work also has a separate session recovery copy.
The current project name and saved revision remain the baseline. Choose Save to
create a new revision, then check/publish normally if you want to deploy it.
Restoring never changes a manager or imprinted disk. Edits made while loading
prevent the late reply from replacing your work. Only the project's owner may
retrieve source; possession of an execution disk or digest is insufficient.

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

Supported nodes are Timer, Pulse, Port edge, Input, Output, If, Forget, Redstone
output, Transfer and Print. The Add step list scrolls above its fixed footer.
Source containing unsupported statements remains available in Code; conversion
refuses before replacing it with a partial graph.

Transfer represents the existing two-, three- and four-argument item action
without `INTO`. Its fields edit fixed source/destination roles and explicitly
add/remove optional filter/count suffixes. The third expression may be an item
filter or an integer count; a fourth amount requires an item filter in the third
position. Print has one field per expression,
in source order (one to eight), with Add/Remove controls. Quote literal messages.
Both nodes accept variables in scope, literals, arithmetic, constructors, known
built-ins such as `LEN`/`ORE`, and receiver queries. Variables and current loops
also have Visual nodes. Result capture (`INTO`), player functions, WAIT, ghost
circuit actions and direct ELSE IF still require Code. Unsupported source is
preserved by refusing a partial import.

**Port edge** starts a block from named native Redstone Port input edges. It is
distinct from **Pulse**, which keeps the existing manager pulse trigger. Basic
**Fields** offers an **Edge** selector (`RISING`, `PULSE`, `FALLING`) and
1–32 ordered literal label rows. For example:

```text
every redstone rising from "signal" north side
```

Use `RISING` (or its `PULSE` alias) or `FALLING`, one or more legacy label names,
and optional physical sides (`NORTH`, `EAST`, `SOUTH`, `WEST`, `TOP`, `BOTTOM`, or
`EACH SIDE`). These are label names, not `LABEL(...)` expressions. Slots, round
robin, relative sides and `NULL SIDE` are rejected. Omitted sides keep all six.
The initial observation establishes a baseline; it does not synthesize an edge.
Fields validates the whole header on Apply, retaining body/source on refusal.
Each label row has a bounded draft-reference picker, not a live network scan.
Case, duplicate labels and ordering are kept. **Physical sides** accepts comma-separated
`TOP`, `BOTTOM`, `NORTH`, `SOUTH`, `EAST`, `WEST`; blank restores the default all six,
and `EACH` explicitly selects all. Leave out the `SIDE` keyword. Relative faces,
`NULL`, `UP` and `DOWN` are not accepted by this field.
Headers over 32 labels retain one complete manual **Trigger header** field,
without `DO` or `END`; no rows are truncated and no partial edge controls appear.
Invalid pending rows, edge or side values block the entire Apply. Cancel leaves source
unchanged; one successful Apply is one undo step. Unchanged header lexemes and
untouched physical-side spelling and gaps are preserved. Changing an existing side
value keeps its `SIDE` keyword and surrounding gaps; clearing removes the entire
qualifier while retaining its boundary whitespace. Adding sides inserts a separating
space before the new qualifier and keeps existing trailing whitespace.
Named trigger bodies must still contain only supported Visual statements.

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

Simple legacy **If / HAS** conditions expose 1–32 ordered label rows, comparison,
a literal threshold and 0–32 ordered **Resources (OR)** rows. The selectors form
one matching set; the existing set operator determines how counts are compared.
Zero resource rows restores match-all, while blank/invalid rows block Apply.
Keep at least one label. Parentheses, set operator (including EACH), side/slot
restrictions, round robin, filters and exclusions stay intact. Compound/negated
legacy conditions, trailing OR and groups over 32 use the full clause.

Self-contained typed **If** predicates use one **Condition** field. For example,
`(LABEL("source").available() AND NOT LABEL("source").empty())`,
`source HAS >= (2 + 3)` and `REDSTONE >= (2 + 3)` use existing runtime syntax.
Keep the parentheses around typed Boolean expressions and computed thresholds;
leave out `IF`, `THEN` and `END`. No query runs in Fields: one Apply validates and
commits the whole predicate, while Cancel and Undo retain their existing behavior.
Variables in scope, known built-ins and existing receiver methods are eligible.
Player functions and unsupported body statements still require Code.
New typed predicates have bounded depth/operator/postfix work. Legacy label-list
commas do not reset the enclosing Boolean budget; real data-container siblings
retain local budgets. Unsupported or over-budget source is retained intact.
**Signal** exposes 1–32 ordered destination label rows and a literal level in either
statement order and either quantity position. Add, Pick or Remove references,
keeping at least one label. Invalid/blank rows block the whole Apply, including
pending level edits. EACH, round robin, sides/slots and untouched source stay.
An omitted level remains the existing default 15; static levels retain the
compiler's existing 0–15 clamp. Two-quantity, computed-level and over-32-label
forms stay manual; no new qualifier controls or live connectivity check is added.

**Input / Output** nodes with 1–32 literal labels offer ordered label rows and
the existing allowance/resource/routing fields. Edit or Pick each row, append
with **Add label**, or **Remove** a row while keeping at least one. Duplicate
names, exact case, legacy quoted lexemes and order are retained. These are
program references; this does not rename blocks or observe live bindings.
Groups larger than 32 stay fully manual, without truncation.

Every row operation and picker choice stays pending with the other fields until
one **Apply fields**; Cancel preserves source and history. Pick can repair blank
or invalid pending text, but every row must be one valid literal before Apply.
Removing a row removes its following comma/spacing, or its preceding delimiter
for the last row; unaffected separators/lexemes stay exact. Appending adds
`, ` and the new row. Necessary token-boundary spaces stay inside the edited
label-list span. Retained/reopened drafts preserve row identity and pending text;
pickers reject stale row identities and structural revisions, including ABA.
After closing a pending inspector with Escape or Done, use **Go > Reopen Fields**
or **Go > Reopen clause** to resume it, including at compact sizes. Reopening
does not apply edits or reload source; Apply/Cancel or Apply/Reset remain explicit.

Fields errors describe the last attempted action, not live validation. A successful
pending edit clears its old error and disables its old **Details**, even if the new
text is still invalid. Use Apply to check again. No-op or rejected edits retain the
current error. Cancel, successful Apply and rebuilt/recovered Fields retire that
form's feedback without discarding another status or compiler Problems. The footer
may say **Fields changed; Apply to check** until Apply or Cancel; that is not a
claim that the pending fields are valid.

Accepted text edits clear the selected visual node and its clean cached inspector,
including comment edits and code Undo/Redo. Select the node again after returning
to Visual/Split; retained layout identity must not expose an older clause for Apply.
No-op text changes keep selection. Genuine pending Fields/raw edits still block
text editing until Apply/Cancel or Apply/Reset.
An accepted text edit also cancels a pending click-to-connect wire from the older
graph. Cursor movement or an unchanged source does not clear selection or cancel it.

Eligible Input / Output nodes offer an **OreDictionary filter** with **Off**,
**With** or **Without** and one name pattern, such as `ingotIron` or `ingot*`.
Only Off removes the filter; an enabled blank/invalid pattern blocks Apply.
Mode and pattern remain pending with the other fields, including pattern edits
while Off. Escape/reopen, resize and recovery keep both controls. Applying an
unchanged clause clears pending controls without adding a source/history edit.

This legacy filter applies to **items only**: fluids ignore it. It matches
OreDictionary names with `*` wildcards and ASCII case-insensitive matching;
unknown names are valid (With matches none, Without excludes none). It does not
translate modern tag names, accept `?`/regex, or inspect NBT. No registry scan or
ore picker is performed. Eligible OR alternatives share this one filter.
Independent limits use full clause rows rather than nested resource/filter controls.
Single-limit trailing OR, trailing inclusion commas and oversized selector groups
keep structured resource/filter editing manual. Compound filters remain unsupported by this
backport. Other safe fields remain available.
Unchanged WITH/WITHOUT/TAG/# spelling and spacing are retained. Removing a
filter-only clause preserves the existing Input/Output quantity/retention defaults.

Eligible Input / Output nodes offer **Resources (OR)** with 0–32 ordered selectors
inside one resource limit. An eligible unrestricted node or existing single static limit also offers
**Unlimited / Limited** movement amount and **Leave at least** (Input) or
**Fill up to** (Output) retention. Enable a number to enter it; an initially
omitted number starts blank and blocks Apply until filled. Disabled numbers emit
no syntax: amount defaults to unlimited, Input retention to zero, Output retention
to no stock cap. Explicit `0` differs from omission. Values are nonnegative signed
64-bit integers, measured in items or fluid mB. Each number's **EACH** applies per
resource type, independently of the other number and container EACH.

Disabling a number retains its last text/EACH locally for re-enabling and recovery;
only enabled values are validated/emitted. Apply, Cancel or explicit conversion
to full rows ends that latent control state. Removing the final limit term can
restore implicit defaults, keeping global EXCEPT/labels. Initially unrestricted
nodes start with both numbers disabled and blank latent cells; leaving them off
preserves the exact original clause. Numbers, resources, ore filters, EXCEPT and
routing can be edited together in one Apply. **Add independent limit** remains
unavailable on an initially unrestricted node until Apply and reopening Fields
establishes an explicit limit; no implicit match-all first row is created.
Multiple rows, computed values and unsupported forms retain their old controls or
manual fallback. Numeric controls are retired after **Add independent limit**;
the full row text then owns their values.

**Resources (OR)** selectors are
inside one resource limit, in either statement order. **OR shares one quantity,
retention and ore filter**; it does not create independent allowances. Add a row,
type one selector or Pick a base item ID. Blank/invalid rows block Apply; use
Remove to delete. Removing all rows explicitly restores the existing match-all
default while retaining quantity/retention and their different IO defaults.
Duplicates, case, quoted OR text and unchanged selector/separator spelling stay.
Quantity/retention EACH scopes, WITH/WITHOUT tags, EXCEPT, routing and EMPTY SLOTS
remain intact. Lexer checks keep compact quoted boundaries token-safe. Single-limit
trailing OR, trailing inclusion commas and selector groups over32 stay manual
with an inline explanation; other safe Fields remain editable. Manual
fluid IDs and quoted metadata remain legal; no NBT filter syntax is added.

Existing IO clauses with **2–32 independent limits** offer numbered full-clause
rows. Each row is one complete static SFML limit, for example
`16 EACH RETAIN 2 item:minecraft:iron_ingot OR item:minecraft:gold_ingot WITH #ingot*`.
Comma rows have separate allowances; OR shares only its row. The first matching
row wins, so order matters for overlapping selectors. Edit text horizontally,
**Add limit** or **Remove**, keeping at least one row. Blank/invalid rows block
the whole Apply, including global label/EACH/exclusion/routing/EMPTY edits.
These rows have no nested typed controls or pickers. An eligible existing single
static limit offers **Add independent limit** beside Resources (OR). It validates
all pending Fields, keeps the resulting limit as row 1 and adds a blank row 2.
Amount/retention/OR/ore controls become full-clause text; global fields stay active.
Conversion changes no program source until Apply. Invalid pending fields refuse
conversion without losing edits; Cancel discards all pending Fields. Absent limits
and unsupported forms do not offer conversion. A pending list reduced to one row
keeps row mode until Apply; reopening uses existing single-limit Fields.
Unchanged row lexemes, duplicates, case, Unicode and comma gaps are retained.
Top-level trailing commas, over32 limits and unsupported forms stay manual without
truncation. Dynamic limits still require Text; no new Visual syntax is introduced.
Overly nested or complex row syntax is refused before parsing; parentheses inside
quoted resource IDs remain ordinary literal characters. Comments are not row text.

**Exclusions** edits 0–32 ordered global IO `EXCEPT` selectors, independently of
inclusion-limit complexity. Add a row, type one resource or Pick a client item
ID. Fluids and quoted metadata remain manual. Duplicates, case and unchanged
selector/separator spelling are retained. A blank or invalid row blocks Apply;
use **Remove** to delete it. Removing the last row removes the complete EXCEPT
clause. Trailing exclusion commas and groups over 32 stay fully manual, without
truncation. Inclusion limits, ore filters, labels and routing stay intact.
Choose only changes that pending row; all rows and other Fields commit together
on Apply. Cancel, recovery, stale-owner/row guards and exact Undo remain intact.

Manual-only exclusions now show an inline notice explaining the row limit or
trailing comma. Other Fields remain editable: Apply or Cancel them before using
the full clause. If an error summary ends in `...`, use **Details** to read the
complete wrapped message, scroll vertically or **Copy error**. **Back to fields**
or Escape returns to the same pending edits without applying or discarding them.

Eligible fields have **Pick**: labels come only from references in the current
draft, and items come from client base registry IDs. This is not a live network
browser; metadata/NBT and complex expressions remain manual. Bare `LABEL("…")`
and `ITEM("…")` Transfer arguments are eligible; `.side()`/`.slots()` expressions
are not replaced. Selecting a row is temporary; **Choose (pending)** changes
only the field, then **Apply fields** changes the graph in one undo step.
Picker Cancel keeps earlier pending edits; Fields Cancel discards unapplied
fields. A changed project, node or source requires reopening Fields.

Snapshots are taken once per explicit opening: at most 256 draft labels with
4,096 reference visits, or 8,192 item-key visits, including invalid keys.
Search is limited to 96 characters and displays at most 64 matches. Capped or
unrepresentable/invalid entries are reported; manual editing stays available.
Legacy and typed label strings retain their existing distinct escaping rules.

Problems refreshes once when entering or leaving pending inspector state,
including Fields Cancel/Apply, a manual revert and raw Reset/Apply. It never
silently applies pending work. Genuine compiler errors and incomplete visual
drafts retain priority; ordinary idle ticks do not reparse source.

Supported visual edits preserve source comments and reuse node layout identities
where their structure can still be matched. Disconnected nodes stay in an
incomplete visual draft; saving is blocked until they are connected or undone.
The last safe source remains available for preview.

## Reveal source and graph nodes

Use **Go > Reveal node** (F6) at the code caret, or **Go > Reveal source**
(Shift+F6) for the selected graph node. Wide layouts also show direct buttons.
These are explicit actions: typing, moving the caret or selecting a node does
not switch panes or steal focus. Split stays Split when there is enough space;
a narrow workspace explicitly reveals the target in Visual or Code.

Reveal node matches instruction/header tokens, including trigger `DO` and If
`THEN`, by trigger and TRUE/FALSE/AFTER branch roles. Duplicate identical
statements resolve to their own current UUIDs. NAME, comments, whitespace, ELSE,
END, EOF and a caret inside a surrogate pair have no node; move onto a token.
Reveal source selects the exact instruction or header, including its interior
formatting/comments, with the caret at its first character. It preserves raw
saved line endings while accounting for the editor's normalized LF display.

Navigation pans/zooms the viewport without moving saved nodes, rewriting code,
changing dirty state or clearing text/graph Undo and Redo. Pending fields/raw
clauses, an active canvas gesture/wire, an in-flight project request, an
incomplete graph or unsupported code refuse navigation with a notice. It never
silently Applies. Maps are built only on explicit requests and checked again
after pane layout against the exact project, source and ordered graph identity.

## Recover unfinished work

Switching away from unsaved work offers **Keep draft & continue**. Returning to
that project restores its own source, graph and unapplied fields. A rejected or
uncertain write also keeps the local draft; reconcile the saved revision before
trying another write.

Choose **Discard draft & continue** to delete the current unsaved changes and
their local recovery copy. Saved server projects and published builds remain.
Discard waits until pending or unconfirmed server requests are resolved.

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


## Print argument rows

**Print Fields** offers 1–8 ordered expression rows. Add an argument, enter its
expression, or Remove an argument; at least one must remain. Quote literal
messages. Commas inside strings, arrays, structs and calls stay in their row.
Blank or invalid rows block the whole Apply. Apply commits all rows together as
one undo step; pending edits do not run. Cancel leaves source untouched.
Untouched expressions and separator spacing stay exact. Changed expressions trim
only outer whitespace when applied, matching the original scalar field editor.
Complex expressions that exceed the editor's nesting/chain budget stay intact
for Text editing. Compiler syntax, type and visual-capability checks still apply.

## Transfer overload controls

**Transfer Fields** keeps Source and Destination required. **Add filter / count**
adds the third argument, then **Add amount** adds the fourth. New fields start
blank and block Apply until completed. Remove amount before removing the item
filter; required roles cannot be removed. No default expression is inserted.

Two arguments transfer all available items. A third selector transfers matching
stock using the existing finite snapshot; a third integer caps all items.
Four arguments use a selector and an explicit integer amount:
**zero moves nothing**, and removing the amount restores automatic stock. Fields
shows which mode is pending. Apply checks all roles and commits once; pending
edits do not transfer anything. Cancel, copied recovery and Undo/Redo preserve
the source and role identities.

**Pick** accepts only a bare `LABEL("...")` or `ITEM("...")`. Clicking it on a
blank/complex expression explains that manual editing is required. Label side/
slot expressions and exact item keys remain valid manual expressions. Picker
Choose changes only its pending role, even when another role is invalid. Stale
picker actions after structural changes or copied recovery are refused. Result
capture with `INTO`, unsupported functions and over-budget expressions stay in
Text. Existing runtime and visual-capability rules remain unchanged.

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


## Visual variables and loops

Add **Program value** (program-wide LET) or **Persistent state** (STATE) as a
standalone declaration card. These have no execution wires. They initialize
before triggers; declaration order follows their creation/source order, not
their screen positions. Use **Local value** after a trigger or inside a body,
and **Set value** for reassignment. Fields expose the name/target and value.

**Counted loop**, **Array loop** and **While loop** have **BODY** and **AFTER**
outputs. Connect repeated steps to BODY and the following step to AFTER. Do not
wire a body back into the loop: repetition is implicit. Loop Fields expose
bounds, array or Boolean condition. Loop variables are read-only; body locals
keep normal SFML scopes. Invalid scope/type edits retain the last complete code
and report the issue instead of silently saving a partial program.

## Inline suggestions and readable problems

After a short typing pause, a gray inline suffix can suggest a variable, method,
function or keyword at the end of a line. **Tab** accepts only a fully visible
suggestion; **Esc** dismisses it. Otherwise Tab retains indentation/placeholder
behavior. Suggestions never change code automatically. Scoped names require a
parseable draft; **Ctrl+Space** retains the explicit completion menu.

The **Problems** dock and window show bounded, wrapped issue cards with compiler
locations, messages and source excerpts. Click an issue to jump to it, or focus
the list and use Up/Down then Enter. Stale locations never overwrite or navigate
against a different draft; current syntax is rechecked first.


Dynamic arrays use `array("type")` or `array(sequence)` with explicit REF mutation.
The Insert/API catalog describes all collection helpers. ARRAY and pure readers
can appear in supported Visual expressions; mutation calls remain Code-only.
See [dynamic arrays](SFML_GUIDE.md#dynamic-arrays) for bounds, index-ref
invalidation, O(n) replacement costs and persistence restricted to plain data.


Visual also supports **For loop** (`for (init; test; update)`) and **Indexed array
loop** (`for (index, value) in sequence`). Both use BODY/AFTER. The former has a
whole-header field; the latter has separate index/value/source fields. Completion
and syntax coloring respect mutable header LET scope versus read-only foreach
binders. Mandatory header separators stay part of executable/layout identity.

## Enter completion and selection scrolling

Press Enter after an opening brace or a keyword block header to create an indented
body and its matching closer. An existing matching closer is reused. The edit and
caret position undo together. Strings, comments and selected replacement text do
not trigger completion. Settings → Auto-close toggles it for this client session.
Drag a code selection toward or beyond the viewport edges to keep scrolling; hold
the pointer there to continue. Wrapping disables horizontal selection scrolling.
