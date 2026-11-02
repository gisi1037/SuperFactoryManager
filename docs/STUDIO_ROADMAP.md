# SFM Studio roadmap

SFM Studio is the full-screen authoring interface for Programming Station projects.
This is an experimental development snapshot; unsupported Visual constructs must
preserve their source and explain why they require Code.

## Available now

- RAM-only dynamic `T[]` arrays use explicit ARRAY construction/conversion,
  REF mutation helpers and bounded immutable replacements. Fixed `T[N]` stays
  unchanged. Captured indexed references/assignments reject structural changes
  across WAIT; fixed-only path costs are retained. Native helper/Studio/VS Code
  documentation follows the compiler. Dynamic STATE and reserve/capacity remain
  separate follow-ups; growth is O(n), not amortized constant time.

- Visual supports program/local LET, STATE, assignment, counted FOR, array FOR
  and WHILE, including scoped expressions and known built-ins. Loop BODY/AFTER
  roles remain acyclic; declarations are separate ordered roots. Full source
  compilation retains scope/type authority and invalid edits keep safe source.
- Problems uses wrapped, selectable issue cards with locations and source
  excerpts. Automatic inline suggestions are debounced, optional to accept with
  Tab, and dismissible with Esc; explicit Ctrl+Space remains available.
- Older graph palettes page their expanded node lists without overlapping the
  canvas. Loop body and declaration identities participate in layout/navigation.

- Published build source can be restored to the current local draft through
  Build revisions. Restoration preserves the saved project baseline, guards
  unsaved work and late replies, and creates one undoable text edit. Save/check/
  publish remain explicit; running managers and disks are unchanged.
- Value-returning functions accept explicit REF parameters alongside void
  procedures. References retain exact types, aliases and WAIT progress; calls
  with REF effects remain excluded from global initializers and named-redstone
  subscription expressions. Native actions still require triggers or void
  procedures.

- Separate fluid API: `transferf(source, destination)` and optional mB amount or
  fluid selector/exact key, plus `favailable`, `fempty`, `fcount` and `fcontents`.
  Compiler help, insert snippets and GuideNH cover all forms. Direct Forge tank
  observations and recovery are regression-tested; pack-specific tank/cover and
  proxy qualification remains separate.

- Code, Visual and Split views; syntax highlighting, comments, undo/redo,
  searchable insertion helpers, compiler diagnostics, projects and templates.
- Graph/source navigation by node identity and UTF-16 source spans, including
  repeated instructions and Unicode. Outline captions fit a single row.
- Pending typed Fields for input/output limits, ordered labels, resources,
  exclusions, ore filters, conditions, Print arguments and Transfer arguments.
  Apply validates the complete candidate; rejected edits preserve the document.
- Port edge roots for named native Redstone Port inputs, distinct from manager
  Pulse roots. Structured headers support rising/PULSE/falling edges, 1–32 label
  rows, and physical side selections. Larger headers retain a complete manual
  field. Blank physical sides use the six-face default; EACH selects all explicitly.
- Fields ownership and copied recovery, bounded reference pickers, source maps,
  and document history. Unsupported bodies still require Code.
- The operational Factory Manager interface provides status/activity, terminal,
  logs, profiling and variable watches. Inventory integration remains optional.

## Validation and remaining work

The latest physical-side changes passed 583 focused tests with one unchanged
opt-in skip across 47 suites, plus 198 independent model/runtime checks. The
preceding full checkpoint passed 1,532 tests with 14 skips. A new complete build
and in-game qualification of the physical-side control remain pending; these
counts are not a full-pack performance or compatibility certification.

Native tooltip rendering, precise divider dragging and wide outline wheel routing
remain separate qualification gaps. Compact outline navigation and preservation
checks passed within the existing logical-input harness. Hardware/IME, native CRLF,
clipboard, broader multiplayer and real-provider qualification remain separate.

Next work is in-game physical-side editing/recovery checks, remaining visual
constructs, the live network browser, reusable libraries and recipe-query APIs.
Performance changes should follow correctness-checked measurements and preserve
program cadence, ordering, item/NBT identity, sides/slots and recovery behavior.
Event-assisted availability remains a later adapter-specific experiment.

See [Studio usage](STUDIO.md), the [SFML guide](SFML_GUIDE.md), and
[program performance](PROGRAM_PERFORMANCE.md) for details.

## Modern SFML authoring

