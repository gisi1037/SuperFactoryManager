# Default SFML functions and logistics foundations

Draft v1, 2026-10-28. The status column distinguishes existing APIs, this candidate
and planned work. Planned spellings below are illustrative, not accepted syntax.
Default primitives belong in the Java engine; higher-level reusable helpers can
ship as versioned standard libraries after A4's library/build contracts land.

| Group | Default functions | Status |
| --- | --- | --- |
| Arithmetic | MIN(a,b), MAX(a,b), CLAMP(value,low,high), ABS(value) | This candidate |
| Size | LEN(array), LEN(string), LEN(contents/locations) | This candidate; strings count Unicode code points |
| Handles | LABEL(literal), ITEM(literal), .side(literal), .slots(first,last) | Existing |
| Ore matching | ORE(literal) as RESOURCE_SELECTOR | This candidate; exact name only |
| Stock observations | .available([selector]), .empty([selector]), .count(selector) | Existing; access/visible stock, not transferable capacity |
| Signal observations | .redstone(), .powered(), .redstoneAvailable() | Existing |
| Diagnostics | PRINT(values...), terminal state/log commands | Existing |
| Assertions | ASSERT(condition,message), structured diagnostic fields | Planned |
| Strings | contains, startsWith, endsWith, find, slice, bounded split/join, explicit conversion | Planned; define Unicode/index rules before implementation |
| Collections | contains, find, bounded filter/map/append, aggregation helpers | Planned; preserve fixed-array semantics, no unbounded growth |
| Item identity | Exact registry ID + metadata + canonical NBT key; selector matching and ore membership inspection | ITEM_KEY candidate implemented; further selector/ore-inspection helpers planned |
| Inventory scans | .contents([selector]), exact grouped items and physical origins | Candidate implemented; direct inventories/chest pairs, headless qualification |
| Capacity/access | canInsert, canExtract, acceptedAmount, missing(targetStock) | Planned qualified simulations; full/empty are distinct from permission |
| Logistics | TOTAL(contents/locations), STOCK(contents,exactKey), SHORTAGE(required,available); TRANSFER(src,dest[,keyOrSelector[,amount]]) [INTO result] | Candidate; explicit statement action, further policies/helpers planned |
| Recipes | Find candidates by output+recipe map, inspect requirements/results, choose recipe, resolve machine maps | Planned server catalog |
| NEI authoring | Use selected recipe in station/editor, validate server descriptor, insert query/template | Planned optional client adapter |

## This candidate's primitive contracts

MIN/MAX accept exactly two signed 64-bit integers, CLAMP exactly three and ABS
one. CLAMP is inclusive and faults on low > high. ABS(Long.MIN_VALUE) faults
instead of wrapping. LEN accepts a fixed array, string or read-only contents/location sequence; it counts outer array
entries, actual sequence rows or Unicode code points, not grapheme clusters or UTF-8 bytes. It evaluates
its argument even when array length is known. Dynamic string scans reserve their
UTF-16 scan work before traversing, using the invocation's expression budget.

Calls evaluate arguments once, left to right. Built-in spellings ignore case;
existing exact-name player functions win in ordinary calls. Player functions
remain case-sensitive, unbounded recursion remains forbidden, and calls are not
folded as constants. A default name used in STATE cannot then be redefined by an
exact-name player function, so declaration ordering cannot silently change its
meaning. Pure built-ins may initialize STATE. No player call frames are allocated
for primitive built-ins; this is not a measured timing improvement.

ORE("ingotIron") is RESOURCE_SELECTOR, not a concrete stack or a separate
ore-dictionary value type. Exact ore names are case-sensitive, 1..256 ASCII letters,
digits or underscores. No wildcard/regex, colon prefix or dynamic string argument
is accepted. An unknown name matches no item and never creates an ore entry.
Membership is read afresh, including Forge wildcard metadata and later ore
registration. Membership work/copy units share the query budget. Forge's internal
callback/allocation cannot be interrupted by that budget. ORE is a reserved
constructor name; old ITEM and legacy #ore:/WITH TAG filters retain their contracts.

```sfml
NAME "Bound an ore stock count"
EVERY 20 TICKS DO
  LET $source = LABEL("stock")
  LET $iron = ORE("ingotIron")
  IF ($source.available()) THEN
    LET $batch = CLAMP($source.count($iron), 0, 64)
    PRINT("iron", $batch, "source count", LEN([1, 2, 3]))
  END
END
```

This is observation only. ITEM/ORE values cannot yet replace static resource
filters in INPUT/OUTPUT, and LABEL_REF values cannot yet replace their FROM/TO
grammar fields. Player-written functions still perform data/query work only.

## Implement the missing logistics foundations in this order

