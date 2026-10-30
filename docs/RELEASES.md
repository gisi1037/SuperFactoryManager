# Release builds

Install one ordinary SFM JAR on both client and server. The inventory mod is
optional. Do not install both target JARs, a `-dev` JAR or a sources archive.

| Build target | GTNH pack | MUI2 | GTNHLib | NEI compile API |
| --- | --- | --- | --- | --- |
| `rc2` | 2.9.0-RC-2 | 2.3.92 | 0.11.52 | 2.8.155 |
| `nightly` | daily-2026-10-30+807 | 2.3.94 | 0.11.54 | 2.8.171 |
| `daily803` (development default) | daily-2026-10-27+803 | 2.3.93 | 0.11.54 | 2.8.169 |

Targets are pinned, not automatically updated when another nightly appears.
The common required API floor is MUI2 2.3.92 and GTNHLib 0.11.52; both release
profiles compile and test the same code against their selected dependencies.
Use the pack's Java 17-or-newer launcher. GuideNH is optional.

## Build

Use JDK 25 for Gradle; output targets Java 8 bytecode. From a clean checkout:

```powershell
./scripts/gradle.ps1 build writeManualClientArtifact '-PsfmTarget=rc2' '-PsfmVersion=0.1.0-alpha.1'
./scripts/gradle.ps1 build writeManualClientArtifact '-PsfmTarget=nightly' '-PsfmVersion=0.1.0-alpha.1'
```

`build/manual-client-artifact.txt` names the exact reobfuscated production JAR
from the most recent command. Its filename and Forge version include the target.
Never select the newest file from `build/libs`: earlier targets may still be there.

CI builds both profiles and uploads production artifacts. Release publication is
explicit so a later CI run cannot replace downloads underneath published checksums.
Before publishing, verify both normal JARs, their versions and dependency floor,
compiled tests, bundled licenses/book and SHA-256 checksums. Exclude performance
fixtures and development/compiler tooling. Publish development builds as prereleases
and describe the actual scope of runtime qualification in the release notes.
