---
navigation:
  title: "Redstone"
  parent: /automation/index.md
  position: 30
  keywords: ["sfm", "redstone", "pulse", "rising", "falling", "port", "signal"]
categories: ["sfm", "automation"]
updated: "2026-11-01"
item_ids: ["superfactorymanager:redstonePort"]
---
# Redstone

SFM has two input paths: the manager's own observed power, and named native Redstone Ports connected to its network. Bare REDSTONE and an unnamed pulse use manager/legacy input. Named queries and FROM triggers address native ports. They do not automatically combine into one signal.

Choose [manager pulses](#manager-level-and-pulse), [native port reads](#native-redstone-port),
[named edges](#named-rising-and-falling-triggers) or [signal output](#emitting-signals).
For a complete small setup, try the [remote gate](#switch-a-remote-output-with-an-input).

## Manager level and pulse

In a condition, REDSTONE alone means REDSTONE > 0. `REDSTONE LEVEL >= 8` compares the observed manager level. The optional Steve's Factory Manager bridge can contribute supported legacy receiver power.

```sfml
NAME "One batch per manager pulse"
EVERY REDSTONE PULSE DO
  INPUT 8 item:minecraft:cobblestone FROM "source"
  OUTPUT TO "dest"
END
```

A pulse occurs when sampled observed power rises from zero to positive. Holding it high does not repeatedly pulse; it must return to zero before another rise. This is a sampled tick event, not a queue of every physical transition. Optional legacy BUD updates can also supply pulses, and several updates may coalesce. BUD is not a general native block-update subscription API.

Use STATE to make a pulse unlock a scheduled attempt:

```sfml
NAME "Unlock one scheduled attempt"
STATE $Lock = FALSE
EVERY REDSTONE PULSE DO
  SET $Lock = FALSE
END
EVERY 20 TICKS DO
  IF (NOT $Lock) THEN
    INPUT FROM "chest_in"
    OUTPUT TO "Ped"
    SET $Lock = TRUE
  END
END
```

FALSE initially permits one attempt before a pulse. TRUE initially waits for the first pulse on a new disk or after an explicit state reset; changing a default does not overwrite existing saved state. The lock is set even if the output moves zero. Source order lets a due pulse unlock before the due timer. Ped is a case-sensitive label. See [STATE](/language/variables.md) for persistence rules.

## Send a pulse without waiting

Use `pulse(port, ticks)` to turn an output on and automatically off. The next
statement runs immediately; no WAIT is required.

```sfml
name "Timed output pulse"
every 40 ticks {
  pulse(label("signal").side("north"), 2)
}
```

Connect and label a native Redstone Port `signal`. This sends a two-game-tick
pulse on its north face every 40 ticks. Default strength is 15; an optional third
argument selects 1..15. Duration is 1..2147483647 game ticks. Omit `.side()` to
select all faces. This is a Code-mode trigger/void-procedure action, not a value.

Another pulse restarts the timer on those faces. An explicit OUTPUT takes over
and cancels their timer, even for the same level. A still-high retrigger does not
create a second rising edge. Pulse end sets zero. Timers are saved on the port,
so a paused program still finishes its pulse; unloaded ports reconcile on load.
Legacy emitters and slot-selected handles are unsupported. A group failure may
leave pulses already started on earlier members active.

## Native Redstone Port

The Redstone Port (`superfactorymanager:redstonePort`) both reads and emits signals. Connect it to an SFM cable or directly to the manager, then label it. One block provides independent input readings and output latches on all six absolute faces. No separate detector/emitter block is required.

```sfml
NAME "Copy north input to east output"
EVERY 20 TICKS DO
  LET $port = LABEL("signal").side("north")
  IF ($port.redstoneAvailable()) THEN
    OUTPUT ($port.redstone()) REDSTONE LEVEL TO "signal" EAST SIDE
  ELSE
    OUTPUT 0 REDSTONE LEVEL TO "signal" EAST SIDE
  END
END
```

| Method on LABEL_REF | Meaning |
| --- | --- |
| `redstone()` | INTEGER maximum input over all selected ports/faces. |
| `powered()` | Boolean: that input level is greater than zero. |
| `redstoneAvailable()` | Boolean: every selected port/face is present, connected and readable. |

Omitting `.side()` selects all six faces; use down/up/north/south/west/east, with bottom/top aliases. Slot selections are invalid for redstone. Plain `.available()` remains an item query, not port availability. Missing labels, unloaded members, unsupported blocks and incomplete groups are unavailable. Unguarded redstone/powered queries then fault instead of inventing zero. Provider errors and exhausted budgets also fault, including during availability checks.

Typed observations may run in read-only player functions and share the invocation's query budget. They read fresh values at their execution point, so an earlier output can affect a later query. Circuit feedback can carry a port's output back into its input through strongly powered neighboring blocks.

## Named rising and falling triggers

```sfml
NAME "Count named transitions"
STATE $rises = 0
STATE $falls = 0
EVERY REDSTONE PULSE FROM "signal" NORTH SIDE DO
  SET $rises = $rises + 1
END
EVERY REDSTONE FALLING FROM "signal" NORTH SIDE DO
  SET $falls = $falls + 1
END
```

RISING is an alias for PULSE in a named trigger. Zero-to-positive is rising; positive-to-zero is falling. Other positive-level changes are neither. Named native ports maintain counters from neighbor notifications, so a short notified pulse may be observed after it has ended. Several changes coalesce: each matching trigger runs at most once per manager invocation, and both rising and falling triggers can run for a short pulse. Reading an edge does not consume it for another trigger or manager. One held-high port does not hide another port's edge in the same label group.

### When an edge is delivered

| Situation | Result |
| --- | --- |
| Start of an invocation | Eligibility for all named triggers is captured before any trigger body runs. |
| A body changes an input | A later query can see it now; the new event can become eligible next invocation. |
| Any selected member is missing/unavailable | The whole group is suppressed; valid members' observed counters still advance. |
| Edge-snapshot budget fails | Execution faults before any trigger body runs. |

Idle subscriptions use cached counters. Every member is freshly validated before
an edge is delivered. Events are observations, not a durable queue.

### When edge history resets

| Change | Effect |
| --- | --- |
| First observation, replacement, unload/reload, pause/resume, recovery or recompile | Establish a baseline without replaying old edges. |
| Disconnect/reconnect, relevant network edits or covered chunk gaps | Rebase; a pending edge may be discarded even if a particular cable path survives. |
| Manager-neighbor notifications or changes with unknown locations | Conservatively rebase. |
| Unrelated edits or periodic discovery refresh alone | Preserve pending edges. |

Ordinary signal changes do not invalidate the network.

Reliable capture requires neighbor notifications. Unnotified transitions and edges
across connection gaps cannot be reconstructed. Named triggers reject round robin,
slot selections, relative faces and NULL SIDE. Visual's
[Port edge node](/advanced/editors.md) edits named RISING/PULSE/FALLING triggers;
its separate Pulse node watches the manager input.

## Switch a remote output with an input

**Setup:** connect two Redstone Ports, labelled `sensor` and `emit`. Put a lever
against the sensor's north face and a redstone lamp against the emitter's east
face. Start with the lever off. Give the manager time to establish its baseline,
then switch the lever on and off.

```sfml
name "Remote signal gate"
every redstone rising from "sensor" north side do
  output 15 redstone level to "emit" east side
end
every redstone falling from "sensor" north side do
  output 0 redstone level to "emit" east side
end
```

**Expected:** the lamp switches on after the rising event and off after the
falling event. A lever already on when the program starts is a baseline, not a
new edge. This example latches the last output; it does not continuously copy
the input while the manager is stopped.

## Emitting signals

```sfml
NAME "Latch all output faces high"
EVERY 20 TICKS DO
  OUTPUT 15 REDSTONE LEVEL TO "signal"
END
```

Outputs write connected native ports and can fall back to supported legacy Steve's Factory Manager emitters when available. A labelled vanilla block does not become an emitter. Choose a level from 0 through 15; zero switches the output off. Select faces with ordinary statement-side syntax, for example `EAST SIDE`; omitted sides select all faces. Typed native input queries do not imply a legacy receiver adapter.

Output levels latch until another write changes them, including while the manager is paused or removed, and survive world reload. Input readings and edge histories do not survive as saved events. Selected faces update before neighbor notifications; unchanged writes are silent. Native writes share a limit of 256 attempts per manager invocation across loops and due triggers. Earlier writes remain if later work faults.

Visual's Signal node supports literal output levels; computed levels require Text. Supported providers must work with guarded loaded-world access. A provider that requires casting that view to World is unsupported. SFM checks its own direct reads/notifications without force-loading chunks; downstream mod callbacks still have their usual world behavior. Verify the actual circuit and any legacy adapter before making production depend on it.

Studio fields edit 1–32 ordered destination labels and a literal level in either
statement order and either quantity position. Add, Pick or Remove references,
keeping at least one label. Blank/invalid rows block Apply, including pending
level edits. EACH, round robin and side/slot restrictions stay intact. Omitted
levels keep default 15; static literals retain the compiler's existing 0–15 clamp.
Two-quantity, computed-level and over-32-label clauses stay in the full-clause
editor. **Pick** uses draft references only, without checking connectivity;
**Choose (pending)** followed by **Apply fields** commits the change.
