---
navigation:
  title: "INPUT and OUTPUT"
  parent: /automation/index.md
  position: 120
  keywords: ["sfm", "input", "output", "transfer", "quota"]
categories: ["sfm", "automation"]
updated: "2026-10-28"
---
# INPUT and OUTPUT

INPUT registers a source declaration for later OUTPUT statements in the current trigger. It does not immediately extract a chest into a hidden inventory. OUTPUT attempts real transfers from the active inputs, subject to source and destination filters, remaining allowances, capacity and machine access rules.

```sfml
NAME "Priority then overflow"
EVERY 20 TICKS DO
  INPUT 16 item:minecraft:iron_ingot FROM supply
  OUTPUT RETAIN 8 item:minecraft:iron_ingot TO priority
  OUTPUT item:minecraft:iron_ingot TO overflow
END
```

The priority output fills toward eight existing-plus-new ingots. Overflow then uses what remains of the same sixteen-ingot input allowance. If priority receives three, overflow can receive at most thirteen. OUTPUT keeps the input declaration active; successful movement consumes its allowance across later outputs. Another output does not renew that allowance.

Several INPUT statements can be active together. An OUTPUT can use their eligible resources, so isolate independent stages with [FORGET](/automation/forget.md). A new INPUT registration has its own quota. Inputs are cleared when the trigger ends, including inputs declared inside an executed branch. Branch boundaries alone do not clear them.

Inside loops, each executed INPUT creates an independent registration. At most 256 loop INPUT registrations are admitted per manager invocation; FORGET does not restore that work allowance. Avoid registering a growing list of inputs when one shared source declaration will do.

The basic statement forms are:

```text
INPUT [limits] [EXCEPT selectors] FROM [EACH] labels [access modifiers]
OUTPUT [limits] [EXCEPT selectors] TO [EACH] labels [access modifiers]
OUTPUT [limits] TO EMPTY SLOTS IN labels [access modifiers]
```

Leave limits out to allow all matching resources the endpoints can move. Prefer an explicit item/fluid filter where both handler types may be present. Quantities are maxima; a batch may partially complete. Missing labels, disconnected blocks, no active input, an exhausted allowance, empty source, full destination or a refusing machine can produce zero movement.

Items use inventory extraction and insertion rules, including sided inventories. Ordinary fluid routing uses Forge fluid handlers and millibucket quantities. Optional Steve's Factory Manager compatibility adds supported legacy handlers when that mod is present. A matching label or a successful compile does not grant arbitrary machine, cover, proxy or tank support. Test the specific block and face.

For computed limits, put an integer expression in parentheses:

```sfml
NAME "Computed batch"
EVERY 20 TICKS DO
  LET $batch = 4 * 2
  INPUT ($batch) item:minecraft:iron_ingot FROM supply
  OUTPUT RETAIN (16 + $batch) item:minecraft:iron_ingot TO reserve
END
```

The INPUT captures its evaluated limits when registered. Later variable changes do not rewrite that registration. Negative evaluated quantities fault; a zero movement allowance permits no movement. See [quantities](/automation/quantities.md) for EACH and RETAIN, and [sides and slots](/automation/sides-slots.md) for access modifiers.

Ordinary IO does not return a moved count to your program. When you need a result record for a computed item request, use [TRANSFER](/automation/transfers.md). Its request is independent of these active INPUT declarations and retention limits.

Confirmed earlier movements are committed even if later work faults. A runtime fault pauses execution; it does not undo the whole trigger. [Recovery](/reference/recovery.md) explains retained resources and uncertain transfers.
