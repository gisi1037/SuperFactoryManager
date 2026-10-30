---
navigation:
  title: "Bitwise operators"
  parent: /language/index.md
  position: 30
  keywords: ["sfml", "bitwise", "flags", "shifts"]
categories: ["sfm", "language"]
updated: "2026-10-30"
---

# Bitwise operators

Bitwise operators inspect and modify the bits of a signed 64-bit integer. They require integer operands and do not convert booleans or strings.

| Operator | Meaning | Example |
| --- | --- | --- |
| `&` | Bitwise AND | `6 & 3` gives `2` |
| `\|` | Bitwise OR | `6 \| 3` gives `7` |
| `^` | Bitwise XOR | `6 ^ 3` gives `5` |
| `~` | Invert all 64 bits | `~0` gives `-1` |
| `<<` | Shift left and fill low bits with zero | `3 << 2` gives `12` |
| `>>` | Shift right and copy the sign bit | `-8 >> 2` gives `-2` |
| `>>>` | Shift right and fill high bits with zero | `-1 >>> 1` gives `9223372036854775807` |

Shift distances must be integers from zero through 63. Negative distances and distances of 64 or more fault instead of being masked. Bits shifted beyond the 64-bit value are discarded: `1 << 63` is the minimum signed integer, and `1 << 63 << 1` is zero. This differs from checked arithmetic.

```sfml
NAME "Flags"
STATE $flags = 0
EVERY 20 TICKS DO
  LET $ready = 1 << 2
  SET $flags = $flags | $ready
  IF ($flags & $ready = $ready) THEN
    SET $flags = $flags & ~$ready
  END
END
```

This sets bit 2, tests it and clears it. Bit positions start at zero. The mask `1 << 2` has decimal value four.

Precedence from highest to lowest is member/index access and grouping; unary `+`, `-`, `~`; multiplication/division/remainder; addition/subtraction; shifts; `&`; `^`; `|`; comparisons; `NOT`; `AND`; `OR`. Binary operators at each arithmetic or bitwise level associate left to right.

Consequently, `$flags & $ready = $ready` means `($flags & $ready) = $ready`, and `1 << 2 + 1` means `1 << (2 + 1)`. Use parentheses to make masks clear when mixing operators.

Binary bitwise operators evaluate both operands from left to right. Boolean `AND` and `OR` provide short-circuit logic; bitwise `&` and `|` do not.

Next: [Loops](loops.md). Related: [Expressions](expressions.md) for checked arithmetic and captured quantities.
