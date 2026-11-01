---
navigation:
  title: "Performance and limits"
  position: 60
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["performance", "budget", "limits", "lag", "scale", "profile"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Performance and limits

The cheapest automation does only the work it needs, at a useful interval, over
the smallest correct inventory selection. A fast timer is not automatically a
fast factory: it can repeatedly inspect unchanged containers.

## Practical habits

- Choose an interval that matches production speed. Many storage routes need
  seconds, not every tick. Timers currently use literal intervals, not a mutable
  runtime interval variable.
- Use specific labels and slot/side selections when possible. `available()`
  followed by another query is extra provider work; guard a query when you need
  its unavailable behavior, but do not expect the guard to cache the next scan.
- Capture `.contents()` once in an invocation and reuse that snapshot for LEN,
  indexing, loops, TOTAL and STOCK. Calling `.contents()` inside every item loop
  repeats the scan.
- STOCK searches captured groups linearly. For a broad ore total, filter one
  `.contents(ORE("name"))` scan and call TOTAL instead of repeated exact lookups.
- A visible stock count does not measure insertion capacity. Check actual
  transfer results: a full output can cause source scans without any progress.
- Turn PRINT capture and verbose logs off when finished diagnosing.
- Avoid repeatedly rebuilding a large cable network while other managers are
  running. Network maintenance is work too.

Snapshots are deliberately fresh observations, not persistent caches. They stay
unchanged after a transfer; another query is needed for new stock. Do not put
them or their keys in STATE. Cross-run cache invalidation and event-driven
destination availability are future work.

## Shared execution bounds

Limits below are current guardrails. Several limits apply at once and the first
one exceeded wins. A declared maximum does not promise every program below it
will fit in a tick or that a third-party callback can be interrupted.

| Area | Current bound |
| --- | --- |
| Expression work / constructed data cells | 16,384 units each per manager invocation. |
| Loop steps | 1,024 shared across due triggers/functions in one invocation. |
| Player function calls / call depth | 256 calls / depth 16 per invocation. |
| Player definitions / parameters | 128 definitions / 16 parameters per definition. |
| Active function-local cells | 4,096 shared by active call frames. |
| Local bindings / collection width | 256 per trigger / 256 entries or fields. |
| Program-wide LET memory | 256 globals, 4,096 retained cells, 16,384 characters and 256 KiB of opaque handle/key data. |
| Data/expression nesting | 32. |
| Persistent declarations / expanded cells | 256 / 4,096 total cells. |
| One string | 4,096 UTF-16 code units. |
| Constructed string characters | 16,384 per invocation. |
| Native query positions / provider calls | Default 4,096 / 32,768 per invocation; server configurable. |
| Native query slot-work / allocation units | Default 65,536 each per invocation; server configurable. |
| One contents snapshot | At most 256 exact groups and 1,024 physical locations. |
| One exact item's NBT identity | Depth 16, 1,024 tag nodes and 8,192 canonical bytes. |
| Potentially mutating TRANSFER actions | 64 per invocation, shared by due triggers and loops; automatic amounts count, explicit zero is exempt. |
| Loop nesting / loop INPUT registrations | Depth 16 / 256 registrations per invocation; FORGET does not renew this allowance. |
| Native redstone writes | 256 attempts per invocation. |
| Manager editor source + layout | 30,000 UTF-8 bytes, subject to the disk's remaining space. |
| Whole execution disk NBT | 30,000 uncompressed bytes shared by source, labels, STATE, layout and recovery. |

An invocation can contain several due triggers. Work already spent by an earlier
trigger is not reset for a later trigger, function or action. A 256-group
headline limit does not guarantee a scan of 256 NBT-heavy variants will fit.
Large scopes currently need narrower selections or program redesign; coherent
stock paging/paced queries are not available yet.

The server setting `performance.queryBudgetMultiplier` in
`config/superfactorymanager.cfg` scales the four native query allowances together.
Its default is 4; values 1-16 are supported. Value 1 restores the original
1,024 positions, 8,192 provider calls and 16,384 slot-work/allocation units.
Restart the server (or single-player game) after editing it. All due triggers,
functions and actions still share one allowance per manager update. Other limits,
including item NBT size, transfer action counts and expression work, stay fixed.
Higher settings permit larger batches and can increase server tick time; they
are not a throughput improvement or a fixed millisecond allowance.

## What a runtime limit means

An exceeded work/shape/type limit faults the program. No partial contents list
is returned as though it represented all stock. Failed data assignments do not
publish their replacement value. Earlier successful statements and confirmed
transfers remain completed. If a provider operation is uncertain, the recovery
path can pause the manager with evidence rather than silently retrying it.

## Manager profiling and activity

**Overview > Expand** shows up to twenty chronological complete-update cost
samples, latest/mean/max/peak and runtime state. Bars scale to the visible window;
their height is not a percentage of the server tick budget. The scheduler-clock
marker can advance without a due trigger or successful transfer. Statement
tracing, node highlighting and breakpoints are not available yet.

**Profile** records complete server manager updates, including idle checks,
recovery and discovery. **Record** defaults to 1,200 updates and stops
automatically. **Stop** preserves the sample; **Reset** clears it. Results include
count, updates with a due trigger, average, p50/p95/p99 and maximum milliseconds.
The collector starts off. Terminal commands `profile start 1200`, `profile status`,
`profile stop` and `profile reset` control the same capture; start accepts
1-4,096 updates. This does not measure whole-server TPS, per-instruction work,
background compiler work or manual runs.

## Operator profiling

```text
/sfmprofile start
/sfmprofile status
/sfmprofile stop
```

The profiler records aggregate scheduled manager-update work in a 600-tick ring.
It starts off, retains a stopped capture and resets on start. It is not a
measurement of the entire server tick or every manual/packet operation. Compare
actual transferred amounts and workload as well as time when tuning a program.

## Spread a batch across ticks

A query-budget error is a per-run work limit: split a large batch across runs.
Increasing the trigger interval reduces frequency, not the work in one run.

**Setup:** label three connected source chests `chest1`, `chest2`, `chest3` and
an output chest `dest`. Put at least eight items in each source.

```sfml
name "Paced chest batch"
every 100 ticks {
  for index = 1 to 3 {
    transfer(label("chest" + index), label("dest"), 8)
    wait(1)
  }
}
```

**Expected:** up to eight items move from each chest, with a one-tick gap between
requests. Locals and loop position survive WAIT; completed transfers do not
repeat on resume. Each resumed invocation gets fresh work allowances. A single
expensive scan or request still has to fit its own allowance. WAIT(0) does not
spread work across ticks. See [scheduling](/automation/scheduling.md) for lifecycle
cancellation and busy-trigger behavior.

## Published programs loading

Reference programs can briefly show a loading state while waiting for capacity.
Temporary capacity waits retry automatically. A missing or corrupt publication
instead pauses activation until repaired and retried. Keep the world's `sfm`
directory in backups; the disk does not contain a spare source copy.

Next: [contents](/automation/contents.md), [stock helpers](/automation/stock.md),
and [debugging](debugging.md).
