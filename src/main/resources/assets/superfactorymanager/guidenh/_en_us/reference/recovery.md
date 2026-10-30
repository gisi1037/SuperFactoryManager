---
navigation:
  title: "Recovery and paused transfers"
  position: 70
  icon: "superfactorymanager:manager"
  parent: /reference/index.md
  keywords: ["recovery", "quarantine", "pending", "uncertain", "operator", "loss", "duplication"]
categories: ["sfm"]
updated: "2026-10-30"
---

# Recovery and paused transfers

A provider can reject insertion, disappear, or throw after a transfer begins.
SFM attempts ordinary rollback when safe. If ownership cannot be settled, it
preserves recovery evidence on the disk and pauses the manager.

Keep the disk and record intact. Clearing NBT to resume execution can destroy
the information needed to determine which inventory owns the items or fluid.

## Know which state you have

| State | Meaning | Next action |
| --- | --- | --- |
| Known item pending remainder | The disk owns a specific unreturned item stack. | An operator can claim it into a suitable adjacent inventory. |
| Uncertain | A callback may have changed a provider before failing. The recorded amount is evidence, not an extra owned stack. | Inspect actual source and destination, reconcile manually, then acknowledge the current record. |
| Unknown/corrupt record | SFM cannot safely interpret the saved state. | Preserve the evidence and investigate; it remains blocked. |

Completed earlier transfers stay completed when later work faults. A trigger is
not one all-or-nothing transaction across every inventory. Ordinary world saves
and a disk save are also not a crash-atomic transaction.

## Operator item commands

Run these for a loaded manager in your current dimension. Replace coordinates
and the recovery ID with the values for the affected manager.

```text
/sfmrecovery x y z inspect
/sfmrecovery x y z claim <recoveryId> <direction>
/sfmrecovery x y z resolve-present <recoveryId>
```

1. Inspect and read the current record ID, ownership state, amount and failure stage.
2. For **pending**, place a suitable inventory beside the manager and claim
   toward north, south, east, west, up or down. The destination's faces, slots,
   filters and capacity still apply. A partial claim keeps the remainder's ID.
3. For **uncertain**, account for the actual source/destination contents first.
   `resolve-present` acknowledges that reconciliation and creates no items. It
   cannot discard a known pending remainder.
4. Check the route and deliberately restart the manager. Recovery never resumes it for you.

These commands require operator permission. A claim cannot load distant chunks,
materialize uncertain evidence or use an old completed record to make another
stack. Keep a backup when investigating a provider failure.

## Operator fluid commands

```text
/sfmfluidrecovery x y z inspect
/sfmfluidrecovery x y z resolve-present <recoveryId>
```

Inspect the real tanks and reconcile uncertain effects before acknowledgement.
The command creates no fluid, fills no tank and leaves the manager paused.
There is no fluid `claim` or generic `discard` command. Known pending fluid
returns follow their existing return policy; this acknowledgement accepts only
the current supported uncertain record.

An unknown or malformed record, conflicting item/fluid records, a legacy state
or a stale ID is preserved and rejected. There is no ordinary-player recovery
GUI. Ask the server operator when a manager reports recovery-blocked status.

Next: [debugging](debugging.md) and [performance](performance.md).
