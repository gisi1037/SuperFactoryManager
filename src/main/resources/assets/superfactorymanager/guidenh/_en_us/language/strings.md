---
navigation:
  title: "Strings"
  parent: /language/index.md
  position: 50
  keywords: ["sfml", "strings", "text", "escaping"]
categories: ["sfm", "language"]
updated: "2026-10-28"
---

# Strings

A quoted data value is an immutable `STRING`. A string variable may hold text of different lengths, while retaining its type.

```sfml
NAME "String values"
STATE $message = ""
EVERY 20 TICKS DO
  LET $prefix = "Iron"
  SET $message = $prefix + " ready"
  IF ($message = "Iron ready") THEN
    PRINT($message, "characters", LEN($message))
  END
END
```

Use `+` to concatenate two strings and `=` or `EQ` to compare their exact contents. For inequality, write `NOT ($a = $b)`. String ordering, indexing, interpolation and implicit integer-to-string conversion are unsupported. For debugging, pass mixed values as separate `PRINT` arguments instead of concatenating them.

## Escapes

These escapes apply to data strings:

| Escape | Value |
| --- | --- |
| `\"` | Double quote |
| `\\` | Backslash |
| `\n` | Newline |
| `\r` | Carriage return |
| `\t` | Tab |
| `\b` | Backspace |
| `\f` | Form feed |
| `\uXXXX` | A UTF-16 code unit written as four hexadecimal digits |

Raw Unicode and multiline strings are accepted. Unknown escapes and unpaired UTF-16 surrogates fail compilation. `--` inside a quoted string is text, rather than a comment.

```sfml
NAME "Escaped text"
EVERY 20 TICKS DO
  LET $message = "Line one\nLine two: \"ready\""
  LET $path = "folder\\file"
  PRINT($message, $path)
END
```

## Length and limits

`LEN(string)` counts Unicode code points. It does not count bytes, UTF-16 units or displayed grapheme clusters. For example, an emoji encoded as a valid surrogate pair counts as one code point, while a combining mark counts separately from its base letter.

Each string is limited to 4,096 UTF-16 code units. Compiled constants, runtime-created strings and combined persistent strings each have a separate 16,384-code-unit allowance. Concatenation allocates a new value and charges character work. Comparisons and dynamic length scans also consume the shared expression-work budget.

Persistent strings additionally share the disk's total NBT size limit with source and other data. If a concatenation or `SET` exceeds a limit, the assignment retains its previous value and execution faults. Avoid growing a persistent log on every trigger; use the debug terminal for output.

Next: [Expressions](expressions.md). Related: [Arrays and structs](collections.md) and the [function reference](/reference/functions.md).
