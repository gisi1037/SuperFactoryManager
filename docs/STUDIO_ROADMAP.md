# SFM Studio roadmap

SFM Studio is the full-screen authoring interface for Programming Station projects.
This is an experimental development snapshot; unsupported Visual constructs must
preserve their source and explain why they require Code.

## Available now

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
