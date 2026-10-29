# Operator item recovery

Item recovery is available through an operator-level server command. It runs on
loaded managers in the command sender's current dimension and leaves the manager
paused. It never loads a missing chunk or returns items to a source automatically.

```text
/sfmrecovery x y z inspect
/sfmrecovery x y z claim <recoveryId> <direction>
/sfmrecovery x y z resolve-present <recoveryId>
```

1. Inspect the paused manager. The response shows the record ID, ownership state,
   item ID/count, source, failure stage and destination when available. Inspect
   assigns an ID to older version-1 records that lack one; the payload is retained.
2. For **pending**, place a suitable inventory next to the manager and claim into
   it using an absolute direction: north, south, east, west, up or down. The
   command obeys the destination's slots, faces, item rules and capacity. For
   example, `west` enters the west neighbor through that inventory's east face.
   Full or rejecting inventories move zero. Partial capacity leaves the known
   remainder on the disk; repeat with the same ID after making room. The original
   source can be missing or replaced. Other managers cannot receive a claim.
3. For **uncertain**, inspect the actual source and destination and reconcile the
   attempted transfer manually. The record is evidence, not another owned stack.
   Once accounted for, `resolve-present` acknowledges that reconciliation. It
   creates no items. It cannot discard a known pending remainder.
4. Check the inventories and restart the manager explicitly when ready. Recovery
   never resumes a program on the operator's behalf.

Actions require the ID from the current record. A stale or repeated completed
claim cannot create another stack. A callback failure during a claim quarantines
its unresolved amount as uncertain before it can be retried. New failure records
also retain the original destination's position, dimension, slot and face.

The most recent resolved record is retained under `sfm:last_item_recovery`, with
actor, outcome and world tick. This bounded history is inactive and cannot be
claimed. This is not a general event log.

Unsupported versions/states, conflicting fluid/item records and invalid or missing
item payloads stay blocked with their evidence retained. Fluid recovery keeps its
existing policy; this command handles item records only. There is no ordinary
player GUI for these actions yet.

Headless tests exercise conservation, metadata/NBT, partial/full capacity, sided
access, missing sources/chunks, stale IDs, callback mutations/exceptions, owner
changes and command permissions. Scoped daily-803 tests also exercised orderly
world saves and reloads. Disk and destination saves are not atomic; these tools do not promise
exactly-once transfers after a process or host crash, or for arbitrary handlers
that violate their inventory contract.

## Fluid callback quarantine, 2026-10-28

An actual drain, destination fill or source rollback callback may throw after
changing its tank. SFM now records `sfm:recovery_fluid` with version, recovery ID,
`state=uncertain`, stage, fluid payload and source/destination position/face/dimension,
then pauses. The amount is attempted/remainder evidence with unknown ownership,
not a second owned tank. It is never automatically returned, even if the older
`recoveryBlocked` flag is cleared. Unknown states/versions are retained and paused.

Known refused rollback remains `pending` and uses the existing paced source-return
policy. A return callback that throws upgrades the record to uncertain and blocks
retries. Earlier version0/1 records without a state retain legacy compatibility.
Records cannot overwrite an existing item/fluid recovery entry and survive orderly
NBT persistence. This is not crash-atomic ownership across independent saves.

The item command above handles item records. Use the separate fluid procedure
below; real tank/cover failure qualification remains open.

## Operator fluid reconciliation

```text
/sfmfluidrecovery x y z inspect
/sfmfluidrecovery x y z resolve-present <recoveryId>
```

These require operator permission level 2 and a loaded current manager in the
sender's dimension, on the server thread. Inspect pauses the manager and returns
an evidence snapshot without inventing an ID/version or changing the record.
The command displays bounded identity, amount, stage and source/target fields;
the helper copies existing evidence, so inspection cost still depends on payload.

Inspect the actual source and destination and reconcile the uncertain effects
manually first. `resolve-present` is an explicit acknowledgement of that work,
not a transfer or a claim. It accepts only typed version-1 `uncertain` evidence
with its current canonical UUID and valid amount/provenance. Known pending
remainders, legacy/unknown states/versions, corrupt payloads and conflicting item
records remain intact. Unavailable fluid names can still be evidence; no registry
lookup, source/destination access, fill, drain or materialization occurs.

A successful acknowledgement removes the active evidence and replaces one fixed
`sfm:last_fluid_recovery` summary: ID, outcome, amount/name, stage/provenance,
actor (at most 64 characters) and world tick. Name/stage are at most 256 characters;
arbitrary fluid NBT is omitted from the inactive receipt. Preserve/export any
additional diagnostic payload before acknowledging if it is needed. Restart the
manager explicitly only after reconciliation; acknowledgement always leaves it paused.
There is deliberately no fluid `claim` or generic `discard` command.

Eight headless test methods cover permission/thread/loading boundaries, unchanged
pending pacing, seventeen malformed/legacy variants, conflicts, stale/repeated
IDs, zero provider callbacks and compressed NBT reload before/after acknowledgement.
A fresh full daily-803 Java17 dedicated server with 296 mods and Dynamic Inventory
passed the command sequence and an orderly restart using synthetic evidence:
exactly one inactive receipt persisted, stock/STATE were unchanged, and distant
source/target regions were not generated. This does not qualify real tank failures,
crash atomicity or two-player authority timing.
