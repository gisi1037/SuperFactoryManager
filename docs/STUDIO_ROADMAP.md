# Native Programming Station roadmap

Updated 2026-10-29. The interactive IDE prototype is the design reference; the
in-game implementation uses native Java widgets in MUI2. This document describes
the current native integration checkpoint, not a claim that the prototype is complete.

## Implemented native surfaces

- Full-screen dark industrial/pixel styling with responsive toolbars and editor
  bounds. The station owns keyboard focus; player inventory slots and inventory
  overlays are unavailable while editing.
- A text buffer with syntax colors, line numbers, scrolling, wrapping,
  indentation, undo/redo, literal Find/Replace and searchable categorized snippets.
- Context-aware function/method completion, cached signature/help on hover and
  current-program function declarations in the searchable API reference.
- Scope-aware `$variable` completion for parseable drafts, with declaration
  locations, function/trigger isolation and block/loop lifetime boundaries.
- Keyboard selection and insertion in completion lists, scroll-to-selection,
  and Escape cancellation for authoring dialogs.
- Compiler validation, source error markers and navigation to reported errors.
- Code, Visual, and Split views share one source owner. Split requires at least
  640 logical pixels and has a draggable divider; narrower windows use one
  authoring surface at a time.
- Graph nodes for timers, redstone pulses, input, output, conditions, FORGET, and
  redstone output. Drag nodes, connect or disconnect pins, pan, zoom, fit, and
  undo graph changes. A disconnected graph blocks saving while retaining the
  last safe source and the incomplete visual draft.
- A scrollable clause inspector with timer intervals, routing labels, allowances,
  retention, EACH, round robin, sides/slots and empty-slot output fields. Ambiguous
  clauses remain editable through their full SFML clause.
- An outline, searchable API reference, and Problems panel. The API reference
  includes built-ins, label methods, and functions declared in the current source.
- A compact manager terminal opened from the station. Choose a nearby loaded
  manager explicitly; the server checks the station session and manager access
  for every command. Opening the station does not enable PRINT capture.
- Named projects with server-authorized draft operations, separate validation,
  revision checks, and local draft recovery when a write is rejected or uncertain.
- Existing SFML execution remains in the server's Java engine. Editing a draft
  does not execute it or modify a running manager program.

These native surfaces are being exercised and repaired together. Source recovery,
late save/load acknowledgements, modal input, cross-view undo, resizing, and
inventory isolation require both regression coverage and in-game checks. An
implemented control is not by itself a claim that every workflow is qualified.

The previous GUI checkpoint passed 1,144 regression tests, with eleven optional skips.
Scoped daily-803 Java17/LWJGL3 checks with NEI, MUI2 2.3.93 and 72 player-storage
slots passed 42 in-game assertions covering publication, source/visual ownership,
terminal focus/status, GUI scales, inventory isolation and slot gestures. Player
number swaps preserve exact indices even with handler-backed MUI2 slots. Synthetic
event injection does not qualify OS/IME or hardware-held Shift/resize behavior;
two-client multiplayer workflows remain open. These checks establish no new
whole-pack performance budget or speedup.

## Next integration steps

The Factory Manager now has a separate operational dashboard and canonical split
player storage views. Studio's Builds view publishes immutable root-source bundles
from exact saved revisions. Reference disks and runtime activation now have a guarded development workflow;
qualification and cold-start admission remain gates before default enablement.
Mutable compiled programs remain private to each manager.

1. Complete full-pack and multiplayer workflow qualification. Scoped development
   client checks cover safe project switching/recovery, acknowledged saves, graph
   connections, cross-view undo, target replacement and inventory isolation.
2. Expand visual coverage only with explicit graph nodes and round-trip tests.
   Unsupported language constructs must continue to preserve source and explain
   why Visual is unavailable. Advanced data statements currently require Code.
3. Improve beginner authoring with resource/label pickers, broader structured node
   properties, and clearer connection guidance. Keep the raw clause editor for
   advanced options that do not yet have a dedicated field.
4. Extend completion and function help, error navigation, and beginner guidance.
   Keep the insert catalog aligned with the actual supported language.
5. Evolve the compact terminal into the prototype's docked drawer after its target
   and command lifecycle are qualified. Keep project validation separate from live
   manager actions; the terminal is not an arbitrary SFML evaluator or breakpoint debugger.
