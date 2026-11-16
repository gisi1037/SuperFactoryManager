# Mod Storage Adapter

Place the adapter against an unconfigured ME Interface, connect SFM cable, and
label the adapter. Its metadata selects one neighboring face. Sneak-right-click
empty-handed to cycle that face. The placing player's UUID and profile name are
saved in tile NBT. There is no ticking tile, hidden storage buffer or AE grid node.

The initial integration supports block and multipart ME Interfaces through the
target pack's optional AE2 API. It requests the Interface's authorized monitor,
refuses its local-buffer fallback and configured stocking mode, and requires an
active node and powered grid. The placing identity must retain Build, Extract and
Inject permission, including while offline. Transfers use AE powered simulation,
extraction and insertion. A missing mod, owner, Interface or permission makes
access unavailable. No AE types enter block registration, common API signatures,
saved programs or SFML data values.

## Player API

Label the adapter and use existing item `contents`, `count`, `empty`, `available`
and corresponding `fcontents`, `fcount`, `fempty`, `favailable` methods.
`transfer` and `transferf` work in both directions. Item and fluid exact keys
retain their normal metadata/NBT matching. Fluids use mB.

Legacy INPUT/OUTPUT and slot selections are refused for adapter endpoints.
Exact-transfer helpers retain their qualified-storage limits. Snapshot locations
identify adapter rows, not stable physical slots. A same-tick query opens a fresh
observation. Shared network identity deduplicates aliases within a label group
and prevents adapter-to-adapter self transfers.

Insertions do not scan the destination network. Source scans are bounded to 1024
positive stored variants per type, excluding craftable-only entries. Existing
contents result limits and invocation budgets still apply and fail explicitly.
Counts use long. Backends that opt into bulk item moves can extract one exact
item identity for up to 16 destination inventory slots at once. Two bulk-capable
networks can transfer up to 65,536 of one identity per native batch. SFM still
validates each destination slot and its selected faces before inserting. The
whole action shares the normal work budget. Native fluids use their existing
write-ahead journal. Item batches record a count-one exemplar and a separate
quantity. Failed rollback retains the known remainder and an uncertain callback
requires operator review.

## Backend contract

`common.storage.StorageBackend` is the common implementation boundary for future
mod integrations. `TileEntityModAdapter.open` dispatches to the optional provider.
Backends are operation-scoped and must provide a shared backing identity, current
binding validation, detached bounded snapshots, capability checks, simulations
and authoritative actual return values.

All world access and mutations run on the server thread. Implementations must
revalidate their tile, selected face, owner, remote endpoint, network identity
and access permissions before provider operations. Respect the supplied budget,
including the held commit budget. Never retain a query budget or snapshot in a
tile, or load a target chunk.

The item snapshot bridge is internal to the guarded transfer service. It cannot
be mutated through ordinary IInventory methods. Physical destinations keep their
existing extraction validation, insertion rules and recovery. Additional backends
must qualify their mutation behavior and rollback paths before enabling writes.

Headless tests cover long counts, aliases, actual transfers, insertion without a
network scan, stale callbacks, bounded bulk payloads, partial rollback, fluids,
empty versus unavailable, slot refusal and optional dedicated-server loading.
The actual AE network, security/offline player behavior, channel/power lifecycle,
part geometry and two-player full-pack checks remain manual.
