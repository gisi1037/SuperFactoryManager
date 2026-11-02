# Storage Drawers item sources

Direct standard Storage Drawers, including unlimited-vending drawers, support
`transfer(...)` extraction through their public backing-storage API. No SFML
syntax changes are needed.

The visible output slot is a virtual stack. After removing one item from a drawer
holding 100, the backing quantity becomes 99 while its output may still display
64. SFM validates that backing change and the exact item identity instead of
requiring the display to become 63. The same rule handles items whose stack limit
is one. Ordinary sources still have to decrease by the extracted amount.

Only the drawer API's explicit unlimited-vending flag permits unchanged backing
quantity. An unlimited-capacity upgrade alone is not an infinite item source.
Mode, mapping, identity and quantity are checked again after callbacks. Unused
items return through the backing API; failures after a write retain uncertain
recovery evidence. Capacity/item callbacks cannot overwrite newer storage state
or mutate the owned rollback stack through an inspection argument.

Compacting drawers require pooled/fractional-count qualification. Controller and
slave sources require physical drawer mapping qualification. These recognized
sources refuse before actual extraction while that work is pending. Connect a
standard drawer directly for the qualified path. Other creative-storage mods are
not covered by this adapter.

Qualification uses Storage Drawers 2.2.31-GTNH's real `DrawerData`,
`StorageInventory` and standard tile in an isolated Forge runtime, with capacity
and upgrade policy supplied by the fixture. This is distinct from a full-pack
multiplayer or GUI qualification. The mod has no required Storage Drawers runtime
dependency, and common loading is tested with its classes forbidden.

Optional native regression setup: pass `-PstorageDrawersTestJar=<path-to-dev-jar>`
to the normal Gradle test task. Use the matching official development artifact;
the ordinary suite also runs without that file.

An existing transfer-recovery record needs its own reconciliation; installing
this fix does not establish whether an earlier uncertain extraction completed.
Use the [recovery guide](RECOVERY.md) to inspect the record and physical slots.
