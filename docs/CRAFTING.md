# Crafting and cable conversions

The direct recipes target late-HV GTNH progression. They replace the previous
iron/redstone recipes; NEI shows the installed shapes and pack alternatives.
No OpenComputers dependency is added. GT ingredients are resolved once after
mod recipe loaders, not while a program runs.

| Output | Count | Ingredients |
| --- | --- | --- |
| Factory Manager | 1 | 4 stainless-steel plates, screen, 2 HV circuits, HV hull, HV motor |
| Full-block Inventory Cable | 2 | 4 stainless-steel plates, 3 AE2 Fluix ME Glass Cables, 2 ProjectRed neutral bundled cables |
| Blank program disk | 1 | 4 electrum foils, advanced epoxy circuit board, 2 fine electrum wires, blank data stick, HV circuit |
| Label Gun | 1 | Stainless-steel plate, screen, HV sensor, HV circuit, HV emitter, HV motor |
| Programming Station | 1 | 2 stainless-steel plates, screen, 2 advanced epoxy circuit boards, HV hull, 2 HV circuits, Factory Manager |
| Redstone Port | 1 | 4 stainless-steel plates, HV sensor, 2 HV circuits, HV emitter, Inventory Cable |
| Disk Copier | 1 | Stainless-steel plate, screen, 2 advanced epoxy circuit boards, HV hull, 2 HV circuits, HV sensor, HV emitter |

HV circuit means the circuitAdvanced OreDictionary category. Plates, wires,
foils and cables use their normal GT ore categories. Other entries use the exact
GT ItemList components. Missing GT components/materials are reported during
startup, with no cheaper vanilla fallback. Without GT, direct recipes are absent;
available cable/legacy conversions still work. This does not change runtime mod
dependencies or add hidden EU consumption to blocks.

Inventory Cable uses the same ingredients and output count as the GTNH SteveFM
recipe, rotated to avoid competing outputs. Put Fluix ME Glass Cable down the
middle column, neutral bundled cable on the left/right of the middle row, and
stainless-steel plates in the corners.

The exact pack inputs are AE2 `item.ItemMultiPart:16` and ProjectRed
`projectred.transmission.wire:17`. Missing AE2/ProjectRed cable parts disable only
this direct recipe; the other GT machine recipes and available conversions remain.

## Free reversible conversions

Place one plain item anywhere in the crafting grid. Conversions consume one
and produce one, without additional upgrade materials:

- Steve's Factory Manager cable ↔ our full-block Inventory Cable.
- Our full-block Inventory Cable ↔ our Fancy Inventory Cable.
- Steve's Factory Manager controller ↔ our Factory Manager.
- Steve's Addons Labeler ↔ our Label Gun, when that addon is installed.

To convert our full cable, use a 3x3 crafting grid: the left column returns
a legacy cable; the right column produces Fancy Cable (any row). The center
column has no conversion. Legacy cable and Fancy Cable convert back to our
full cable in any slot. Controller and Label Gun swaps also work in any slot.
These placements avoid competing outputs for the same input. Both cable shapes
have identical network behavior; the full block has full-cube collision.

The legacy pack recipes remain intact, and conversions retain their progression
as requested. They are not a new late-HV gate on previously crafted legacy items.
Both legacy and new cables/tools remain visible in NEI.

## Data-bearing items

These recipes refuse nonempty NBT on their inputs. They are not program, label,
energy or saved-machine-data migration tools. A programmed data stick, renamed
item, saved labeler or data-bearing block item will not silently lose its data
through crafting. Use a fresh plain ingredient and retain the original data.
Conversion metadata is exact; advanced/cluster variants are not treated as
ordinary cables. Empty NBT compounds are accepted as plain.

## Disk Copier

The separate Disk Copier makes **up to eight copies per cycle**, consuming one
fresh blank SFM disk for each copy. A cycle defaults to **20 loaded server ticks**
(one second at 20 TPS). It needs no operating EU; its recipe gates
access to late HV. The source is retained. Program text or the exact published
execution reference and all saved labels carry over. Copies receive a new
document identity and fresh runtime STATE/clock. Custom names/foreign metadata,
recovery records and authoring ownership do not carry over. Recovery-blocked,
malformed/unsupported or oversized source metadata refuses without consuming media.
Copying a published reference retains its existing execution-only bearer rights;
it does not publish a build, reveal private project/library source or repair a
missing/revoked reference. Legacy source text is copied verbatim, not compiled.

The GUI uses normal MUI2/player slots and leaves room for NEI. Insert a programmed
source in the center slot, up to eight blanks in the left grid, and take copies
from the right grid. Copies keep independent identities and label tables, so
programmed disks remain unstackable. A full output grid prevents work; partial
space produces only as many copies as can fit. Automation inserts source from the top and
blank media from horizontal sides, and extracts copies from the bottom. The
source can be removed manually; automation cannot drain it. Disks are unstackable.

Server configuration in `config/superfactorymanager.cfg`, category `machines`:

| Setting | Default | Range |
| --- | --- | --- |
| `diskCopierTicksPerCycle` | 20 ticks | 1-12000 ticks |
| `diskCopierCopiesPerCycle` | 8 | 1-8 copies |

Restart the server to apply configuration changes. Each active cycle keeps its
captured duration and bound media. Smaller batch limits automatically process
the remaining queued blanks in subsequent cycles. Existing three-slot saves keep
their source/blank/output indices; extra slots simply load empty.

Changing any slot resets progress. Unload/restart discards pending work, consuming
nothing; a new full cycle begins on reload. In-place metadata changes are checked
before completion. After refusal, reinsert the corrected disk to retry. Copying
does not read project files during machine ticks. See the
[in-game checklist](IN_GAME_TEST_CHECKLIST.md) for native qualification.
