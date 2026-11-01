# In-game acceptance checklist

Use a disposable test world and record the exact SFM JAR, GTNH pack, Java version,
single-player/dedicated server and optional mods. Test one matching client/server
build. A prepared JAR is not necessarily installed. Older8fbe82e lacks the newer
NBT/BREAK and late-HV recipe changes; use the matching updated test artifact.
The disk copier and other explicitly planned features below are still future work.

For each failure, keep the smallest program that reproduces it, installed build
revision, expected/actual result, exact error and a screenshot if useful. Source
locations refer to the installed program, which may differ from a Studio draft.

## Current build: highest-value manual pass

### How to run the copyable programs

Create a separate station project for each program below. Save, Check, publish
and imprint the exact revision onto a fresh disk. Copy the Label Gun table to
the manager's installed disk, then Start. Open Terminal and turn PRINT capture
on. Pause the manager before changing programs or resetting a fixture. Keep
the test output chest separate from valuable storage. These snippets are also
compiler-checked by the headless suite; actual machine behavior still needs
these in-game checks.

#### T1: Counted item transfer and same-run fresh reads

Connect chests named `test_in` and `test_out`. Put exactly32 iron ingots in
test_in and leave test_out empty. Run until test_in empties, then Pause.
Expect8 moved per run,32 total in test_out, and no missing/extra ingots. Each
printed source count after TRANSFER must already reflect that run's move.

```sfml
name "T1 counted transfer"
every 20 ticks {
  let source = label("test_in")
  let destination = label("test_out")
  print("before", source.count(item("minecraft:iron_ingot")))
  transfer(source, destination, item("minecraft:iron_ingot"), 8)
  print("after", source.count(item("minecraft:iron_ingot")))
}
```

Fill/block test_out and repeat with8 ingots in test_in. It should retain the
unmoved stock. Repeat with sided GT machine access; record the exact machine
and face. Completed earlier moves are not rolled back as one program transaction.

#### T2: Contents groups, metadata and physical locations

Use test_in with two identical stacks in separate slots and a different item
or NBT variant. Expect identical exact keys grouped together, with their summed
quantity and separate origin slots. Distinct NBT/metadata must form distinct
groups. The code is observational and must not move or change anything.

```sfml
name "T2 contents"
every 40 ticks {
  let stock = label("test_in").contents()
  print("groups", len(stock), "items", total(stock))
  for group in stock {
    print(group.id, group.metadata, group.total)
    for origin in group.locations {
      print(origin.x, origin.y, origin.z, origin.slot, origin.amount)
    }
  }
}
```

#### T3: STATE, dynamic arrays and independent controllers

No inventories are needed. Expect an increasing counter and at most four
recent values in history. Pause/resume preserves both. Save/reload the world
and check the next counter continues. A second freshly imprinted disk/manager
must have its own values. Use the confirmed STATE reset to restart at defaults.

```sfml
name "T3 state and arrays"
state count = 0
state history = array("integer")
every 20 ticks {
  count = count + 1
  push(ref history, count)
  if (len(history) > 4) { let removed = removeat(ref history, 0) }
  print(count, history)
}
```

#### T4: Loop forms and Visual round trip

Expect each loop to print3,5,7 in order, with indices0,1,2. Run in Code first,
then switch to Visual/Split, edit a number, switch back and check one coherent
program. Undo should restore that edit without losing node layout.

```sfml
name "T4 loops"
every 40 ticks {
  let values = [3, 5, 7]
  for (let i = 0; i < len(values); i = i + 1) {
    print("indexed", i, values[i])
  }
  for (index, value) in values { print("foreach", index, value) }
  let cursor = 0
  while (cursor < len(values)) {
    print("while", cursor, values[cursor])
    cursor = cursor + 1
  }
}
```

#### T5: A returning function that sleeps

Expect one before/after pair per trigger, about five game ticks apart, and one
increment. The before message must not repeat on resume. Removing/replacing the
disk while sleeping must cancel its old continuation rather than applying it
to another installed program. This test needs no inventories.

