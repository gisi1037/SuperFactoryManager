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

## Next integration steps

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

## Boundaries

The Programming Station is a full-screen authoring surface. Split player-inventory
panels belong to the separate Factory Manager interface. MUI2 supplies layout and
input infrastructure; completion, diagnostics, graph fidelity, and safe server
operations remain SFM responsibilities.

See the [SFML guide](SFML_GUIDE.md) for executable language behavior. Planned
features in this roadmap do not introduce accepted syntax or a new interpreter.