6. Qualify GUI scales, resizing, input focus, modal transitions, save conflicts,
   multiplayer authority, and the split-inventory mod's overlay suppression.

Each slice needs focused regression tests and an in-game check before being
described as complete. Larger scripts and graphs also need bounded rendering,
validation work, and responsive input; timing claims require measurements.


## Reference disks and operational activity

Exact-build disk writing and cold manager activation are implemented behind an
opt-in development-client gate. The default UI keeps writing disabled pending
qualification. The server authenticates the selected immutable publication and
rechecks the live world/service, station/session/connection and exact fresh held
medium before one inventory replacement. Candidate document/reference metadata
is complete and size-validated before publication. Duplicate acknowledgements do
not write again, and a stale completion cannot clear a newer pending request.

Managers submit cold preparations only from server updates. NBT reads, item
tooltips and client synchronization never resolve source. Completion checks the
world, loaded tile, physical disk, reference/document, STATE ownership and request
generations before publishing its private plan. Pause and recovery decisions win
over late results. Missing/corrupt builds pause visibly with explicit Start retry; temporary
capacity waits retain run intent and retry automatically through bounded admission. Warm updates retain the loaded plan without filesystem I/O.
Legacy source disks and their unload/global semantics remain supported.

Reference metadata and source are redacted from general block packets. Authorized
physical inventory synchronization carries the actual bearer disk. Runtime
reference execution suppresses automatic reconstructed-source debug logs and uses
generic failure messages; intentional PRINT output remains supported. Source-only
editing/reset controls cannot convert a reference to an embedded-source disk.
World backups must include projects, bundles and execution grants alongside
inventories. An inventory-only copy into another world does not carry its grant.

The manager Overview now has a bounded measured update-cost chart and an expanded
activity view, with runtime states and a scheduler-clock marker. It samples at
most twenty timings; it does not infer successful transfers or a live instruction
pointer. Source-authorized opt-in statement tracing remains a separate task.

The combined source checkpoint passes the clean public build, formatting,
Checkstyle, compiler bridge and GuideNH packaging: 1,229 tests total, 1,217 passed
and twelve optional skips. Native qualification is recorded separately; these
regressions alone do not establish two-player behavior or a throughput budget.

Scoped combined-pack checks also passed actual saved-build selection/writing,
reference installation, running/pausing, warm execution with the bundle absent,
and a cold missing-bundle failure followed by exact-artifact restoration and UI
retry. Saved STATE resumed with only the newly due trigger increments. The
activity dialog fits logical 320x240 and 427x240 screens with split player storage.
These checks use an isolated daily-803 Java17/LWJGL3 client with MUI2, NEI and
Dynamic Inventory; they do not qualify simultaneous two-player workflows.

Remaining order:

1. Qualify disk writing, activation, pause/retry, missing/corrupt storage and
   reloads in the combined pack. Complete real two-client authority and hardware
   input checks before enabling the reference workflow by default.
2. Qualify the implemented bounded FIFO admission under real chunk reload/churn.
   Eight outstanding cold jobs and 24 authoring jobs reserve progress for both
   workloads. Waiting capacity is 4,096; overflow retry is paced, and FIFO fairness
   applies to enrolled requests. See [measurements](REFERENCE_PERFORMANCE.md).
3. Keep provider/recovery and scaling gates open. Explicit loaded-reference STATE
   reset is implemented and passed the native single-player workflow; multiplayer checks remain. No new whole-pack
   throughput or hard tick-latency budget is claimed. Source-authorized statement
   and graph tracing remains separate work.
4. Continue reusable dependency libraries, typed visual nodes/pickers, source/node
   selection, the live network browser and docked diagnostics afterward.

## Boundaries

The Programming Station is a full-screen authoring surface. Split player-inventory
panels belong to the separate Factory Manager interface. MUI2 supplies layout and
input infrastructure; completion, diagnostics, graph fidelity, and safe server
operations remain SFM responsibilities.

See the [SFML guide](SFML_GUIDE.md) for executable language behavior. Planned
features in this roadmap do not introduce accepted syntax or a new interpreter.


The admission/reset/lexer source checkpoint passes 1,252 tests total: 1,238 passed
and 14 optional skips, including two opt-in performance suites. The default UI
still keeps reference writing disabled while runtime qualification remains open.
