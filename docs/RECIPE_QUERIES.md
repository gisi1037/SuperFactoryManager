# Read-only recipe inspection

`recipes(outputKey, map)` searches the shared server catalog. `recipe(reference)`
reads one candidate's detailed requirements. These calls do not execute recipes,
choose a machine or plan transfers.

The first qualified GT maps are `gt.recipe.macerator`, `gt.recipe.metalbender` and
`gt.recipe.cuttingsaw`. Other maps and unknown requirements remain explicitly
unsupported. Qualified loaded-machine inspection is described below; optional NEI
selection remains separate work.

## Inspect a connected machine

`recipemaps(label("machine"))` returns `status`, `current` and `available`.
The first qualified family is the stock GT single-block `MTEBasicMachineWithRecipe`
using one of the three maps above. `current` is its current map ID and `available`
is the native advertised map list (a singleton for this family). Subclasses,
multiblocks, unknown versions/backends/maps and other machine families remain
unsupported. This observation does not prove voltage, structure, heat, cleanroom
or recipe-specific feasibility.

Require `status == "matches"` before using `current` with `recipes(output, current)`.
Missing/unloaded/disconnected targets report `unavailable`; multiple distinct
machines report `ambiguous`, even when they use the same map. `not_ready` means no
active recipe session, and `stale` means the session changed during the call.
All unsuccessful results have an empty current ID and available array. Use a
whole-machine label without side/slot restrictions. Each call observes live loaded
tile/meta-tile/map identities; no chunk is forced and no provider is retained.

## Exact output keys

Use `itemkey("registry:id", metadata)` for an exact null-NBT identity. Omitted
metadata means zero. This constructor does not consult registries. For exact
NBT-bearing output identities, use a captured `contents()[i].item` instead.
`item("registry:id")` is a broader selector and is not accepted by `recipes`.

## Candidate summaries

Search results have `status`, `coverageComplete`, `ambiguous` and `recipes`.
The last field is a dynamic array of summaries, each containing:

- `reference`: session/revision/adapter/map/fingerprint inspection reference.
- `eut`, `duration` (ticks), `amperage`, `hidden`, `needsEmptyOutput`.
- `itemInputCount`, `itemOutputCount`, `fluidInputCount`, `fluidOutputCount`.
- `unsupportedCount`.

No candidate is selected automatically. Check coverage and ambiguity before
choosing one. Summaries let a program inspect a large ambiguous result without
materializing every ingredient list.

## Detailed requirements

`recipe(summary.reference)` returns the same four outer result fields, with zero
or one detailed row in `recipes`. Each row contains costs and flags as above,
plus `itemInputs`, `itemOutputs`, `fluidInputs`, `fluidOutputs` and
`unsupportedRequirements`.

Item parts expose `slot`, `item`, `amount`, `chance`, `matching`, `nbtSensitive`,
`ore`, `alternatives`, `displayAlternatives`. Fluid parts expose `slot`, `fluid`,
`amount`, `chance` and `alternatives`; each fluid alternative has its own `fluid`
and `amount`.

Slots preserve their native indices and order. Input amount zero denotes a
required nonconsumed catalyst. All chances use a denominator of 10000, including
input consumption chances. Fluid amounts are millibuckets. Matching policies are
`exact`, `gt_native_item`, `ore_or_explicit` and `gt_native_fluid`; the fluid input
policy is implicit in a fluid requirement. Display alternatives do not become
execution predicates. Representative keys do not replace ore, wildcard or native
NBT matching rules. Expected yields are not guaranteed production.

## Status and lifetime

Statuses are lowercase: `matches`, `no_match`, `unsupported`, `not_ready`,
`stale`, `limit_exceeded`. `no_match` means a complete search found no candidate.
An unsupported result can still carry inspectable candidates; it does not prove
absence or uniqueness.

Searches capture references in the same admitted catalog observation. Rendering
that immutable observation makes no further catalog/provider calls. If the
catalog is replaced afterward, the old observation stays intact and resolving
its reference reports `stale`. References grant no execution/source authority.

The catalog bounds candidate count and shared per-tick work. Per-invocation
expression/query budgets also apply. Views preflight cells, text and retained
identity bytes before allocating rows; an oversized view faults without returning
a partial result. Query functions need runtime and cannot initialize STATE.

Query inside a trigger when you need a fresh observation. A program-wide LET
initializer runs once; it does not automatically refresh a `not_ready` result
captured before the catalog becomes ready.

## Operator checks

`/sfmprofile recipes` reports readiness and cold-build statistics.
`/sfmprofile recipes <map> <item-id> <metadata> [candidate]` previews an exact
null-NBT query through the index, with at most two entries per input/output group.
`/sfmprofile recipecheck <map> <item-id> <metadata> [candidate]` also exercises
fixed read-only SFML expressions through the compiler/runtime and real provider,
comparing the result with that index. These commands require operator permission.

`/sfmprofile recipemachine <x> <y> <z>` directly inspects an already-loaded native
tile in the command sender's dimension. It uses the same qualified adapter without
building a cable network or changing a machine. The script function additionally
requires a connected label and checks its manager/disk identity.