### L1: Exact immutable item identity

Implemented candidate: a bounded native key from registry ID, metadata and canonical NBT bytes.
Sort compound keys, preserve tag kinds/list and array order, and distinguish no
tag from an empty compound. Copy no mutable ItemStack, inventory or world into
the key. Validate depth/node/byte bounds before own copies/loops and charge shared
work/allocation budgets. Never use TransferQuota.ItemKey for this: it omits NBT.
Do not reuse STATE's narrower NBT codec for arbitrary item tags.

### L2: Bounded contents observations

Implemented candidate, pending full-pack/lifecycle qualification: read-only ITEM_CONTENTS / ITEM_LOCATIONS sequences and ITEM_KEY values.
They have homogeneous fixed element types and runtime lengths, including zero.
They are immutable and nonpersistable, also inside containers. Materialization
checks the native result and runtime share one query-budget owner; functions
share that budget. Locals reset between invocations. This is not a general Java
handle-expiration service or an authorization token for transfers. Existing fixed arrays remain fixed.
Indexing, LEN and FOR IN use one sequence contract; do not fabricate
256 padded rows or quietly make ordinary arrays growable.

An item group should expose item, id, metadata, total and locations. Each location
records dimension, physical block coordinates, physical local slot, amount and
the selected side mask. For double chests, logical combined-slot selection is
resolved to the correct physical half/local slot. Coordinate records are data,
not permission to mutate arbitrary blocks.

Scan each unique physical view once, deduplicate shared labels/chest aliases,
then group by exact key while collecting origins. Avoid a new inventory scan
for every discovered item and quadratic append-by-copy. Charge actual rows,
cells, key/NBT work and allocations before construction; cap per invocation and
service-wide retained data. Fail the whole observation on incomplete/unloaded/
unsupported views or exhausted bounds; return no misleading partial stock.
Never generate/load chunks for queries. Views remain visibility observations;
counted stock may still be nonextractable.

Candidate syntax, implemented and headless-tested:

```sfml
LET $stock = LABEL("storage").contents()
FOR $group IN $stock DO
  PRINT($group.id, $group.metadata, $group.total)
  FOR $where IN $group.locations DO
    PRINT($where.x, $where.y, $where.z, $where.slot, $where.amount)
  END
END
```

The sequence has its actual length, including zero. Groups expose `item: ITEM_KEY`,
`id: STRING`, `metadata: INTEGER`, `total: INTEGER`, `locations: ITEM_LOCATIONS`.
Locations expose integer dimension/x/y/z/slot/sideMask/amount. Exact keys support
`=` without registry/world reads; LABEL/RESOURCE_SELECTOR/collection equality is
unchanged. Key display omits NBT payload, so matching-looking printed summaries
are not proof of key equality. No public ITEM_KEY constructor or NBT inspector yet.

Provisional native caps are 256 groups/1024 locations; shared budgets can fail
earlier. Exact NBT is limited to depth 16, 1024 nodes and 8192 canonical bytes/key,
with all normal 1.7 tags, sorted compound names and ordered lists/arrays. Raw
floating-point bits are preserved, including distinct NaN payloads. Canonical
bytes are an internal identity format, not persisted vanilla NBT. Unsafe
subclasses/corrupt/replaced list layouts fail closed. MCP/SRG list reflection and
real-provider behavior still require production-pack qualification.

Materialized sequence cells are charged by real rows, not the opaque type's
single handle cell: 1+6*groups+8*locations, plus child-reference/string work and
native scan costs. Bounds protect resource use rather than promise acceptance
of every input below a headline cap. Large scopes need restricted views today;
coherent bounded paging/paced scans are future scale work. No silent truncation.

### L3: Value-driven transfer actions

Candidate syntax: `TRANSFER ($source, $destination[, $itemOrSelector[, $amount]])
[INTO $result]`. Missing item selects ITEM("*"); missing amount uses a finite
matching visible-stock snapshot. An optional INTO declares a fresh local
structural result; without INTO the result is discarded. Endpoint/access,
conservation/recovery and work bounds apply to every form. Automatic requests
consume action credit even for zero observed stock; explicit zero remains exempt.
The command is
absent from data expressions and current FUNCTION bodies. Future effectful
composition uses a separate ACTION/CALL boundary; no implicit effect upgrade.
See the guide for statuses, shared limits and conservative source-slot overlap
exclusion. Runtime/scale qualification remains required after headless checks.

Accept source/destination handles, exact ITEM_KEY or explicit broad selector and
requested amount. Retain an explicit distinction between read-only expression
functions and effects; do not turn arbitrary predicates/arguments into implicit
transfers or constant-fold action calls. Define how player action functions can
compose them before lifting the current pure/query-only function restriction.

