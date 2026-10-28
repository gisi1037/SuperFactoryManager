# SFM and SFML guide for the GTNH port

Reviewed on 2026-10-28 for this development fork: Minecraft 1.7.10 / Forge
10.13.4.1614, with GTNH `daily-2026-10-27+803` as the pinned integration target.
Implemented language behavior and real-pack qualification are separate. This
guide does not claim complete modern SFM or RetroSFM compatibility.

SFML is the program stored on a disk. Java executes it on the server. The first
RetroSFM visual editor produces that same SFML; there is no separate graph
execution engine. Local and disk-persistent variables, fixed arrays/structs and
checked integer arithmetic and strings are candidate implementations. The latest
focused check passed 120 tests, including all 28 complete guide examples; the full
suite and full-pack qualification remain open. Rebuild the mod and restart Minecraft to load language changes. Lua is not installed.

## Contents

- [First working transfer](#first-working-transfer)
- [Disks, labels and cables](#disks-labels-and-cables)
- [Choosing an editor](#choosing-an-editor)
- [Program structure and scheduling](#program-structure-and-scheduling)
- [Data: locals, persistent state and expressions](#data-locals-persistent-state-and-expressions)
- [INPUT and OUTPUT](#input-and-output)
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

### First RetroSFM visual slice

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
  keeps bounded history. **Name** edits the header. **Cancel** discards the draft.

Every node must connect to a trigger before save. Triggers cannot have incoming
wires. Unconnected nodes cause errors instead of being silently ignored.

Import compares grammar tokens and refuses unsupported or lossy programs.
Direct ELSE IF import currently belongs in Text. Programs using LET, SET, STATE
or data expressions also belong in Text: Visual refuses them intact rather than
dropping their declarations or instructions. Imported comments move above
the generated program, with their text retained. A refusal leaves original disk
source available through Text.

Layout is a versioned comment beginning `-- sfm:blueprint:v1 `. The server still
executes ordinary SFML. Editing generated text makes layout stale; reopening
Visual rebuilds from current text instead of reviving old instructions. This is
a port-specific format, not modern RetroSFM serialization.

Visual saves use a source-revision guard. If disk source changed since opening,
the server refuses the stale save. Copy the draft, reopen the current disk and
reconcile changes. Save waits for server synchronization. A timeout retains the
draft; check disk and chat rather than assuming success or failure.

Current visual limits: 256 nodes, 4,096 characters per clause, nesting depth 32
and 32,700 UTF-8 bytes for source plus layout. Expanded layout also has a bound.
Unicode can use multiple bytes per character. These are editor limits, not an
engine scalability guarantee. Graph concepts derive from Saereth's MIT-licensed
RetroSFM commit `b6776c456e87ace2ada5e5464fea9e4367d74bad`. Its notice is preserved
at `src/main/resources/META-INF/licenses/RetroSFM-MIT.txt`.

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

### LET declares; SET assigns

LET declares and initializes a mutable local binding. SET changes a visible
existing binding; it does not create one. All variable references have a `$`.
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

### Fixed types, arrays and structs

Scalar data types are signed 64-bit integers, booleans and immutable strings.
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
Use `NOT ($a = $b)` for inequality. No automatic integer-to-string conversion,
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
2. Unary `+` / `-`.
3. `*`, `/`, `%`.
4. `+` / `-` (`PLUS` is another plus spelling).
5. Comparisons `>`, `<`, `=`, `>=`, `<=`, or GT/LT/EQ/GE/LE.
6. NOT.
7. AND.
8. OR.

Arithmetic is left-associative. Comparisons do not chain: use
`$a < $b AND $b < $c`. Equality accepts same-type scalars only; collection equality
is not supported. AND/OR require booleans and short-circuit left to right.
Expressions cannot call inventory providers or embed HAS; combine a pure
expression with an ordinary HAS/REDSTONE condition in the outer boolean grammar.

Addition, subtraction, multiplication, negation and division are checked for
signed 64-bit overflow. Division truncates toward zero; remainder has the
dividend's sign. Division/remainder by zero fault. The minimum literal
`-9223372036854775808` is valid; dividing it by -1 faults, while remainder by -1
is zero. No saturation or wraparound occurs. For example, `-7 / 3` is -2 and
`-7 % 3` is -1.

Use parentheses for pure expression IF conditions, including a lone boolean:
`IF ($ready) THEN`. `IF (FALSE AND 1 / 0 > 0) THEN` does not divide by zero.
Unreachable code must still have valid names and types. Runtime arithmetic and
budget faults pause the manager and stop later statements/triggers for that
invocation; earlier successful state/world effects remain.

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

Dynamic redstone **output** levels are explicitly rejected in this milestone.
Use a literal level for the existing legacy-emitter output. Dynamic trigger
intervals, labels, resource IDs, side/slot selections and collection sizing are
also outside this milestone. There are no functions, loops, recursion, append,
remove, direct fullness/acceptance queries or runtime cadence setter.

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
`>=` or GT, LT, EQ, LE, GE. Use `=`, not `==`.

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

Boolean logic supports TRUE, FALSE, NOT, AND, OR and parentheses. Parenthesize
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

Use STATE for a lock shared by both triggers. LET is local to one trigger run;
assignments need SET, and IF needs THEN.

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

### Output is legacy-emitter-only

```sfml
NAME "Legacy emitter example"
EVERY 20 TICKS DO
  OUTPUT 15 REDSTONE LEVEL TO signal
END
```

This changes supported connected legacy Steve's Factory Manager emitters through
the optional bridge. A labelled vanilla block does not become an emitter.
Without a supported emitter/integration there is no useful output target. Level
0 switches it off; values clamp to 0-15. Visual's Signal node has the same limit.
Syntax does not establish full current-pack or arbitrary GT-cover qualification.

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
Profiling observations apply to the measured workload; they do not establish a
whole-pack performance guarantee.

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
items; fluid recovery has an existing separate policy and further qualification
work. There is no ordinary-player recovery GUI yet.

Tests cover in-process failure and orderly save/reload. Disk and external
inventory saves are not one atomic transaction: crash-proof exactly-once behavior
and dishonest handler contracts are not guaranteed. Follow
[RECOVERY.md](RECOVERY.md) for the operator procedure.

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
| Redstone output | Supported legacy emitters only. | No generic native redstone emission claim. |
| Text and clipboard editing | Existing client front ends. | Broader multiplayer/stale-container qualification remains. |
| First RetroSFM visual editor | Candidate port with generation, preview, undo, bounded layout and guarded saves. | Initial development-client edit/save/reopen smoke; not all RetroSFM features. |
| Item recovery / operator commands | Implemented. | Known/uncertain ownership, partial claims and orderly reload exercised; no player GUI or crash-atomicity guarantee. |
| Periodic discovery phases | Implemented without altering SFML due times. | Daily-803 comparison evidence; forced invalidations can spike, no full-pack budgets. |
| Local/persistent variables, strings, fixed arrays/structs, checked arithmetic | Candidate implementation in grammar, typed AST/runtime and bounded disk state store. | Focused language and persistence tests pass; Text only, full-pack qualification remains open. |
| Runtime-adjustable cadence / fullness and acceptance queries | Requested future shared Java API work. | No current SFML syntax or API. |
| Event-driven destination availability | Deferred. | No implementation or measured cache gain. |
| Lua | Under evaluation. | No selected/installed runtime. |
| EU / RF and arbitrary energy APIs | Not established by this item/fluid language. | No supported energy-transfer example or adapter claim. |
| AE2 and other pack-specific systems | No blanket support claim. | Explicit adapter contracts and evidence required. |

The sections above describe the available syntax and its limits. Future features
will be documented when implemented.

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
