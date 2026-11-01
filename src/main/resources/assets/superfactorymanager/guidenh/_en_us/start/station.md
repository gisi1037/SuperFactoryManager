---
navigation:
  title: "Save a Studio project"
  position: 60
  icon: "superfactorymanager:programmingStation"
  parent: /start/index.md
  keywords: ["programming station", "studio", "project", "draft", "save"]
categories: ["sfm"]
updated: "2026-11-01"
item_ids: ["superfactorymanager:programmingStation"]
---

# Save a Studio project

A Programming Station keeps named SFML projects for you. This is optional:
your [first transfer](first-transfer.md) runs from an ordinary disk without one.

<BlockImage id="superfactorymanager:programmingStation" scale="3" />

## Create and save

1. Open the station. In **Projects**, choose **+** or a template.
2. Name your project and paste a complete program, such as the
   [first transfer](first-transfer.md).
3. Choose **Save** and wait for confirmation.
4. Choose **Check** to validate the saved revision.

**Expected:** the project appears in the list. Reopening Studio remembers your
last project. You can save unfinished source even when Check reports errors.

**Visual / Split / Code** changes your editing view. **Insert** supplies statements
and examples. You can explore these after saving the copied program.

## Put the program on a disk

Hold **one fresh blank SFM disk** before opening Studio.

1. Save and Check the project.
2. Open **Build revisions** and choose **Publish saved**.
3. Select the build, then press the dock's **Imprint disk** button.
4. Insert the disk in a manager, copy labels from your gun, and press **Start**.

**Expected:** the manager runs that published program. Later project edits do not
change this disk. To deploy another build, imprint a fresh blank disk.

## Before leaving

Save important work before leaving the world. **Keep draft & continue** keeps a
temporary recovery copy, but it does not survive a restart or world unload.
**Discard draft & continue** removes unsaved changes and that recovery copy.

More: [Studio projects, settings and recovery](/advanced/studio.md).
Need help with the running route? [Nothing moving?](troubleshooting.md).

## Move programs to another world

1. Save your changes. Open the top project selector, then **Transfer**.
2. Choose **Export all to clipboard**. Keep the Base64 string somewhere safe.
3. In the other world, open **Transfer**, then **Read clipboard**.
4. Check the preview and choose **Import as new**.

This copies your saved names and SFML source, including projects on other pages.
It keeps existing projects and your open draft. Builds, unsaved edits, runtime
state and world labels are excluded. Library references need relinking in the
new world. Interrupted imports may leave some copies; inspect projects before
retrying. **Stop** waits for the current reply and keeps confirmed copies.
