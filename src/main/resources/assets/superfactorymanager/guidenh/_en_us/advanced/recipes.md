---
navigation:
  title: "Inspect machine recipes"
  position: 65
  icon: "minecraft:book"
  parent: /advanced/index.md
  keywords: ["recipes", "itemkey", "catalyst", "chance", "recipe map", "NEI"]
categories: ["sfm"]
updated: "2026-11-02"
---

# Inspect machine recipes

Use recipe inspection when a program needs to **look up requirements**.
It reads the server's recipe catalog. It does not run a machine or craft an item.

**Search → choose → inspect**

1. `recipes(output, map)` returns candidate summaries.
2. Check `status`, `coverageComplete` and how many candidates were found.
3. `recipe(candidate.reference)` reads one candidate's details.

## Try a lookup

This prints the ingredients for a single iron plate recipe. Enable PRINT capture
in the manager terminal before starting it.

```sfml
name "Inspect an iron plate recipe"
every 100 ticks {
  let found = recipes(itemkey("gregtech:gt.metaitem.01", 17032), "gt.recipe.metalbender")
  print(found.status, len(found.recipes), found.ambiguous)
  if(found.coverageComplete and len(found.recipes) == 1) {
    let details = recipe(found.recipes[0].reference)
    if(details.status == "matches") {
      for ingredient in details.recipes[0].itemInputs {
        print(ingredient.item, ingredient.amount, ingredient.chance, ingredient.matching)
      }
    }
  }
}
```

`itemkey(id, metadata)` is an **exact item identity with no NBT**. Metadata defaults
to zero when omitted. For an item with NBT, use an observed `contents()[i].item`.
`item("id")` is a broader transfer/query filter and cannot replace an exact key here.

## Choose an explicit map

The first supported maps are:

| Machine | Map string |
| --- | --- |
| Macerator | `gt.recipe.macerator` |
| Bending Machine | `gt.recipe.metalbender` |
| Cutting Machine | `gt.recipe.cuttingsaw` |

Other maps return `unsupported`. To inspect one connected stock single-block GT
machine, use `recipemaps(label("machine"))`. Check `status == "matches"` before
passing its `current` map string to `recipes`. Its `available` array contains the
native advertised maps, currently one for this qualified family. Multiple labelled
machines are `ambiguous`; unloaded/missing machines are `unavailable`. Unknown
families, subclasses, maps or versions are `unsupported`. This does not check
voltage, structure or other machine requirements. NEI selection remains planned.

## Read the result

- `matches`: candidate data is available with complete coverage for this output.
- `no_match`: complete search found no candidate for that exact output.
- `unsupported`: coverage or a requirement is unsupported; candidates may still be shown.
- `not_ready`, `stale`, `limit_exceeded`: wait for readiness, repeat the search or reduce query work.

Several recipes can produce the same output. `ambiguous` reports that case;
SFML does not choose a recipe for you. Summaries contain costs and input/output
counts, plus the reference used to request details.

## Details that matter

Details contain `itemInputs`, `itemOutputs`, `fluidInputs`, `fluidOutputs`,
`eut`, `duration`, `amperage`, `hidden`, `needsEmptyOutput` and `unsupportedRequirements`.

- Slots keep their original order and indices. Byproducts are retained.
- `amount == 0` on an input means a required nonconsumed **catalyst**.
- `chance` is out of **10000**, for both inputs and outputs.
- Fluid quantities are **mB**. Fluid alternatives retain their own quantities.
- Item requirements also expose `matching`, `nbtSensitive`, `ore`, `alternatives`
  and `displayAlternatives`. A representative item is not an exact replacement
  for an ore predicate or a wildcard requirement.

These observations do not prove that a particular machine can currently execute
the recipe. Keep its machine tier, configuration, catalyst and matching rules.

References expire after a server/catalog session change. Search again when a
reference returns `stale`. Searches and detail views have shared work and size
limits; inspect a candidate only when its details are needed.

## Pick an output from NEI

1. Focus a recipe in a supported GT tab and press **Ctrl+Shift+S**.
2. Open the station: **Settings → Recipe inspector**.
3. Click **NEI hint**, then **Lookup**.
4. Click a server candidate to inspect it. **Copy inspection query** puts a
   template on your clipboard; paste it through the normal editor.

You can also enter an exact item ID, metadata and map without NEI. The display is
only a hint: the server supplies and revalidates the candidates. Nothing is
selected automatically, and this tool never edits or runs your program.

This first shortcut refuses NBT-bearing outputs. More than32 candidates reports
an explicit UI limit; use SFML for larger results. The preview shows only two
entries per group, with full counts; `recipe(reference)` reads the full data.
Map membership does not prove machine voltage/structure or recipe feasibility.
