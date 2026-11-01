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
code --install-extension dist/sfml-language-0.3.0.vsix
```

You can also select **Extensions: Install from VSIX…** in the Command Palette and
choose that file. Reload VS Code if prompted.

## Features

- TextMate highlighting for case-insensitive keywords, variables, resource IDs,
  strings, comments, types, function calls and operators, including bitwise
  operators and `!=`.
- Bracket and quote pairing, `--` line comments, indentation and block folding.
- 52 lowercase snippets with editable tab stops. Type a prefix and select the
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

Useful prefixes include `every`, `everyglobal`, `pulse`, `redstonetrigger`,
`redstoneout`, `let`, `globallet`, `state`, `assign`, `ifelse`, `for`, `forin`,
`while`, `function`, `functionlabels`, `void`, `voidref`, `functionlegacy`, `print`,
`label`, `item`, `ore`, `fluid`, `contents`, `count`, `empty`, `available`,
`fcontents`, `fcount`, `fempty`, `favailable`, `transfer2`, `transfer3`,
`transfer4`, `transferamount`, `transferf2`, `transferf3`, `transferf4`,
`transferfamount`, `into` and `pedestals`.
`side` and `slots` append restrictions to a typed label handle; `array`, `struct`
and `string` insert data literals.

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

Function return types can also be fixed arrays or structs. For example,
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

Use `functionlabels` to insert this function shape. A different returned array
length is a type error; unsized `label_ref[]` is not a supported type. Struct
returns declare their fields, such as
`{ready: boolean, amount: integer} inspect() { return {ready: true, amount: 0}; }`.
Captured `item_contents` and `fluid_contents` are native snapshot sequences,
rather than fixed arrays; both support indexing, `len(...)` and `for ... in ...`.

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

The package is written to `dist/sfml-language-0.3.0.vsix`. Open this extension
directory in VS Code and press **F5** to try it in an Extension Development Host.
The runtime has no external JavaScript dependencies and performs no program
execution or network requests.

Tests use VS Code's TextMate/Oniguruma tokenizer. The mod's
`VscodeSfmlExtensionTest` also compiles every snippet's default expansion against
the real SFML compiler in its documented insertion context.

The extension follows the official VS Code guides for
[syntax highlighting](https://code.visualstudio.com/api/language-extensions/syntax-highlight-guide),
[language configuration](https://code.visualstudio.com/api/language-extensions/language-configuration-guide)
and [VSIX packaging](https://code.visualstudio.com/api/working-with-extensions/publishing-extension).