Re-resolve network membership, loaded world/provider identity, sides, slots and
live stack identity at use. Reuse the existing simulation/extraction/insertion/
rollback/recovery path. A scan is not a reservation or an atomic transaction.
Report requested/moved/remaining/status, preserving partial-success behavior.
Keep deterministic source ordering initially; round-robin/priority policies are
explicit options. Two requests cannot spend the same observed count twice.

### L4: Logistics helpers

Candidate first slice: TOTAL over contents/locations, exact-key STOCK over one
captured contents view, and nonnegative SHORTAGE. They do not rescan providers;
TOTAL/STOCK are bounded linear walks, with checked aggregation and shared work.
STOCK has no broad-selector overload. More advanced feeding policies, qualified
capacity simulations, dynamic source sets and callable ACTION helpers remain open.

Expose find/group/stock/shortage operations over one captured scan and bounded
multi-source feeding by item+amount. Do not store live snapshots in STATE or
reuse them across manager invocations. Event-driven availability remains deferred.
Retained cross-run caches require separate invalidation qualification.

## R1-R4: Recipes and NEI integration

R1 introduces an authoritative server recipe catalog and immutable descriptors,
initially for a qualified subset of ordinary GT recipe maps. Output+map produces
candidates, not a guessed unique recipe. R2 resolves loaded machine modes/maps
with qualified server adapters. R3 adds NEI's optional station/editor selection
action. R4 adds shortage/batch/input planning and transfer helpers separately;
automatic recursive crafting is later work.

Illustrative operations: RECIPES.find(output,map,options), result status/count/at,
recipe input/output counts and indexed descriptors, machine.recipeMaps(), and
server validation of a selected NEI recipe. Names/types require final API review.
Machine map IDs are stable adapter identities, not localized display names or
an assumption that one machine means one map. Choose among differing recipes
explicitly. Distinguish no match, ambiguous, unsupported, not ready, stale and
limit exceeded; truncated results cannot prove absence or uniqueness.

Descriptors preserve item/fluid identities and amounts, ingredient alternatives,
ore predicates/NBT matching policies, consumed vs nonconsumed catalysts/tools,
input-consumption and output chances, byproducts, EU/t, duration, and known
special requirements (mode, heat, cleanroom, research, special slots, dimension).
Unknown constraints mean incomplete applicability. Ore equivalence/unification
does not make every representative interchangeable. Expected chance yields are
not guaranteed production. GT's [recipe-builder documentation](https://github.com/GTNewHorizons/GT5-Unofficial/blob/master/docs/RecipeBuilder.md)
describes metadata requirements; verify behavior against pinned artifacts.

Pinned local inspection: GT 5.09.54.250 has ordinary RecipeMap registries plus a
separate assembly-line registry; NEI 2.8.169 uses display handlers. Existing SFM
NEICompat only hides items. dependencies.gradle currently compiles NEI 2.8.139, so
qualify/update the optional API boundary before adding new runtime calls.
Never load NEI client classes from common/server execution. A client selection
sends a bounded descriptor/fingerprint; the server resolves it against its own
catalog. Client display stacks are never authoritative recipe requirements.

Build one shared immutable reverse-output index after a qualified recipe/script
lifecycle point, not getAllRecipes per manager tick. Key caches/tokens by server
session, catalog revision, adapter version, map and matching policy. Explicitly
invalidate/rebuild after supported mutation/reload; absent reliable hooks, expose
a documented startup snapshot and reject stale tokens. No universal durable GT
recipe ID was established. Persistent recipe pins need version/fingerprint checks.
Charge candidate visits, alternatives, NBT/copies and result work; bound query and
aggregate service work. Registry/world callbacks stay on the server thread.

## Required qualification

- Exact metadata/NBT grouping, null/empty NBT, all normal 1.7 tags, canonical key
  ordering, immutable inputs, oversized/deep/malformed tags and shared budgets.
- Empty/dynamic sequences, nested iteration/functions, wrong/expired invocation,
  duplicate labels/chests, sided slots, overflow, zero-sized/unavailable providers,
  and operation counts proving one scan rather than item-by-inventory scans.
- Provider mutation/replacement/unload between scan and move, refused/partial
  insertion, recovery, quotas/retention, competing requests and no duplication.
- Recipe ambiguity, ore alternatives, catalysts, probabilistic inputs/outputs,
  fluids, special/dynamic backends, hidden/fake entries, mode changes, stale
  catalogs, hostile descriptors, no optional mod and dedicated classloading.
- Correctness-equivalent scaling: total/tail scan and transfer time, calls,
  allocations/GC, snapshot/index memory, index rebuild and concurrent authoring.
  Caps alone are not a tick-time guarantee or a numerical performance claim.
