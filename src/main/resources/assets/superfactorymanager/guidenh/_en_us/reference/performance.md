---
navigation:
  title: "Performance and limits"
  position: 60
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["performance", "budget", "limits", "lag", "scale", "profile"]
categories: ["sfm"]
updated: "2026-10-28"
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
- Count visibility and insertion capacity separately. A full output can cause
  useful-looking source scans without any progress.
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
| Local bindings / collection width | 256 per trigger / 256 entries or fields. |
| Data/expression nesting | 32. |
| Persistent declarations / expanded cells | 256 / 4,096 total cells. |
| One string | 4,096 UTF-16 code units. |
| Constructed string characters | 16,384 per invocation. |
| Native query positions / provider calls | 1,024 / 8,192 per invocation. |
| Native query slot-work / allocation units | 16,384 each per invocation. |
| One contents snapshot | At most 256 exact groups and 1,024 physical locations. |
| One exact item's NBT identity | Depth 16, 1,024 tag nodes and 8,192 canonical bytes. |
| Potentially mutating TRANSFER actions | 64 per invocation, shared by due triggers and loops; automatic amounts count, explicit zero is exempt. |
| Manager editor source + layout | 30,000 UTF-8 bytes, subject to the disk's remaining space. |
| Whole execution disk NBT | 30,000 uncompressed bytes shared by source, labels, STATE, layout and recovery. |

An invocation can contain several due triggers. Work already spent by an earlier
trigger is not reset for a later trigger, function or action. A 256-group
headline limit does not guarantee a scan of 256 NBT-heavy variants will fit.
Large scopes currently need narrower selections or program redesign; coherent
stock paging/paced queries are not available yet.

## What a runtime limit means

An exceeded work/shape/type limit faults the program. No partial contents list
is returned as though it represented all stock. Failed data assignments do not
publish their replacement value. Earlier successful statements and confirmed
transfers remain completed. If a provider operation is uncertain, the recovery
path can pause the manager with evidence rather than silently retrying it.

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

Next: [contents](/automation/contents.md), [stock helpers](/automation/stock.md),
and [debugging](debugging.md).


Reference programs wait in a bounded loading queue. Temporary worker capacity
does not turn off requested running state; overflow registration retries are
staggered. Missing/corrupt publications still pause until repaired and retried.
Cold loads always validate the actual world grant and exact program bundle.
The lexer now caches separate strict/editor automata, reducing repeated compiler
allocation without sharing mutable execution plans. Headless results are not a
whole-pack throughput guarantee.
