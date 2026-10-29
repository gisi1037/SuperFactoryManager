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

The current checkpoint passes 1,144 regression tests, with eleven optional skips.
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
from exact saved revisions. Reference disks and runtime activation remain a
separate next step: define executable/state binding identity, verify owner-scoped
receipts, and fence asynchronous loads before exposing installation. Mutable
compiled programs must remain private to each manager.

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


## Reference-disk foundations

The reference-disk backend now separates content identity from permission to run
it. An authenticated owner can issue an opaque, world-local execution grant for
an exact existing published revision. Its disk reference contains a format
version, random token and build digest. Project IDs and content hashes alone
grant no access. Execution/copy permission does not grant project editing or
source-export permission. Ordinary possession and authorized inventory viewing
can share the bearer reference; generic block-description packets redact it.

Every cold preparation checks the stored grant and exact compatible bundle and
compiles a separate mutable program instance on the bounded storage worker. It
never substitutes the latest project draft. Missing, malformed or unsupported
data fails closed and is preserved. No preparation cache is used in this slice.
Grants have finite world quotas and are never automatically evicted. Back up
`world/sfm` with the rest of the world; an inventory-only copy into another world
does not bring its execution grant with it. A complete world clone retains it.

Legacy source disks continue to work. Strict reference/invalid modes prevent
legacy editors and reset actions from replacing a reference with empty source.
STATE now binds to the exact disk execution identity; changing a reference or its
document metadata invalidates that binding while still allowing owned committed
heap values to flush before schema replacement. Each physical copy has its own
mutable STATE store.

This is a tested backend foundation, not an installable reference-disk workflow.
Studio does not yet write reference disks, and managers deliberately keep such
media paused. Next, add the server-validated fresh-medium write transaction and
queued manager activation with world/tile/disk/document/request generation
fences. Preserve intervening pause/recovery decisions, keep all warm manager ticks
free of storage I/O, and qualify stale completions and multiplayer before exposing
disk writing. Reusable dependency libraries and new editor features follow that
activation boundary.

The foundation checkpoint passes the clean public build, formatting, Checkstyle,
compiler bridge and GuideNH packaging: 1,189 tests total, 1,177 passed and twelve
optional skips. The 34 added tests cover grants, storage corruption/ownership,
strict disk metadata, STATE identity/transport bounds and inert runtime/network
handling. One new skip is an unavailable Windows symlink fixture. Native runtime
installation and two-player reference workflows remain unqualified because they
are not exposed in this slice. No throughput or tick-latency improvement is claimed.

## Boundaries

The Programming Station is a full-screen authoring surface. Split player-inventory
panels belong to the separate Factory Manager interface. MUI2 supplies layout and
input infrastructure; completion, diagnostics, graph fidelity, and safe server
operations remain SFM responsibilities.

See the [SFML guide](SFML_GUIDE.md) for executable language behavior. Planned
features in this roadmap do not introduce accepted syntax or a new interpreter.
