# SFM and SFML guide for the GTNH port

The in-game GuideNH book is maintained in
[the guide assets](../src/main/resources/assets/superfactorymanager/guidenh/_en_us/index.md).
See [GuideNH maintenance and installation](GUIDENH_GUIDE.md) for its format,
validation and content-only updates.

Reviewed on 2026-10-30 for this development fork: Minecraft 1.7.10 / Forge
10.13.4.1614, with GTNH `daily-2026-10-27+803` as the pinned integration target.
Implemented language behavior and real-pack qualification are separate. This
guide does not claim complete modern SFM or RetroSFM compatibility.

Java executes SFML on the server. Existing execution disks carry their source;
the new Programming Station stores editable project drafts separately in the
world save. It saves drafts, publishes immutable builds and imprints reference
disks that managers can run. The RetroSFM visual editor produces the same SFML and uses the
same engine. Locals, disk-persistent STATE, fixed arrays/structs, strings, checked
arithmetic, queries, bounded loops and typed player functions are implemented in
this development fork. Scoped GUI checks are documented in
INTERFACE_QUALIFICATION.md. An already-running older client does not gain new
features automatically.

## Contents

- [First working transfer](#first-working-transfer)
- [Disks, labels and cables](#disks-labels-and-cables)
- [Choosing an editor](#choosing-an-editor)
- [Programming Station projects](#programming-station-projects)
- [Program structure and scheduling](#program-structure-and-scheduling)
- [Data: locals, persistent state and expressions](#data-locals-persistent-state-and-expressions)
- [Player-written functions](#player-written-functions)
- [INPUT and OUTPUT](#input-and-output)
- [Typed item queries](#typed-item-queries)
- [PRINT and the debug terminal](#print-and-the-debug-terminal)
- [Resource selectors](#resource-selectors)
- [Quantities, EACH and RETAIN](#quantities-each-and-retain)
- [Sides, slots and round robin](#sides-slots-and-round-robin)
- [Conditions and branches](#conditions-and-branches)
- [Redstone](#redstone)
- [FORGET](#forget)
- [Logs, diagnosis and recovery](#logs-diagnosis-and-recovery)
- [Feature status](#feature-status)
- [Maintaining this guide](#maintaining-this-guide)

Every fenced `sfml` block is a complete program for the candidate compiler.
Fenced `text` blocks contain fragments or commands, not complete programs.
Assign example labels to actual blocks in your own world.

## First working transfer

Start with two ordinary single chests, a Super Factory Manager, a disk, a label
gun and supported cable connecting both chests to the manager. Use the pack's
item browser for available items and recipes; no particular progression recipe
is assumed here.

1. Put a small, countable stack in the source chest. Leave the destination empty.
2. Insert the disk into the manager.
3. Right-click the label gun in air to open its menu. Set its active label to
   `source`, then right-click the source chest to toggle the label onto it.
4. Set the active label to `dest`, then right-click the destination chest.
5. Right-click the manager with the gun, without sneaking, to push the gun's
   label table onto its disk. Read the chat confirmation.
6. Open the manager and choose **Text**. Save this program:

```sfml
NAME "First transfer"
EVERY 20 TICKS DO
  INPUT FROM source
  OUTPUT RETAIN 1 TO dest
END
```

7. Enable the manager's running state if paused. After a due execution, the
   destination should hold one item and the source the remainder. Count both.

`RETAIN 1` on an output is a stock cap. Further executions do not add more while
the destination holds one matching item. Removing it permits replacement on a
later execution. Change the output to `OUTPUT TO dest` to move everything the
destination can accept.

This pattern has scoped vanilla-chest evidence in the real pack. It does not
qualify every machine, cover, tank or unusual inventory.

## Disks, labels and cables

A disk holds source text and a label-to-position table. Labels name world
positions, not items or network addresses. One label can contain several blocks;
a block can have several labels. Use the same spelling and case in the gun and
source. Keyword case-insensitivity does not make labels case-insensitive.

Quote labels with spaces, punctuation or words that collide with keywords:

```sfml
NAME "Quoted labels"
EVERY 20 TICKS DO
  INPUT FROM "Input chest"
  OUTPUT TO "Finished goods"
END
```

| Label-gun action | Effect |
| --- | --- |
| Right-click air | Opens the menu. |
| Right-click a normal block | Toggles the active label on the position. |
| Sneak-right-click a labelled normal block | Picks a label if another is active; clears the block's gun labels if the selected one is already active. Read chat before repeating. |
| Right-click a manager | Pushes the gun's table to its disk. |
| Sneak-right-click a manager | Pulls the disk's table into the gun. |
| Sneak + mouse wheel | Cycles active label. |

Push/pull copies a table. Keep a deliberate master copy; an unrelated gun can
overwrite useful disk labels. Editing the gun after syncing does not update the
disk automatically. SFML `FORGET` never deletes these stored labels.

A labelled position must also be reachable through the manager's cable network
for ordinary IO and HAS observations. Labelling a distant disconnected chest does
not connect it. Cables do not override machine access rules. The manager uses
loaded-world access and is not a chunk loader.

Use **Rebuild cable network** when a connection is not visible. The ordinary
periodic discovery refresh has a maximum 21-tick freshness window, phased across
managers. This is maintenance timing, not a program's EVERY interval. Explicit
invalidation can refresh earlier. Repeated changes can still incur costly rebuilds.

Positions can remain labelled after block replacement. Check what now occupies
a labelled position before restarting automation.

## Choosing an editor

### Text

Choose **Text** for direct SFML editing. This is the most complete front end for
the current language, including ELSE IF chains and programs outside the visual
importer's subset. Manager clipboard paste is another source-entry route.

Keep a manager paused while changing production routing. Check saved source,
labels and the first transfer before leaving it running. Saving source and
choosing the running state are separate concerns.

### Visual Studio Code

The [SFML extension](../editors/vscode-sfml/README.md) provides `.sfml` syntax
highlighting, `--` comment toggling, bracket/quote pairing, block indentation and
folding, and 35 snippets for the current language. It supports lowercase or
uppercase keywords, program-wide LET, direct assignment, loops/functions and
typed queries/transfers. Install its packaged VSIX, then open an `.sfml` file.
Function hovers describe built-ins/methods and same-file player signatures.
Dedicated variable colors can be customized by theme. With the prepared local
backend, the extension reports actual SFML compiler diagnostics in the Problems
panel. Run `prepareSfmlCompiler` after compiler changes and use **SFML: Restart
Compiler**. This does not inspect live world labels or execute programs; runtime
debugging and transfer testing still belong to the in-game tools.

### First RetroSFM visual slice

The current candidate uses MUI2 for the manager, Text, Visual, label gun and logs.
Editors open as secondary panels inside the manager's existing container. Text
offers line numbers, syntax colors, wrapping, scrollbars, snippets, indent and
undo/redo. **Save** keeps editing; **Save & Close** waits for a server reply before
returning. Both editors keep drafts after a rejected or unconfirmed save.

Client-session draft recovery normally admits eight manager positions per world;
it is not a file backup and does not survive closing the game. Text and Visual
drafts are kept separately. A recovered Visual draft for a changed disk is
read-only and can be copied for reconciliation. **Cancel** leaves the installed
program unchanged and retains a recovery draft for reopening during this session.

With the inventory mod installed, the manager binds the actual expanded player
inventory and shows its storage through a nine-column, three-row scrolling
viewport plus hotbar. GUI scale and combined inventory interactions still require
in-game qualification for this candidate. Legacy screen source remains in the
development tree during that transition; normal manager and label-gun entry
points use MUI2.

Choose **Visual** for the candidate node editor. The palette contains Timer,
Pulse, Input, Output, If, Forget and Signal (redstone output). IO nodes hold an
ordinary SFML clause; specialized forms for every modifier are not present yet.

- Select a node, edit the bottom field, then use **Enter** or **Apply**.
- Trigger fields hold a header such as `EVERY 20 TICKS`, without DO/END.
- An If field holds a condition such as `source HAS >= 1`, without IF/THEN/END.
- Other fields hold complete statements such as `OUTPUT 2 TO dest`.
- Click an output pin then an input pin to wire. Each has one connection; a valid
  replacement rewires it. Cycles are rejected.
- If nodes have TRUE, FALSE and AFTER outputs. AFTER runs after the chosen branch.
- Drag headers to move nodes, wheel to zoom and right-drag empty canvas to pan.
  Right-click a pin to disconnect. Delete removes selection when the field is
  unfocused.
- **Preview** shows SFML. **Copy** exports SFML without layout metadata. **Undo**
  keeps bounded history. **Name** edits the header. **Cancel** leaves the installed program unchanged.

Every node must connect to a trigger before save. Triggers cannot have incoming
wires. Unconnected nodes cause errors instead of being silently ignored.

Import compares grammar tokens and refuses unsupported or lossy programs.
Direct ELSE IF import currently belongs in Text. Programs using LET, SET, STATE
or data expressions also belong in Text: Visual refuses them intact rather than
dropping their declarations or instructions. Moving nodes or saving unchanged
clauses retains source formatting and comment placement; a separator newline is
added if needed. Changing a graph clause regenerates SFML, collecting comments
above the generated program with their text retained. A refusal leaves original
disk source available through Text.

Layout is a versioned comment beginning `-- sfm:blueprint:v2 `; existing v1
layouts remain readable. This is a port-specific format, not modern RetroSFM
serialization. The server still executes ordinary SFML. Text carries the old
layout forward as a matching candidate. Reopening Visual uses the current text
for every instruction and branch, then restores positions/IDs where matches are
clear. Number and item-ID changes usually keep positions. Ambiguous matches get
fresh IDs and positions; major restructuring may reset more of the layout.
Malformed metadata falls back to current text, never old instructions. If the
sidecar would exceed the source save limit, Text can save without it and reports
that layout was reset.

Comment/spacing/layout-only saves retain the running compiled program and its
round-robin cursors. The server still validates the complete disk and advances
edit revisions. Executable token changes recompile; STATE reconciliation and
paused/running state continue to follow the guarded save rules.

Text and Visual saves use a document, session and source-revision guard. If the disk changed since opening,
the server refuses the stale save. Copy the draft, reopen the current disk and
reconcile changes. Save waits for an explicit server acknowledgment. A timeout retains the
draft; check disk and chat rather than assuming success or failure.

Current visual limits: 256 nodes, 4,096 characters per clause, nesting depth 32
and 30,000 UTF-8 bytes for source plus layout. Expanded layout also has a bound.
Unicode can use multiple bytes per character. These are editor limits, not an
engine scalability guarantee. Graph concepts derive from Saereth's MIT-licensed
RetroSFM commit `b6776c456e87ace2ada5e5464fea9e4367d74bad`. Its notice is preserved
at `src/main/resources/META-INF/licenses/RetroSFM-MIT.txt`.

## Programming Station projects

The **Programming Station** opens full-screen Studio. Use its **Projects** column
or top selector to browse projects, and **+** or a template to start a private
draft. Studio has Text/Visual/Split editing, highlighting, line numbers, undo/redo,
indentation and wrapping. Inventory slots and NEI are unavailable while authoring.
**Settings** includes the tool mode and independent Studio GUI scale. The block
has a dark industrial texture with red accents. A shaped recipe uses iron,
redstone, books and a crafting table. See [Studio](STUDIO.md) for current controls.

**Save** accepts invalid SFML so unfinished work survives closing the game.
**Check** checks the saved project revision, never executes it or changes a
manager/disk. Save changed source/name before validating; the result identifies
the revision checked. A stale edit is rejected rather than overwriting another
save. Copy and retained local recovery drafts preserve text after a failed or
unconfirmed request within the current client/world session. Local recovery is
RAM-only; save or copy important text before leaving the world. A timeout does
not cancel a save that may already have committed; reopen/reconcile the saved
revision before writing again.

Projects are private to the authenticated player's UUID. Knowing a project ID,
copying station NBT or using another player's station does not grant source
access. Another station can browse your projects after the original is broken.
Station NBT stores only the last project identifier, not source. Files live under:

```text
<world>/sfm/projects/<owner-uuid>/<project-uuid>/draft.bin
```

Include `sfm/projects` in world backups. The versioned file contains owner, ID,
revision, name and source in one bounded atomic replacement. File data is forced
before replacement; world/chunk saves and directory metadata are not one
power-loss transaction. Unknown or malformed formats are preserved and rejected.
File reads/writes and validation run on a bounded worker; completions return on
the server thread. Removing a station suppresses delivery to its stale screen,
but an already admitted save may still finish.

Provisional authoring bounds are 30,000 UTF-8 source bytes per project, a 96-byte
name, 128 projects per owner, 32 catalog entries per page and 32 outstanding
storage jobs/results. These are resource bounds, not measured performance claims.
This first station slice does **not** publish runnable bundles, write execution
disks, import reusable libraries or install code in a controller. Existing disk
editing remains available; publication and reference-only disks are the next
separate workflow.

## Program structure and scheduling

A program has an optional NAME header followed by triggers. Each uses DO and END.
Whitespace and indentation aid reading but do not delimit statements. Keywords
are case-insensitive. `--` comments continue to the end of the line.

```sfml
NAME "Two rates"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:cobblestone FROM source
  OUTPUT TO dest
END
EVERY 5 SECONDS DO
  IF reserve HAS < 16 item:minecraft:iron_ingot THEN
    INPUT 4 item:minecraft:iron_ingot FROM supply
    OUTPUT RETAIN 16 item:minecraft:iron_ingot TO reserve
  END
END
```

A second means 20 game ticks, not a wall-clock promise during lag. Specify a
positive integer interval. Zero, out-of-range values and seconds conversion
overflow are rejected.

Ordinary timers use the manager's saved running-tick counter. It advances while
a valid running program is processed; paused, invalid and recovery-blocked time
is not running time. GLOBAL timers use world time instead.

```sfml
NAME "Global phase"
EVERY 20 GLOBAL PLUS 2 TICKS DO
  INPUT 1 FROM source
  OUTPUT TO dest
END
```

This is due when world time minus 2 is nonnegative and divisible by 20. PLUS uses
the interval's units, so SECONDS multiplies the offset by 20 too. `20G` is an
alternate global spelling. Follow the shown header order; diagnostic
pretty-print text is not always pasteable source.

Triggers and statements run in source order. Each trigger has its own active
INPUT list and quota lifetime. Unloading does not queue all missed executions
for catch-up. Wider pulse/restart behavior remains a qualification item. There
is no current runtime statement to change interval or sleep a program.

## Data: locals, persistent state and expressions

This is the candidate data-language milestone. It keeps the existing transfer
engine and adds pure computations that supply quantities and conditions. Use
**Text** for these programs; the first Visual slice refuses data programs intact.
Compiler/headless acceptance is separate from full-pack and multiplayer evidence.

### LET declares; direct assignment updates

LET declares and initializes a mutable binding. `$name = value` changes a visible
existing binding; it does not create one. `SET $name = value` remains accepted.
The same direct syntax updates fields/indices, such as `$jobs[0].amount = 8`.
All variable references have a `$`.
Variable and field names are case-sensitive, start with an ASCII letter or
underscore, and continue with letters, digits or underscores. `$Batch` and `$batch`
are different. Keywords remain case-insensitive.

```sfml
NAME "Computed local batch"
EVERY 20 TICKS DO
  LET $batch = 8
  SET $batch = $batch * 2 + 4
  IF ($batch > 0) THEN
    INPUT ($batch) item:minecraft:iron_ingot FROM source
    OUTPUT TO dest
  END
END
```

This computes twenty before registering an INPUT. Newlines do not terminate
statements; the grammar does. There is no implicit label-to-variable conversion.
Bare resource names and old labels such as LET, SET or STATE retain their
legacy identifier meaning outside data-statement contexts.

Locals are fresh for each trigger invocation. A LET becomes visible after its
initializer and lasts to the end of its enclosing DO, THEN or ELSE block. An inner
block can SET a visible outer binding. A branch-local declaration does not escape
that branch. Self-reference, use before declaration, unknown variables and
shadowing a visible name are compile errors. Separate triggers and sibling
branches can reuse a local name. FORGET affects IO inputs, not variables.

### Program-wide LET

Place LET after NAME and before functions/triggers to share a value between all
triggers in one manager. It initializes once on the first active execution,
before testing which triggers are due. Later SET operations update that value.

```sfml
name "In-memory invocation counter"
let $runs = 0
let $source = label("chest_in")
every 20 ticks do
  set $runs = $runs + 1
  print("runs", $runs)
end
```

Program-wide LET stays in RAM. Pause/resume and formatting/comment-only saves
preserve it. Executable edits, recompile, disk replacement/removal, chunk unload,
world reload and a successful State reset discard it. Use STATE when a value
must survive reloads or travel with a disk. LET does not write values into source
files, the execution disk or controller NBT.

Initializers may use earlier program-wide LET bindings, STATE, pure functions
and bounded queries. Self/forward global references and duplicate/shadowed names
are rejected. Queries run at activation, not while compiling/saving. All global
initializers publish together; a fault pauses execution without partial global
values, and resume retries initialization. Functions still receive globals and
STATE through explicit parameters rather than capturing them.

Globals may retain label/selector handles and immutable exact item keys/contents
snapshots. They do not keep a live inventory or automatically refresh a snapshot.
The per-manager store allows 256 bindings, 4,096 retained value cells, 16,384
characters and 256 KiB of retained opaque key/handle data, with conservative NBT
array-capacity accounting. Assignments check bounds before replacing a value.
CONST remains planned.

### Fixed types, arrays and structs

Scalar data types are signed 64-bit integers, booleans and immutable strings.
Local typed label references and item selectors are also available; see typed item queries below.
There are no floating-point numbers or automatic truthiness/conversions.
Existing quoted program names, labels and selectors retain their own meanings.

Types and collection shape are inferred at compile time and cannot change:

| Value | Rule |
| --- | --- |
| `8` or `-2` | Signed integer. |
| `TRUE` / `FALSE` | Boolean, separate from integer. |
| `"minecraft:iron_ingot"` or `""` | String; its length can change without changing its type. |
| `[1, 2, 4]` | Fixed-length, zero-based array; elements have one identical type/shape. |
| `{target: 64, enabled: TRUE}` | Struct with fixed named fields and field types. |
| `$jobs[0].amount` | Nested array/field access. |

A field's spelling and case matter; field source order does not define the type.
Duplicate fields and unknown field names fail compilation. Different array
lengths are different types. `[1, TRUE]`, assigning a boolean to an integer, or
assigning a three-element array to a two-element binding fails. Empty arrays have
no inferable element type and are rejected. Empty structs are allowed. One
trailing comma is allowed in literals. Indexes must be integers within range;
negative/out-of-range indexes fault instead of clamping, wrapping or growing.

Collections have value semantics:

```sfml
NAME "Independent collection values"
STATE $last = { original: 0, updated: 0 }
EVERY 20 TICKS DO
  LET $jobs = [{ amount: 4, enabled: TRUE }, { amount: 8, enabled: FALSE }]
  LET $edited = $jobs
  SET $edited[0].amount = $jobs[1].amount * 3 + 2
  SET $last.original = $jobs[0].amount
  SET $last.updated = $edited[0].amount
  IF ($edited[0].enabled) THEN
    INPUT ($edited[0].amount) item:minecraft:iron_ingot FROM source
    OUTPUT TO dest
  END
END
```

`$last.original` becomes four and `$last.updated` becomes twenty-six. Editing
`$edited` does not change `$jobs`. Immutable storage may be shared internally;
path updates copy just the containing path. Whole-value assignment does not
perform a recursive copy. There are no mutable aliases or cycles.

For a path assignment, the runtime captures the old root, evaluates indexes in
path order, validates the path, evaluates the RHS, checks budgets, then publishes
one replacement root. A failed assignment changes nothing in that binding.
Assignments and transfers that completed before a later fault remain completed;
the whole trigger is not a rollback transaction.

### Strings and item-stack records

Strings support `+` concatenation with another string and `=` / `EQ` equality.
Use `$a != $b` or `NOT ($a = $b)` for inequality. No automatic integer-to-string conversion,
string ordering, interpolation or string methods are implemented.

Data-string escapes are `\"`, `\\`, `\n`, `\r`, `\t`, `\b`, `\f`, and `\uXXXX`.
Raw Unicode and multiline strings are accepted; unknown escapes and unpaired
UTF-16 surrogates are errors. A string is limited to 4,096 UTF-16 code units.
Constants, newly allocated runtime strings and combined persistent strings have
separate 16,384-code-unit budgets. Comparisons and concatenation charge work for
the characters involved. Persistent strings also obey the complete disk NBT byte
limit; a growing SET that would exceed it fails before changing the value.

```sfml
NAME "String values"
STATE $message = ""
EVERY 20 TICKS DO
  LET $prefix = "Iron"
  SET $message = $prefix + " ready"
  IF ($message = "Iron ready") THEN
    INPUT 8 item:minecraft:iron_ingot FROM source
    OUTPUT TO dest
  END
END
```

Item-stack data can use ordinary structs. Read members with dot notation, assign
with SET, and combine zero-based indexing with member access for arrays:

```sfml
NAME "Item stack records"
STATE $lastItem = ""
EVERY 20 TICKS DO
  LET $stack = { item: "minecraft:iron_ingot", count: 8, metadata: 0 }
  LET $stacks = [$stack, { item: "minecraft:gold_ingot", count: 4, metadata: 0 }]
  SET $stack.count = $stack.count + 1
  SET $stacks[0].count = 16
  SET $lastItem = $stacks[0].item
  IF ($stack.item = "minecraft:iron_ingot") THEN
    INPUT ($stack.count) item:minecraft:iron_ingot FROM source
    OUTPUT TO dest
  END
END
```

Here `$stack.count` becomes nine; `$stacks[0].count` becomes sixteen without
changing `$stack`. Nested structs use the same notation, such as
`$stack.details.damage` when that field exists. Names are case-sensitive, fields
and field types are fixed, and unknown fields fail compilation. Field names are
resolved to offsets when compiling, rather than looked up by string each tick.

These records are ordinary values. Creating or editing one does not read an
inventory, create a Minecraft ItemStack, or move items. The planned container-
contents query and full metadata/NBT representation are separate work. A string
field does not yet substitute for a resource selector or label in an IO statement.

### Arithmetic and conditions

Write multiplication with whitespace on **both** sides: `$a * $b`, `2 * 3`.
`2*3` and `$a*$b` are rejected. This preserves existing resource wildcard tokens
such as `a*b` and `*3`. `--` still starts a comment; write subtraction of a
negative value as `$a - (-2)`.

Precedence, highest first:

1. Field/index access and parenthesized expressions.
2. Unary `+` / `-` / `~`.
3. `*`, `/`, `%`.
4. `+` / `-` (`PLUS` is another plus spelling).
5. Shifts `<<`, `>>`, `>>>`.
6. Bitwise `&`.
7. Bitwise `^`.
8. Bitwise `|`.
9. Comparisons `>`, `<`, `=`, `==`, `!=`, `>=`, `<=`, or GT/LT/EQ/GE/LE.
10. NOT / `!`.
11. AND / `&&`.
12. OR / `||`.

Binary arithmetic, shifts and bitwise operations are left-associative. Comparisons do not chain: use
`$a < $b AND $b < $c`. Equality accepts same-type scalars only; collection equality
is not supported. AND/OR require booleans and short-circuit left to right.
The symbolic logic forms preserve the word forms' precedence. Write
`!($a == $b)` to make grouping explicit. `=` remains valid equality in a
condition; declarations and SET assignments require a single `=`.
Expressions cannot embed HAS; combine a pure expression with an ordinary
HAS/REDSTONE condition in the outer boolean grammar. The separate typed item
query methods below can read live inventory data inside expressions.

Addition, subtraction, multiplication, negation and division are checked for
signed 64-bit overflow. Division truncates toward zero; remainder has the
dividend's sign. Division/remainder by zero fault. The minimum literal
`-9223372036854775808` is valid; dividing it by -1 faults, while remainder by -1
is zero. Arithmetic does not saturate or wrap. For example, `-7 / 3` is -2 and
`-7 % 3` is -1.

Use parentheses for pure expression IF conditions, including a lone boolean:
`IF ($ready) THEN`. `IF (FALSE AND 1 / 0 > 0) THEN` does not divide by zero.
Unreachable code must still have valid names and types. Runtime arithmetic and
budget faults pause the manager and stop later statements/triggers for that
invocation; earlier successful state/world effects remain.

### Bitwise operations

Bitwise operators work on signed **64-bit integers**. They do not accept booleans,
strings or collections, and do not convert types implicitly.

| Operator | Meaning | Example |
| --- | --- | --- |
| `&` | Bitwise AND | `6 & 3` is `2` |
| `|` | Bitwise OR | `6 | 3` is `7` |
| `^` | Bitwise XOR | `6 ^ 3` is `5` |
| `~` | Invert every bit | `~0` is `-1` |
| `<<` | Shift left, filling low bits with zero | `3 << 2` is `12` |
| `>>` | Shift right, copying the sign bit | `-8 >> 2` is `-2` |
| `>>>` | Shift right, filling high bits with zero | `-1 >>> 1` is `9223372036854775807` |

Shift distances must be **0 through 63**. Negative distances and distances of
64 or more fault; they are never silently masked. Shifts discard bits that leave
the 64-bit value, so `1 << 63` is `-9223372036854775808` and `1 << 63 << 1` is
zero. This is intentional bit manipulation and differs from checked arithmetic.
Bitwise binary operators evaluate both operands, left to right; use boolean
AND/OR when you need short-circuit logic.

```sfml
NAME "Flags"
STATE $flags = 0
EVERY 20 TICKS DO
  LET $ready = 1 << 2
  SET $flags = $flags | $ready
  IF ($flags & $ready = $ready) THEN
    SET $flags = $flags & ~$ready
  END
END
```

This sets bit 2, tests it, then clears it. Comparisons bind **after** bitwise
operators: `$flags & $ready = $ready` means `($flags & $ready) = $ready`.
Existing AND/OR/NOT boolean rules are unchanged. Bitwise expressions are Text-only
until corresponding Visual nodes exist.

### FOR and WHILE loops

Counted FOR uses inclusive integer bounds. Start, end and STEP evaluate once,
left to right. STEP defaults to `1`; use a negative step to count down. Zero STEP
faults before the body; a range pointing the other way is empty. Signed-64-bit
endpoints are supported without an overflow after the last valid iteration.

Array FOR evaluates its array once and visits an immutable snapshot in index
order. Reassigning the source array during the loop does not change traversal.
The iteration variable is local and read-only, including its struct/array members;
copy it to a LET local if you need to edit that value. Existing no-shadowing rules
apply, and the iteration variable is unavailable after END.

```sfml
NAME "Loop examples"
STATE $total = 0
EVERY 20 TICKS DO
  LET $items = [{count: 2}, {count: 5}]
  FOR $item IN $items DO
    SET $total = $total + $item.count
  END
  FOR $i = 1 TO 3 DO
    PRINT("index", $i)
  END
  FOR $i = 3 TO 1 STEP -1 DO
    PRINT("countdown", $i)
  END
  LET $attempt = 0
  WHILE ($attempt < 3) DO
    SET $attempt = $attempt + 1
  END
END
```

WHILE uses the same condition syntax as IF and reevaluates before every body
execution. Parenthesize typed expressions; existing HAS/REDSTONE conditions also
work. Body LET initializers execute again each iteration. Outer locals and STATE
remain assignable. BREAK/CONTINUE and brace syntax are not implemented.

All nested loops and due triggers share **1,024 loop steps per invocation**.
A FOR body entry consumes one step. Every WHILE condition test consumes one,
including the final FALSE test, before the condition observes the world. Thus a
WHILE that performs 1,023 bodies can also finish its final test; 1,024 bodies leave
no allowance for that final test. Loop nesting is limited to 16. Expression/query
budgets remain shared; every statement executed inside a loop also consumes an
expression-work unit, including legacy statements. PRINT retains its separate
16-line allowance.

INPUT/OUTPUT are supported in loops. Every executed INPUT registers independent
captured quotas, accumulating until FORGET or trigger end. An INPUT outside the
loop retains its single allowance across repeated OUTPUTs. Loop INPUT executions
have an additional shared cumulative limit of 256 per invocation; FORGET does not
reset it. Label round-robin cursors retain their existing lazy-discovery ordering.

Budget faults stop the invocation, retaining prior STATE writes and completed
transfers. Loops never yield between extraction and insertion/recovery. These
limits prevent endless loops; they do not guarantee a fixed tick time for a large
legacy inventory scan or slow third-party callback. Loops currently require Text.
Array iteration does not add dynamic label/resource substitution to legacy I/O;
typed handles can be used with the query methods below.

### Player-written functions

Define functions after top-level STATE/LET declarations and before triggers.
Parameters and return values have explicit types; LET locals inside a function
still infer their types. Calls use ordinary names or namespaced names such as
`math.sum(...)`. Function names and variables are case-sensitive; keywords and
type names are case-insensitive. Forward calls are allowed.

```sfml
NAME "Typed player functions"
STATE $total = 0

FUNCTION math.sum($values: INTEGER[3]) RETURNS INTEGER DO
  LET $sum = 0
  FOR $value IN $values DO
    SET $sum = $sum + $value
  END
  RETURN $sum
END

FUNCTION next($previous: INTEGER) RETURNS INTEGER DO
  RETURN $previous + 1
END

EVERY 20 TICKS DO
  LET $values = [2, 4, 8]
  SET $total = next(math.sum($values))
  PRINT("total", $total)
END
```

This prints15 when capture is enabled. Supported signature types are `INTEGER`,
`BOOLEAN`, `STRING`, `LABEL_REF`, `RESOURCE_SELECTOR`, `ITEM_KEY`,
`ITEM_CONTENTS`, `ITEM_LOCATIONS`, fixed arrays such as
`INTEGER[3]` and structural records such as `{count: INTEGER, name: STRING}`.
Types and collection shapes must match exactly. Functions can also return arrays,
structs and handles. Handles still cannot be stored in persistent STATE.

Parameters are function-local values. SET may update a parameter or local, but
an updated struct/array is an immutable replacement and cannot change the
caller's original. Pass state explicitly and use the returned value in caller
SET. Functions cannot implicitly read caller locals, program-wide LET or STATE, and they cannot
mutate persistent state. There are no closures or library-global variables.

Query helpers take the caller's label/selector handles and observe current stock:

```sfml
NAME "Player query helper"
FUNCTION stock($src: LABEL_REF, $what: RESOURCE_SELECTOR) RETURNS INTEGER DO
  RETURN $src.count($what)
END
EVERY 20 TICKS DO
  LET $src = LABEL("chest_in").slots(0, 8)
  IF ($src.available()) THEN
    LET $amount = stock($src, ITEM("minecraft:iron_ingot"))
    PRINT("iron", $amount)
  END
END
```

Arguments evaluate once, left to right; AND/OR retain short-circuit behavior.
User calls are not constant-folded, so query helpers read fresh values and see
earlier transfers. LABEL literals inside function bodies are included in the
program's referenced-label set. Functions receive no direct world/Java access.

Function bodies support local LET/SET, typed IF/ELSE, bounded FOR/WHILE and
RETURN. Every reachable path must RETURN a value of the declared type. Early
RETURN exits nested loops/branches. Loops conservatively need a subsequent
RETURN because they may run zero times. Code after an unconditional return is
rejected. Direct/indirect recursion, duplicate names, overloading, optional/
variadic parameters, void/procedure calls, imports and extension methods are not
implemented. LABEL/ITEM constructor names and boolean operator spellings are
reserved. RETURN is only valid inside a function.

Functions cannot contain PRINT, INPUT/OUTPUT, FORGET, bare manager REDSTONE
conditions or other actions. Typed native-port observations such as
`$port.redstoneAvailable()`, `$port.redstone()` and `$port.powered()` are allowed.
Put actions in the trigger, using returned values
for conditions or computed transfer quantities. Definitions/calls require Text;
Visual reports their unsupported constructs without altering source.

Limits are 128 definitions, 16 parameters per definition, 256 local bindings per
frame, 256 calls per manager invocation, call depth16 and 4,096 active function
local cells. Calls and all due triggers share expression, allocation, string,
query and loop allowances. Function entry never resets them, and frames unwind
after return/failure. These finite limits do not preempt third-party callbacks
or establish a hard tick-time guarantee.

### Computed quantities and snapshots

Existing literal forms keep their meaning. Wrap new expressions in parentheses
at quantity/threshold sites: INPUT, OUTPUT, RETAIN, HAS and REDSTONE input-level
comparisons. Values must evaluate to nonnegative integers; zero is valid.
An expression is evaluated before the statement's world work, not once per slot
or destination.

```sfml
NAME "Input snapshot"
EVERY 20 TICKS DO
  LET $n = 8
  INPUT ($n) item:minecraft:iron_ingot FROM source
  SET $n = 32
  OUTPUT 3 item:minecraft:iron_ingot TO dest
  OUTPUT ($n) item:minecraft:iron_ingot TO dest
END
```

The INPUT keeps its captured allowance of eight even after `$n` becomes 32. If
the first output moves three, only five remain available to the second. Each
OUTPUT snapshots all its limits before any transfer, so an invalid later limit
cannot be discovered after an earlier limit from that same statement moves items.
An earlier, separate OUTPUT is not undone by that fault.

A fluid example combines computed stock checks and retention:

```sfml
NAME "Computed water buffer"
EVERY 20 TICKS DO
  LET $batch = 1000
  LET $reserve = 2000
  LET $target = 4000
  IF water_source HAS >= ($batch + $reserve) fluid:water THEN
    INPUT ($batch) RETAIN ($reserve) fluid:water FROM water_source
    OUTPUT RETAIN ($target) fluid:water TO water_buffer
  END
END
```

Amounts remain millibuckets and all existing fluid qualification limits still
apply. Expressions do not make a generic fluid adapter more qualified.

Redstone **input conditions** can use a computed threshold:

```sfml
NAME "Computed signal threshold"
EVERY 20 TICKS DO
  LET $threshold = 8
  IF REDSTONE LEVEL >= ($threshold) THEN
    INPUT 4 item:minecraft:cobblestone FROM source
    OUTPUT TO dest
  END
END
```

Redstone OUTPUT also accepts a parenthesized INTEGER expression, evaluated once
before target selection or writes. Dynamic levels must be 0-15; an invalid value
faults before that statement writes. Historic literal levels still clamp to 0-15.
Functions and loops are described above. Dynamic trigger intervals, resource IDs,
growable arrays, recursion and runtime cadence setters remain future work.

### STATE follows the program disk

STATE declarations appear after NAME and before triggers. Their defaults must be
pure constant expressions, with no variable reads or world predicates. A STATE
name is visible to every trigger and cannot be shadowed by LET. State uses the
same fixed types and value semantics as locals.

```sfml
NAME "Persistent invocation counter"
STATE $runs = 0
STATE $config = { batch: 8, reserve: 16 }
EVERY 20 TICKS DO
  SET $runs = $runs + 1
  INPUT ($config.batch) RETAIN ($config.reserve) item:minecraft:iron_ingot FROM source
  OUTPUT TO dest
END
```

`$runs` increments on each due invocation that reaches that SET, including when
nothing transfers. It is not proof of a successful or exactly-once delivery.
Later successful SET operations are immediately visible to later statements and
other due triggers in source order. Each disk/program instance owns its values;
locals never persist.

Accepted state changes stay in validated heap values and mark state dirty.
Ordinary reads/SET operations do not serialize NBT or write the drive. Snapshots
are encoded at save, code/schema activation, disk movement/replacement/export,
destruction, and synchronization/exposure boundaries. GUI slot polling can show
the last snapshot without encoding every tick; an actual pickup/copy/movement
flushes current values first. External inventory access can request a fresh
snapshot. Unchanged stores skip encoding.

State follows the disk when moved between managers, through pause/resume and
ordinary world save/reload. A copied disk starts as an independent snapshot;
later writes do not share mutable state. Labels, graph layout, cable rediscovery
and recovery records are separate. There is no atomic save transaction spanning
STATE and external inventories, so process crashes cannot be promised exactly-once
resource/accounting behavior.

### Source edits, schemas and explicit reset

State identity is its case-sensitive name plus fixed recursive type/shape:

| Accepted source/schema edit | Outcome |
| --- | --- |
| Formatting/comment/layout change | Existing values remain. |
| Change a default with the same name/type | Existing value remains; the new default applies to a new state or explicit reset. |
| Add a declaration | Initialize that name from its default. |
| Remove a declaration | Drop its value when activation succeeds. |
| Rename a declaration | Remove old + add new; initialize the new name from its default, with an activation warning. |
| Change an existing type, array length or struct shape | Pause/reject activation with a reset diagnostic; do not reinterpret old values. |
| Invalid source/corrupt or unsupported state | Preserve evidence and refuse execution; correct the source or explicitly reset variables. |

Removed values must not resurrect if a name is added later. Copy source before
editing important programs. A rejected source can remain visible in the text
editor, but it is not run against an incompatible old state layout.

The manager now exposes **Run/Stop** for explicit scheduling control. It requires
a compiled program and unresolved transfer recovery prevents enabling execution.
Use **State** for the separate confirmed reset of persistent variables. It is not
the general **Reset** button that clears program/labels. Correct source first;
a variable reset initializes current defaults while keeping source, labels,
layout and resource-recovery records. A reset does not resolve item/fluid recovery.
Reset pauses the manager and leaves it paused. Inspect diagnostics and
deliberately choose Run when ready. Never delete state NBT manually as the normal recovery procedure.

### Bounds

These are provisional correctness/allocation guardrails, not server tick-time
budgets. The first exceeded limit wins:

| Limit | Candidate bound |
| --- | --- |
| Source | 32,767 characters, plus applicable editor/wire byte bounds. |
| Local bindings per trigger | 256 lexical declarations. |
| Persistent declarations | 256. |
| Fixed collection width | 256 elements/fields. |
| Data/expression nesting | 32. |
| Persistent expanded value cells | 4,096 total, including container nodes. |
| Constant pool cells | 16,384 per compiled program. |
| Expression work | 16,384 units shared across due triggers in one manager invocation. |
| Constructed/copied collection cells | 16,384 per invocation. |
| State envelope alone | 256 KiB uncompressed, with validated NBT kinds/depth/counts. |
| Whole disk NBT | 30,000 uncompressed bytes, including state, source, labels, layout and recovery. |

The whole-disk bound protects the legacy item-stack transport's signed-short
compressed-NBT length; it normally constrains usable STATE well before 256 KiB.
Long names, labels or source leave less room for data. Shape bounds and encoded
size are validated before publishing replacement state. There is no unbounded
collection growth. Budget exhaustion faults before a failed assignment publishes
its replacement; completed prior statements remain completed.

## INPUT and OUTPUT

INPUT registers source views for later outputs in this trigger. It does not
immediately extract everything into an invisible inventory. OUTPUT attempts
transfers from active inputs, obeying both filters/quotas, capacity and machine
access rules.

INPUT stays active across later outputs until forgotten or the trigger ends.
Successful moves consume its allowance across outputs; OUTPUT does not consume
the declaration itself.

```sfml
NAME "Priority then overflow"
EVERY 20 TICKS DO
  INPUT 16 item:minecraft:iron_ingot FROM supply
  OUTPUT RETAIN 8 item:minecraft:iron_ingot TO priority
  OUTPUT item:minecraft:iron_ingot TO overflow
END
```

The first output fills priority toward eight. The second uses the rest of the
same sixteen-item allowance. If the first moves three, the second can move at
most thirteen more. INPUT quantity is not renewed for every output.

Absent inputs, missing labels, disconnected blocks, full targets and refusing
machines can move zero. Quantities are maxima, not promises of atomic completion;
ordinary transfers can succeed partially.

## Typed item queries

The first typed-method slice adds local `LABEL_REF` and `RESOURCE_SELECTOR`
values. Constructors take one quoted literal; a string variable or concatenated
string is not accepted. Names of constructors/methods ignore case, while label
names and registered item identifiers are compared exactly.

```sfml
NAME "Read before moving"
EVERY 20 TICKS DO
  LET $src = LABEL("chest_in")
  LET $iron = ITEM("minecraft:iron_ingot")
  IF ($src.available($iron) AND $src.count($iron) >= 8) THEN
    INPUT 8 item:minecraft:iron_ingot FROM chest_in
    OUTPUT TO chest_out
  END
END
```

`LABEL("chest_in")` names the group in this invocation's disk labels. It does not
capture a block or inventory, and can refer to multiple blocks. `ITEM("*")`
selects all items. Other item selectors use an exact `namespace:name` and include
all damage/NBT variants of that item. A valid but unregistered identifier matches
nothing. The `item:` prefix, metadata filters, wildcard patterns, NBT filters and
`FLUID(...)` are not supported by ITEM yet. Use `ORE("ingotIron")` for an exact
case-sensitive ore-dictionary membership selector; it has the same RESOURCE_SELECTOR
type and supports Forge wildcard metadata registrations. Unknown names match
nothing and do not create registry entries. ORE takes one literal name of 1..256
ASCII letters, digits or underscores; no wildcard, prefix or computed string.
Legacy transfer/HAS selectors keep their existing behavior.

| Method | Result |
| --- | --- |
| `$src.available($iron)` | Boolean: a nonempty selected group is connected, loaded, supported and has visible permitted slots in every distinct inventory view. Does not test matching stock or insertion/extraction permission. |
| `$src.count($iron)` | Exact signed-64-bit matching visible item total. An incomplete/unavailable group faults instead of returning a partial total or zero. |
| `$src.empty($iron)` | Boolean: the complete available group contains zero matching visible items. Missing or incomplete groups return FALSE. |
| `$src.empty(ITEM("*"))` | Tests all visible items in the group. Does not inspect fluids or hidden slots. |
| `$src.empty()` | Same item-only check as `empty(ITEM("*"))`. |
| `$src.available()` | Same access-only check as `available(ITEM("*"))`. |

Count requires exactly one typed selector argument. Empty and available accept
zero or one; the zero-argument form explicitly means all visible **items**, not
fluids or insertion capacity. Implicit string conversion is not supported.
LABEL/ITEM literals are limited to 256 characters; label names must be nonempty
and have no surrounding whitespace. Handles may live in locals, fixed arrays and
structs. They cannot be stored in STATE, including inside containers. Persistent
query results can be ordinary integers/booleans.

Default item access is the union of valid accessible slots from the six faces of
an `ISidedInventory`, or all ordinary slots of an unsided inventory. Repeated
slot indices and faces count once; invalid indices are ignored. Slots can hold
visible stock even when extraction is refused. Zero-sized or entirely hidden
views are unavailable, not confirmed empty.

Restrict a label's observation with `.side("north")` and `.slots(first, last)`:

```sfml
NAME "Selected input slots"
EVERY 20 TICKS DO
  LET $all = LABEL("chest_in")
  LET $first = 0
  LET $last = 8
  LET $selected = $all.side("north").slots($first, $last)
  IF ($selected.available() AND NOT $selected.empty()) THEN
    LET $iron = $selected.count(ITEM("minecraft:iron_ingot"))
  END
END
```

Side takes a quoted absolute-side literal: down, up, north, south, west or east
(case-insensitive). Bottom/top are aliases for down/up. Relative front/back/left/
right and computed side strings are not supported. Sides restrict the slots an
`ISidedInventory` exposes; an ordinary inventory or vanilla chest is visible
from every physical face. This does not test whether insertion/extraction is
allowed through a face.

Slot bounds are inclusive, zero-based integers: `0 <= first <= last <= 2147483647`.
They may use integer expressions, evaluated receiver, first, then last, once each.
Invalid bounds fault if evaluated. A range with no visible slots is unavailable;
it is not confirmed empty. Chest pairs use one stable combined slot order (the
west/north half first), regardless of which half was labeled. Every modifier
returns a new immutable handle; `$all` above is unchanged. Repeating a modifier
replaces that restriction while retaining the other: the last side/slot call wins.
Observation restrictions do not modify separate legacy INPUT/OUTPUT statements.

Reads resolve current world/provider identity on every call and see earlier
transfers in the same trigger. They do not reserve stock, guarantee a later
transfer or reuse a stale count. `available` is a separate observation, so a later
`count` can still fault if the provider changes. AND/OR retain short-circuiting;
arguments and receiver evaluate once, receiver first. Any runtime fault pauses
the manager; already completed transfers and STATE writes are not rolled back.

The first adapter supports direct IInventory tiles and vanilla chest pairs.
Duplicate direct provider objects and vanilla chest halves are counted once per
query, with stable combined chest-slot ordering. Chest adjacency probes never
load chunks; uncertain unloaded adjacency makes a query unavailable. Steve's
proxy/cluster adapters and distinct proxy tiles sharing hidden backing storage
need separate identity qualification. Do not treat their combined view totals
as a proven physical network total.

All new queries share provisional per-invocation limits across due triggers:
1,024 labeled-position visits, 8,192 provider calls, 16,384 slot-work units and
16,384 collection/dedup entry reservations. Slot-work includes returned sided
indices (even duplicates/invalid entries) and stack reads. Limits are checked
before callbacks, walks and retained-entry allocations. Budget exhaustion,
invalid negative stack sizes and throwing providers fault rather than looking
empty or unavailable. Fixed helper objects and provider-internal allocation are
not byte-accounted; a third-party callback cannot be preempted. Existing topology
refresh work also retains its own lifecycle. These caps are engineering limits,
not measured tick-time guarantees, and do not impose new cutoffs on legacy HAS,
INPUT or OUTPUT.

Query programs currently require Text. Visual refuses the complete unsupported
construct without dropping source. Removing unsupported data expressions can
restore Visual availability. The Labels and Queries palettes include the new snippets,
and constructor/method calls receive action coloring.

## PRINT and the debug terminal

`PRINT(...)` is a diagnostic statement with one to eight expressions. Open the
manager's **Terminal** page and enable capture to see its output. Capture starts
off, stays in that manager's RAM and is not saved to the disk or world NBT.

```sfml
NAME "Debug item counts"
STATE $cycles = 0
EVERY 20 TICKS DO
  SET $cycles = $cycles + 1
  LET $src = LABEL("chest_in").slots(0, 8)
  PRINT("cycle", $cycles, "available", $src.available())
  IF ($src.available()) THEN
    PRINT("visible items", $src.count(ITEM("*")))
  END
END
```

Arguments evaluate once, left to right, only while capture is enabled and this
invocation's print allowance remains. Disabled printing does not evaluate its
arguments, perform their inventory queries or format their values. Names and
types still validate at compile time. Do not put observations you need for program
logic exclusively inside PRINT. PRINT does not return a value and cannot appear
on the right-hand side of LET/SET. Current player-written functions are read-only;
PRINT remains a trigger statement rather than an allowed function effect.

Values are separated by spaces; strings print as text. Arrays/structs and handles
use a bounded readable representation, with truncation for large/deep values.
Output is limited to 16 print statements per invocation, 512 characters per line,
200 retained lines and 16,384 retained characters. Excess output is dropped or old
history evicted, with a visible dropped-line count. Printing is diagnostic work,
not unbounded logging or a persistence mechanism. Runtime faults in evaluated
arguments retain the ordinary program fault behavior.

The terminal supports these commands:

| Command | Behavior |
| --- | --- |
| `help` | Show supported commands. |
| `status` | Inspect the current manager status. |
| `state` | Inspect bounded snapshots of live persistent STATE values. |
| `state $name` | Inspect one persistent variable. |
| `labels` | List the first 15 saved label names and their saved position-entry counts. |
| `labels Input Chest` | Show the first 15 saved positions for exact case-sensitive name `Input Chest`, including spaces. |
| `logs` / `logs 15` | Show the latest 10 engine logs, or a requested count from 1 to 15, oldest first. |
| `capture on` / `capture off` | Enable/disable PRINT capture for this manager. |
| `clear` | Clear PRINT history; engine logs remain unchanged. |

Commands do not execute arbitrary SFML, alter STATE, transfer items or expose Java.
Trigger-local LET values end with their trigger run; use PRINT inside the trigger to inspect
them. STATE inspection reads live heap values without forcing disk serialization.
`labels` reports saved bindings, including stale/disconnected positions; it is
not a connectivity or inventory-content query. Label/position results explicitly
show the displayed count when truncated. Neither it nor `logs` scans world
providers, flushes STATE or changes the log level/capture. Engine logs are those
already retained at the configured log level; the command does not enable tracing.

Use **Up/Down** in the command field to recall up to 32 submitted commands. Down
past the newest restores the unfinished command you were typing. Consecutive
duplicates and background polls do not fill recall history. Recall belongs to
this open panel and is not saved or shared with other players.

Output is requested only while the terminal is visible, through the currently
authorized manager container. Source diagnostics and the full Logs view remain
available in their existing pages. PRINT programs currently require Text.

## Resource selectors

Use explicit item/fluid selectors when a program could reach both handler types.
An omitted filter matches both and is harder to reason about.

| Fragment | Meaning |
| --- | --- |
| `item:minecraft:iron_ingot` | Registered item with this namespace/name. |
| `minecraft:iron_ingot` | Same name without explicit item-only type. |
| `item:*:*` | All items. |
| `fluid:water` | Forge fluid registered as water; amounts are millibuckets. |
| `"item:minecraft:wool:14"` | Metadata 14; numeric metadata needs quotes under this grammar's identifier rules. |
| `"item:some_mod:part_*"` | Glob over registered names; substitute actual IDs. |
| `"#ore:ingotIron"` | Item OreDictionary-name selector. |

Use lower-case type prefixes `item:` and `fluid:`. Name/namespace matching uses
glob `*` and `?`, treating ordinary characters literally. It is not arbitrary
Java regex. Quote punctuation that an unquoted identifier does not admit. Use
registered IDs rather than translated display names.

Fluid matching uses the Forge fluid name. Do not infer modern namespaced-fluid
or fluid-NBT behavior from how many colons parse. Disjoint tanks exposing the
same fluid and NBT-bearing fluid variants need adapter qualification.

OR joins alternatives within one limit. Commas separate limits with independent
allowances. EXCEPT follows limits and precedes FROM or TO:

```sfml
NAME "Filtered routing"
EVERY 20 TICKS DO
  INPUT 32 item:minecraft:iron_ingot OR item:minecraft:gold_ingot FROM metals
  OUTPUT TO dest
  FORGET
  INPUT 8 item:*:* EXCEPT item:minecraft:cobblestone FROM miscellaneous
  OUTPUT TO overflow
END
```

The first INPUT permits a combined 32 ingots, not 32 of each. For independent
allowances use comma-separated limits or quantity EACH deliberately.

Single-tag WITH/WITHOUT uses the 1.7.10 OreDictionary, not modern datapack tags
or arbitrary NBT predicates:

```sfml
NAME "Ore dictionary filter"
EVERY 20 TICKS DO
  INPUT 16 item:*:* WITH TAG ingotIron FROM source
  OUTPUT TO dest
END
```

`WITHOUT TAG ingotIron` is the complementary item filter. Compound/parenthesized
WITH expressions are rejected by the AST builder even though grammar recognizes
them. Use separate statements or IF logic. Quoted `"#ore:..."` selectors strip
the optional `ore:` prefix; WITH TAG matches the supplied OreDictionary name
directly.

## Quantities, EACH and RETAIN

The positions of EACH describe different scopes:

| Fragment | Scope |
| --- | --- |
| `INPUT 8 FROM source` | Eight matching units shared across selected source blocks. |
| `INPUT 8 FROM EACH source` | Eight per selected source block. |
| `INPUT 8 EACH FROM source` | Eight per resource identity, shared across source blocks. |
| `OUTPUT 8 TO dest` | Eight total for this OUTPUT. |
| `OUTPUT 8 TO EACH dest` | Eight for each selected destination block. |
| `OUTPUT 8 EACH TO dest` | Eight per resource identity for this OUTPUT. |
| `RETAIN 8 EACH` | Independent reserve/cap per resource identity. |

All quantities are upper bounds. Quantity EACH and statement EACH can combine.
Statement EACH means blocks, not slots or labels. Item quota identity is item
plus metadata; NBT variants do not get independent allowances. That grouping
differs from stricter equality when actually merging stacks.

RETAIN has direction-specific meaning:

- **INPUT** reserves matching stock to leave in source views. INPUT RETAIN 8
  makes only stock above that reserve eligible.
- **OUTPUT** caps matching stock in destination views. OUTPUT RETAIN 8 fills
  toward eight, including existing stock. It does not remove an existing excess.

Without statement EACH, reserves/caps aggregate across selected blocks instead
of guaranteeing stock in each block. Without resource EACH, types matching one
limit share its reserve/cap. Retention is not multiplied by slots or faces.

```sfml
NAME "Reserve supply and stock machines"
EVERY 20 TICKS DO
  INPUT 64 RETAIN 16 item:minecraft:iron_ingot FROM supply
  OUTPUT 8 RETAIN 16 item:minecraft:iron_ingot TO EACH machines
END
```

This leaves a shared sixteen-ingot source reserve, allows at most 64 additional
ingots through this INPUT, and attempts at most eight per destination while
bringing each toward sixteen. Current stock, capacity, refusal and remaining
input allowance can reduce the result.

```sfml
NAME "Per resource supply"
EVERY 20 TICKS DO
  INPUT 8 EACH RETAIN 4 EACH item:minecraft:iron_ingot OR item:minecraft:gold_ingot FROM supply
  OUTPUT 8 EACH TO dest
END
```

This allows eight of each kind while leaving four of each in the source scope.
Two comma-separated limits can also express independent allowances. When
selectors overlap, the first matching limit wins; avoid accidental overlap.

A fluid-only example uses millibuckets:

```sfml
NAME "Water supply"
EVERY 20 TICKS DO
  INPUT 1000 fluid:water FROM water_source
  OUTPUT RETAIN 4000 fluid:water TO water_buffer
END
```

This is implemented generic Forge handler behavior, not qualification of a
particular GT tank or cover. Verify amounts in a disposable fixture.

## Sides, slots and round robin

Qualifiers follow labels in this order: round robin, side selection, slot
selection. A side is the accessed inventory's face, not the manager's facing.

```sfml
NAME "Explicit access views"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM source TOP SIDE SLOTS 0-8
  OUTPUT TO dest BOTTOM SIDE SLOT 1
END
```

Supported SFML spellings: TOP, BOTTOM, NORTH, SOUTH, EAST, WEST and NULL, followed
by SIDE. A list looks like `NORTH, WEST SIDE`. EACH SIDE uses all face views
without multiplying the same physical stock/quotas. An omitted qualifier also
uses default all-face selection. NULL SIDE is unsided/unknown access and does
not guarantee a sided machine allows it.

LEFT, RIGHT, FRONT and BACK are rejected: relative orientation is unimplemented.
Use TOP/BOTTOM in SFML; the recovery command's up/down are not SFML spellings.

Slots are zero-based backend indices, potentially different from GUI ordering.
Ranges are inclusive. Sets look like `SLOTS 0-8, 12, 20-22`. Access still obeys
sided-slot lists and machine rules. Slots do not select individual fluid tanks.
Test reserved/circuit slots and covers explicitly.

TO EMPTY SLOTS IN restricts item insertion to empty slots instead of topping
existing stacks. It is not a container-fullness query:

```sfml
NAME "Only empty item slots"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM source
  OUTPUT TO EMPTY SLOTS IN dest TOP SIDE SLOTS 0-8
END
```

ROUND ROBIN BY BLOCK selects one unique block from the supplied labels whenever
the access resolves. BY LABEL selects one label, which may contain many blocks:

```sfml
NAME "Rotate destination blocks"
EVERY 20 TICKS DO
  INPUT 4 item:minecraft:cobblestone FROM source
  OUTPUT TO machines ROUND ROBIN BY BLOCK
END
```

Rotation advances on resolution, not proof of delivery. A missing, disconnected
or full selected target can receive nothing. It does not search for the next
available target. The compiled expression owns the cursor; it is not a durable
fairness ledger across recompilation/restart. Perfect balancing is not promised.

## Conditions and branches

IF condition THEN statements ELSE statements END selects a branch. ELSE is
optional. Text supports ELSE IF chains; direct ELSE IF import is not yet in the
first Visual slice. Nested ordinary IFs can be represented visually.

```sfml
NAME "Stock-based priority"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM supply
  IF priority HAS < 16 item:minecraft:iron_ingot THEN
    OUTPUT RETAIN 16 item:minecraft:iron_ingot TO priority
  ELSE IF reserve HAS < 32 item:minecraft:iron_ingot THEN
    OUTPUT RETAIN 32 item:minecraft:iron_ingot TO reserve
  ELSE
    OUTPUT TO overflow
  END
END
```

HAS counts stock in connected selected views. Use resources, sides and slots
when you need that narrower observation. Comparisons are `>`, `<`, `=`, `<=`,
`>=`, `==`, `!=` or GT, LT, EQ, LE, GE. Both `=` and `==` test equality.

| Operator before label | Test |
| --- | --- |
| Omitted / OVERALL | Compare the sum across considered blocks. |
| SOME | At least one considered block meets the comparison. |
| EVERY / EACH | Every considered block meets it, with at least one considered. |
| ONE | Exactly one considered block meets it. |
| LONE | At most one meets it, including zero. |

Disconnected positions are skipped. `OVERALL missing HAS = 0` can be true when
no block exists; it does not prove an empty connected inventory. EVERY with no
considered blocks is false. Use item-only/fluid-only selectors to avoid summing
items and millibuckets into an unhelpful number.

Boolean logic supports TRUE, FALSE, NOT/`!`, AND/`&&`, OR/`||` and parentheses. Parenthesize
mixed expressions to clarify grouping:

```sfml
NAME "Gate on supply and redstone"
EVERY 20 TICKS DO
  IF (supply HAS >= 8 item:minecraft:iron_ingot) AND (REDSTONE > 0) THEN
    INPUT 8 item:minecraft:iron_ingot FROM supply
    OUTPUT TO dest
  END
END
```

HAS measures stock, not free space, insertion capacity, a reservation or whether
a machine accepts an item. There is no current FULL/CAN_ACCEPT function, direct
capacity query or dynamic interval function. A condition is an observation, not
a promise that a later transfer completes.

## Redstone

### Level input and pulses

REDSTONE alone means REDSTONE > 0. `REDSTONE LEVEL >= 8` compares observed manager
power. The optional bridge can contribute legacy Steve's Factory Manager
receiver power. This is not arbitrary labelled vanilla-sensor syntax.

```sfml
NAME "One batch per pulse"
EVERY REDSTONE PULSE DO
  INPUT 8 item:minecraft:cobblestone FROM source
  OUTPUT TO dest
END
```

A pulse is detected when observed power rises from zero to positive. Keeping it
high does not pulse every tick. Returning to zero permits another rise. This is
a sampled tick event, not a queue of every physical transition.

Legacy BUD updates can also supply pulses through the bridge. BUD is not a
general native block-update event API; several updates can coalesce. Fast
transitions, pause/resume and unload/reload need broader live qualification.

### Unlock a scheduled transfer with a pulse

Use STATE for a lock that must survive reloads, or a top-level LET for a shared
RAM-only lock. A LET inside a trigger is local to that run. Assignments use a
single `=` with optional SET, and IF needs THEN.

```sfml
NAME "Infusion"
STATE $Lock = FALSE

EVERY REDSTONE PULSE DO
  SET $Lock = FALSE
END

EVERY 20 TICKS DO
  IF (NOT $Lock) THEN
    INPUT FROM chest_in
    OUTPUT TO Ped
    SET $Lock = TRUE
  END
END
```

The initial FALSE permits one attempt before a pulse. Use TRUE initially to wait
for the first pulse on a new disk or after an explicit state reset. Existing saved
state keeps its value when only the default changes. The lock is set after an
attempt even if no items move; this is not a successful-transfer check. Label
names such as Ped are case-sensitive. When both triggers are due in one tick,
source order allows the pulse to unlock before the scheduled attempt.

### One native Redstone Port for input and output

The **Redstone Port** (`superfactorymanager:redstonePort`) both detects and emits
signals. Attach it to an SFM cable or directly to the manager and label it with
the Label Gun. Its six absolute physical faces have independent input readings
and output latches. No separate detector or emitter block is needed. Use north,
south, east, west, up or down; omitting a side selects all six faces.

```sfml
NAME "Copy north input to east output"
EVERY 20 TICKS DO
  LET $port = LABEL("signal").side("north")
  IF ($port.redstoneAvailable()) THEN
    OUTPUT ($port.redstone()) REDSTONE LEVEL TO signal EAST SIDE
  ELSE
    OUTPUT 0 REDSTONE LEVEL TO signal EAST SIDE
  END
END
```

`redstone()` returns an INTEGER maximum over every selected port and face;
`powered()` returns that level > 0. `redstoneAvailable()` requires every selected
port/face to be present, connected and readable. Missing labels, unloaded inputs,
unsupported blocks and incomplete groups are unavailable. Unguarded level/powered
queries then fault rather than inventing a zero. Provider errors and exhausted
budgets also fault; the availability method does not swallow them. Slot selections
are invalid for redstone. The plain `available()` method remains an item query.

These typed observations work inside player-written functions and share the
invocation's query budget. They refresh at their execution point, so an earlier
output can affect a later reading. Input and output may use different faces of
the same port. Strongly powered neighbors can feed an output back into its input
through the circuit, just as with vanilla redstone.

Outputs remain latched until another OUTPUT changes them, including while the
manager is paused or removed. Set level 0 to switch them off. Output latches
survive world reload; input readings and edge histories do not. All selected
faces are updated before neighbor notifications; unchanged writes are silent.
Native write attempts share a 256-per-manager-invocation limit, including loops
and all due triggers. Earlier completed writes remain if a later operation faults.
This is a work bound, not a hard timing guarantee for third-party callbacks.

### Named rising and falling edges

```sfml
NAME "Count separate input transitions"
STATE $rises = 0
STATE $falls = 0
EVERY REDSTONE PULSE FROM signal NORTH SIDE DO
  SET $rises = $rises + 1
END
EVERY REDSTONE FALLING FROM signal NORTH SIDE DO
  SET $falls = $falls + 1
END
```

`RISING` is an alias for `PULSE`. Zero-to-positive is rising; positive-to-zero is
falling. Other level changes are not edges. Counters are maintained by neighbor
notifications, so a short notified pulse can be observed even after it ends.
Each matching trigger runs at most once per manager invocation; several changes
coalesce, and both rise and fall triggers can run for one short pulse. Edge reads
do not consume events for another trigger or manager. One held-high port cannot
hide another port's edge when they share a label.

All named eligibility is captured before any trigger body runs. A body changing
an input can trigger it on the next invocation, while typed queries later in that
body see fresh readings. Idle subscriptions read cached counters rather than
resampling all power providers. Before delivering an edge, every member of its
group is freshly validated. Missing or unavailable members suppress the whole
group and valid members' observed counters still advance. Budgets are shared
with typed queries; an incomplete/failed snapshot runs no trigger bodies.

The first observation, replacement, unload/reload, pause/resume, recovery or
recompile establishes a new baseline without replaying old edges. Native
subscriptions track their scanned network footprint: unrelated block/chunk edits
preserve pending edges, while relevant connection edits, covered chunk gaps,
manager-neighbor notifications and unknown-location edits conservatively rebase.
A disconnect/reconnect between observations also rebases. A network-local edit
can still discard pending edges even if one particular port's path survives.
Periodic refresh alone does not rebase, and ordinary signal changes do not
invalidate the network. Reliable capture depends on provider neighbor notifications;
arbitrary unnotified transitions and edges across connection gaps cannot be reconstructed.

Named triggers require Text until matching visual nodes are added. The visual
editor reports this limitation without rewriting the program. Round robin, slot
selection, relative faces and NULL SIDE are invalid in named triggers. Bare
`REDSTONE` and `EVERY REDSTONE PULSE DO` retain the manager/legacy behavior above;
they do not automatically combine all native ports.

### Output compatibility

```sfml
NAME "Native or legacy emitter example"
EVERY 20 TICKS DO
  OUTPUT 15 REDSTONE LEVEL TO signal
END
```

This writes connected native Redstone Ports, with optional legacy Steve's Factory
Manager emitters as a compatibility fallback. A labelled vanilla block does not
become an emitter. Visual's Signal node supports literal outputs; computed levels
require Text. Typed native input queries do not imply legacy receiver adapters.
Providers must support the guarded IBlockAccess view; providers that cast it to
World are unsupported and reported safely. Loaded checks include nested vanilla
dust reads and SFM's direct notification destinations, without force-loading them.
Downstream vanilla/mod neighbor callbacks run with their ordinary World access;
SFM cannot constrain arbitrary further reads made by another mod's callback.
Syntax does not establish arbitrary GT-cover or full current-pack qualification.

## FORGET

FORGET clears active INPUT declarations in this trigger. FORGET source removes
that label from active accesses while keeping their other labels. It does not
remove stored labels, discard items, clear a disk or reset destinations.

Use it to prevent later outputs from using an earlier independent stage:

```sfml
NAME "Independent iron and gold stages"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM iron_source
  OUTPUT TO iron_dest
  FORGET
  INPUT 4 item:minecraft:gold_ingot FROM gold_source
  OUTPUT TO gold_dest
END
```

A partial multi-label removal:

```sfml
NAME "Remove one source label"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM source_a, source_b
  OUTPUT 2 TO priority
  FORGET source_a
  OUTPUT TO overflow
END
```

The second output uses source_b and the remaining allowance. FORGET does not
renew an existing declaration's quota. A new INPUT gets its own quota. Inputs
clear between triggers; one trigger cannot leave inputs for another later.

## Logs, diagnosis and recovery

### Diagnosis order

1. Check disk presence, compilation, running state and manager status first.
2. Check labels on the disk, not just the gun. Check coordinates and actual
   block identity, particularly after replacing a machine.
3. Check cables and loaded chunks. Rebuild the network when appropriate.
4. Check registered resource ID and metadata. Narrow a disposable fixture to
   one item, source and destination before diagnosing a large factory.
5. Check face, slots, filters, machine mode, covers, capacity and current stock.
   OUTPUT RETAIN may already be satisfied; INPUT RETAIN may reserve everything.
6. Check whether an earlier output spent the shared INPUT allowance. Use FORGET
   only when you intend a new independent source stage.
7. Capture a diagnostic execution and count source/destination changes.

**View logs** opens manager logs. INFO, DEBUG and TRACE reveal increasing detail.
Current code can turn these levels OFF after an execution that did something;
do not assume verbose capture stays enabled. Optional `exportLogsToFile` writes
to `logs/sfm_server_export.log` and defaults false.

Operator performance commands:

```text
/sfmprofile start
/sfmprofile status
/sfmprofile stop
```

The aggregate profiler records complete scheduled manager-update work across
dimensions, including idle paths, in a 600-tick ring. It defaults off, retains
capture after stop and resets on start. It does not measure the whole server
tick or all manual/packet operations. Short captures are not full-pack budgets.
See [PACK_BENCHMARKS.md](PACK_BENCHMARKS.md) and
[DISCOVERY_PERFORMANCE.md](DISCOVERY_PERFORMANCE.md) for measured scope.

### Recovery is an ownership decision

A handler may fail after extraction or accept only part of a transfer. Ordinary
rollback is attempted. When it cannot safely finish, the port can retain a
versioned disk recovery record and pause. Do not delete records or edit NBT just
to make execution resume.

- **pending** is a known disk-owned remainder. An operator may claim it into a
  suitable adjacent inventory.
- **uncertain** means a callback's outcome could not be established. Inspect the
  actual source/destination, reconcile manually, then acknowledge. It is
  evidence, not another stack safe to materialize.
- Unknown/corrupt records stay blocked with evidence preserved.

```text
/sfmrecovery x y z inspect
/sfmrecovery x y z claim <recoveryId> <direction>
/sfmrecovery x y z resolve-present <recoveryId>
```

These require operator permission and a loaded manager in the sender's current
dimension. Use the current record ID from inspect. Direction is north, south,
east, west, up or down, relative to the manager. Claim respects the neighbor's
face/slot rules and can complete partially; the remainder keeps its ID.
Uncertain records cannot be claimed as items. resolve-present creates nothing
and cannot discard a known pending remainder. Recovery leaves the manager
paused; restart deliberately after reconciliation.

Item records preserve metadata/NBT and a bounded inactive last-resolution
history. Reset and disk removal retain pending ownership. This command handles
items. Fluid evidence uses the separate operator commands below, after manually
reconciling actual tanks. There is no ordinary-player recovery GUI yet.

```text
/sfmfluidrecovery x y z inspect
/sfmfluidrecovery x y z resolve-present <recoveryId>
```

Only valid version-1 uncertain fluid evidence can be acknowledged with its current
ID. Known pending/legacy/unknown/corrupt/conflicting records remain. Acknowledgement
never fills/drains a tank or recreates fluid, saves one bounded inactive summary
and keeps the manager paused. Real tank/cover failures still need qualification.

Tests cover in-process failure and orderly save/reload. Disk and external
inventory saves are not one atomic transaction: crash-proof exactly-once behavior
and dishonest handler contracts are not guaranteed. Follow
[RECOVERY.md](RECOVERY.md) for procedure and evidence.

## Feature status

Syntax that compiles is not by itself a qualified pack feature.

| Feature | Fork implementation | Qualification and limits |
| --- | --- | --- |
| SFML Java execution | Implemented with a versioned core corpus. | Headless tests plus scoped daily-803 fixtures, not full modern-SFM conformance. |
| Disk, gun and cables | Implemented. | Dedicated-server classloading and fixture persistence exercised; broader client/multiplayer/lifecycle work remains. |
| Item quantity, RETAIN, EACH, FORGET | Implemented and regression-tested. | Vanilla chests/reloads and one disconnected GT ULV input-bus slice have live evidence. |
| Sides, slots and deduplication | Supported inventory-interface behavior implemented. | Absolute faces only; other machines, covers, reserved slots and double inventories need their own fixtures. |
| Generic Forge fluids | Implemented; quantity/retention/rollback tests. | Real tanks/covers, disjoint same-fluid tanks and NBT variants remain unqualified. |
| Resource globs and OreDictionary filters | Documented subset implemented. | No arbitrary regex or NBT expression language. |
| Compound WITH / relative sides | Rejected by AST builder. | Use simple tags and absolute faces. |
| IF / ELSE IF / HAS | Text implementation. | Advanced condition corpus remains incomplete; empty/missing views have documented special behavior. |
| Round robin | Access selection implemented, overflow repaired. | Resolution-based rotation; no restart-persistent fairness guarantee. |
| Redstone level / pulse | Implemented with optional receiver/BUD bridge. | Wider live edge/restart testing remains. |
| Redstone input/output | One native Redstone Port; typed readings and named rising/falling triggers. Legacy output fallback remains. | Native faces are absolute; arbitrary blocks/covers are not implied adapters. |
| Text and clipboard editing | Existing client front ends. | Broader multiplayer/stale-container qualification remains. |
| First RetroSFM visual editor | Candidate port with generation, preview, undo, bounded layout and guarded saves. | Initial development-client edit/save/reopen smoke; not all RetroSFM features. |
| Item recovery / operator commands | Implemented. | Known/uncertain ownership, partial claims and orderly reload exercised; no player GUI or crash-atomicity guarantee. |
| Periodic discovery phases | Implemented without altering SFML due times. | Daily-803 comparison evidence; forced invalidations can spike, no full-pack budgets. |
| Local/persistent variables, strings, fixed arrays/structs, checked arithmetic | Implemented in grammar, typed AST/runtime and bounded disk state store. | Headless coverage; Text only, broader pack qualification remains open. See [SFML_DATA_MODEL.md](SFML_DATA_MODEL.md). |
| Integer bitwise operators and shifts | Implemented for signed 64-bit integers with checked 0–63 shift distances. | Text only; headless coverage, no new full-pack runtime qualification. |
| Typed item count/empty/availability | LABEL/ITEM/ORE handles, side/slot modifiers and no-argument item predicates; bounded live reads, Text only. | Unit/world fixtures cover the item slice; fluid/proxy adapters and broader pack qualification remain open. |
| PRINT and manager debug terminal | Opt-in bounded output, capture controls, status/STATE/saved-label/recent-log inspection and panel-local command recall. | Headless command/protocol/runtime coverage; exact client scope recorded separately. |
| Counted/array FOR and WHILE | Readonly loop locals, array snapshots, shared step/work/registration limits; Text only. | Headless signed-boundary, scope, iteration and item/fluid transfer regressions; no hard tick-time guarantee. |
| Player-written functions | Explicit typed parameters/RETURN, local pure/query bodies, forward calls and shared invocation bounds; Text only. | No recursion, actions, implicit STATE capture, imports or user extension methods in this slice. |
| Programming Station | Full-screen MUI2 projects, guarded saves/checks, published builds and imprinted reference disks that managers can run. | Root-source builds; reusable published libraries and imports remain planned. |
| Runtime-adjustable cadence / fullness and acceptance queries | Requested future shared Java API work. | count/empty do not measure capacity or acceptance; no current cadence/fullness API. |
| Event-driven destination availability | Deferred. | No implementation or measured cache gain. |
| EU / RF and arbitrary energy APIs | Not established by this item/fluid language. | No supported energy-transfer example or adapter claim. |
| AE2 and other pack-specific systems | No blanket support claim. | Explicit adapter contracts and evidence required. |

See [COMPATIBILITY.md](COMPATIBILITY.md) for the executable contract,
[IMPLEMENTATION_PROGRESS.md](IMPLEMENTATION_PROGRESS.md) for evidence and open
work, and [SCRIPTING_DIRECTION.md](SCRIPTING_DIRECTION.md) plus
[DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md) for future proposals.

## Maintaining this guide

Update the guide in the same change that alters observable language/editor
behavior. Describe what a player can actually use, separately from proposals.

1. Review `src/main/resources/assets/superfactorymanager/grammar/SFML.g4` together
   with `src/main/java/ca/teamdman/sfml/ast/ASTBuilder.java`. Grammar acceptance
   alone does not establish implemented semantics.
2. Check `ProgramContext`, `TransferQuota`, triggers, conditions, inventory
   adapters and recovery when changing execution. Document scope, order, units,
   defaults and empty/missing cases in player-facing terms.
3. Compile every fenced sfml example with current ProgramBuilder. Put fragments
   and unsupported syntax in text fences. Add meaningful execution fixtures for
   changed semantics: compile-only success does not prove transfer results.
4. Preserve/expand the versioned corpus and quota, RETAIN, EACH, aliases,
   sides/slots, trigger isolation and recovery regressions before wider claims.
5. When extending Visual, update controls, import limits, conflict behavior and
   metadata version policy. Compare generation with text execution; test stale
   source/layout fallback.
6. Update implementation and qualification columns independently. Link pack/JVM/
   artifact evidence. Do not promote fake-handler success into GTNH certification.
7. Replace proposed milestone status only after actual syntax, bounds, errors,
   persistence/restart behavior and validation are available. Add complete
   examples for the implemented subset, including both local and persistent data.
8. Keep compatibility, recovery and guide descriptions consistent. Preserve
   adapted-source notices. Recheck examples after selector, scheduling,
   pretty-printing, persistence or adapter changes.

## Editor and audit updates, 2026-10-28

In the manager Text editor and Programming Station, press **F4** or click the
status row to check the current draft and move to its first compile error.
Locations use one-based source lines and zero-based Unicode code-point columns;
navigation preserves the draft and undo history. It does not save, execute or
provide runtime stepping. Hover the status for the complete diagnostic.

In the debug terminal, **Tab / Shift+Tab** cycles fixed commands matching the
current prefix: help, status, state, labels, logs, clear, capture on/off. Existing
arguments stay intact. Completion never submits a command. Press Enter or Run;
one command can wait behind a background status refresh. A second explicit
pending command is refused with a waiting notice. Closing cancels the local queue.

Tag filters use literal glob syntax: only `*` and `?` are operators, matching
Unicode code points with ASCII case-insensitivity. Punctuation has no regex
meaning. Earlier accidental regex interpretation is corrected. Malformed quoted
metadata selectors now report an error rather than silently becoming wildcard;
empty, `*`, `-1` and Forge wildcard metadata retain their established behavior.

Manager recovery normally admits eight positions per client world. At capacity,
save/reconcile a retained draft before editing a ninth manager. Existing input
is never silently evicted on forced closure; exceptional fallback can exceed
that admission limit. This is still client-session recovery, not a file backup.

Fluid mutation exceptions now pause with `sfm:recovery_fluid` evidence marked
`uncertain`, including failure stage and source/destination. That amount may
already be partly present in a tank; SFM never automatically replays uncertain
records. Known rollback remainders retain the earlier pending-return policy.
Do not delete evidence just to restart. Operators can inspect and explicitly
acknowledge a manually reconciled uncertain fluid record with `/sfmfluidrecovery`;
it never creates fluid and leaves the manager paused. `/sfmrecovery` handles items.
See [recovery](RECOVERY.md) for the current-ID requirements and full procedure.

New log/gun authority packets require matching client/server builds. Gun actions
are tied to the actual held stack and session. If an immediate gun replacement
opens before authority arrives, reopen after delivery; an old editor cannot
follow a replacement gun. Stale feedback and dedicated same-slot stale packets are qualified; induced
delivery latency and early-open timing remain qualification work.

### Default arithmetic and length functions

MIN(a,b), MAX(a,b), CLAMP(value,low,high) and ABS(value) operate on signed 64-bit
integers. CLAMP includes both bounds and rejects reversed bounds. ABS of the
minimum signed 64-bit value faults. LEN(array/contents/locations) counts outer entries; LEN(string)
counts Unicode code points, not bytes or grapheme clusters. Dynamic string scans
charge work before traversal. All arguments evaluate once, left to right, under
the shared invocation budget. These primitives do not allocate player call frames.

Built-in names ignore case. Existing exact-name player functions still take
precedence in ordinary calls. Pure defaults may initialize STATE, but defining
an exact-name player function that conflicts with a default used by STATE is
rejected. Player functions still cannot initialize STATE or perform transfers.

```sfml
EVERY 20 TICKS DO
  LET $source = LABEL("stock")
  IF ($source.available()) THEN
    LET $batch = CLAMP($source.count(ORE("ingotIron")), 0, 64)
    PRINT("batch", $batch, "length", LEN([4, 8, 16]))
  END
END
```

ORE is now a reserved constructor, alongside LABEL/ITEM. A selector remains a
filter, not an exact item-stack identity. Typed ITEM/ORE/LABEL values cannot yet
replace the static resource and FROM/TO fields in INPUT/OUTPUT. Bounded contents
sequences and exact NBT-aware keys are implemented below. The candidate TRANSFER
statement accepts these values through a separate action boundary; INPUT/OUTPUT
keep their existing syntax. [The catalog](STANDARD_LIBRARY.md) records scope and
the planned NEI/recipe API.

### Grouped contents snapshots

`LABEL("stock").contents()` captures visible items using the same direct-inventory,
loaded-chunk, chest-pair, side and slot rules as count. Pass ITEM or ORE to filter,
for example `.contents(ORE("ingotIron"))`. Missing/incomplete/unsupported views and
budget failures fault the whole query. A complete available empty view returns
zero rows. No partial stock is returned and no items are moved or reserved.

```sfml
NAME "Where is our stock?"
EVERY 20 TICKS DO
  LET $stock = LABEL("inputs").contents()
  PRINT("distinct variants", LEN($stock))
  FOR $group IN $stock DO
    PRINT($group.id, $group.metadata, $group.total)
    FOR $where IN $group.locations DO
      PRINT($where.dimension, $where.x, $where.y, $where.z,
            $where.slot, $where.amount)
    END
  END
END
```

`ITEM_CONTENTS` is a read-only sequence of records: item (ITEM_KEY), id (STRING),
metadata (INTEGER), total (INTEGER), locations (ITEM_LOCATIONS). Each location
record contains integer dimension/x/y/z/slot/sideMask/amount. Coordinates and
slot refer to the actual physical inventory, including the appropriate chest
half. sideMask is observation visibility, not a claim of extraction permission.
Order is first label-position encounter and ascending eligible slots.

Index with `$stock[0].locations[0].amount` after checking lengths. LEN and FOR IN
use actual row counts, including zero; nested locations can have different
lengths. Ordinary fixed arrays keep their existing types and update rules.
Snapshots and their location sequences reject indexed SET updates. Copying an
individual ordinary record to a local permits value-copy edits without changing
the captured snapshot. Iteration variables remain read-only.

ITEM_KEY distinguishes exact registry ID, metadata and canonical NBT; `=` and
`==` compare two keys exactly, and `!=` detects a difference automatically,
including an NBT-only change. Quantity is separate from identity. Debug display omits NBT,
so equal-looking key summaries may refer to different variants. Keys/sequences
can be passed to/returned from typed player functions, but cannot live in STATE,
even nested in arrays/structs. They have no source-level constructor.

A program-wide LET can retain a baseline snapshot across runs. Compare
`$current[0].item != $original[0].item` after checking both lengths. This detects
identity changes, not a universal crafting-complete signal: some machines may
change NBT while processing or produce an identical final key. General NBT field
inspection remains planned. The
[automatic infusion example](../examples/logistics/infusion-auto-nbt.sfml)
loads one item per pedestal, waits for empty surrounding pedestals plus a changed
center key, and retries collection if the output is full. It assumes one prepared
recipe in a 27-slot chest (center in slot 0, other ingredients in slots 1–26).
Reloading resets its RAM-only phase/baseline; clear/recover an interrupted batch
before restarting. Legacy HAS/IO has no equivalent exact-NBT comparison.

Each query reads providers once; indexing, LEN, printing and loops use its
materialized data. Earlier snapshots retain their observed quantities after a
transfer; another query observes fresh stock. Current snapshot data is advisory,
not transfer authority. The candidate TRANSFER action below accepts label and
item values; player-written effectful functions remain future work.

Provisional caps: 256 exact groups, 1024 locations, and per-key NBT depth 16 /1024
nodes /8192 canonical bytes. Shared work/allocation limits often fail earlier.
Oversized/corrupt NBT fails closed. Current provider qualification is headless;
production pack, SRG list access, lifecycle and scaling checks remain open.
[Full contracts and next work](STANDARD_LIBRARY.md).


### Value-driven item transfers

The standalone item action has three forms. Source and destination are LABEL_REF
values; the optional item is an exact ITEM_KEY or broad RESOURCE_SELECTOR:

```sfml
every 20 ticks do
  let $source = label("inputs")
  let $destination = label("assembly")
  transfer($source, $destination)
  transfer($source, $destination, item("minecraft:iron_ingot"))
  transfer($source, $destination, item("minecraft:iron_ingot"), 16) into $result
  print($result.moved, $result.remaining, $result.status)
end
```

Two arguments select all items, equivalent to ITEM("*"); three select all stock
matching the supplied item key/selector. An omitted amount is a finite snapshot
of matching visible source stock during preflight, across the selected label
group/faces/slots. Extraction restrictions may prevent moving some of that stock.
The same bounded engine makes one attempt; it does not wait, retry indefinitely
or move fluids. Each statement observes stock left by earlier statements.

The four-argument form supplies a nonnegative aggregate maximum explicitly:

```sfml
EVERY 20 TICKS DO
  LET $source = LABEL("inputs").side("up")
  LET $destination = LABEL("assembly").slots(0, 8)
  LET $item = ITEM("minecraft:iron_ingot")
  LET $missing = SHORTAGE(32, $destination.count($item))
  TRANSFER ($source, $destination, $item, $missing) INTO $result
  PRINT($result.moved, $result.remaining, $result.status)
END
```

INTO is optional for every form. When present it declares a fresh local record,
with integer requested/moved/remaining and string status; omitting it discards
the result without weakening access/recovery/work limits. An automatic amount's
requested field is the actual completed stock observation, not an unlimited
sentinel. If endpoint preflight is unavailable before that observation completes,
requested/remaining are zero and status is unavailable; this is not proof of an
empty source. A valid available empty source completes a zero request.

The amount is one aggregate cap across every matching item and
container in the label group. Negative amounts fault; an explicit zero completes without
accessing native providers. Arguments evaluate once, left to right. Use a label
shared by several input containers to pull from the whole group. No new arrays
or saved snapshot locations are required to perform that pull.

Statuses: complete means the requested amount moved (including a zero request);
partial means some moved but the request was not fulfilled; no_match means no
extractable matching source was found; no_capacity means matching source stock
made no progress into eligible outputs; unavailable means endpoint preflight
could not establish supported loaded inventory access. remaining always equals
requested minus moved. Provider exceptions, work-limit exhaustion and recovery
uncertainty fault execution; they do not masquerade as successful normal results.
Earlier confirmed transfers remain committed if later work faults.

TRANSFER resolves live network membership, providers, side/slot access and stack
identity. A captured `$stock[0].item` selects exact metadata/NBT; ITEM/ORE selectors
are intentionally broader. Old observed totals do not reserve items. Each new
request checks current stock, and the engine checks again around provider calls.
Direct inventories and canonical vanilla chest pairs are the initial scope;
unqualified proxy adapters are unavailable. All callbacks stay on the server thread.

An output physical slot also selected as an input is excluded, even if initially
empty. This conservative overlap policy prevents inserted items becoming another
source and inflating the moved total during the same request. Disjoint selected
slots in one inventory can still participate. Existing INPUT registrations,
allowances and RETAIN quotas remain scoped to their original statements; TRANSFER
does not consume or apply those registrations. Put the desired amount/restrictions
on each self-contained TRANSFER action explicitly.

TRANSFER is text-only until Visual has an equivalent node. It cannot appear in
STATE defaults, predicates, ordinary call arguments or current player FUNCTION
bodies. A future ACTION/CALL boundary will let players compose effects explicitly;
ordinary FUNCTION calls remain read-only. It is not an atomic multi-container
transaction: each accepted pair reuses simulation/extraction/insertion/rollback
and disk-recovery sequencing. Budget reservations happen before extraction so
recovery is still allowed after a work limit is reached.

The provisional limit is 64 potentially mutating transfer actions per invocation,
including automatic-amount calls even if their observed stock is zero;
explicit four-argument zero requests are exempt. The limit is
shared by due triggers and loops, plus the existing shared query/expression caps.
One external callback cannot be interrupted by these counters. Large views may
hit a limit earlier; no silent truncation or whole-tick latency guarantee is implied.

### Stock and shortage helpers

`TOTAL(contents)` sums group totals; `TOTAL(locations)` sums location amounts.
`STOCK(contents, itemKey)` returns the captured total for one exact key, or zero.
It does not accept ITEM/ORE selectors: filter the original contents scan if a
broad category is needed. `SHORTAGE(required, available)` accepts nonnegative
integers and returns the additional amount required, clamped to zero. Totals use
checked signed 64-bit arithmetic and fault on overflow.

```sfml
EVERY 20 TICKS DO
  LET $stock = LABEL("inputs").contents(ORE("ingotIron"))
  PRINT("all matching variants", TOTAL($stock))
  IF (LEN($stock) > 0) THEN
    PRINT("first exact variant", STOCK($stock, $stock[0].item))
    PRINT("origins agree", TOTAL($stock[0].locations))
  END
END
```

TOTAL and STOCK walk captured rows under shared work limits; they do not query
providers or allocate lookup indices. Repeated STOCK calls are linear searches,
not a persistent stock cache. These helpers can be called by read-only player
functions. SHORTAGE can initialize STATE; live snapshots/keys cannot.
Runnable source examples are [target stock](../examples/logistics/target_stock.sfml)
and [exact variant](../examples/logistics/exact_variant.sfml).
