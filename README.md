# Super Factory Manager for GTNH

A development fork of Super Factory Manager for Minecraft 1.7.10 and Forge
10.13.4.1614, targeting GregTech New Horizons. The current integration target is
GTNH daily-2026-10-27+803. This is an experimental development snapshot.

Connect inventories with SFM cables, assign labels with the label gun, and run an
SFML program from a disk in a manager. Java executes programs on the server.

## Features

- Item and Forge-fluid transfers with filters, quantities, retention, sided/slot
  access and round-robin selection.
- Scheduled and redstone-pulse triggers, conditions and logical expressions.
- Text editing with syntax highlighting and an initial visual editor adapted
  from RetroSFM. Visual preserves or refuses unsupported text rather than
  silently changing it; new data statements currently use Text.
- Local and persistent variables, strings, fixed arrays/structs and checked
  signed 64-bit integer arithmetic. Persistent values live in memory and are
  snapshotted onto the program disk at lifecycle boundaries.
- Transfer recovery and operator recovery commands, bounded diagnostics and
  phased periodic network discovery for smoother shared-network workloads.

Start with the [SFM/SFML guide](docs/SFML_GUIDE.md). Operators should also read the
[item recovery procedure](docs/RECOVERY.md).

## Build

Install JDK 25 or newer for Gradle. The project targets Java 8 bytecode using the
GTNH build conventions. The pinned GTNH integration environment uses Java 17.

On Linux/macOS:

```sh
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The optional Windows helper also accepts `SFM_JAVA_HOME` or `JAVA_HOME`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 build
```

Built JARs are written to `build/libs/`. For a build without tests, use `assemble`.
Regenerate the checked-in language parser with `generateSfmlParser` after editing
`src/main/resources/assets/superfactorymanager/grammar/SFML.g4`.

Correctness tests are included. Expensive characterization tests are opt-in with
`-PauditBenchmark=true`; full-pack benchmark fixture classes are included only
with `-PpackBenchmark=true` and are excluded from ordinary release builds.

## Development status

This fork does not claim complete compatibility with modern SFM or RetroSFM.
Broader multiplayer, real-machine/cover/fluid integration and performance at scale
remain under qualification. Arrays have fixed shapes; user-defined functions,
named remote redstone detectors and inventory-contents queries are not yet added.
The text editor currently has a known footer layout issue when resized.

## Credits and license notices

Based on [mgomezch's 1.7.10 backport](https://github.com/mgomezch/SuperFactoryManager)
of [TeamDman's SuperFactoryManager](https://github.com/TeamDman/SuperFactoryManager).
The initial visual editor adapts concepts from
[RetroSFM](https://github.com/breakinblocks/RetroSFM).
See [NOTICE.md](NOTICE.md), the existing [SFM license](src/main/resources/LICENSE)
and the bundled [RetroSFM MIT notice](src/main/resources/META-INF/licenses/RetroSFM-MIT.txt).
