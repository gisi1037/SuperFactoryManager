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
changes and command permissions. Disk and destination saves are not atomic;
these tools do not promise
exactly-once transfers after a process or host crash, or for arbitrary handlers
that violate their inventory contract.
