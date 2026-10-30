# Reference loading: performance and limits

The reference loader uses a server-owned FIFO waiting registry rather than pausing
every manager rejected by a full storage worker. Continuously valid enrolled
requests keep FIFO order. Unregistered overflow requests retry after a staggered
20-39 world ticks; their ordering is best-effort under sustained churn. Pause
intent is retained during both kinds of waiting. Invalid grants/bundles and
activation failures still pause and require explicit Start after repair.

Current provisional bounds are 4,096 waiting entries, 64 registration attempts per
server callback drain, eight authority-fence inspections/starts per drain, eight
outstanding cold jobs and 24 outstanding authoring jobs. Outstanding jobs include
completed results awaiting server callbacks. The service drains at most eight
callbacks each server tick. These count limits are not hard CPU-time guarantees:
compilation, STATE activation and external handlers cannot be preempted midway.

Waiting cancellation removes its entry immediately. Submitted cancellation keeps
its worker slot until actual completion drains, while releasing live manager
references. World closure clears all waiting authority. Warm execution does not
enroll, compile, copy NBT or read files. Callback failures are reported after
bounded drainage/admission so a failed UI callback cannot starve cold loading.

## Measurements

Baseline: public source `7c7bdcb`. Candidate: fair admission, explicit reference
STATE reset, and the policy-specific lexer DFA cache. Tests run on Java 17.0.21
with a 512 MiB heap and 28 available logical processors; Gradle uses JDK 25 to build
Java 8 compatible code. Two fresh JVMs use seeds 1037/1038 and shuffled case order.

The headless matrix uses real temporary-world grant/bundle files and private
freshly compiled Programs, with a fake Minecraft world. Sizes are 32/128/512 with
small state-free or complex stateful programs; 128-manager cases also use unique
builds. Complex programs contain 32 three-local helper functions and one persistent
counter, around 3.5 KiB source. Setup, publication and four compiler priming loads
are excluded. Cold means no installed plan, not an OS page-cache flush. Polling
requests 1 ms sleeps, whose actual Windows pacing varies; logical callback ticks
are recorded separately. These are not 20 TPS game or whole-pack throughput results.

|512-manager repeated complex case|Baseline|Candidate|
|---|---:|---:|
|Initial burst completed without manual Run retries|32/512|512/512|
|Equal-work completed loads|512|512|
|Admission offers including test retries|112,656-118,693|960|
|Equal-work wall time|8.79-9.27 s|1.18-1.20 s|
|Worker cumulative allocated bytes|30.53 GB|0.608-0.611 GB|
|Recorded GC collection time during cold section|135-136 ms|11-12 ms|
|Server cohort-frame p99|0.77-0.92 ms|0.51-0.78 ms|

Every successful load owns a distinct mutable Program; all 512 loads complete in
both equal-work rows. The baseline's test explicitly presses Run on busy-latched
managers to finish the cohort. Its rejection/retry volume is a stress-harness
cost, not a claim that normal baseline managers silently retried that often.
Do not report its incomplete initial burst as a speedup. The frame tails overlap;
first-case/JIT spikes elsewhere in the matrix exceed these 512-manager figures.
No general server tick budget or tail-latency improvement is established.

A separate 256-load/32-author-request contention case completes the same work.
Baseline author-request p99 was 4.833 s; FIFO/reservations with the original lexer
gave 146 ms; the final cache/admission candidate gave 66 ms in the first run. These
single-case latency figures are diagnostic and need real multiplayer qualification.

The lexer fix accounts for roughly 98% less cumulative compiler-worker allocation
in this complex case. A separate Java Flight Recorder profile attributes most
baseline sampled worker allocation to LexerATNSimulator start closures. The
leading UNUSED semantic guard prevented caching that closure for every token.
Separate strict/highlighting automata specialize only that constant guard. Token
rules, compiler diagnostics and strict malformed-input recovery are unchanged;
differential tests compare complete tokens/errors, Unicode, resets and flag
changes between nextToken calls. Mid-match flag mutation is unsupported.

The registry initially started only two jobs per tick. After the lexer fix this
pacing dominated completion: the 512-complex case took 3.98 s with those settings.
Eight starts retain the eight-job cold cap and editor reservation while reducing
that artificial backlog. This is a measured count-limit choice, not a hard-time
budget. Every tick still revalidates actual world/tile/disk/STATE authority.

## Reproduce

Set `SFM_REFERENCE_BENCHMARK_OUTPUT` to a writable JSON path and optionally
`SFM_REFERENCE_BENCHMARK_LABEL`, then run:

```powershell
./scripts/gradle.ps1 test --tests ca.teamdman.sfm.audit.ReferenceLoadingCharacterizationTest -PauditBenchmark=true -PcharacterizationSeed=1037
```

Repeat in a fresh invocation with seed 1038. The separate contention test is
`ReferenceAuthoringLatencyCharacterizationTest`; it uses
`SFM_REFERENCE_AUTHORING_BENCHMARK_OUTPUT` and
`SFM_REFERENCE_AUTHORING_BENCHMARK_LABEL`. JSON contains per-case useful-work
counts, raw latency samples, allocation/GC data and explicit limitations. Keep
diagnostic timings out of ordinary regression assertions.

## Remaining bottlenecks and gates

A subsequent [program performance pass](PROGRAM_PERFORMANCE.md) reduces warm
allocation by materializing transfer collections only when used, and separately
reuses identifier regex patterns during compilation. It does not pool contexts. Expensive STATE activation and many distinct
large programs need dedicated profiles. Cache only lexer automata, never mutable
Programs or unchecked file contents. Preserve actual grant/bundle verification
on every cold activation. Cooperative real two-client reference flows and clean
reloads subsequently passed; see [qualification scope](MULTIPLAYER_QUALIFICATION.md). Adversarial
request/session races, chunk churn, hardware input and provider/recovery
qualification remain open for full runtime qualification. The subsequent UI
redesign enables normal Imprint disk with the existing server transaction checks. Statement/graph tracing remains separately planned.


For a comparable untouched-original-versus-current score, use the separate
[overall benchmark design](OVERALL_BENCHMARK.md). Do not combine the intermediate
percentages in this report into a cumulative speedup.
