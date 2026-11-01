---
navigation:
  title: "Recipes and cable shapes"
  position: 25
  icon: "superfactorymanager:cable"
  parent: /advanced/index.md
  keywords: ["craft", "recipe", "HV", "conversion", "full", "fancy", "cable"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Recipes and cable shapes

For a first network, follow [Build the network](/start/hardware.md).
Use **NEI** to see the exact crafting shape and accepted ingredients in your pack.

## Choose the cable shape

<BlockImage id="superfactorymanager:cable" scale="2" />
<BlockImage id="superfactorymanager:fancyCable" scale="2" />

The full-block Inventory Cable and thin Fancy Cable discover the **same network**.
Mix them freely. The full block has full-cube collision; Fancy Cable leaves more
visual space around your machines. Neither loads distant chunks.

## Free one-for-one conversions

Use **plain items** with no saved data. Each conversion consumes one and returns one.

| Input | Placement | Output |
| --- | --- | --- |
| Steve's Factory Manager cable | Any crafting slot | Our full-block cable |
| Our Fancy Cable | Any crafting slot | Our full-block cable |
| Our full-block cable | Left column of a 3×3 crafting grid, any row | Steve's cable |
| Our full-block cable | Right column of a 3×3 crafting grid, any row | Our Fancy Cable |
| Plain Steve's controller / our manager | Any crafting slot | The other controller |
| Plain Steve's Addons Labeler / our Label Gun | Any crafting slot | The other tool, if the addon is installed |

The center column gives no full-cable conversion. Left and right placement choose
between two outputs without extra ingredients. Existing legacy recipes remain;
the swap retains the progression of the item you already crafted.

**Keep saved data safe:** a programmed data stick, renamed item, saved labeler or
other ingredient with nonempty NBT is refused rather than stripped. These recipes
do not migrate programs, labels, energy or installed disks. Use a fresh plain item.

## Direct recipes: late HV

The direct SFM recipes use GTNH materials and components. Here, an **HV circuit**
means an accepted `circuitAdvanced` ingredient. NEI shows alternatives.

| Output | Main ingredients |
| --- | --- |
| Factory Manager ×1 | Stainless-steel plates, screen, 2 HV circuits, HV hull and HV motor |
| Full-block cable ×2 | Stainless-steel plates, single gold cables and an HV circuit |
| Blank program disk ×1 | Electrum foil/fine wire, advanced epoxy board, blank data stick and an HV circuit |
| Label Gun ×1 | Stainless steel, HV sensor, HV emitter, HV motor and an HV circuit |
| Programming Station ×1 | Stainless steel, screen, advanced epoxy boards, HV hull, 2 HV circuits and a manager |
| Redstone Port ×1 | Stainless steel, HV sensor, HV emitter, 2 HV circuits and our full-block cable |

No OpenComputers parts are required. Direct recipes need the GT components;
there is no cheaper vanilla fallback if a component is missing. Conversions remain
separate from direct crafting. Check the server log if NEI has no direct recipe.

Next: [labels and connected blocks](labels.md), or [Studio and program disks](studio.md).