Studio and the compiler support plain or `$` variables, mandatory quoted literal
labels or LABEL_REF expressions, computed LABEL names, braces or DO/THEN/END,
type-first function declarations and space-separated typed parameters. Legacy
FUNCTION/RETURNS declarations remain aliases. Unquoted literal label names must
be converted explicitly; old disks do not receive an implicit fallback.

Value functions may read the calling manager's STATE; VOID procedures can update
it and perform explicit actions. REF parameters and arguments are explicit at
both ends, preserve aliases and frozen member/index paths, and can survive WAIT.
WAIT pauses a bounded RAM call stack on game ticks without blocking the server.
SETCIRCUIT configures supported native GT ghost selectors. These advanced forms
remain in Text when the visual graph cannot preserve them; Studio and VS Code
help, diagnostics and snippets follow the actual compiler.

Qualification covers 1,726 headless tests (1,710 pass, 16 optional skips), compiler
checked guides/snippets, formatting, Checkstyle and assembly. Native machine and
cover compatibility remains a separate in-game qualification step.


## Runtime failure diagnostics

Manager and Studio terminals expose `error` / `error clear` and an Errors action.
Runtime failures include the statement location, bounded function context and
underlying cause even when PRINT capture and log recording are off. Reference
program details remain restricted to source authors. Retained details are local
to the loaded manager and clear on program replacement or unload; this does not
add persistent logs or alter transfer permissions. Formatting-only saves preserve
running state and map later failures to saved source coordinates.

## Indexed loops

Studio supports Java-style three-clause FOR and zero-based index/value foreach,
including BODY/AFTER graph pins and editable loop headers. WAIT resumes at the
same loop phase without replaying completed effects. Loop-local completion,
colors and compiler-backed VS Code snippets follow the same scope rules.

## Persistent dynamic data

Dynamic arrays of persistable data can live in STATE, including typed empty and
nested collections. Actual retained cells and cached serialized sizes enforce
bounds before heap replacement; saves remain lifecycle operations. Existing
fixed-array disk encoding is retained. Formatting-only saves preserve sleeping
references and their structural epochs; reload/reset creates a fresh lifetime.
Label handles and query snapshots remain RAM-only. A synthetic collection
characterization covers incremental and bulk growth, removal, live heap writes
and separate persistence verification without timing-based pass/fail thresholds.

## Exact reusable libraries

The station can publish a saved functions-only project as an immutable library
and insert an exact owned revision through a paged picker. Calls use explicit
aliases. Closed V2 program bundles embed every selected leaf library; later
edits or missing authoring artifacts do not substitute another revision at run
time. V1 program/grant formats remain supported. Modules share normal compiler
and runtime bounds, but cannot implicitly capture root state or private names.
Runtime diagnostics preserve library coordinates. Source restoration returns the
root program only, and linked pretty-printing never flattens private modules.
Cross-player sharing, module constants and nested imports remain separate work.

## Network inspection and function execution

The Manager Network tab now provides read-only search and item/fluid filters
over cached discovery and saved label positions, including unlabeled and
unavailable blocks. Explicit bounded paging never loads chunks, reads stock or
rebuilds discovery automatically. Snapshot freshness and partial indexes are
visible; cable label editing remains separate work.

Functions proven unable to WAIT no longer allocate their own continuation
bookkeeping inside sleeping callers. Direct/transitive WAIT, including WAIT(0),
retains resumable execution. Configured call/depth/work limits stay enforced;
reduced allocation charges can allow more work within the allocation allowance.
An opt-in benchmark separates compilation and actual scheduled execution for
equivalent inline and immutable-library programs. Synthetic results are not a
full-pack throughput guarantee. Native/full-pack qualification remains separate.

## Cable, crafting and editor feedback checkpoint

Full-block cable is available in NEI, with free reversible plain-item conversions.
Direct recipes target late HV; see CRAFTING.md. Studio adds native text cursors,
matching delimiter feedback and subtle two-space indent guides. Full-pack crafting,
cursor appearance and scaled/wrapped editor rendering still require manual checks.
Disk copier and automatic block completion are implemented; native qualification remains pending.

## Trigger-level BREAK

Outside loops, BREAK ends only the current trigger activation. It starts fresh
at its next normal schedule/edge; other due triggers continue. Nearest-loop
behavior, completed effects and function-local restrictions remain intact.

## Enter completion and drag selection

Enter now inserts a block body and missing closer, with existing-closer reuse,
caret placement and one-step undo. Settings exposes session-wide Auto-close.
Text selection scrolls at viewport edges each frame, including with a stationary
held pointer. Scaled native mouse/keyboard behavior remains a manual check.

