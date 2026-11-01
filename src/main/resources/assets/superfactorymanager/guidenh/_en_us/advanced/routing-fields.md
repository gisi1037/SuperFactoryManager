---
navigation:
  title: "Edit routing fields"
  position: 10
  icon: "superfactorymanager:manager"
  parent: /advanced/editors.md
  keywords: ["fields", "picker", "limits", "exclusions", "ore", "port edge"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Edit routing fields

In Studio, select a node and open **Fields**. Settings chooses Essentials or
Advanced; Advanced also exposes the raw clause. Both modes support the same
language and full Insert/API catalog. Some complex clauses keep manual fields.

## Apply one complete edit

Edit related rows, then choose **Apply fields** once. Changes remain pending
until Apply; **Cancel** leaves source and Undo unchanged. The whole program must
pass syntax, type, scope and Visual checks. Pending edits never run a program.

Blank or invalid enabled rows block Apply. Use Remove to delete an unwanted row.
If Escape or Done closes pending work, use **Go > Reopen Fields** or
**Go > Reopen clause**. Finish or cancel it before editing code or another clause.

An error describes the last attempted action. Editing its pending value clears
the old error; Apply checks the new value. **Details** opens a long error with
Copy error; **Back to fields** or Escape returns without losing pending changes.

## Try it: cap an unrestricted route at eight iron ingots

**Setup:** pause the Manager. Use two connected single chests labelled `source`
and `dest`; copy those labels to its disk. Put twelve iron ingots and four
cobblestone in source, and empty dest. In Studio, start with this complete route:

```sfml
name "Fields batch"
every 100 ticks {
  input from "source"
  output to "dest"
}
```

Choose Visual and reuse its existing Input node. Open **Fields** and change only
the following controls, keeping the source label and routing unchanged:

| Field | Change |
| --- | --- |
| Amount (items / fluid mB) | Switch Unlimited to **Limited**; enter **8**. |
| Resources (OR) | Choose **Add resource**; enter `item:minecraft:iron_ingot`. |
| Amount: EACH | Keep **EACH: off**. |
| Allowance per container | Keep false. |
| Leave at least (source) | Keep **Default: 0**. |

Choose **Apply fields** once. The draft now contains the equivalent of this
complete source; generated keyword case or spacing may differ:

```sfml
name "Fields batch"
every 100 ticks {
  input 8 item:minecraft:iron_ingot from "source"
  output to "dest"
}
```

**Expected while editing:** no items move. The Input clause has one eight-item
allowance restricted to iron; Output uses that allowance without a stock target.
Save and Check, then [publish and imprint a fresh disk](studio.md). Insert it in
the paused Manager, copy its labels and Start. For an ordinary source disk, the
equivalent program can instead be saved through Manager > Program > Edit code.

**Expected in the chests:** after the first due run, source has four iron and
four cobblestone; dest has eight iron. After the next run, dest has twelve iron
and source still has all four cobblestone. Later runs leave those counts unchanged.
The batch cap limits each run, rather than the final destination total.

## Choose labels and resources

Input/Output Fields offers 1–32 ordered literal label rows. Add or Pick a label,
or Remove while retaining at least one. These are program references: editing
them does not label world blocks or change the disk's label table.

**Pick** suggests labels referenced in this draft or base client item IDs.
It does not scan connected inventories. Choose **Choose (pending)**, then
**Apply fields**. Picker Cancel keeps earlier pending edits; Fields Cancel
cancels the whole form. Metadata, NBT and complex expressions stay manual.

Suggestions report capped results. They inspect up to 256 draft labels / 4,096
reference visits or 8,192 item keys; search shows at most 64 matches. Reopen after
changing the project or source. A suggested name does not prove connectivity.

**Resources (OR)** offers 0–32 selectors sharing one limit. For example, iron OR
gold shares the same movement allowance. Remove all rows to restore match-all.
Fluids and quoted metadata forms stay manual. Unknown but valid selectors can
match nothing. Unsupported forms retain their complete clause.

## Set a batch, reserve or stock target

| Field | Meaning |
| --- | --- |
| Unlimited / Limited | Omit the movement cap, or enter a maximum batch. |
| Input: Leave at least | Reserve matching source stock. |
| Output: Fill up to | Stop adding when matching destination stock reaches the target. |
| Resource EACH | Give each resource identity its own allowance or reserve/target. |
| Container EACH | Give each selected block its own allowance or reserve/target. |

Enabled numbers must be filled in before Apply. Explicit **0** differs from an
omitted cap. Disabled movement is unlimited; disabled source reserve is zero;
disabled output target adds no stock cap. Fluid quantities use mB.
The resource and container EACH switches are independent.

Disabled text stays pending for re-enabling but is neither validated nor emitted.
Apply/Cancel ends that temporary state. Existing complex or computed limits may
use a full clause instead. See [quantities](/automation/quantities.md) for runtime scope.

## Filter and exclude resources

An eligible **OreDictionary filter** offers Off, With or Without and one name
pattern such as `ingotIron` or `ingot*`. Off removes the filter; enabled blank or
invalid patterns block Apply. These are legacy ore names, not item NBT or modern
datapack tags. Matching ignores ASCII case and supports `*`, not `?` or regex.
Unknown names are valid: With matches no items; Without excludes none.
**Fluids ignore this filter.** OR alternatives share the filter.

**Exclusions** offers 0–32 global EXCEPT rows. Add, type or Pick one selector;
Remove deletes it. Removing the last row removes EXCEPT. Fluids/metadata remain
manual. Exclusions apply across inclusion limits; they are separate from OR rows.

## Give resources independent limits

An eligible existing single static limit offers **Add independent limit**.
It validates pending edits, keeps them in row 1 and adds a blank row 2.
Enter complete SFML limit fragments, such as:

```text
16 RETAIN 2 item:minecraft:iron_ingot OR item:minecraft:gold_ingot
```

Each of 2–32 rows has its own allowance. OR shares only its row; overlapping
selectors use the first matching row. Add/Remove keeps at least one row. Global
labels, EACH, exclusions and routing stay in the same pending form. No nested
per-row pickers or ore controls appear. Reopening after applying one remaining
row restores eligible single-limit controls.

An unrestricted node must first gain an explicit limit through Apply/reopen
before offering this conversion. It never inserts a hidden match-all row.
Invalid conversion preserves the draft. Trailing commas/OR, oversized groups,
comments and overly nested forms retain manual editing rather than truncating.
Unchanged spelling, duplicates, case, order and supported separator gaps are preserved.

## Edit Print and Transfer arguments

**Print Fields** offers 1–8 ordered expression rows. Add/Remove keeps at least
one. Quote literal messages. Nested commas stay inside their expression row.
Apply commits all rows as one undo step. Capture is needed to see actual PRINT output.

**Transfer Fields** requires Source and Destination. **Add filter / count** adds
a third argument: an integer caps all matching stock, while a selector/key chooses
matching stock. **Add amount** adds a fourth argument after a selector/key.
Complete new blank fields before Apply; remove amount before removing its filter.

An omitted amount uses the available source snapshot; explicit zero moves nothing.
TRANSFER handles items; a TRANSFERF clause uses fluids and mB. Fields does not move
anything while editing. Pick works for bare `LABEL("...")` or `ITEM("...")`
arguments; side/slot expressions and exact keys stay manual. Result capture with
INTO requires Code. See [transfers](/automation/transfers.md) and
[fluid methods](/automation/fluid-methods.md).

## Edit conditions and signals

Simple **If / HAS** offers 1–32 labels, a comparison, a literal threshold and
0–32 OR resource selectors. Removing all resource rows restores match-all.
Keep at least one label. The set operator still decides how counts are compared.
Compound/negated legacy forms and oversized groups keep the full clause;
typed predicates and computed thresholds use Condition.

**Signal** offers 1–32 destination labels and a literal level. Omitted level
keeps default 15; static levels retain the compiler's 0–15 clamp. Computed and
ambiguous forms stay manual. Changing references does not configure or connect a port.

## Watch a named port edge

**Port edge** is distinct from the manager's Pulse trigger. Choose RISING,
PULSE or FALLING and 1–32 literal label rows. PULSE aliases RISING.
A complete header example is:

```text
every redstone rising from "signal" north side
```

**Physical sides** accepts NORTH, EAST, SOUTH, WEST, TOP, BOTTOM separated by
commas. Leave blank for default all faces, or enter EACH for explicit all faces.
Leave out SIDE; Fields supplies it. UP/DOWN, relative faces, NULL, slots, round
robin and LABEL expressions are not supported in this header field.

Unsupported headers keep a complete Trigger header field without block delimiters.
Initial observations establish a baseline, rather than producing a false edge.
Connect and label a native Redstone Port before running the program; see
[redstone](/automation/redstone.md) for event and availability rules.

Next: [graph workflow](visual-editing.md) or [Studio projects](studio.md).
