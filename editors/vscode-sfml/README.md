# SFML for Visual Studio Code

Language support for the SFML language in this Super Factory Manager GTNH fork.
Requires Visual Studio Code 1.85.0 or newer. Extension ID:
`gisi1037.sfml-language`.

Open a `.sfml` file to activate SFML highlighting and editing support. For an
existing file with another extension, use **Change Language Mode → SFML**, or add
an association to your VS Code settings:

```json
{
  "files.associations": {
    "*.sfml": "sfml"
  }
}
```

## Install

Install the packaged extension from the repository's `editors/vscode-sfml`
directory:

```sh
code --install-extension dist/sfml-language-0.4.0.vsix
```

You can also select **Extensions: Install from VSIX…** in the Command Palette and
choose that file. Reload VS Code if prompted.

## Features

- TextMate highlighting for case-insensitive keywords, variables, resource IDs,
  strings, comments, types, function calls and operators, including bitwise
  operators and `!=`.
- Bracket and quote pairing, `--` line comments, indentation and block folding.
- 55 lowercase snippets with editable tab stops. Type a prefix and select the
  snippet from completion, or use **Insert Snippet**. Tab moves between fields.
- A dedicated variable color for plain names and optional `$names`, with dark/light/high-contrast defaults.
  Comments and string contents are excluded. Set `sfml.variableHighlighting.enabled`
  to false to use only the theme's TextMate colors, or customize the
  `sfml.variableForeground` entry in `workbench.colorCustomizations`.
- Hover signatures, descriptions and examples for built-ins and label methods,
  including selector versus exact-key types. Same-file player functions show
  their declared signatures, including type-first headers and `void` procedures.
  Variable hovers show the declaration or parameter type. Hovers ignore comments and strings.
- Actual SFML compiler errors in the Problems panel and editor, using the optional
  local compiler backend described below.
- Interactive completion for functions, receiver methods, same-file qualified
  functions and lexical variables (plain or `$` names). Existing parentheses are
  preserved: completions insert the name only. Comments/strings are excluded.
- Signature help while typing `(` or `,`, including overloads and active argument
  underlining. Use **Ctrl+Shift+Space** to request it explicitly. Nested calls,
  arrays, structs and quoted commas do not advance the enclosing argument.

Completions and signatures are presentation assistance, not type validation.
Receiver suggestions list available methods without guessing an expression's
type. Same-file functions retain exact case; the Java compiler still diagnoses
invalid scope, receiver types, overloads and arguments.

Useful prefixes include `every`, `everyglobal`, `pulse`, `redstonetrigger`,
`redstoneout`, `let`, `globallet`, `state`, `assign`, `ifelse`, `for`, `forin`,
`while`, `function`, `functionlabels`, `void`, `voidref`, `functionlegacy`, `print`,
`label`, `item`, `ore`, `fluid`, `contents`, `count`, `empty`, `available`,
`fcontents`, `fcount`, `fempty`, `favailable`, `transfer2`, `transfer3`,
`transfer4`, `transferamount`, `transferf2`, `transferf3`, `transferf4`,
`transferfamount`, `into` and `pedestals`.
`side` and `slots` append restrictions to a typed label handle; `array`, `struct`
and `string` insert data literals. `dynamicarray` inserts one complete program
using typed empty construction, fixed-array conversion, `push` and `pop`.

Snippets are building blocks: insert declarations, expressions and statements in
their appropriate contexts and replace example labels with labels from your
disk. Place program-wide `let` and `state` declarations before functions and
triggers. Functions can appear before or after triggers; keeping them together
before triggers is a useful convention. A program-wide `let` retains its value
in RAM between runs; `state` stores supported persistent data on the disk.
Executable edits, unloads and reloads reset RAM bindings. Value-returning functions
may read STATE. Use triggers or `void` procedures for transfers, prints and STATE
writes; pass program-wide RAM values as parameters to functions.

The default snippets use brace blocks and plain variable names. `$name` and `name`
refer to the same binding; `FUNCTION name(...) RETURNS type DO ... END` remains
supported alongside type-first headers such as `integer next(integer value) { ... }`.
Literal labels always need quotes, including `input item:* from "source"`,
`output item:* to "destination"` and `if "source" has > 0 item:* then ... end`.
A plain name in a label position is a variable reference, not a literal fallback.

Void procedures and value-returning functions can declare `ref integer value` parameters. Calls must also use
`ref`, such as `increment(ref count)`, with a writable variable, array element or
struct field of exactly the declared type. Writes are immediate; ordinary
parameters remain value copies.

