---
navigation:
  title: "Getting started"
  position: 100
  icon: "superfactorymanager:manager"
  parent: /index.md
  keywords: ["tutorial", "setup", "beginner"]
categories: ["sfm"]
updated: "2026-11-01"
---

# Getting started

Your first goal is small: **one chest supplies one item to another chest**.

<ItemImage id="superfactorymanager:manager" scale="2" />
<ItemImage id="superfactorymanager:disk" scale="2" />
<ItemImage id="superfactorymanager:labelgun" scale="2" />
<ItemImage id="superfactorymanager:cable" scale="2" />

1. **[Build the network](hardware.md)** — connect a manager and two chests.
2. **[Name the chests](labels.md)** — save source and destination labels.
3. **[Run your first program](first-transfer.md)** — copy, save and check the result.

**Stuck? [Nothing moving?](troubleshooting.md)** checks the first route.

## Two ways to write blocks

SFML accepts both styles. They have the same behavior:

| Block | Braces | Keywords |
| --- | --- | --- |
| Trigger or loop | `every 20 ticks { ... }` | `every 20 ticks do ... end` |
| Condition | `if (condition) { ... }` | `if (condition) then ... end` |

The dots stand for the statements inside the block. Close each block with its
matching `}` or `end`. Indentation helps you read nesting; the delimiters decide
where a block ends. Keywords can be lowercase or uppercase.

**Semicolons are optional** after ordinary statements. The beginner examples
omit them. A Java-style `for (start; condition; update)` loop still needs the two
semicolons separating its header parts; that is covered in the Advanced guide.

## After it works

- [Change your route](change-route.md): try a small batch, item filter or stock target.
- [Choose an editor](editors.md): keep using Text, try Visual, or save Studio projects.

The [Advanced guide](/advanced/index.md) holds the detailed routing rules,
language and API reference.
