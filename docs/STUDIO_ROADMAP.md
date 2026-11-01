# SFM Studio roadmap

SFM Studio is the full-screen authoring interface for Programming Station projects.
This is an experimental development snapshot; unsupported Visual constructs must
preserve their source and explain why they require Code.

## Available now

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
