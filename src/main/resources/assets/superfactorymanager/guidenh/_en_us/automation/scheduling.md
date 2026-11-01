---
navigation:
  title: "Scheduling"
  parent: /automation/index.md
  position: 130
  keywords: ["sfm", "every", "timer", "global", "ticks"]
categories: ["sfm", "automation"]
updated: "2026-10-31"
---
# Scheduling

A program may start with NAME, followed by its declarations and triggers. A timer trigger uses `every ... { ... }` or `EVERY ... DO ... END`. Keywords ignore case; labels and data names retain their case. Whitespace helps readability, while the grammar determines where statements end. `--` starts a comment through the end of the line.

```sfml
NAME "Two rates"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:cobblestone FROM "source"
  OUTPUT TO "dest"
END
EVERY 5 SECONDS DO
  IF "reserve" HAS < 16 item:minecraft:iron_ingot THEN
    INPUT 4 item:minecraft:iron_ingot FROM "supply"
    OUTPUT RETAIN 16 item:minecraft:iron_ingot TO "reserve"
  END
END
```

One second means twenty game ticks. Server lag can make twenty ticks take more than a second of real time. Choose a positive integer interval; zero, out-of-range values and overflowing seconds conversions are rejected. TICK/SECOND singular spellings also work.

Ordinary timers follow the manager's saved running-tick counter. It advances while a valid running program is processed. Paused time, invalid-program time and recovery-blocked time do not advance that counter. Unloading a manager does not queue every missed execution for a burst of catch-up transfers.

GLOBAL timers follow world time instead:

```sfml
NAME "Global phase"
EVERY 20 GLOBAL PLUS 2 TICKS DO
  INPUT 1 FROM "source"
  OUTPUT TO "dest"
END
```

This trigger is due when world time minus two is nonnegative and divisible by twenty. PLUS uses the interval's units: `EVERY 5 GLOBAL PLUS 1 SECONDS` means a 100-tick interval with a 20-tick offset. `20G` is an alternate spelling for `20 GLOBAL`. Keep the header order shown above.

Due triggers run in source order. Statements inside them also run in source order. An earlier transfer or STATE write is visible to later work in that invocation. Each trigger starts with an independent empty active INPUT list; declarations and allowances from one trigger never feed another trigger.

Changing the interval requires editing source. WAIT suspends the current trigger without blocking the server; there is no dynamic-interval setter or background worker statement. [Redstone triggers](/automation/redstone.md) provide event-oriented alternatives. Pause, reload and edge-baseline rules are described there.

Cable discovery's periodic refresh is separate from EVERY. Its freshness window does not turn a 20-tick program into a 21-tick program. [Labels and networks](/start/labels.md) explain connection discovery.

## Waiting between steps

`wait(n)` suspends the current trigger and its active function calls for a
nonnegative integer number of **game ticks**. With a loaded, running manager,
`wait(5)` resumes five ticks later, independently of the original EVERY interval.
Lag changes elapsed real time. Zero is a no-op; negative values and deadline
overflow fault. A paused manager does not execute; an already-due continuation
can resume when execution is enabled again.

All execution still happens on the server's main thread. WAIT returns control
to the game; it does not start a worker, block the thread, or make providers safe
to call asynchronously. Other triggers continue. A busy trigger does not queue
new timer firings or redstone pulses while its earlier invocation is waiting.

Locals, function parameters, loop position and completed expression operands
survive the wait. A completed operand is not evaluated again. Later reads see
current STATE and providers: INPUT retains quantity already spent, but provider
access and retention are checked again after waking. Work budgets refresh for
the new invocation, while retained continuation memory remains bounded.

WAIT is allowed inside value-returning functions as well as void procedures:

```sfml
state answer = 0;

integer later(integer value) {
  wait(5);
  return value + 1;
}

every 20 ticks {
  answer = later(answer);
}
```

Suspended call stacks exist only in RAM. Program recompile/reload, disk removal
or replacement, chunk/world unload, STATE reset and runtime faults cancel them.
They are not serialized with the disk or restored after restart. Persistent
STATE remains subject to its normal save rules; completed transfers are not
rolled back. Use explicit STATE phases when a workflow must recover after reload.
WAIT cannot run in top-level LET/STATE initialization.
