---
navigation:
  title: "Conditions and branches"
  parent: /automation/index.md
  position: 80
  keywords: ["sfm", "if", "has", "condition", "branch"]
categories: ["sfm", "automation"]
updated: "2026-10-28"
---
# Conditions and branches

IF chooses which statements run. Write IF, a condition, THEN, the chosen statements, and END. ELSE is optional. Text also accepts ELSE IF chains:

```sfml
NAME "Stock-based priority"
EVERY 20 TICKS DO
  INPUT 8 item:minecraft:iron_ingot FROM supply
  IF priority HAS < 16 item:minecraft:iron_ingot THEN
    OUTPUT RETAIN 16 item:minecraft:iron_ingot TO priority
  ELSE IF reserve HAS < 32 item:minecraft:iron_ingot THEN
    OUTPUT RETAIN 32 item:minecraft:iron_ingot TO reserve
  ELSE
    OUTPUT TO overflow
  END
END
```

Only one branch runs. The first true condition wins. Nested ordinary IF statements work in Visual; direct ELSE IF import currently requires Text. Inputs registered before an IF remain active inside the chosen branch and afterward. Inputs registered inside a chosen branch also remain active until FORGET or the end of the trigger.

HAS counts current stock in the connected views selected by its labels, sides, slots and resource filter. Comparison symbols are `>`, `<`, `=`, `==`, `!=`, `<=` and `>=`; word spellings are GT, LT, EQ, LE and GE. Both `=` and `==` test equality. Boolean operators also accept `!`, `&&` and `||` for NOT, AND and OR.

| Before the label | Comparison scope |
| --- | --- |
| Nothing, or OVERALL | Sum stock across all considered blocks. |
| SOME | At least one considered block satisfies the comparison. |
| EVERY, or EACH | Every considered block satisfies it, and at least one block is considered. |
| ONE | Exactly one considered block satisfies it. |
| LONE | At most one satisfies it; zero is allowed. |

Disconnected positions are skipped. Therefore `OVERALL missing HAS = 0` can be true without a connected inventory. EVERY over no considered blocks is false. LONE can be true when none satisfy the condition. Choose an explicit item/fluid filter so the sum does not mix item counts with millibuckets.

HAS observes stock rather than free space or permission. A destination with zero ingots might be full of other items or refuse insertion entirely. For complete item-view availability and stricter missing-group behavior, use [typed item queries](/automation/queries.md).

Boolean conditions support TRUE, FALSE, NOT, AND, OR and parentheses. AND and OR short-circuit. Parentheses make mixed conditions clear:

```sfml
NAME "Supply and signal"
EVERY 20 TICKS DO
  IF (supply HAS >= 8 item:minecraft:iron_ingot) AND (REDSTONE > 0) THEN
    INPUT 8 item:minecraft:iron_ingot FROM supply
    OUTPUT TO dest
  END
END
```

Data expressions must produce booleans; there is no automatic integer/string truthiness. Put a data condition in parentheses, for example `IF ($amount > 0) THEN`. An integer expression can also supply a HAS threshold in parentheses. [Expressions](/language/expressions.md) explains arithmetic and typed comparisons.

A condition does not reserve stock or guarantee the next output succeeds. Queries later in the same invocation see earlier confirmed transfers, while already captured data remains a snapshot. A runtime fault pauses the manager without undoing earlier transfers or STATE writes. Use [TRANSFER's result](/automation/transfers.md) when later logic must distinguish actual movement from an attempted output.
