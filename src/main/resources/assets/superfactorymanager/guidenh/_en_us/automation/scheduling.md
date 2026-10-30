---
navigation:
  title: "Scheduling"
  parent: /automation/index.md
  position: 130
  keywords: ["sfm", "every", "timer", "global", "ticks"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Scheduling

A program may start with NAME, followed by its declarations and triggers. A timer trigger starts with EVERY and ends with END. Keywords ignore case; labels and data names retain their case. Whitespace helps readability, while the grammar determines where statements end. `--` starts a comment through the end of the line.

```sfml
NAME "Two rates"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:cobblestone FROM source
  OUTPUT TO dest
END
EVERY 5 SECONDS DO
  IF reserve HAS < 16 item:minecraft:iron_ingot THEN
    INPUT 4 item:minecraft:iron_ingot FROM supply
    OUTPUT RETAIN 16 item:minecraft:iron_ingot TO reserve
  END
END
```

One second means twenty game ticks. Server lag can make twenty ticks take more than a second of real time. Choose a positive integer interval; zero, out-of-range values and overflowing seconds conversions are rejected. TICK/SECOND singular spellings also work.

Ordinary timers follow the manager's saved running-tick counter. It advances while a valid running program is processed. Paused time, invalid-program time and recovery-blocked time do not advance that counter. Unloading a manager does not queue every missed execution for a burst of catch-up transfers.

GLOBAL timers follow world time instead:

```sfml
NAME "Global phase"
EVERY 20 GLOBAL PLUS 2 TICKS DO
  INPUT 1 FROM source
  OUTPUT TO dest
END
```

This trigger is due when world time minus two is nonnegative and divisible by twenty. PLUS uses the interval's units: `EVERY 5 GLOBAL PLUS 1 SECONDS` means a 100-tick interval with a 20-tick offset. `20G` is an alternate spelling for `20 GLOBAL`. Keep the header order shown above.

Due triggers run in source order. Statements inside them also run in source order. An earlier transfer or STATE write is visible to later work in that invocation. Each trigger starts with an independent empty active INPUT list; declarations and allowances from one trigger never feed another trigger.

Changing the interval requires editing source. There is no runtime sleep, dynamic-interval setter or background worker statement. [Redstone triggers](/automation/redstone.md) provide event-oriented alternatives. Pause, reload and edge-baseline rules are described there.

Cable discovery's periodic refresh is separate from EVERY. Its freshness window does not turn a 20-tick program into a 21-tick program. [Labels and networks](/start/labels.md) explain connection discovery.