## Saved-source transfer

Project selector → Transfer exports all your saved program names/source into one
Base64 string. Read clipboard previews it; Import as new creates fresh copies.
Existing projects, current drafts and the station selection remain intact. Builds,
permissions and runtime state do not transfer; library references need relinking.
Import is sequential, keeps confirmed copies on interruption and never auto-retries.
Native clipboard and two-world UI checks remain manual.

## Selection scroll-coordinate fix

Per-frame text selection now samples the editor viewport before its content
scroll transform. Scrolling is applied once, preventing an interior drag from
jumping to EOF when the last line is visible. Real MUI2 matrix tests cover scale
and both axes; native pointer behavior remains a manual regression check.

The top project selector now opens its project dialog instead of only refreshing
the sidebar. A direct Transfer programs sidebar button also opens source archive
export/import, preserving the current draft.

## Runtime random values and fixed timer phases

RANDOM(minimum,maximum) draws an inclusive INTEGER at runtime. EVERY 20 PLUS
(RANDOM(0,19)) TICKS snapshots one phase per manager execution state and keeps
the 20-tick period, including with WAIT/BREAK. Old numeric timers are unchanged.
Runtime-offset headers stay text-only; native insertion/help and VS Code 0.3.9
cover the API. Idle polls do not reroll. This is not a dynamic period or guaranteed
load balancer; random phases can collide. Reload/reset can pick a new phase.

## Disk copying and compact source editing

The Disk Copier writes up to eight independent program/label copies per cycle,
consuming one fresh blank per copy in 20 loaded server ticks by default. It has a late-HV recipe, no operating EU, a normal
MUI2 inventory window and sided automation. Runtime STATE, recovery ownership and
private authoring access do not propagate. See CRAFTING.md and the in-game checklist.
Native hopper/pipe, GUI and multiplayer qualification remains pending.

Studio dividers now capture release outside their handles and stop on lost
focus/button state. The native I-beam follows text-line height, including Studio
scale. The Manager source editor uses compact toolbars/footer to show more code.


Manager visual/preview controls now reflow across the available width. Hidden
node palettes and property fields release their reserved space; long diagnostic
messages stay in one status line with complete hover details. The native cursor
has a one-pixel stem and a shorter height. Copier timing and batch limits are
server-configurable, with eight blank/output queue slots and legacy save indices
preserved. Native visual/automation qualification remains in the checklist.

## Startup diagnostics, project cleanup and crafting

Managers now save their PRINT-capture setting and restore it before the first
program run after reload. Enable it once after updating an older world. Output
history remains bounded and RAM-only; the preference belongs to the Manager,
not a copied program disk. Printing does not mark the chunk dirty.

Studio provides Delete open project and per-project Delete actions. Confirmation
checks the saved revision; deleted drafts leave the browser and source exports.
Published disks and libraries remain usable. Unsaved open source becomes a new
local draft, and the last saved source is archived for world-backup recovery.

The Disk Copier recipe includes an HV Sensor and HV Emitter; the Label Gun
includes a display. Inventory Cable uses SteveFM's four stainless-steel plates,
three Fluix ME Glass Cables and two neutral bundled cables in a rotated layout,
producing two cables without competing with the original recipe. Free reversible
conversions remain available.

Stable 2.9 headless qualification: 2,151 tests, 2,132 passed, 19 skipped; no
failures or errors. Compiler bridge, style checks and GuideNH packaging passed.
Native UI, restart capture and NEI checks remain in IN_GAME_TEST_CHECKLIST.md.

## Cable label editor discovery foundation

A separate incremental scan now supports the forthcoming cable label editor.
It inspects at most 128 positions per server tick, caps discovery at 32,768
positions and refuses stale or truncated snapshots. It lists all peripheral
kinds and candidate Managers without reading inventory contents or opening
unloaded chunks. A selected-Manager scan includes that Manager's separate
branches; Managers do not become cable bridges. Existing runtime discovery and
program execution are unchanged. The MUI2 screen and permission-checked label
writes are still pending; this checkpoint adds no clickable cable editor yet.

Seven discovery regressions cover bounds, repeated calls, loaded-only access,
multiple Managers, runtime parity, invalidation and server-thread enforcement.
Full stable qualification: 2,158 tests, 2,139 passed, 19 skipped; no failures or
errors. Style, compiler bridge and GuideNH checks passed. Native qualification
remains separate.

## Cable label editing

