---
navigation:
  title: "Debugging and the terminal"
  position: 80
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["print", "debug", "terminal", "error", "logs", "troubleshooting"]
categories: ["sfm"]
updated: "2026-11-02"
---

# Debugging and the terminal

Start with the manager's status. A valid program can still be paused, lack
reachable containers, meet an existing stock target or be blocked by recovery.

## A useful diagnosis order

1. Check disk presence, compilation, running state and the current status.
   If it reports a runtime error, open **Terminal → Errors** first.
2. Inspect the disk's saved labels, not just the gun. Check case and coordinates.
3. Check loaded chunks, the cable path and the actual block at each saved position.
4. Test one known item, one source and one empty output before a larger route.
5. Check faces, slots, filters, machine mode, covers and capacity.
6. Check RETAIN and shared INPUT allowances. An earlier output may already have spent the allowance.
7. Enable PRINT capture, run a due trigger, and compare physical inventories.

## PRINT

Open **Terminal** in the manager and enable capture. New managers start with it
off. The on/off choice is saved with that manager and restored before its program
runs after chunk reload or server restart, even before anyone opens the terminal.
It is not a disk setting. PRINT history remains bounded RAM-only data and starts
empty after reload; it does not replay old messages.

After installing a build that adds this persistence, enable capture once on each
manager you want to inspect. Older saves have no saved capture choice. To diagnose
startup behavior, record label availability separately from item counts and print
the reason when setting a shutdown flag. Program-wide LET resets on reload;
STATE restores its saved value. Old output can roll out of the bounded history.

```sfml
NAME "Inspect a route"
STATE $cycles = 0
EVERY 20 TICKS DO
  SET $cycles = $cycles + 1
  LET $source = LABEL("source").slots(0, 8)
  PRINT("cycle", $cycles, "available", $source.available())
  IF ($source.available()) THEN
    PRINT("visible items", $source.count(ITEM("*")))
  END
END
```

PRINT accepts one to eight expressions. Values are formatted in a bounded
readable form, with truncation for large/deep collections. Disabled capture or
an exhausted print allowance skips the arguments, including their queries.
Compute observations needed by program logic separately from PRINT.

There are at most 16 printed statements per manager invocation, 512 characters
per line, 200 retained lines and 16,384 retained characters. Excess or evicted
output contributes to the dropped count. A failing evaluated argument still
causes the ordinary program fault.

## Terminal commands

| Command | Behavior |
| --- | --- |
| `help` | Show supported commands. |
| `status` | Show manager status, compilation and capture state. |
| `error` | Read the last retained runtime failure, including its cause and source/function location when available. Reference-disk details are author-only. |
| `error clear` | Clear the retained failure, with the same author restriction. PRINT and ordinary logs remain. |
| `state` | Show bounded live persistent values. |
| `state $cycles` | Inspect one persistent variable. |
| `watch` / `watch $cycles` | Inspect initialized program-wide LET and STATE values, or one exact name. |
| `trace start` / `trace start 128` | Record the next 64 statements, or up to 128; expires after 200 running manager ticks. |
| `trace status` / `trace stop` / `trace clear` | Read, stop, or discard the RAM-only trace. |
| `trace list` / `trace list 1` | Show the latest 15 events, or page from a chosen event. |
| `locals 12` | Read up to eight lexical local/parameter snapshots for trace event 12. |
| `profile start 1200` | Start a bounded complete-update timing capture; accepts 1–4,096 updates. |
| `profile status` / `profile stop` / `profile reset` | Read, stop while retaining, or clear that profiler capture. |
| `labels` | Show the first 15 saved label names and position-entry counts. |
| `labels Input Chest` | Inspect the exact case-sensitive saved name, including spaces. |
| `logs` / `logs 15` | Show the latest 10 logs, or a count from 1 to 15, oldest first. |
| `capture on` / `capture off` | Enable/disable PRINT capture. |
| `clear` | Clear PRINT history without clearing engine logs. |

Up/Down recalls up to 32 submitted commands and restores an unfinished command
when you move past the newest entry. Tab/Shift+Tab completes supported command
names; completion does not execute the command.

Commands do not evaluate arbitrary SFML, mutate STATE or move items. Use PRINT or
an explicit trace to inspect trigger/function locals. Traces show statement
locations and completed, waiting, break or failed attempts, never pause execution,
and do not consume script work allowance. Values are snapshots after that attempt;
a resumed WAIT has a new attempt. Ref parameters direct you to the caller's
snapshot; library locals are omitted. Disk/program replacement and unloading
discard traces. They are not saved or included in public logs. Recording adds
bounded overhead and is off until requested.
Program-wide LET values remain available through Watch after initialization.
For a reference disk, only its verified source author can inspect variable names
and values; execution-disk holders can still use profiling and deliberate PRINT
output. Inspection does not initialize globals or flush STATE. `labels` lists
saved bindings even if disconnected; it does not scan contents or test network
connectivity. Truncated output says what was displayed.

## Runtime failures

Open the manager's **Terminal** page and click **Errors**, or enter `error`.
The result stays in the main window. Use **Copy** to keep the details.
You do not need PRINT capture or log recording enabled.

Check the reported line, column, function and cause when available. The location
belongs to the program installed in the manager, which can differ from your
current Studio draft. The retained failure lives in manager memory, not on disk.
For reference disks, only the verified source author can read the details;
other players see a generic runtime-error status.
Authors can also read the retained failure in **Logs**, even with ordinary
recording off.
Pause and Run retain it; a later failure replaces it. Use `error clear` to remove
it explicitly. Editing/resetting the source, replacing the disk or unloading the
manager also clears it. Clearing ordinary Logs leaves this separate record alone.

## Compile errors and common mistakes

Use the editor's status row or **F4** to jump to the first current error.
Check the preceding clause as well as the reported line: a missing THEN or END
can make a later valid token look unexpected.

| Symptom | Check |
| --- | --- |
| A declaration after a trigger is rejected | Put program-wide LET/STATE before function definitions and triggers; LET inside a body is local. |
| `$lock = FALSE` reports an unknown variable | Declare it with LET or STATE first; direct assignment and `SET $lock = FALSE` both update existing variables. |
| IF body rejected | Use `IF (...) THEN`, and close both IF and trigger with END. |
| Empty test unlocks at the wrong time | `> 0` means stock exists; `= 0` tests zero. |
| Count says stock exists, transfer moves none | Visibility is not extraction permission or output capacity. |
| A second OUTPUT moves nothing | The earlier output may have consumed the shared INPUT allowance. |
| Visual refuses the program | Keep advanced features in Text; refusal preserves source. |
| Save rejected after changing disk | Copy the draft, reopen the current disk and reconcile. |

## Engine logs

**Logs** opens inside the Manager window, alongside Terminal, Profile and Watch.
The server recording level is separate from the local severity/search filter.
Use Wrap, Follow, Live/Frozen, scrolling and Copy to inspect retained output.
Opening Logs does not enable recording. INFO, DEBUG and TRACE reveal more detail;
current logging can switch off after a useful execution. `logs` only reads the
history already captured and does not turn tracing on. Automatic source-bearing
logs are suppressed for reference disks. Ask a server operator
about recovery or repeated runtime faults rather than deleting disk data.

Next: [performance](performance.md) and [recovery](recovery.md).
