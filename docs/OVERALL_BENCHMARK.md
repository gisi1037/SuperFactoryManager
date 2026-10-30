# One comparable original-port performance benchmark

Design status: draft awaiting a portable harness and correctness preflight. No
original-relative overall score has been measured. The proposed public manifest
is [common-factory-trace-v1.json](../benchmarks/common-factory-trace-v1.json).

Use one named, fixed-work playlist, `sfm-common-factory-trace-v1`, against the
unchanged original port `9037f2d10f214794be0abba8056631be346f5216` and current
`8337f090e837bf3fddd7bcbac519d143268ae76b`. Later current revisions can repeat the
same frozen profile. Publish one overall result plus domain/case breakdowns.

## What the overall number means

For each revision, add the elapsed time of every measured engine API batch in the
playlist. For each independent paired run:

`suite speedup = original total timed engine work / current total timed engine work`

A result of 2.0 means this precise playlist completes its checked engine work in
half the time. It does not mean every factory or the whole modpack doubles its TPS.
Take the median ratio across five fresh paired JVM runs and show every pair plus
an uncertainty interval resampled across whole pairs, not individual frames.
Record the two absolute totals and the raw case distributions alongside the ratio.

The playlist's frozen operation counts define its compile/runtime/topology mix.
Do not sum arbitrary existing benchmark batches after seeing their results, change
weights to favor improvements, or add/multiply earlier unrelated percentages.
Alternative factory profiles need new names/manifests and separate results.

## Proposed common-feature playlist (20 cases)

| Area | Workload | Fixed measured work |
| --- | --- | --- |
| Compilation | Short full-drain transfer | 100 fresh builds |
| Compilation | HAS true/false branch program | 100 fresh builds |
| Compilation | Flat 32-trigger legacy program | 50 fresh builds |
| Runtime | Active, blocked with witness, empty with witness, sparse source | 16 managers x128 updates per case |
| Runtime | HAS true and false, many unused labels | 16 managers x128 updates per case |
| Runtime | Not due, paused, no disk, manager redstone pulses | 16 managers x128 updates per case |
| Lifecycle | Fresh diskless manager first update, 32/512 cables | 64 fresh managers per case |
| Topology | Fresh 32- and 512-cable networks | 64 constructions per case |
| Topology | Separate and shared periodic maintenance | 16 network views x420 logical ticks per case |

The JSON records exact source and its UTF-8 SHA256, inventory sizes, fixed stocks,
expected moved quantities, pulse input cycle, counts, seeds and provisional JVM
settings. Before scoring, finish and freeze fixture geometry, provider behavior,
resolved dependency/toolchain hashes and the test-only adapter/harness digest.
The design defaults must not be described as a completed frozen experiment.

The original supports full-drain INPUT/OUTPUT, legacy HAS, timers and manager
redstone pulse triggers. Use identical source in both arms. New STATE, functions,
loops, native queries/redstone ports and reference publication are measured in a
separate current-feature suite, without an original-relative score.

## Correctness comes before scoring

Every scored case must pass on both revisions before and during measurement.
Check complete item/metadata/NBT identities, conservation, exact destination slots,
visible statement order, due ticks and continued witness work after a rejected or
empty primary transfer. Check exact network membership and no forced chunk loads.
Count requested updates, due cycles and completed useful work. Probe/read counts
may legitimately differ when an optimization removes redundant work.

Known original defects include ignored OUTPUT quantities and clearing INPUT after
each OUTPUT. Full-drain cases use a fresh INPUT for each independent transfer;
they do not pretend those defects are fixed. Keep quantity/multiple-OUTPUT/partial-
FORGET/retention/provider-change/recovery/alias cases as visible unscored
compatibility diagnostics. The overall score applies only to the declared common
workloads, not all original features.

If a selected scored case crashes, times out or produces the wrong result on either
revision, mark the suite unqualified and retain its diagnostics. Publish no qualified
overall speedup. Do not patch the original, drop the failed case or silently
renormalize the score after looking at timings. A revised playlist needs a new
version and a documented pre-measurement reason.

## Runner and measurement boundaries

Build original and current in separate isolated checkouts/JVMs/classpaths, keeping
original production code and checked-in generated lexer unchanged. Their common
ProgramBuilder, ProgramContext, manager and CableNetwork entry points permit a
small self-contained test-only adapter. Do not copy current RecoveryFixtures or
modern audit classes wholesale into the original. Lifecycle cases L1/L2 time each
fresh diskless manager's complete first update, including its initial network
rebuild. Preserve the original's constructor scan plus explicit second scan;
never inject a prebuilt network for these rows. Fresh-object warmup uses separate
managers. Direct CableNetwork construction rows measure a different API boundary.
Current diskless managers may legitimately leave the network absent: do not
force discovery in an assertion. Verify membership only for an existing network,
and check unchanged inventories/clock and every completed update in both arms.
Avoiding unnecessary discovery is part of this lifecycle cost. Cold here means
fresh objects, not a flushed OS cache or an uninitialized JVM.

Time complete manager updates externally. The original internal timer measured
only Program.tick, whereas later instrumentation includes more work. Comparing
those counters would be invalid. Time whole compiler/network calls similarly;
perform reflection, construction where excluded, stock refills and result checks
outside timers. A compiler batch must still produce fresh independent Programs.

Use the same machine, Java17 build, heap/GC settings, providers, optional mods,
logging settings and loaded fake-world geometry in both arms. Build with the
pinned JDK25/Jabel Java8 target. Archive resolved Maven/mapping dependencies and
artifact checksums; the original build admits mavenLocal and remote inputs.

Use five fresh A/B pairs with seeds1037-1041 and balanced randomized revision order.
Shuffle case order identically within each pair. Separate declared warmup from
measurement, never grant the faster arm different work, and do not force GC.
Keep JFR/tracing in separate diagnostic runs. Record allocation, GC, p50/p95/p99,
maximum latency, wall time, all raw samples and useful-work counts. Allocation
and tail latency remain separate metrics; lower allocation is not a timing score.

This primary metric sums timed engine segments and includes GC pauses occurring
inside them. Report whole-process elapsed time and whole-run GC separately because
setup/assertion work outside segments can affect collection placement. Do not
subtract a noisy control or include deliberate 20-TPS sleeping in engine time.

Start with the explicitly labeled common-engine synthetic suite. Then qualify
the portable workloads in the pinned full pack, timing normal SFM work and server
tick tails separately. If the unchanged original cannot start or pass a pack case,
record that comparison as unavailable. A headless score is not a substitute for
a measured whole-pack speedup.

## Delivery order

1. The separate cooperative two-player reference workflow qualification has
   passed; keep adversarial packet, hardware input and provider/churn gates open.
   See [the qualification scope](MULTIPLAYER_QUALIFICATION.md).
2. Implement portable fixtures/adapters; run correctness only against both commits.
3. Review the proposed playlist and freeze every input/hash/count before timing.
4. Run fresh paired measurements, validate reports and publish the overall ratio
   with its breakdown and explicit coverage limits.
5. Keep the frozen profile for future comparisons; add modern-feature workloads
   and real-pack qualification as separate named reports.
