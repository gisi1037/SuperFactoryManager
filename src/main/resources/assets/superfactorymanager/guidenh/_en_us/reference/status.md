---
navigation:
  title: "Feature status"
  position: 40
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["supported", "planned", "sfml", "nei", "recipe", "nbt"]
categories: ["sfm"]
updated: "2026-10-30"
---

# Feature status

This book targets the current GTNH SFM fork. It describes what you can use now;
the same name in a modern SFM release or another backport may mean something
different.

## Implemented

- SFML item/fluid INPUT/OUTPUT, resource filters, quantities, retention,
  conditions, FORGET and supported round-robin selections.
- Manager timers and pulses, plus the combined native Redstone Port for named
  edge detection, typed reads and signal output.
- Text/Visual MUI2 editing, line numbers, highlighting, undo/redo, snippets,
  diagnostics, logs, PRINT and a bounded inspection terminal.
- LET locals and program-wide memory, disk-persistent STATE, strings, fixed arrays/structs, checked
  integer arithmetic, logical/bitwise expressions and bounded loops.
- Typed value functions, VOID action procedures, explicit REF parameters,
  direct STATE access, math/size defaults and stock helpers.
- Brace or DO/THEN/END blocks, plain or `$` variables, quoted literal labels and
  computed LABEL expressions. Old unquoted literal names must be converted.
- Nonblocking game-tick WAIT, including nested functions, plus optional GregTech
  ghost-circuit configuration through SETCIRCUIT.
- Typed inventory selections, visible counts/emptiness/availability and exact
  grouped item snapshots with physical origins.
- Standalone item TRANSFER and fluid TRANSFERF actions with confirmed progress
  results; separate fcount/fempty/favailable/fcontents observations use mB.
- Private Programming Station drafts with remembered projects, revision validation,
  immutable root-source builds and reference-disk imprinting.
- A normal Manager inventory with inline diagnostics, live variable watches and
  an opt-in full-update profiler; Studio settings, GUI scale and draft recovery.

## Boundaries to keep in mind

The Visual editor represents a supported subset and rejects advanced programs
it cannot preserve. Text exposes the fuller current language. Arrays have fixed
size; snapshots are separate read-only runtime-length sequences. Current player
functions calculate/query data and cannot perform transfer or redstone effects.

Typed item queries and TRANSFER initially cover direct inventories and canonical
vanilla chest pairs. Proxy inventories, unusual machines and covers need
qualified adapters. A machine appearing in NEI does not establish adapter support.
No general EU/RF/energy-transfer interface is provided by these item/fluid examples.

Exact item keys preserve registry ID, metadata and NBT, including the distinction
between no tag and an empty compound. There is no player-facing general item-NBT
inspector or constructor for arbitrary exact ITEM_KEY values yet. A user struct
with `id` and `amount` fields is data, not a native ItemStack or transfer key.

## Publishing and operational views

The station publishes immutable root-only or library-linked bundles from exact
saved revisions. [Reusable functions](/advanced/libraries.md) covers exact owned
library revisions; the build embeds its complete leaf-library source closure.
Imprint disk is available in the normal station UI, with exact-publication and fresh-medium server checks. Ordinary source disks remain supported. The manager
Overview/Expand chart shows measured full-update cost and scheduler state without
exporting private source. Statement tracing, node highlighting and breakpoints
remain separate work.

## Planned, not callable today

- Cross-player library sharing, nested library imports and module constants.
- Server recipe candidates/requirements, machine recipe-map resolution and
  optional NEI selection tools. GuideNH displaying a crafting recipe is separate
  from an SFML recipe-query API.
- Rich NBT inspection and more collection/string helpers.
- A connected-inventory browser and cable-based network label editing.
- Runtime-adjustable scheduling and event-driven destination availability.

Use [the default API](functions.md) for current callable names. If a proposed
method is absent there and the compiler rejects it, adding a struct member with
that name does not implement the operation.

Return to [the book home](/index.md).
