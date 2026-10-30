---
navigation:
  title: "Automation"
  parent: /advanced/index.md
  position: 100
  keywords: ["sfm", "automation", "routing"]
categories: ["sfm", "automation"]
updated: "2026-10-30"
---
# Automation

An SFM program describes when to run, which connected blocks to observe, and where to move resources. Start with [your first transfer](/start/first-transfer.md), then choose the parts you need:

- [Scheduling](/automation/scheduling.md): timers, world time and statement order.
- [INPUT and OUTPUT](/automation/input-output.md): register sources and route their stock.
- [Resource selectors](/automation/selectors.md): choose items, fluids and ore memberships.
- [Quantities and retention](/automation/quantities.md): limit batches, leave reserves and fill targets.
- [Sides and slots](/automation/sides-slots.md): select access faces, slot ranges and round robin.
- [Conditions](/automation/conditions.md): choose routes with HAS and IF.
- [Item queries](/automation/queries.md): use typed label handles for live observations.
- [Contents snapshots](/automation/contents.md): inspect exact variants and physical origins.
- [Value-driven transfers](/automation/transfers.md): move a computed amount and inspect its result.
- [Stock helpers](/automation/stock.md): total snapshots and calculate shortages.
- [Redstone](/automation/redstone.md): manager pulses and named Redstone Ports.
- [FORGET](/automation/forget.md): end an input stage within a trigger.

Ordinary INPUT/OUTPUT supports items and Forge fluids. Typed item queries, contents and TRANSFER currently operate on items. Redstone has its own input, event and output behavior. These interfaces are related, but their selectors, access checks and results differ; each chapter identifies the differences.

Use Text for the full language. Visual offers the basic timer, pulse, input, output, condition, forget and signal nodes; it preserves unsupported source by refusing a lossy import. [Editor details](/start/editors.md) explain how to choose a front end. Locals, STATE, loops and player functions are covered in [the language chapters](/language/index.md).

Every example label must be assigned to an appropriate connected block in your world. A label does not connect a block, load a chunk, grant machine permissions or reserve resources. Check counts and machine sides with a small batch before expanding a production route.
