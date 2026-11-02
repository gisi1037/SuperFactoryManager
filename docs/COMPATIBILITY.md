
## Storage Drawers item sources

The drawer-source adapter is qualified against Storage Drawers2.2.31-GTNH's real
standard tile, DrawerData and StorageInventory in an isolated Forge runtime.
Backing quantities validate refilling automation slots, including stack-size-one
items. Explicit unlimited-vending mode has a separate unchanged-count contract.
Compacting pools, controller and slave mappings refuse before extraction until
qualified. Other creative-storage providers are not implicitly trusted. See
[Storage Drawers sources](STORAGE_DRAWERS.md) for scope and recovery behavior.
These tests do not constitute full-pack multiplayer or GUI qualification.
