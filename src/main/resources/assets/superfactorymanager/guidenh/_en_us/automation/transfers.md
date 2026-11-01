---
navigation:
  title: "Value-driven item transfers"
  parent: /automation/index.md
  position: 50
  keywords: ["sfm", "transfer", "moved", "remaining", "status"]
categories: ["sfm", "automation"]
updated: "2026-11-01"
---
# Value-driven item transfers

TRANSFER is a standalone item-moving statement. It takes a source LABEL_REF and a destination LABEL_REF, with optional item selector and amount:

```sfml
name "Transfer shorthand forms"
every 20 ticks do
  let $source = label("inputs")
  let $destination = label("assembly")
  -- One selected request. Replace it with another form from the table below.
  transfer($source, $destination, item("minecraft:iron_ingot"), 16) into $result
  print($result.moved, $result.remaining, $result.status)
end
```

**Setup:** label a source chest `inputs` and an empty chest `assembly`; put
twenty iron ingots in the source. Open **Manager > Terminal** and enable
**PRINT capture** before starting to see the result fields.
**Expected:** the first run moves sixteen,
leaving four. The next run moves the remaining four and reports `partial`.

Choose one form per request:

| Form | Request |
| --- | --- |
| `transfer(source, destination)` | All currently visible items. |
| `transfer(source, destination, 16)` | Up to sixteen items across all variants. |
| `transfer(source, destination, item("minecraft:iron_ingot"))` | All visible iron ingots. |
| `transfer(source, destination, item("minecraft:iron_ingot"), 16)` | Up to sixteen iron ingots. |

Two arguments select all items. A third integer limits the total item count, equivalent to `TRANSFER(src,dest,ITEM("*"),amount)`. A third ITEM_KEY or RESOURCE_SELECTOR instead selects all matching stock. An omitted amount snapshots matching visible source stock across the selected group/faces/slots; extraction restrictions can prevent moving some of it. This makes one bounded attempt, never waits for destination capacity, and moves items only. Fluids use separate [TRANSFERF and fluid queries](/automation/fluid-methods.md), or legacy INPUT/OUTPUT with `fluid:*`. Fluid quantities are millibuckets (1000 mB = one bucket). Later statements observe stock remaining after earlier transfers.

The four-argument form explicitly supplies a nonnegative integer maximum:

```sfml
NAME "Maintain assembly stock"
EVERY 20 TICKS DO
  LET $source = LABEL("inputs").side("up")
  LET $destination = LABEL("assembly").slots(0, 8)
  LET $item = ITEM("minecraft:iron_ingot")
  LET $missing = SHORTAGE(32, $destination.count($item))
  TRANSFER ($source, $destination, $item, $missing) INTO $result
  PRINT($result.moved, $result.remaining, $result.status)
END
```

INTO is optional in every form. When supplied it declares a fresh local record. Its integer fields are `requested`, `moved` and `remaining`; `status` is a string. Remaining always equals requested minus moved. Omitting INTO discards the result while preserving all access, recovery and work checks. Use `$result.moved` to test actual movement instead of treating an attempted statement as success.

Automatic amounts report the real observed stock as requested. If endpoint preflight is unavailable before completing that observation, requested and remaining are zero with status `"unavailable"`; this does not prove the source is empty. An available empty source completes a zero request.

| Status string | Meaning |
| --- | --- |
| `"complete"` | The whole requested amount moved, including a zero request. |
| `"partial"` | Some moved, but less than requested. |
| `"no_match"` | No extractable matching source was found. |
| `"no_capacity"` | Extractable matching source stock made no progress into eligible output slots. |
| `"unavailable"` | Supported loaded endpoint access could not be established. |

Provider exceptions, exhausted work budgets and recovery uncertainty fault execution. They are not normal status values. If a later operation faults, earlier confirmed transfers remain committed. A partial result may reflect capacity, supply, refusal or a later access change; it is not a promise that every endpoint was available throughout.

The amount is one aggregate cap across the matching variants and containers in the request. It is not multiplied by blocks or resources. Negative amounts fault. An explicit zero request completes without accessing native providers; an automatic zero is known only after preflight. Arguments evaluate once from left to right. One shared input label can contain several source containers; no origin array is required to pull from the group.

ITEM selects all metadata/NBT variants of an exact item ID; ORE selects an ore category. An ITEM_KEY obtained from [contents](/automation/contents.md) selects one exact metadata/NBT variant:

```sfml
NAME "Move one exact variant per group"
EVERY 20 TICKS DO
  LET $source = LABEL("inputs")
  LET $stock = $source.contents()
  FOR $group IN $stock DO
    TRANSFER ($source, LABEL("dest"), $group.item, MIN($group.total, 4)) INTO $sent
    PRINT($group.id, $sent.moved)
  END
END
```

Snapshots are advisory. Each request resolves current network membership, loaded providers, side/slot visibility and identity, then rechecks around provider callbacks. The initial provider scope is direct inventories and canonical vanilla chest pairs; unqualified proxy adapters are unavailable. This statement moves items only. Use [TRANSFERF](/automation/fluid-methods.md) or ordinary INPUT/OUTPUT for supported Forge fluid routing.

A physical slot selected as a source cannot also be an output during that request, even if it was empty at the start. This prevents inserted items feeding later source reads and inflating moved totals. Disjoint selected slot ranges within one inventory can participate. Overlapping labels do not waive this exclusion.

TRANSFER is self-contained. It neither needs nor consumes active INPUT declarations. INPUT allowances, OUTPUT caps and RETAIN values do not apply to it. Specify the desired amount and side/slot restrictions on each request; use SHORTAGE or another calculation to express a stock target.

This is not an atomic transaction across an entire label group. Accepted pairs use simulation, extraction, insertion and rollback/recovery handling. Work needed to finish recovery is reserved before extraction. See [recovery](/reference/recovery.md) for blocked uncertain resources.

The provisional limit is 64 potentially mutating transfer actions per manager invocation, shared across loops and all due triggers. Automatic-amount calls consume credit even when their observed stock is zero; explicit four-argument zero requests are exempt. Shared query/expression work can run out sooner, and one external provider callback cannot be interrupted. Nothing is silently truncated to fit a budget.

Visual has a Transfer node for the two-, three- and four-argument forms without INTO. Its expressions can use self-contained LABEL/ITEM constructors, literals and receiver methods; variables in scope, current loops and known built-ins are supported too. Player functions, result capture and other unsupported constructs still require Code. See [editor limits](/advanced/editors.md). TRANSFER is allowed in triggers and void procedures. It cannot appear inside a STATE default, predicate, ordinary call argument or value-returning function body. Void procedures compose explicit effects and can be called only as statements.
