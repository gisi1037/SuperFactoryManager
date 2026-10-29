---
navigation:
  title: "Debugging and the terminal"
  position: 80
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["print", "debug", "terminal", "error", "logs", "troubleshooting"]
categories: ["sfm"]
updated: "2026-10-28"
---

# Debugging and the terminal

Start with the manager's status. A valid program can still be paused, lack
reachable containers, meet an existing stock target or be blocked by recovery.

## A useful diagnosis order

1. Check disk presence, compilation, running state and the current status.
2. Inspect the disk's saved labels, not just the gun. Check case and coordinates.
3. Check loaded chunks, the cable path and the actual block at each saved position.
4. Test one known item, one source and one empty output before a larger route.
5. Check faces, slots, filters, machine mode, covers and capacity.
6. Check RETAIN and shared INPUT allowances. An earlier output may already have spent the allowance.
7. Enable PRINT capture, run a due trigger, and compare physical inventories.

## PRINT

Open **Terminal** in the manager and enable capture. Capture starts off, belongs
to this manager's memory and is not saved to the disk or world.

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
| `state` | Show bounded live persistent values. |
| `state $cycles` | Inspect one persistent variable. |
| `labels` | Show the first 15 saved label names and position-entry counts. |
| `labels Input Chest` | Inspect the exact case-sensitive saved name, including spaces. |
| `logs` / `logs 15` | Show the latest 10 logs, or a count from 1 to 15, oldest first. |
| `capture on` / `capture off` | Enable/disable PRINT capture. |
| `clear` | Clear PRINT history without clearing engine logs. |

Up/Down recalls up to 32 submitted commands and restores an unfinished command
when you move past the newest entry. Tab/Shift+Tab completes supported command
names; completion does not execute the command.

Commands do not evaluate arbitrary SFML, mutate STATE or move items. LET locals
exist only during trigger execution, so inspect them with PRINT. `labels` lists
saved bindings even if disconnected; it does not scan contents or test network
connectivity. Truncated output says what was displayed.

## Compile errors and common mistakes

Use the editor's status row or **F4** to jump to the first current error.
Check the preceding clause as well as the reported line: a missing THEN or END
can make a later valid token look unexpected.

| Symptom | Check |
| --- | --- |
| Top-level LET rejected | Use STATE above triggers for persistence; put LET inside a trigger/function. |
| Bare `$lock = FALSE` rejected | Assignment requires `SET $lock = FALSE`. |
| IF body rejected | Use `IF (...) THEN`, and close both IF and trigger with END. |
| Empty test unlocks at the wrong time | `> 0` means stock exists; `= 0` tests zero. |
| Count says stock exists, transfer moves none | Visibility is not extraction permission or output capacity. |
| A second OUTPUT moves nothing | The earlier output may have consumed the shared INPUT allowance. |
| Visual refuses the program | Keep advanced features in Text; refusal preserves source. |
| Save rejected after changing disk | Copy the draft, reopen the current disk and reconcile. |

## Engine logs

**View logs** exposes engine logs. INFO, DEBUG and TRACE reveal more detail;
current logging can switch off after a useful execution. `logs` only reads the
history already captured and does not turn tracing on. Ask a server operator
about recovery or repeated runtime faults rather than deleting disk data.

Next: [performance](performance.md) and [recovery](recovery.md).
