# Compilation and runtime program measurements

The opt-in `ProgramDiversityCharacterizationTest` separates fresh compilation from
already-compiled manager execution. Eight public fixtures cover a persistent
counter, arithmetic/branches, bounded FOR/WHILE loops, arrays/struct copies and
foreach, nested player functions, item availability/empty/count queries, native
redstone query/output, and a counter whose 1,200-tick trigger is not yet due.
The helpers in the function fixture actually execute; the larger 32-helper cold
loading fixture separately measures compilation of a larger program.

Each runtime cohort has 16 managers with private Programs, disks and STATE stores.
Every frame verifies due-cycle counts, exact results, unchanged item contents,
redstone reads/writes/sides and no warm STATE encoding. The not-due case verifies
zero trigger cycles rather than reporting skipped work as increased throughput.
Full `updateEntity` calls are timed, including context creation and ordinary small
network maintenance. Setup, compilation, activation and assertions are excluded.

Measurements use two fresh Java 17.0.21 JVMs per arm, 512 MiB heaps, 28 available
logical processors and shuffled orders with seeds 1037/1038. Compilation has four
warmups and 24 samples; runtime has 16 warmup frames and 64 measured cohort frames.
DFA/JIT caches may be warm during fresh compilation. GC deltas include untimed
checks. These loaded fake-world/peripheral fixtures do not establish pack TPS,
hardware/provider qualification or a hard tick budget. Raw samples and completed
work counts are emitted as JSON; no machine-dependent timing assertion is used.

## Confirmed costs and changes

A separate diagnostic JFR run attributed most counter warm allocation to eager
`ProgramContext` transfer collections. Data-only, not-due and redstone invocations
constructed item/fluid identity maps they never used. Those collections now begin
as immutable empty sentinels and become private mutable collections on first use.
Materialized storage is retained across FORGET within its context; fresh contexts
still get independent quotas and maps. There is no context pool, shared mutable
plan or cached external inventory contents.

Compilation profiling also found repeated `String.matches` calls compiling the
same identifier/name regexes. Two immutable Pattern instances now use private
matchers. Qualified-name prefix extraction uses a literal dot search instead of
splitting with another regex. The accepted patterns and errors are preserved.

Baseline is `6183f7b`, which already includes the lexer cache described in
[reference loading](REFERENCE_PERFORMANCE.md). The candidate additionally applies
lazy collections and identifier-pattern reuse. Reductions below compare median
cumulative allocated bytes per sample across the two runs. A runtime sample is
16 manager updates, not a single instruction or server tick.

| Program | Compilation allocation reduction | Warm allocation reduction |
| --- | ---: | ---: |
| Counter | 23.6-24.1% | 82.1-83.9% |
| Arithmetic and branches | 31.4% | 78.2-81.2% |
| Bounded loops | 29.7% | 56.2-57.2% |
| Arrays and structs | 22.9-23.5% | 70.7-71.3% |
| Nested functions | 28.3% | 70.2-71.8% |
| Item queries | 22.3% | 33.8-37.3% |
| Redstone | 21.0% | 44.6-45.0% |
| Trigger not due | 24.1% | 90.7% |

All arms complete the same work and produce the same checked results. Latency
tails vary: several candidate p99 values improve, others regress, and this short
matrix does not establish a general latency speedup. The reported improvement is
allocation reduction. The earlier roughly 98% compiler-worker allocation reduction
uses a different pre-lexer baseline and complex cold-load workload; it is not a
98% runtime speedup and must not be added to these percentages.

## Transfers and remaining bottlenecks

The existing `PerformanceCharacterizationTest` adds 13 active/blocked/empty/sparse/
rule-rejected transfer cases, with 54/1,000 source slots, 27/1,000 destination slots,
and 1/20/200 output statements, plus four full 100-manager due/not-due cases with
two or 1,000 labels. Both pre/post arms pass exact conservation and work checks.
Lazy storage barely changes workloads that actually need transfer maps.

The blocked 1,000-by-1,000 slot case still performs 1,000,000 insertion-rule probes,
allocates about 40.6 MB per attempt and had a roughly 54 ms baseline median in one
run. This is a real synthetic worst-case bottleneck, not a normal chest estimate.
A separate transfer JFR profile attributes most sampled allocation to per-pair
ItemStack copies. Removing those copies or reusing provider results blindly could
break mutable callback isolation, item/NBT identity, sided rules and recovery.

