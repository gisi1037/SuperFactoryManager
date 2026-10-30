# Native SFM interfaces and development roadmap

Updated 2026-10-30. The Programming Station is a full-screen MUI2 authoring
workspace. The Factory Manager is a separate centered machine GUI. The web draft
guides the design; native controls use the actual compiler and authorized server
services, without a browser runtime or simulated sandbox.

## Delivered interface redesign

- Manager: compact machine footprint, equipment disk and Start/Pause, normal
  MUI2 player inventory/hotbar, NEI around the window. Inventory extension support
  belongs to Dynamic Inventory; SFM no longer invokes its API or owns split-panel
  layout controls. Other packs can use SFM alone.
- Inline Manager Terminal, searchable/filterable/wrapping Logs with separate
  recording/filter levels, follow/freeze/copy, bounded opt-in Profile and live
  read-only Watch. Overview latency and the activity popup remain available.
- Studio: persistent Projects/Templates sidebar, remembered project reopening,
  top project/draft/safe/tool/save/check/imprint controls, categorized insert/API
  catalog, Visual/Split/Code at the authoring surface and a bottom dock for
  Problems, Debug terminal, Watch and Build revisions. Project and dock dividers
  are draggable; small GUI scales use a compact arrangement.
- Raised charcoal industrial panels/buttons with muted red accents and opaque
  text surfaces. No accessible player inventory, hotbar or NEI in Studio.
- Remembered Studio-only GUI scale in Tools, with a Game reset and temporary
  fitting for smaller windows. Editor/dialog transforms keep pointer targeting
  together; a recessed tray groups Insert tools above Visual/Split/Code tabs.
- Normal Imprint disk workflow: exact saved revision -> immutable publication ->
  selected build -> one fresh held disk. Server ownership/session/revision/medium
  checks and replay-safe acknowledgement behavior remain enforced. The former
  development-only UI gate is removed.
- Opt-in profiler: complete server manager update cost, 1–4,096 samples (default
  1,200), average/percentiles/max and due-update count. No per-statement tracing or
  throughput claim. Live values use bounded snapshots, not arbitrary evaluation;
  reference variables require the verified source author.

Existing source/visual ownership, graph identity, incomplete-draft protection,
server-revision save/validation, local recovery, modal authority, undo, syntax
colors, completion, hover help, API and compiler diagnostics remain in place.

## Verification and limits

The full build, compiler bridge, GuideNH packaging, formatting and Checkstyle
pass. Current regression count: 1,272 total, 1,257 passed, 15 optional/environment
skips. Focused tests cover profile limits/quantiles/lifecycle, nonmutating watch,
reference-author isolation, project-memory scoping/restart and layout clamps.
The [native interface report](INTERFACE_QUALIFICATION.md) covers final frozen
clients with and without Dynamic Inventory, including exact imprinting and live
values. Minimal Java8/LWJGL2 checks are not full daily-803 Java17 qualification.

The preceding [cooperative two-player qualification](MULTIPLAYER_QUALIFICATION.md)
covers exact reference writing/handoff, copied STATE independence, pause/reset,
disconnect, missing-bundle retry and clean reloads. It does not establish every
adversarial packet, hardware/IME, third-party provider or chunk-churn workflow.

## Next work, in order

1. Collect manual feedback on this native layout; qualify full-pack GUI scales,
   pointer-held resizing/focus and inventory behavior with and without the
   optional inventory extension. Keep Studio's hidden-slot rejection intact.
2. Extend the dock profiler toward qualified per-trigger/statement observations
   only through explicit opt-in instrumentation. Add scoped local-variable
   inspection/breakpoints only with a defined lifetime and execution model.
3. Continue typed graph fields/resource-label pickers and lossless round trips.
   Unsupported constructs must preserve source and explain Visual availability.
4. Add the live network browser, reusable dependency libraries, NEI recipe-query
   foundations and the remaining provider/recovery/churn qualification.
5. Implement the [overall benchmark](OVERALL_BENCHMARK.md) portable adapter and
   original-port correctness preflight, freeze inputs/counts, then measure. Its
   20-case fixed playlist has no original-relative overall score yet.
6. Use measurements to optimize blocked pair search, preserving due ticks,
   ordering, item/NBT identity, sides/slots, EACH/retention and recovery. Event-
   assisted availability remains a later qualified-adapter experiment; ordinary
   providers keep polling.

See [Studio usage](STUDIO.md), the [SFML guide](SFML_GUIDE.md), and
[program performance evidence](PROGRAM_PERFORMANCE.md). Proposed features here
do not add accepted SFML syntax or alter program execution semantics.
