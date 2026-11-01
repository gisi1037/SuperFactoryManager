# Crafting and cable conversions

The direct recipes target late-HV GTNH progression. They replace the previous
iron/redstone recipes; NEI shows the installed shapes and pack alternatives.
No OpenComputers dependency is added. GT ingredients are resolved once after
mod recipe loaders, not while a program runs.

| Output | Count | Ingredients |
| --- | --- | --- |
| Factory Manager | 1 | 4 stainless-steel plates, screen, 2 HV circuits, HV hull, HV motor |
| Full-block Inventory Cable | 2 | 4 stainless-steel plates, 4 single gold cables, HV circuit |
| Blank program disk | 1 | 4 electrum foils, advanced epoxy circuit board, 2 fine electrum wires, blank data stick, HV circuit |
| Label Gun | 1 | Stainless-steel plate, HV sensor, HV circuit, HV emitter, HV motor |
| Programming Station | 1 | 2 stainless-steel plates, screen, 2 advanced epoxy circuit boards, HV hull, 2 HV circuits, Factory Manager |
| Redstone Port | 1 | 4 stainless-steel plates, HV sensor, 2 HV circuits, HV emitter, Inventory Cable |

HV circuit means the circuitAdvanced OreDictionary category. Plates, wires,
foils and cables use their normal GT ore categories. Other entries use the exact
GT ItemList components. Missing GT components/materials are reported during
startup, with no cheaper vanilla fallback. Without GT, direct recipes are absent;
available cable/legacy conversions still work. This does not change runtime mod
dependencies or add hidden EU consumption to blocks.

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

Programming disk copies remain a separate planned machine. No disk copier recipe
is registered before that block exists. GuideNH's hardware page and the
[in-game checklist](IN_GAME_TEST_CHECKLIST.md) describe the verification steps.