Next transfer work should reduce the necessary pair search with explicit provider
qualification, retaining ordered transfer/retention/EACH semantics and commit-time
checks. A proposed event-assisted negative-availability cache can avoid repeated
failed probes between changes for qualified inventories. Unqualified handlers
must retain normal polling. Test notifications, direct stack mutation, topology/
label/program changes and missed events against exact due-tick transfer results;
do not skip triggers or claim full event support for arbitrary inventories.
No event-driven availability implementation is included in this checkpoint.

Parser/token creation, residual data/query allocations, large NBT, fluid and
third-party handlers, forced topology rebuilds and real-pack churn remain profiling
targets. The short optimized JFR language run has too few CPU samples to rank
those sites reliably; its stack samples are diagnostic leads, not budgets.

## Reproduce

Set `SFM_PROGRAM_BENCHMARK_OUTPUT` to a writable JSON filename and optionally
`SFM_PROGRAM_BENCHMARK_LABEL`, then run from the repository:

```powershell
./scripts/gradle.ps1 test --tests ca.teamdman.sfm.audit.ProgramDiversityCharacterizationTest -PauditBenchmark=true -PcharacterizationSeed=1037
```

Repeat with seed 1038 in a fresh invocation. Optional bounded environment settings
are `SFM_PROGRAM_BENCHMARK_COMPILE_SAMPLES` (8-128; default 24) and
`SFM_PROGRAM_BENCHMARK_RUNTIME_SAMPLES` (16-256; default 64). Keep profiler runs
separate from timing comparisons. Existing transfer/full-manager workloads are
`PerformanceCharacterizationTest.transferWork` and `.completeManagerWork` under
the same opt-in property; they emit raw samples and checked useful-work counts.


The separate [overall benchmark design](OVERALL_BENCHMARK.md) defines a common-
feature original-port comparison. These newer-feature measurements stay in their
own track and do not establish a cumulative speedup from the original port.

## Native query allocation reduction (2026-10-31)

Availability checks no longer construct boxed slot sets. Nonsided inventories
validate their selected range directly; sided inventories still visit every
selected face and raw slot entry. All physical inventory members remain subject
to connectivity, loaded-state and error checks. Upfront range/raw-array budget
bounds remain, so an absurd inventory size still fails before stock reads.

Nonsided contents queries now read the selected contiguous range directly in
ascending order, removing the boxed set, copied list and sort. Detached keys,
grouping, origins and live read/registry callback order are preserved. Count and
empty queries retain their existing iteration and full validation; there is no
new contents cache, early return on stock, or skipped later-provider validation.
Fluid tank snapshots pre-size their list after validating the 256-tank bound;
every tank and side-consistency check remains. That fluid change was not timed.

Fixed-work headless measurements compare public baseline `e184e3c` with these
query changes on JDK 26.0.2.1, 512 MiB heaps, multiplier 16, two fresh JVMs per
cohort, 40 warmups and 120 measured samples per workload/JVM. Setup and semantic
assertions are outside timing; same-thread allocated bytes cover the timed work.
Each sample checks completion, totals and origin order. Transfers serve as an
unchanged control, with identical class bytes and identity/order/conservation
checks. These measurements exercise fake loaded worlds and inventory providers.

| Fixed completed workload | Allocated bytes, baseline → final | Reduction |
| --- | ---: | ---: |
| 10 availability queries, 4,096 nonsided slots | 2,608,064 → 5,504 | 99.79% |
| 4 contents queries, 256 slots and four groups | 278,080 → 208,992 | 24.84% |
| Unchanged transfer control, 54 distinct stacks / 108 items | 696,544 → 696,544 | 0% |

Availability's pooled median falls from 0.30965 to 0.00530 ms for the ten-query
fixture. Contents falls from 0.12740 to 0.09495 ms, but latency is noisy: the
byte-identical transfer control varies from 0.29785 to 0.38170 ms between cohorts.
The reproducible conclusions are reduced query allocation and the large
availability improvement; there is no claimed general contents/transfer speedup,
full-pack tick-time result, TPS gain, or cumulative original-port speedup.
