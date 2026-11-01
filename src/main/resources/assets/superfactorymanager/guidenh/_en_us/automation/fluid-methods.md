---
navigation:
  title: "Fluid methods and transfers"
  parent: /automation/index.md
  position: 55
  keywords: ["sfm", "transferf", "fluid", "tank", "fempty", "fcount", "millibuckets"]
categories: ["sfm", "automation"]
updated: "2026-11-01"
---
# Fluid methods and transfers

Use `transferf` for tanks. **1000 mB is one bucket.** Item `transfer` stays unchanged.

**Setup:** connect two supported Forge tanks and label them `supply` and
`machine`. Put at least 2000 mB of water in supply and leave room in machine.
Enable PRINT capture to inspect movement. If a tank has different face views,
select a known working face with `.side("up")` or another physical direction.

```sfml
name "Supply a tank"
every 20 ticks do
  let $source = label("supply")
  let $destination = label("machine")
  if ($source.favailable() and $destination.favailable()) then
    transferf($source, $destination, fluid("water"), 1000) into $sent
    print($sent.moved, "mB", $sent.status)
  end
end
```

**Expected:** each successful run moves up to 1000 mB and prints the moved amount
and status. The example keeps supplying water; it does not maintain a target
stock. A full or refusing tank can move less.

| Form | Action |
| --- | --- |
| `transferf(src, dest)` | All visible fluids. |
| `transferf(src, dest, 1000)` | Up to 1000 mB across all fluids. |
| `transferf(src, dest, fluid("water"))` | All matching water. |
| `transferf(src, dest, fluid("water"), 1000)` | Up to 1000 mB of water. |

`fluid("*")` matches all fluids. Names are exact Forge registry names; water is
`"water"`, not `"minecraft:water"`. An amount is a total limit, not multiplied by
tanks or faces. Missing amount observes a finite source snapshot. Every form
can capture [progress fields](/automation/transfers.md) with `into $result`.

| Query | Result |
| --- | --- |
| `$tank.favailable()` | Every selected tank is available. |
| `$tank.fempty()` | The available group contains no fluid. |
| `$tank.fcount()` | Total mB in the group. |
| `$tank.fcontents()` | Read-only per-tank records. |

Each query optionally takes `fluid("water")` or another fluid selector.
Missing groups are **not empty**: `fempty`/`favailable` return false, and
`fcount`/`fcontents` report an error. Provider failures and exhausted budgets
are errors. Item `.empty()` and `.contents()` remain item-only.

`fcontents` rows contain `fluid`, `id`, `amount`, `dimension`, `x`, `y`, `z`,
`tank` and `sideMask`. A row's `.fluid` is an exact fluid/NBT key for `transferf`;
`fluid("water")` instead matches every NBT variant. Loop/index/`len` work on
the snapshot. It is an observation, not reserved stock or persistent state.

Start with direct Forge tanks. `.side("up")` selects a physical face; `.slots()`
does not select tanks. Equivalent face views are counted once; incompatible
views need an explicit side. Opaque/proxy adapters still need qualification.

Transfers make one bounded attempt now; they do not wait for capacity. Use
a scheduled trigger to check again later, or `wait(20)` to suspend the current
trigger/function stack for twenty game ticks. A WHILE loop without WAIT stays in
one invocation; it cannot let machines advance. WAIT is nonblocking and uses
parentheses, not `wait 20`. See [scheduling](scheduling.md) for cancellation and
resume rules.

Item and fluid actions share 64 potentially mutating actions per invocation,
plus work budgets. Recovery evidence pauses uncertain operations; see
[recovery](/reference/recovery.md). Legacy INPUT/OUTPUT fluids are unchanged.
