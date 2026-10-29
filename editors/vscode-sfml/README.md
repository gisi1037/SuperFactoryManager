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
code --install-extension dist/sfml-language-0.2.0.vsix
```

You can also select **Extensions: Install from VSIX…** in the Command Palette and
choose that file. Reload VS Code if prompted.

## Features

- TextMate highlighting for case-insensitive keywords, variables, resource IDs,
  strings, comments, types, function calls and operators, including bitwise
  operators and `!=`.
- Bracket and quote pairing, `--` line comments, indentation and block folding.
- 35 lowercase snippets with editable tab stops. Type a prefix and select the
  snippet from completion, or use **Insert Snippet**. Tab moves between fields.
- A dedicated variable color for `$names`, with dark/light/high-contrast defaults.
  Comments and string contents are excluded. Set `sfml.variableHighlighting.enabled`
  to false to use only the theme's TextMate colors, or customize the
  `sfml.variableForeground` entry in `workbench.colorCustomizations`.
- Hover signatures, descriptions and examples for built-ins and label methods,
  including selector versus exact-key types. Same-file player functions show
  their declared signatures. Hovers ignore comments and strings.
- Actual SFML compiler errors in the Problems panel and editor, using the optional
  local compiler backend described below.

Useful prefixes include `every`, `everyglobal`, `pulse`, `redstonetrigger`,
`redstoneout`, `let`, `globallet`, `state`, `assign`, `ifelse`, `for`, `forin`,
`while`, `function`, `print`, `label`, `item`, `ore`, `contents`, `count`, `empty`,
`available`, `transfer2`, `transfer3`, `transfer4`, `into` and `pedestals`.
`side` and `slots` append restrictions to a typed label handle; `array`, `struct`
and `string` insert data literals.

Snippets are building blocks: insert declarations, expressions and statements in
their appropriate contexts and replace example labels with labels from your
disk. Place program-wide `let` and `state` declarations before functions and
triggers, and functions before triggers. A program-wide `let` retains its value
in RAM between runs; `state` stores supported persistent data on the disk.
Executable edits, unloads and reloads reset RAM bindings. Player functions are
read-only computations; perform transfers, prints and other effects in triggers.

All keywords accept upper, lower or mixed case. Variables, field names, labels,
item IDs and OreDictionary names retain their own case-sensitive identities.
Exact item keys from `contents()` include metadata and NBT: `!=` detects an
NBT-only difference too. `item("namespace:name")` selects all metadata/NBT
variants; it is not an exact key constructor. An inventory's `available()` query
checks access, not matching stock or free space.

Each `transfer2`, `transfer3` or `transfer4` snippet inserts a complete transfer
statement. Append `into $result` (using the `into` snippet) to capture
`requested`, `moved`, `remaining` and `status`. The `pedestals` snippet uses legacy
`input`/`output` with a one-item cap per labelled pedestal, followed by `forget`.

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

Re-run `prepareSfmlCompiler` after changing the mod/compiler, then use
**SFML: Restart Compiler**. Set `sfml.compiler.enabled` to false to disable
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

The package is written to `dist/sfml-language-0.2.1.vsix`. Open this extension
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
