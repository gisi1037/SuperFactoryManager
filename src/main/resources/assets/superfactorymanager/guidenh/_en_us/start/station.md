---
navigation:
  title: "Save a Studio project"
  position: 60
  icon: "superfactorymanager:programmingStation"
  parent: /start/index.md
  keywords: ["programming station", "studio", "project", "draft", "save"]
categories: ["sfm"]
updated: "2026-10-30"
item_ids: ["superfactorymanager:programmingStation"]
---

# Save a Studio project

A Programming Station keeps named SFML projects for you.
Opening it brings up the full-screen **SFM Studio** editor.

<BlockImage id="superfactorymanager:programmingStation" scale="3" />

## Create and save

1. In **Projects**, choose **+** or a template.
2. Name your project and write or paste its code.
3. Choose **Save** and wait for confirmation.
4. Choose **Check** to validate the saved revision.

**Expected:** the project appears in the list. Reopening Studio remembers your
last project. An unfinished program can be saved even when Check reports errors.

The full-width **Insert** bar supplies statements and examples.
**Visual / Split / Code** changes your editing view. **Settings** contains
tool mode and **Studio scale**; **Game** follows Minecraft's GUI scale.
Drag the Settings title bar to move its dialog.

## Put a saved build on a disk

Hold **one fresh blank SFM disk** before opening Studio.

1. Save and Check the project.
2. Open **Build revisions** and choose **Publish saved**.
3. Select the build, then press the dock's **Imprint disk** button.
4. Insert the disk in a manager, copy labels from your gun, and press **Start**.

**Expected:** the manager loads that exact published program. Later project
edits do not change the disk. Use a fresh disk for a different build.

## Leaving with unsaved work

**Keep draft & continue** keeps a temporary recovery copy.
**Discard draft & continue** removes the unsaved changes and that recovery copy.
The prompt can be moved by its title bar.

Save important work before leaving the world: local recovery does not survive
a restart or world unload.

More: [the complete Studio workflow](/advanced/studio.md), including
publishing, reference disks, live diagnostics, layout and recovery.