```sfml
name "T5 nested wait"
state count = 0
integer delayed(integer value) {
  wait(5)
  return value + 1
}
every 40 ticks {
  print("before", count)
  count = delayed(count)
  print("after", count)
}
```

#### T6: Fluid amount and fresh tank observations

Connect tanks named fluid_in and fluid_out. Put exactly4000mB of one known
fluid in fluid_in; leave fluid_out empty. Expect1000mB per successful run and
4000mB total after four runs, subject to the tank's actual face permissions.
Then test full output and refused faces: fluid must remain accounted for.

```sfml
name "T6 fluid transfer"
every 20 ticks {
  let source = label("fluid_in")
  let destination = label("fluid_out")
  print("before mB", source.fcount())
  transferf(source, destination, 1000)
  print("after mB", source.fcount(), "output mB", destination.fcount())
}
```

#### T7: Named Redstone Port

Connect and label a native Redstone Port start, then toggle its external input.
Expect exactly one message per detected rising edge, none just because the
signal stays high, and no false pulse from an unrelated nearby block edit.
This does not promise delivery while a sensor/network is unavailable.

```sfml
name "T7 named signal"
every redstone rising from "start" do
  print("rising edge")
end
```

#### T8: Read coolant heat and other NBT (requires the NBT-inspection build)

This test is **not available in8fbe82e**. Install the newer qualified NBT build
before running it. Label the accessible reactor inventory reactor; first test
in a chest if the real reactor refuses the selected access face. Put in a
360k Helium Coolantcell whose tooltip shows heat. Its registry ID from the
provided screenshot includes the k: gregtech:gt.360k_Helium_Coolantcell.