Calls with direct or indirect REF effects cannot run in program-wide LET
initializers or named-redstone subscription expressions. Value functions must
receive STATE references explicitly from a trigger or void procedure rather than
acquiring them implicitly. Native actions still belong in triggers or void
procedures. Disabled PRINT skips argument evaluation, including any REF calls.

Function parameter and return types can also be fixed arrays, dynamic arrays or structs. For example,
`label_ref[2]` means exactly two label handles:

```sfml
label_ref[2] pedestals() {
  return [label("ped1"), label("ped2")];
}

every 20 ticks {
  let targets = pedestals();
  for target in targets {
    print(target.empty());
  }
}
```

Use `functionlabels` to insert this fixed-array function shape. A different
returned fixed-array length is a type error. `label_ref[]` declares a dynamic
array whose length belongs to its value; `integer[][2]` is a dynamic array of
fixed pairs, while `integer[2][]` is two dynamic integer arrays. Neither shape
implicitly converts to the other. Struct returns declare their fields, such as
`{ready: boolean, amount: integer} inspect() { return {ready: true, amount: 0}; }`.
Captured `item_contents` and `fluid_contents` are native snapshot sequences,
rather than fixed arrays; both support indexing, `len(...)` and `for ... in ...`.

Dynamic arrays have one exact element type, including nested array/struct types.
`array("integer")` creates a typed empty `integer[]`; the quoted type is a
compile-time literal of at most 1024 characters and depth 32, and cannot be `void`.
`array([1, 2])` explicitly converts a fixed array to a dynamic value. `array(...)`
also copies dynamic arrays and captured contents/location sequences, preserving
their exact element type. An untyped `[]` remains invalid. Function signatures can use `integer[]`, including
`ref integer[] values` for a writable parameter.

`push(ref values, value)` is a standalone `void` operation. `pop(ref values)`
removes and returns the last element, and faults when empty. Both require an
explicit writable reference to a dynamic array. The length is bounded to 256
outer entries and the whole retained value to 4096 logical cells, including
nested children; work, allocation and retained text/opaque payload budgets also
apply. Dynamic arrays can be `state` when every nested element is persistable
(integer, boolean, string, or a collection of those). Handles and snapshots remain
RAM-only. STATE enforces actual aggregate cell, string and complete-disk limits;
failed growth does not replace the target. It stays in heap until a save/disk
lifecycle boundary. Saved length survives changes to its default initializer.
Arrays retain immutable value-copy semantics: mutations install an outer
replacement, while ordinary copies retain their previous contents. Push/pop,
extend, insert and removeAt walk the replacement's outer entries in O(n).

The remaining collection helpers use the same element types and work limits:

| Call | Result and behavior |
| --- | --- |
| `extend(ref values, sequence)` | `void`; append a same-element fixed/dynamic array or captured native sequence in order. An empty source changes nothing. |
| `insert(ref values, index, item)` | `void`; insert before index `0..len(values)`, inclusive. |
| `removeAt(ref values, index)` | Return the removed element; index must be `0..len(values)-1`. |
| `clear(ref values)` | `void`; preserve the element type and remove all entries. Clearing an empty array changes nothing. |
| `peek(values)` | Return the last element without mutation; empty sequences fault. |
| `contains(values, item)` | Return whether an exact typed element deeply matches. |
| `indexOf(values, item)` | Return the first matching zero-based index, or `-1`. |

Mutators require dynamic arrays and explicit writable `ref` targets. The three
readers accept fixed/dynamic arrays and captured native sequences by value.
Membership compares nested values, label names plus side/slot restrictions, and
exact item/fluid NBT keys. Its deep comparisons consume shared work limits; this
does not add collection equality to `==` or `!=`. Empty `extend` and `clear`
operations do not change the root's structural epoch.

The `dynamicarray` snippet inserts this complete program:

```sfml
every 20 ticks {
  let values = array("integer");
  push(ref values, 1);
  let last = pop(ref values);
  let seeded = array([last, 2]);
  print(last, len(values), len(seeded));
}
```

Whole-array references remain valid across replacement. Captured indexed
references and assignments can become stale after structural replacement,
including a same-length replacement, an ABA change (change away and back), or a
direct whole-root rebind of a value containing dynamic arrays. Invalidation is
conservative across the root, so nested or sibling indexed references may also
fault. A successful writer refreshes its own captured reference. RHS effects
already committed before a stale assignment fault are not rolled back. As with
other REF effects, collection mutations cannot run in program-wide LET initializers or
named-redstone subscription expressions.