Empty-hand use of a full or Fancy Cable now opens a MUI2 label tool. Pick a
Manager explicitly, select connected blocks across pages, and add/remove one
exact label on up to 32 blocks. Multi-label memberships, unrelated assignments,
program source, disk identity and live STATE are preserved. Held cable placement
and Label Gun use remain available. This tool grants no Manager program/control
or inventory capability. It never loads chunks or reads container contents.

Topology, Manager/disk identity and saved labels fence every accepted write.
Selected endpoints are rechecked before commit; pending recovery and oversized
candidate NBT refuse without changing labels. Pending heap STATE is flushed for
sizing, then only the label tag is replaced on the live disk root. Source refs
are not renamed. A Label Gun keeps its own table: pull newer disk labels before
pushing an old gun table again.

Discovery, reverse-label indexing, metadata collection and filtering run in
bounded batches (128 units per server tick). Search uses every label, including
those shortened with ... in rows; it never rescans all label groups per block.
Limits: 64 candidate Managers, 8192 connected blocks, 32 selected targets and
one accepted action per tick. Window/session/revision checks protect queued edits.

Stable headless checks passed: 2167 tests, 2148 passed, 19 skipped, no failures
or errors. Style, compiler bridge and GuideNH checks passed. In-game acceptance
is P2 in IN_GAME_TEST_CHECKLIST.md; GUI, multiplayer/claims and full-pack tests
remain open and this checkpoint is not 1.0 release certification.

Native follow-up: the first GTNH 2.9.0 / Java26 run exposed unrelated chunk loads
invalidating the cable window while browsing. Discovery now retains a weakly
registered coordinate/chunk footprint, including observed air neighbors. Changes
inside it and unknown edits still fence snapshots; unrelated block/chunk activity
does not clear selections. No World, tile or Manager is retained by the fence,
and ordinary runtime discovery/redstone cache behavior is unchanged. A snapshot
keeps its own fence after the incremental scanner is released.

Regressions cover partial/completed footprints, unknown edits, adjacent extension,
chunk unload and 1000 unrelated notifications while preserving an accepted label
edit. Full stable checks:2170 tests,2151 passed,19 skipped,no failures/errors;
style/compiler/GuideNH passed. Two isolated full-pack clients launched with
3GiB heaps and verified Java26.0.2.1. Native edit/multiplayer qualification of
this fix is still pending; startup alone is not an acceptance result.

## Label push and legacy fluid callback guards, 2026-11-01

Label Gun pushes now flush pending heap STATE, validate the complete candidate
against the existing 30,000-byte NBT bound and recheck the physical Manager,
installed disk and generation. A refused push keeps old labels; a successful
push replaces only the labels tag on the live root, without replacing STATE.

Legacy INPUT/OUTPUT fluid moves now journal on the original disk before actual
drain/fill/rollback callbacks. Callback replacement, A-to-B-to-A disk changes,
physical Manager replacement and nested manual runs cannot redirect evidence to
a replacement disk. Known rollback remainders remain distinct from uncertain
callback outcomes. Existing EACH/RETAIN quantities and normal resource movement
remain covered by characterization tests. The replacement callback regression
failed against the previous production path before the fix.

Stable automated checks: 2177 tests, 2158 passed, 19 skipped, zero failures/errors;
style, compiler bridge and GuideNH checks passed. Native cable smoke on the prior
scoped-discovery build passed selection and two-block add/remove while keeping
original labels and the Manager running, with the inventory mod present. The
GTNH 2.9.0 dedicated server also cold-started with both mods on Java26/3GiB and
saved/stopped through localhost RCON. Two-player acceptance remains pending.
These results do not certify 1.0, fault-free native providers or crash atomicity.
## Recipe identity foundation, 2026-11-01

Exact item/fluid keys now provide budgeted, versioned SHA-256 identity fingerprints
for the forthcoming catalog. Canonical NBT keeps compound-key order independent,
list order and numeric types exact, and null NBT distinct from empty NBT. Item
metadata and registry names are included; stack amounts and transient fluid IDs
are excluded. Item/fluid domains are separated and returned bytes are fresh copies.
No live provider or world read is performed. This is cold catalog infrastructure;
existing transfers and ordinary query hot paths do not hash these identities.

Eight new identity tests include an independent golden digest, mutation isolation,
fluid quantity independence and work-budget refusal. Full stable checks:2185 tests,
2166 passed,19 skipped,no failures/errors; style/compiler/GuideNH passed. This does
not expose a recipe query yet: immutable descriptors, shared bounded output index,
GT adapter/lifecycle, SFML API and native recipe qualification remain pending.
## Shared recipe catalog core, 2026-11-01

