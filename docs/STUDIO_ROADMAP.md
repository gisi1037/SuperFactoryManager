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

The Disk Copier writes one independent program/label copy onto one fresh blank
in 100 loaded server ticks. It has a late-HV recipe, no operating EU, a normal
MUI2 inventory window and sided automation. Runtime STATE, recovery ownership and
private authoring access do not propagate. See CRAFTING.md and the in-game checklist.
Native hopper/pipe, GUI and multiplayer qualification remains pending.

Studio dividers now capture release outside their handles and stop on lost
focus/button state. The native I-beam follows text-line height, including Studio
scale. The Manager source editor uses compact toolbars/footer to show more code.