The `forindex` and `forindexed` snippets each insert a complete program with a
local `values = [1, 2]` source:

```sfml
every 20 ticks {
  let values = [1, 2];
  for (let index = 0; index < len(values); index = index + 1) {
    print(index, values[index]);
  }
  for (index, value) in values {
    print(index, value);
  }
}
```

Java-style FOR initializers are mutable and visible through the condition, body
and update, then leave scope. Body locals cannot be used in the update clause.
Initializer, condition and update may be omitted; an omitted condition is true.
An indexed sequence loop captures its source once, then binds a read-only
zero-based integer index and read-only value inside each body. Its new bindings
are unavailable in the source expression or after the loop. Both loop forms also
accept `do ... end` bodies and run inside typed functions and void procedures.
WAIT resumes the current phase; RETURN exits before the update. Shared work,
allocation and loop-step limits still apply.

All keywords accept upper, lower or mixed case. Variables, field names, labels,
item IDs and OreDictionary names retain their own case-sensitive identities.
Exact item keys from `contents()` include metadata and NBT: `!=` detects an
NBT-only difference too. `item("namespace:name")` selects all metadata/NBT
variants; it is not an exact key constructor. An inventory's `available()` query
checks access, not matching stock or free space.

Each transfer snippet inserts a complete statement. `transfer(source, destination)`
moves all visible item stock; a third selector/key filters it, while a third
integer caps the total across all item types. Four arguments filter and cap.
Append `into result` (using the `into` snippet) to capture
`requested`, `moved`, `remaining` and `status`. The `pedestals` snippet uses legacy
`input`/`output` with a one-item cap per labelled pedestal, followed by `forget`.

`transferf` has the same four forms for fluids. All fluid amounts are in mB:
`transferf(source, destination, 1000)` caps all fluids at one bucket, while
`transferf(source, destination, fluid("water"), 1000)` caps matching water.
`fluid("*")` selects every fluid. Use `.fcontents()`, `.fcount()`, `.fempty()` and
`.favailable()` for tanks; their omitted selector means all fluids. Item queries
remain separate. `.side(...)` can restrict fluid access; `.slots(...)` is invalid
for fluid queries/transfers.

`wait(20)` suspends the current trigger and active function calls for 20 game ticks,
including inside a value-returning function. It does not pause the server or
other triggers. `setcircuit(label("machine"), 5)` changes a compatible connected
GregTech machine's ghost circuit; `0` or `-1` clears it. It is an explicit action
for triggers or `void` procedures, not a value-returning function.

## Compiler diagnostics