Expect metadata15 (this coolant's reported coarse heat percentage) and heat55872 for that exact captured
item, and tag1. Actual heat changes during use, so compare the value at capture
time. A second sample must capture a new contents view. Equal metadata alone
does not mean equal NBT. This program only reads and prints; it extracts nothing.

```sfml
name "T8 coolant NBT"
every 40 ticks {
  let reactor = label("reactor")
  let contents = reactor.contents(item("gregtech:gt.360k_Helium_Coolantcell"))
  print("coolant groups", len(contents))
  for stack in contents {
    if (stack.metadata == 15) {
      print("keys", stack.item.nbtkeys())
      if (stack.item.hasnbt("/heat")) {
        print("heat", stack.item.nbtinteger("/heat"), "type", stack.item.nbttype("/heat"))
      }
      if (stack.item.hasnbt("/tag")) { print("tag", stack.item.nbtinteger("/tag")) }
    }
  }
}
```

An ITEM selector accepts namespace:name or *, **not a trailing metadata suffix**.
Filter metadata on each group as above. An empty result can mean a wrong ID,
no matching stock or selected slots that expose none; inaccessible groups fault
instead of being reported as empty. Verify names/cables/faces first.

NBT paths use JSON Pointer, relative to the item's tag compound: /heat, /display/Name,
or /list/0/value. A dot is part of a literal tag name, not a separator. Escape /
inside a key as ~1 and ~ as ~0. Empty path selects the root. Use hasnbt(path)
before an optional read; nbttype(path) reports missing or the exact NBT kind.
nbtinteger reads signed byte/short/int/long without rounding; nbtstring reads a
string, and nbttext displays any scalar including float/double. Wrong-type or
missing typed reads fault instead of inventing zero. nbtkeys returns sorted
compound keys; nbtlength counts children/elements or UTF-16 string code units.
Existing per-item snapshot limits and shared work/allocation limits still apply.

For a large live reactor, first test a single selected move per run instead of
one full transfer for every NBT group. This preserves the example's metadata
condition. For this GT coolant cell, metadata is reported as coarse heat percent
(0 cold,100 full); this is item-specific. Capturing many groups can still be expensive;
restrict known input slots or measure before increasing the batch size.

```sfml
name "T10 bounded coolant move - coarse heat"
let reactor = label("reactor")
let destination = label("vacin")
every 20 ticks {
  let contents = reactor.contents(item("gregtech:gt.360k_Helium_Coolantcell"))
  let selected = false
  for group in contents {
    if (!selected and group.metadata > 10) {
      transfer(reactor, destination, group.item, 1)
      selected = true
    }
  }
}
```

If a run reports completed progress before a budget failure, those moves stay
completed. A longer timer interval changes frequency, not the per-run allowance.
WAIT can split work across ticks, but a previously captured key may no longer
match a coolant cell whose heat changed meanwhile. Fresh-snapshot/same-run
selection avoids that deliberate delay; do not assume stale keys reserve stock.

**Reactor-specific requirement:** the user's operating reactor requires removal
and replacement in the same game tick. The bounded-move example above is only
an extraction test in a safe fixture; it is not a complete reactor controller.
Consecutive transfers without WAIT normally execute in one tick, but the second
can fail after the first succeeds. Do not use WAIT between extraction/replacement.
Ordinary TRANSFER pairs provide no atomic coolant-exchange guarantee. Qualification
must cover absent replacement, rejected/wrong slot, full freezer input, insufficient
work budget and exact-slot restoration before deploying that controller.

#### T9: Deliberate runtime diagnostics

No inventories are needed. Check/publish this program, enable capture and Start.
Compilation should succeed; execution should pause with a useful out-of-range
index error and installed-program line/column. The after message must not appear.
Do not treat this intentional failure as an unexplained crash.

```sfml
name "T9 intentional fault"
every 20 ticks {
  let values = [1, 2]
  let index = 3
  print("before intentional fault")
  print(values[index])
  print("after fault - should not appear")
}
```

- [ ] **Basic setup.** Place a manager, cable and two chests. Label and connect
  them. Move a counted item batch, then a wildcard batch. Check exact source and
  destination totals and metadata/NBT. Start/Pause should behave consistently.
- [ ] **Studio projects.** Create/save/check a template, switch projects and return,
  close/reopen the station and restart the client/world. Correct project and source
  return. Unsaved drafts stay in their own project; Keep/Discard/Cancel do what
  their labels say. Exercise Copy/Paste, comments, Undo/Redo and Find.
- [ ] **Visual variables and loops.** Try declarations, assignment, IF, counted
  FOR, indexed FOR, index/value foreach and WHILE. Connect BODY/AFTER paths. Edit
  a value in Code, return to Visual and verify preserved structure/layout where
  supported. Unsupported source must stay intact with a useful explanation.
- [ ] **Dragging/layout.** Drag nodes smoothly; hold Shift for snapping. Resize
  Inspector, Projects and bottom dock. Change Studio-only scale without changing
  game scale. Check compact and large windows, hover/click alignment, scrolling,
  centered popups and readable Problems. Player inventory/NEI must be inaccessible
  inside full-screen Studio; normal Manager inventory remains usable.
- [ ] **Imprinting/restoration.** Save/check/publish an exact revision and imprint
  one blank disk. Install it through inventory controls, start it and confirm the
  expected behavior. Restore an older build into the editor; verify its source
  and that the running revision changes only through explicit deployment. The
  original disk must remain unchanged by unrelated authoring edits.
- [ ] **Owned libraries.** Publish a functions-only project, insert its exact
  revision and call it from another program. Publish a newer library revision;
  the installed program should still use its original pinned build. Restart the
  world and verify execution. This does not test cross-player library sharing.
- [ ] **RAM versus STATE.** Change LET and STATE values, pause/resume, then reload
  the world. LET values reinitialize on activation; valid STATE is restored.
  Include dynamic arrays and nested values. Two managers running the same build
  must not share mutable variables. Test deliberate STATE reset separately.
- [ ] **WAIT/functions/REF.** Put WAIT in a loop and in a nested returning function.
  Earlier PRINT/transfer effects should not repeat when execution resumes. Verify
  a REF update reaches the intended caller variable. Pause/remove/reinsert a disk
  while sleeping and check that an obsolete continuation does not run afterward.
- [ ] **Real GT item machines.** Use assembler input/output faces, blocked output,
  different slot selections and covers. Check amount-only, item-specific and
  all-item TRANSFER plus legacy INPUT/OUTPUT. Query contents after a transfer in
  the same invocation; it should reflect the actual completed move. No duplication
  or unexplained loss is acceptable. Repeat with a vanilla double chest.
- [ ] **Fluids.** Use TRANSFERF with no amount, a selected fluid and a bounded mB
  amount. Compare FCOUNT/FEMPTY/FAVAILABLE/FCONTENTS before and after. Exercise
  empty/full tanks, mixed tanks, refused faces, insufficient capacity and GT covers.
  Observe actual fluid totals, not only the displayed transfer result.
- [ ] **Circuits/redstone.** SETCIRCUIT should target a compatible assembler and
  report the actual result. Verify named rising/falling signals and six-face
  output independently. Include held-high signals, repeated pulses and a nearby
  unrelated block change. Disconnected/unloaded targets must not be force-loaded.
- [ ] **Terminal/errors/watch.** Enable PRINT capture, run a small program and
  check scrolling/filtering/copy/clear. Trigger a compile error and a runtime
  error deliberately: line/column, cause and installed revision should be useful.
  Watch exposed initialized globals/STATE; the UI must not change their values.
  Invocation-local watches, stepping and breakpoints are not delivered features.
- [ ] **Network browser.** Check search, All/Items/Fluids, Next/Previous and explicit
  Refresh. Include an unlabeled inventory, multiple labels, an unloaded saved
  position, a removed inventory and stale discovery. Refresh must not rebuild;
  Rebuild network is explicit. Empty filtered pages may still have Next. After
  30 seconds of game time, refresh an expired snapshot. Rows are metadata, not a
  guarantee of transfer permissions or an inventory-content display.
- [ ] **Profiler/scale.** Run a bounded capture with one controller, then several
  busy controllers and blocked destinations. Capture should stop at its selected
  limit; UI should remain usable and counters should describe that controller.
  Record spikes/stutters rather than interpreting the graph as whole-server TPS.
- [ ] **Optional inventory integration.** Repeat Manager and Studio essentials
  with and without Dynamic Inventory. Verify all player slots remain reachable,
  machine slots never overlap diagnostic controls, and NEI works around the
  machine window. Studio must expose neither usable inventory slots nor NEI.

### T11: BREAK before and after WAIT (requires the BREAK build)

Run without inventories. Expect one body print, followed a tick later by the
after-loop message, per scheduled run. Neither the later body message nor the
Java FOR update should execute. Repeat with counted FOR, foreach and WHILE.
Nested BREAK exits only the innermost loop. BREAK does not yield or revert prior
actions; extraction/refill must still be consecutive and not separated by WAIT.

```sfml
name "T11 break"
every 40 ticks {
  let updates = 0
  for (let i = 0; i < 4; updates = updates + 1) {
    print("body once", i)
    wait(1)
    break
    print("unreachable")
  }
  print("after loop", updates)
}
```

## Dedicated server / two players

- [ ] Publish/imprint as A; hand the disk to B and execute it without exposing A's
  private projects or dependency source. Both players see authoritative status.
- [ ] Open the same manager from both clients. Check Start/Pause/reset, concurrent
  edits and stale confirmations after a disk swap or reopening a window.
- [ ] Log out/reconnect, travel out of chunk range, unload/reload, and restart the
  server. STATE and installed revision survive appropriately; old queued UI
  actions and sleeping continuations never attach to replacement blocks/disks.
- [ ] Exercise missing-publication recovery in a controlled administrator fixture.
  It must fail visibly and retain saved evidence, then recover only through the
  supported retry workflow. No arbitrary private-source access is implied.

## Full-block cable, late-HV recipes and text cursor

- [ ] Find our full-block Inventory Cable in NEI with SteveFM installed. Craft
  the direct recipe (two cables), convert Steve's cable to it, convert it to
  Fancy Cable and reverse the chain. Each swap consumes/produces exactly one.
- [ ] Convert a plain legacy controller and plain addon Labeler both ways.
  Confirm their original pack recipes still exist. A saved/renamed/tagged item
  must refuse conversion without consuming it or discarding data.
- [ ] Actually craft each direct output listed in [Crafting](CRAFTING.md), including
  a blank disk from a blank data stick. Check NEI shapes, ore alternatives and
  output counts. A programmed data stick must not be consumed. Check the startup
  log for missing/unset GT component or material messages.
- [ ] Compare full/fancy/mixed cable routes to the same inventory and Redstone
  Port; break/reconnect and cross a chunk boundary. Discovery/access behavior
  should match. The full cable has full-cube collision and rendering.
- [ ] Hover over Studio code and editable text fields: the cursor becomes an
  I-beam. Move onto buttons, scrollbars, modal controls and another GUI, then
  close to the world: the prior pointer returns. Test game and Studio scales.
  Text selection and typing must behave as before. Native backend appearance
  needs manual qualification; a backend without custom cursor support should
  keep text input working normally.

## Additions: test after implementation, not in the current checkpoint

- [ ] **Disk copier:** one blank consumed per completed copy, unchanged source,
  exact program revision/labels, fresh independent runtime state, full-output
  refusal, disk replacement, unload/restart and hopper interactions. Invalid or
  recovery-blocked sources must refuse without losing media or recovery evidence.
- [ ] **Cable label editor:** clear selected manager/disk, group assignments,
  multi-manager ambiguity, bulk edits, cancellation and stale topology/session
  refusal. Verify authoritative labels in running programs afterward.
- [ ] **Recipe queries:** compare all returned candidates, counts, alternatives,
  circuits, fluids, chances and constraints against the selected real GT recipe
  map/NEI recipe. Unsupported/ambiguous results must be explicit.
- [ ] **NBT/durability:** exact tag types, absent tags, nested/list queries,
  undamageable versus damageable items and a known worn tool. A read must not
  mutate the item; extraction should affect only the selected matching stacks.
- [ ] **Additional round robin:** uneven stock, blocked targets, EACH/RETAIN,
  pause/reset/reload and deterministic target rotation according to the final
  documented policy. Existing policies must retain their behavior.
- [ ] **Expanded debugging/prediction:** verify runtime traces match actual
  statements; suggested names/methods match scope/type and linked API support.
  Accept/cancel suggestions without corrupting source/undo or consuming keystrokes.
  New debugger capabilities need their own tests when implemented.

Event-driven availability and library sharing are deferred and outside this pass.

## Release-level administrator testing

Provider exception/recovery injection, interrupted saves, corrupt metadata,
large-network churn and sustained server load require separate controlled
fixtures. Ordinary manual success does not replace those checks. Review the
existing operator recovery workflow; never erase recovery data to make a test pass.

## Editor feedback and cable placement checks

- [ ] Full cable in a 3x3 grid: left column gives legacy cable; right gives Fancy; center gives neither. Fancy/legacy revert to our full cable in any slot. Each conversion consumes one and produces one.
- [ ] In Studio, nested two-space indents show faint vertical guides. Check horizontal scrolling, wrapping, zoom and selections; guides must stay in leading whitespace.
- [ ] Place the caret beside braces or DO/THEN/END. Matching delimiters highlight without changing source. Test nested mixed blocks, ELSE IF, comments, strings and emoji.


## Trigger-level BREAK

- [ ] Use a timer trigger that prints before BREAK and after it. Only the first PRINT appears once per scheduled run; a second due trigger still runs. Put BREAK inside an IF, then after WAIT, and confirm no skipped statements replay on the next run.
- [ ] Inside a loop, BREAK still exits only that loop and the remaining trigger statements run. A BREAK outside loops in a player function must remain a compiler error. Confirm redstone triggers await the next matching pulse/edge, without a synthetic restart.
