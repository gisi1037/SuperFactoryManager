---
navigation:
  title: "Functions"
  parent: /language/index.md
  position: 10
  keywords: ["sfml", "functions", "parameters", "return", "defaults"]
categories: ["sfm", "language"]
updated: "2026-10-28"
---

# Functions

A player function calculates and returns a typed value. Use it to reuse a calculation or query, then let the trigger perform any actions using the result.

Definitions follow top-level `STATE`/`LET` declarations and precede triggers. Parameters and returns require explicit types; function-local `LET` variables infer their types.

```sfml
NAME "Typed player functions"
STATE $total = 0

FUNCTION math.sum($values: INTEGER[3]) RETURNS INTEGER DO
  LET $sum = 0
  FOR $value IN $values DO
    SET $sum = $sum + $value
  END
  RETURN $sum
END

FUNCTION next($previous: INTEGER) RETURNS INTEGER DO
  RETURN $previous + 1
END

EVERY 20 TICKS DO
  LET $values = [2, 4, 8]
  SET $total = next(math.sum($values))
  PRINT("total", $total)
END
```

This stores and prints fifteen when output capture is enabled. Names such as `next` and namespaced names such as `math.sum` are supported. Function and variable names are case-sensitive; keywords and type names ignore case. A function may call another declared later in the function section.

## Types and value parameters

Signature types are `INTEGER`, `BOOLEAN`, `STRING`, `LABEL_REF`, `RESOURCE_SELECTOR`, `ITEM_KEY`, `ITEM_CONTENTS` and `ITEM_LOCATIONS`. Fixed arrays such as `INTEGER[3]` and structural records such as `{count: INTEGER, name: STRING}` are supported, including nested combinations. Argument and return shapes must match exactly.

Parameters are function-local values. `SET` may replace a parameter or local, including a collection member, without changing the caller's original value. A function cannot read caller locals, program-wide globals or disk state implicitly. Pass those values as arguments, then `SET` the returned result in the trigger.

Arguments evaluate once from left to right. Calls share the caller's budgets and do not turn live queries into cached constants. Query helpers can see inventory changes from earlier transfers.

```sfml
NAME "Player query helper"
FUNCTION stock($src: LABEL_REF, $what: RESOURCE_SELECTOR) RETURNS INTEGER DO
  RETURN $src.count($what)
END

EVERY 20 TICKS DO
  LET $src = LABEL("chest_in").slots(0, 8)
  IF ($src.available()) THEN
    LET $amount = stock($src, ITEM("minecraft:iron_ingot"))
    PRINT("iron", $amount)
  END
END
```

Handles and snapshots can be arguments or return values, but they still cannot be stored in `STATE`. Labels constructed in function bodies count as referenced labels for the program.

## Return flow and allowed work

Bodies support local `LET`/`SET`, typed `IF`/`ELSE`, bounded `FOR`/`WHILE` and `RETURN`. Every reachable path must return the declared type. A return exits nested loops and branches immediately. A loop needs a following return because it may execute zero times. Statements after an unconditional return are rejected.

Functions cannot perform `PRINT`, transfers, `INPUT`/`OUTPUT`, `FORGET`, redstone output or other actions. They cannot mutate persistent state or use bare manager `REDSTONE` conditions. Typed observations such as `$port.redstone()`, `$port.powered()` and `$port.redstoneAvailable()` are allowed. Keep effects in the trigger.

Direct and indirect recursion are rejected, even in unreachable branches. Definitions cannot overload a name or use optional/variadic parameters. There are no void procedures, imports, closures or extension methods. Constructor names `LABEL`, `ITEM` and `ORE`, plus boolean operator spellings, are reserved. `RETURN` is valid only inside a function.

## Default functions

These default functions are available without definitions:

| Function | Result |
| --- | --- |
| `MIN(a, b)` | Smaller of two integers. |
| `MAX(a, b)` | Larger of two integers. |
| `CLAMP(value, low, high)` | Integer restricted to the inclusive bounds; reversed bounds fault. |
| `ABS(value)` | Integer absolute value; the minimum signed integer faults. |
| `LEN(value)` | Array/contents/location outer entry count, or string Unicode code-point count. |

They evaluate arguments once, left to right. Default names ignore case and do not allocate player function frames. Pure default calls may initialize `STATE`; player function calls may not. An exact-name player function takes precedence in ordinary calls. Defining one that conflicts with a default already used by state initialization is rejected.

The [function reference](/reference/functions.md) covers these functions and the typed query, contents and stock helpers. No general string conversion or formatting helper is available.

## Limits

A program supports up to 128 player definitions and sixteen parameters per definition. A function frame supports 256 local bindings. One manager invocation allows 256 player calls, call depth sixteen and 4,096 active function local cells. Functions share loop, expression, allocation, string and query allowances with every due trigger. Returning or failing releases the frame; entering a function does not reset work limits.

Related: [Typed queries](/automation/queries.md), [Loops](loops.md), [Variables and state](variables.md), and the [language overview](index.md).
