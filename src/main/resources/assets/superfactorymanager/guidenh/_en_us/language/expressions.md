---
navigation:
  title: "Expressions"
  parent: /language/index.md
  position: 40
  keywords: ["sfml", "expressions", "arithmetic", "boolean", "precedence"]
categories: ["sfm", "language"]
updated: "2026-10-28"
---

# Expressions

Expressions calculate values for assignments, conditions, function arguments and transfer limits. Types are checked before execution. Integers do not become booleans automatically, and equality requires same-type scalar values. Arrays and structs do not support equality comparisons.

Write multiplication with whitespace on both sides: `$a * $b` or `2 * 3`. Compact forms such as `2*3` retain legacy resource wildcard token rules and are rejected as arithmetic. `--` starts a comment, so subtracting a negative value should look like `$a - (-2)`.

## Arithmetic and logic

`+`, `-`, `*`, `/` and `%` operate on signed 64-bit integers. Addition, subtraction, multiplication, negation and division check overflow. Arithmetic does not wrap or saturate. The minimum integer literal is `-9223372036854775808`; dividing it by `-1` faults.

Division truncates toward zero: `-7 / 3` is `-2`. Remainder has the dividend's sign: `-7 % 3` is `-1`. Division and remainder by zero fault. The minimum integer's remainder by `-1` is zero.

Comparisons are `>`, `<`, `=`, `==`, `!=`, `>=` and `<=`, with word forms `GT`, `LT`, `EQ`, `GE` and `LE`. Both `=` and `==` test equality; `!=` tests inequality. Declarations and assignments (`$x = value` or `SET $x = value`) require a single `=` delimiter. Comparisons do not chain: write `$a < $b AND $b < $c`. Exact ITEM_KEY equality includes full NBT automatically; stack quantity is separate.

`NOT`, `AND` and `OR` require booleans. `AND` and `OR` short-circuit from left to right. `FALSE AND 1 / 0 > 0` skips the division. Skipped code must still contain valid types and names.

Their symbolic aliases are `!`, `&&` and `||`, preserving the same precedence and short-circuit behavior. Use explicit grouping, for example `!($a == $b)`. Single `&` and `|` remain integer bitwise operations.

```sfml
NAME "Bounded batch"
EVERY 20 TICKS DO
  LET $requested = 12
  LET $batch = CLAMP($requested * 2, 0, 64)
  LET $ready = $batch > 0
  IF ($ready AND $batch <= 64) THEN
    INPUT ($batch) item:minecraft:iron_ingot FROM source
    OUTPUT TO dest
  END
END
```

## Precedence

Highest precedence comes first:

| Order | Operators |
| --- | --- |
| 1 | Member/index access and parenthesized expressions |
| 2 | Unary `+`, `-`, `~` |
| 3 | `*`, `/`, `%` |
| 4 | `+`, `-` (`PLUS` also means plus) |
| 5 | `<<`, `>>`, `>>>` |
| 6 | `&` |
| 7 | `^` |
| 8 | `\|` |
| 9 | Comparisons |
| 10 | `NOT` / `!` |
| 11 | `AND` / `&&` |
| 12 | `OR` / `\|\|` |

Binary arithmetic, shifts and bitwise operations associate left to right. Use parentheses whenever grouping would otherwise be hard to read. For example, `NOT $a = $b` means `NOT ($a = $b)`.

## Conditions and captured limits

Use parentheses for a typed expression in `IF` or `WHILE`, including a lone boolean: `IF ($ready) THEN`. Legacy `HAS` and manager `REDSTONE` conditions use their own condition syntax. They may combine with expression conditions in the outer boolean grammar, but cannot appear inside an ordinary data expression. Typed query methods can observe inventory or native-port data from expressions.

Wrap a computed quantity or threshold in parentheses at `INPUT`, `OUTPUT`, `RETAIN`, `HAS` or a redstone level comparison. It must evaluate to a nonnegative integer; zero is valid.

```sfml
NAME "Input snapshot"
EVERY 20 TICKS DO
  LET $n = 8
  INPUT ($n) item:minecraft:iron_ingot FROM source
  SET $n = 32
  OUTPUT 3 item:minecraft:iron_ingot TO dest
  OUTPUT ($n) item:minecraft:iron_ingot TO dest
END
```

The input captures an allowance of eight. If the first output moves three, the second can use at most the remaining five. Changing `$n` does not change the registered input. Each output validates and captures all its limits before beginning that statement's transfers.

A runtime arithmetic, query or budget fault stops the invocation and pauses the manager. A failed assignment preserves its old value. Successful writes and transfers from earlier statements remain completed.

Next: [Bitwise operators](bitwise.md). Related: [Functions](functions.md), [Loops](loops.md) and [Typed queries](/automation/queries.md).