Prepare the backend from the mod checkout with its configured JDK 25+ build
environment (or use the project's existing PowerShell Gradle wrapper):

```sh
./gradlew prepareSfmlCompiler
```

This compiles the development-only adapter and checks the actual SFML compiler
offline. It writes `build/sfml-compiler.json` with the Java executable and local
development classpath. The VSIX does not bundle Minecraft classes or a game
installation. The prepared backend selects the project's Java 17 runtime.

Open the mod checkout as a trusted VS Code workspace. The default
`sfml.compiler.manifestPath` is `build/sfml-compiler.json`, relative to that
workspace folder. For programs elsewhere, set it to the absolute path of this
prepared manifest. The backend also supports untitled SFML documents when a
workspace or an absolute manifest path is available.

The extension debounces edits and reuses one bounded JVM per configured manifest.
Only current document-version results are published. Compiler errors include
parser failures, unknown variables/functions, incompatible types and invalid
arguments. Some semantic failures stop at the first error, as in the game.
Locations without compiler source information appear at the document start.
Unicode columns are converted to VS Code's UTF-16 positions.

Re-run `prepareSfmlCompiler` after changing the mod/compiler. The extension watches
the configured manifest, including an absolute manifest outside the program
workspace, and restarts its backend when that file changes. **SFML: Restart
Compiler** remains available if you need to force a restart. Set
`sfml.compiler.enabled` to false to disable
diagnostics. The extension does not launch Java in an untrusted workspace.
Restricted Mode, missing configuration and unavailable backends appear as tooling
warnings, rather than successful checks. In Restricted Mode, use VS Code's
**Workspaces: Manage Workspace Trust** command and trust your own program folder
to enable compilation. Checking resumes automatically when trust is granted.

Compilation never executes triggers, transfers or queries. Runtime-only behavior,
physical inventories, world labels, provider permissions and work-budget faults
still require in-game testing. This is not a live Minecraft debugger or full
semantic autocomplete system.

See the repository's [SFML guide](../../docs/SFML_GUIDE.md) and
[examples](../../examples/logistics) for current language behavior and complete
programs.

## Development

From `editors/vscode-sfml`, install the pinned development dependencies, run the
extension checks, then create a VSIX. Building requires Node.js 22 or newer;
colors, hovers and snippets use VS Code's built-in runtime. Compiler diagnostics
require the separately prepared Java backend.

```sh
npm ci
npm test
npm run package
```

The package is written to `dist/sfml-language-0.3.3.vsix`. Open this extension
directory in VS Code and press **F5** to try it in an Extension Development Host.
The runtime has no external JavaScript dependencies and performs no program
execution or network requests.

Tests use VS Code's TextMate/Oniguruma tokenizer. The mod's
`VscodeSfmlExtensionTest` also compiles every snippet's default expansion against
the real SFML compiler in its documented insertion context.

Library imports pin an exact owned project revision and digest. Studio's
**Publish library** and **Insert library** workflow prepares these headers;
use `alias.function(...)` to call an export. The grammar colors complete import
headers and namespaces while keeping `import`, `revision` and `digest` usable
as ordinary variable names. Keywords ignore case; aliases/functions do not.
Private world libraries are not resolved by the offline compiler. Save and use
Studio's **Check** for linked validation; an unresolved-import diagnostic does
not mean the Java compiler bridge is unavailable. Library hover/signature lookup
from private server storage is not provided by this extension.

The extension follows the official VS Code guides for
[syntax highlighting](https://code.visualstudio.com/api/language-extensions/syntax-highlight-guide),
[language configuration](https://code.visualstudio.com/api/language-extensions/language-configuration-guide)
and [VSIX packaging](https://code.visualstudio.com/api/working-with-extensions/publishing-extension).

Captured ITEM_KEY NBT methods now have completion and hover help: hasnbt, nbttype, nbtinteger, nbtstring, nbttext, nbtkeys and nbtlength. Use JSON Pointer paths such as /heat. They inspect a contents snapshot without live item mutation or rescanning. Install the matching compiler build for diagnostics.

BREAK now exits the innermost lexical loop in triggers/functions, including after WAIT. It skips Java FOR updates, does not yield, and does not limit the batch size inside a transfer. The real compiler diagnoses outside-loop BREAK inside player functions.

BREAK outside loops now ends only the current trigger activation. Its normal timer
or redstone condition controls the next run; other due triggers keep running.
Inside loops it still exits only the nearest loop. Functions require a local loop.


## Spread periodic work with a random starting offset

Keep a fixed period and choose one starting offset for each manager:

```sfml
name "Staggered transfer"
let phase = random(0, 19)
every 20 plus (phase) ticks do
  input 1 from "source"
  output to "dest"
end
```

The short form is `every 20 plus (random(0, 19)) ticks { ... }`.
`random(minimum, maximum)` returns an integer with both bounds included. Equal
bounds return that value; reversed bounds fault. It is evaluated at runtime,
never while compiling. Use program-wide LET for a runtime initial value;
STATE defaults must remain constant. Full signed 64-bit bounds are supported.

The period remains 20 ticks. The parenthesized offset is evaluated once per
manager execution state, after program-wide LET initialization. Managers sharing
one compiled program keep independent offsets. Changing that variable later does
not reschedule the timer. Pausing keeps the offset while execution state remains
in memory; reloads, program replacement or execution resets can choose it again.

Header expressions must return INTEGER, cannot WAIT, and cannot use REF effects.
Offsets must be 0..2147483647 in ticks after unit conversion. Existing numeric
PLUS offsets and ordinary timers are unchanged. GLOBAL/compact g still selects
the world clock; ordinary timers use the manager clock. Seconds apply to the
period and offset alike; use TICKS for tick-level staggering. A period of one tick
cannot spread its repeated work across different ticks.

Use the same program-wide phase value for related timers that should stay aligned.
WAIT resumes the same activation without overlapping it; BREAK does not reroll the
offset. No random draw or header evaluation occurs on idle eligibility polls.
Random phases reduce clumping, but can collide; this is not a guaranteed load
balancer or a reduction in the work performed. Interval changes/new random delays
on each run remain separate future work. Runtime offset headers require Code mode;
Visual refuses them rather than silently dropping scheduling behavior.
