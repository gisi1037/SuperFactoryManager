---
navigation:
  title: "Loops"
  parent: /language/index.md
  position: 20
  keywords: ["sfml", "loops", "for", "while", "budgets"]
categories: ["sfm", "language"]
updated: "2026-10-28"
---

# Loops

Use counted `FOR` for an integer range, array `FOR` to inspect a sequence, and `WHILE` when a condition decides whether another iteration is needed. All run within finite shared budgets.

```sfml
NAME "Loop examples"
STATE $total = 0
EVERY 20 TICKS DO
  LET $items = [{count: 2}, {count: 5}]
  FOR $item IN $items DO
    SET $total = $total + $item.count
  END
  FOR $i = 1 TO 3 DO
    PRINT("index", $i)
  END
  FOR $i = 3 TO 1 STEP -1 DO
    PRINT("countdown", $i)
  END
  LET $attempt = 0
  WHILE ($attempt < 3) DO
    SET $attempt = $attempt + 1
  END
END
```

The array loop adds seven to `$total`. The counted loops visit `1, 2, 3` and `3, 2, 1`. The `WHILE` body executes three times.

## Range and snapshot rules

Counted bounds are inclusive. Start, end and `STEP` evaluate once, from left to right, before the loop body. The default step is positive one; count down with a negative step. Zero step faults before the body, even for an otherwise empty range. A range pointing the wrong way is empty. Reaching a signed-64-bit endpoint does not cause an extra overflow after the last valid iteration.

Array `FOR` evaluates its source once and visits the resulting immutable snapshot in index order. Replacing the source variable during the loop does not change that traversal. Bounded contents and location snapshots also support iteration; see [Typed queries](/automation/queries.md).

`WHILE` tests its condition before every body execution. Parenthesize typed expressions as in `IF`. Legacy `HAS` and `REDSTONE` conditions are also supported in trigger loops.

## Scope

The iteration variable is local to the loop and read-only, including any array or struct members. Copy it into a `LET` local if you need to change a value. It cannot shadow a visible name and is unavailable after `END`.

Body locals are initialized afresh each iteration. A loop body may `SET` visible outer locals and state. `BREAK`, `CONTINUE` and brace-delimited loop bodies are unsupported. In a function, `RETURN` can exit a loop and the function together.

## Shared budgets

All nested loops, player function calls and due triggers share **1,024 loop steps per manager invocation**. Each `FOR` body entry consumes one step. Every `WHILE` condition test consumes one, including the final false test, before it observes the world. Thus 1,023 `WHILE` body executions can leave room for a final false test; 1,024 body executions cannot.

Loop nesting is limited to 16. Every executed statement in a loop also consumes expression work, including legacy statements. Expression, allocation and query limits remain shared, and `PRINT` retains its separate sixteen-line allowance. Calling a function or reaching another due trigger does not refresh these budgets.

## Transfers inside loops

Each executed `INPUT` registers an independent captured allowance. These accumulate until `FORGET` or trigger end. An input outside the loop retains one allowance across repeated outputs. Up to 256 input registrations executed within loops are allowed per invocation; `FORGET` does not reset that cumulative count.

A budget fault stops execution while retaining earlier state writes and completed transfers. Loops do not yield between extraction and insertion/recovery. These bounds prevent an endless SFML loop, but do not impose a fixed duration on an inventory scan or a third-party callback.

Next: [Functions](functions.md). Related: [Collections](collections.md) and [Expressions](expressions.md).