Immutable descriptors preserve ordered input/output slots, catalysts, item/fluid
consumption and output chances, execution versus display alternatives, fluid
alternative amounts, matching policies, processing costs and unsupported
requirement markers. Canonical fingerprints identify descriptor content; exact
duplicate descriptors coalesce. An exact output index retains distinct candidates,
including ambiguity, and unsupported buckets prevent false no-match conclusions.
Tokens are scoped to a server session, revision, map, adapter and fingerprint.
They authorize inspection only, not crafting or access to authoring source.

Provisional caps:16 maps,16384 recipe visits,131072 postings,64MiB conservative
retained accounting,64 returned candidates,256 shared query work units per tick.
Capped builds cannot publish; capped queries return no truncated candidate list.
These are finite implementation bounds, not measured whole-pack performance.

Fourteen focused regressions and full stable checks passed:2199 tests,2180 passed,
19 skipped,zero failures/errors; style/compiler/GuideNH passed. Optional-mod/server
classloading checks exclude GT, NEI and client GUI types. Native adapter/lifecycle,
SFML exposure and actual-pack recipe coverage/performance remain pending. No new
recipe command is available to players at this core-only checkpoint.
## Qualified recipe adapter and lifecycle, 2026-11-02

A common-only reflective adapter now inspects the pinned GT5.09.54.250/.265 APIs
for the macerator, metalbender and cutting-saw maps. It requires the registered map
identity, exact ordinary backend and known recipe classes. It preserves catalysts,
all chance fields, effective special-item NBT flags, ore-or-explicit alternatives,
display-only alternatives, fluid alternative quantities, byproducts and costs.
Unknown special slots/values/metadata remain unsupported; unknown classes or
unreadable output identities make map coverage incomplete. It never constructs
native input caches or flattens getAllRecipes on ordinary queries.

The shared snapshot arms after server start and waits for GT's first-world work.
Stop/remapping withdraws it and expires tokens. Publication, withdrawal and lookup
freshness are serialized; the cold native scan stays outside the lifecycle lock.
Queries share the actual server-tick allowance. Readiness is visible with the
operator command /sfmprofile recipes. Arbitrary runtime recipe mutation is not a
supported refresh mechanism. Eight new adapter/lifecycle regressions passed,
including concurrent withdrawal during build. Actual-pack coverage/build latency
is still pending; no SFML recipe function or NEI import is exposed yet.
## Complete fluid journal preflight, 2026-11-02

Both native transferf and legacy INPUT/OUTPUT now ask the Manager to preflight the
complete write-ahead fluid record, including destination coordinates/side/dimension,
current flushed heap STATE and space for later recovery stage names. Oversized
requests refuse before any actual drain/fill, retaining old disk metadata except
legitimate STATE flushing. Physical Manager/disk/generation are rechecked before
attaching the journal. This check is before ownership: emergency evidence for fluid
already extracted is never discarded because an external provider misbehaved.

The near-limit legacy regression failed against the prior implementation. Four
new regressions cover legacy and transferf refusal, dirty STATE, valid near-limit
journals including later stages and retained emergency evidence. Combined stable
checks:2211 tests,2192 passed,19 skipped,zero failures/errors; style/compiler/GuideNH
passed. This is not native provider fault/crash-atomicity certification.
Native Java26 follow-up: the isolated2.9.0 server stayed up but the catalog refused
initialization. API inspection found that GT's Java17 multi-release class omits the
Java8 downgrade bridge used by the first adapter. Effective special-item NBT policy
now uses GTppRecipeHelper plus the same three public ItemList prototypes and
GTUtility.areStacksEqual(...,true) as the inspected .250/.265 implementation, without
private access, cache construction or invoking GT's mutating setItemStacks.
Prototypes resolve once per cold build when the flag is enabled. Fixtures now
intentionally omit the downgrade bridge and check the distinct ordinary versus
ore-or-explicit NBT rules. Catalog failures report a bounded readable cause.
45 targeted adapter/lifecycle/common/Forge checks and style passed; the next
actual-pack startup must confirm readiness before SFML recipe exposure.Actual-pack follow-up reached a catalog build cap, with no partial data published.
Cold-build refusals now identify the cap and used/requested/limit counters. Optional
sfm.recipeDiagnostics logs native collection sizes without flattening registries or
constructing input caches. No cap has been raised. 59 targeted checks and style /
compiler bridge passed; measured native coverage remains required before SFML exposure.
