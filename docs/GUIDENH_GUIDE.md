# SFM GuideNH book

The player-facing book lives in
`src/main/resources/assets/superfactorymanager/guidenh/_en_us/`. It is bundled in
the ordinary SFM mod JAR. GuideNH discovers the assets automatically as guide
`superfactorymanager:guidenh`; SFM has no new runtime dependency or client/server
Java integration for the book. Without GuideNH, these are unused resource assets.

The book has tutorials and reference chapters for hardware, labels, MUI2 Text
and Visual editors, Programming Station drafts, scheduling, item/fluid routing,
selectors, retention, sides/slots/round robin, conditions, typed queries, grouped
contents, value-driven transfers, stock helpers, redstone, local/persistent data,
arrays/structs, strings, arithmetic/logic/bitwise expressions, loops, player
functions, PRINT/debugging, recovery and performance. Planned features are
clearly separated from callable features.

## Pinned format reference

Target GuideNH **1.3.53**, already in daily-803. The read-only upstream reference
is tag `1.3.53`, commit `67c2b1116a32f3871ccc635cf2b4cde54e6ac57b`:
[GuideNH source](https://github.com/GTNewHorizons/GuideNH/tree/67c2b1116a32f3871ccc635cf2b4cde54e6ac57b).
Its README, Guide-Page-Format wiki, resource scanner, item index and Markdown
options establish the format used here. This is original SFM content; no
upstream GuideNH implementation is copied into the mod.

Each UTF-8 page has YAML frontmatter, one H1, navigation title/parent/position,
search keywords and an updated date. Rooted links such as `/language/loops.md`
stay inside the SFM guide namespace. Higher sibling positions appear first.
The root and hardware/label/station/redstone pages index the relevant SFM items.
The getting-started hardware page contains a five-block interactive GameScene.

Complete programs use `sfml` fences. Fragments, commands and proposed syntax use
`text` fences. Code blocks offer GuideNH's normal copy button; SFML highlighting
inside GuideNH is not asserted by this content integration. MUI2 remains the
implementation used by SFM's own editable GUI surfaces.

## Build and validate

From the repository root, using the project's JDK wrapper:

```powershell
& "$env:SystemRoot/System32/WindowsPowerShell/v1.0/powershell.exe" -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 test spotlessCheck checkstyleMain checkstyleTest assemble guideNhPack
```

`GuideNhBookTest` compiles every complete SFML example and checks page/parent
reachability, local links, item IDs and item-shortcut uniqueness. The book resource
directory is an explicit test input, so edits invalidate cached corpus results.

`GuideNhNativeParsingTest` is optional. Pass the exact installed GuideNH JAR and,
on the Java 17 standalone test JVM, the JVM-Downgrader Java API compatibility JAR
to use its runtime Markdown/MDX parser and bundled safe YAML parser. These are
validation-only file inputs, not SFM dependencies:

```powershell
& "$env:SystemRoot/System32/WindowsPowerShell/v1.0/powershell.exe" -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 test --tests '*GuideNh*' '-PguideNhJar=C:/absolute/path/to/guidenh-1.3.53.jar' '-PguideNhRuntimeJar=C:/absolute/path/to/jvmdowngrader-java-api-1.3.5-downgraded-8.jar'
```

The native check verifies frontmatter, code preservation, registered corpus tag
names, item-image nodes and the scene's block nodes/attributes. It deliberately
does not execute scenes or initialize renderer/world code. It is syntax/structure
evidence, not a substitute for in-game rendering, search or copy-button QA.
The production GuideNH JAR uses SRG Minecraft names; directly invoking its
Frontmatter/PageCompiler against MCP-named test classes is not a valid test.

`guideNhPack` writes `build/distributions/SFM-GuideNH-en_us.zip`, a resource pack
containing only the same guide assets and `pack.mcmeta`. Enable this resource pack
for a content-only update on an older SFM client. It contains no game code and
does not make absent language features available on an older SFM build.

## Read in game

- Open GuideNH with its configured key (default G), then choose Super Factory Manager.
- Or use the client command `/guidenhc open superfactorymanager:guidenh` in a world.
- Hold the guide key over an indexed SFM item for its shortcut page (default hold: ten ticks).
- Reload changed guide resources with F3+T, or `/guidenhc reload`. The separate server command `/guide reload` requires permission level 3 in this pinned version.

For live development in the existing daily-803 instance, TXLoader can read the
same files at `config/txloader/load/superfactorymanager/guidenh/_en_us/`. F3+T
rediscovers them without changing the running SFM JAR or restarting the game.
Use normal `load`, preserve unrelated content and back up any replaced files.
The loose `config/guidenh/DefaultGuide/` folder is unsuitable when the existing
`DefaultGuide.zip` was selected at startup: the ZIP wins and that choice is cached.

## Keep it current

When a feature changes, update both the affected in-game page and the long-form
[SFML guide](SFML_GUIDE.md), plus the [default API catalog](STANDARD_LIBRARY.md)
where appropriate. Add complete examples or update existing ones, run the corpus
checks, validate local navigation and then inspect the affected page in GuideNH.
Keep planned syntax outside runnable fences. Update item shortcuts when adding
blocks/items. Translations should preserve the English path structure; GuideNH
uses per-page English fallback for missing translations.

Manual QA: home entry, sidebar hierarchy, search, all seven item shortcuts,
copying a program into Text, long tables/code at different GUI scales, the
interactive hardware scene and English fallback. Record the exact SFM artifact,
GuideNH version and which checks actually ran; native parsing is not gameplay
qualification.
