# Cooperative two-player reference workflow qualification

Checked 2026-10-29 against public source
`8337f090e837bf3fddd7bcbac519d143268ae76b` using one dedicated daily-803 server and
two actual clients with separate offline QA identities on loopback. Java17,
LWJGL3, MUI2, NEI and Dynamic Inventory were present. The server heap was 4 GiB;
each client had 3 GiB. The normal production SFM JAR was identical on all three
participants, SHA256
`217f5da4ea3590c81d21191376f5be315a14f33bdf2b2ebcec14a00957dd1764`.
Its version metadata predates the final source commit; the build contains no
performance-fixture classes. The reference-write UI was explicitly enabled only
in the two disposable test clients.

## Passed workflows

- A published an exact saved revision and wrote its reference to a physical disk.
  B's freshly fetched private project catalog remained separate.
- B took A's bearer disk through normal MUI2 inventory controls and installed it
  in another manager. Execution access did not reveal A's private source or add
  A's project to B's authoring catalog; reference source editing stayed blocked.
- Both viewers observed Start, Pause and confirmed STATE reset consistently.
  Counts stayed stable while paused, and reset returned the counter to zero.
- A literal creative copy of a nondefault disk started with the same counter.
  Running the original and resetting the copy demonstrated independent mutable
  execution state. Their immutable reference metadata remained identical.
- With B disconnected, A's controller continued executing. B reconnected and
  observed the authoritative values for both controllers.
- Both controllers survived a clean dedicated-server save and restart. Removing
  only their exact bundle while stopped caused visible cold-load failure with
  both paused. Saved STATE, scheduler clocks and reference metadata survived.
- Restoring that exact bundle did not silently start the other controller.
  Explicit Start retried successfully. The original resumed from count93 to95,
  exactly matching the two newly due ticks at scheduler ticks1905 through1944.
  Resetting the copy left the original unchanged.
- A final ordinary restart with storage present restored compiled, paused
  controllers at counts95 and0. Their saved STATE envelopes, clocks and reference
  metadata matched the preceding clean shutdown exactly.

The fixture used a small persistent counter, not item transfers. Test services
were shut down normally after qualification; temporary settings and the exact
bundle were restored. The user's original world was saved normally before the
test, and its main client was reopened with its existing mod JARs.

## Remaining qualification

These are cooperative native UI flows, not an adversarial packet-replay test.
Stale control-token replay, pending-write/session races, simultaneous toggle
intent, hardware/IME input, nonempty recovery/label preservation, third-party
item/fluid providers and chunk churn remain separate coverage. Existing source
and regression tests are not substitutes for those native scenarios.

The default reference-write UI gate remains disabled. No production behavior
was changed for this qualification. A functional two-player pass establishes
neither a performance speedup nor a whole-pack tick-latency budget.

The new [overall benchmark design](OVERALL_BENCHMARK.md) defines the separate
original-versus-current performance comparison; it has not been scored yet.
