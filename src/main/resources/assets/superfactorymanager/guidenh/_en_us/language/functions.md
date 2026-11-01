---
navigation:
  title: "Functions"
  parent: /language/index.md
  position: 10
  keywords: ["sfml", "functions", "parameters", "return", "defaults"]
categories: ["sfm", "language"]
updated: "2026-11-01"
---

# Functions


Define functions after top-level STATE/LET declarations and before triggers.
Prefer the return type first, with space-separated type/name pairs for parameters:
`integer next(integer previous) { ... }`. Names are case-sensitive; keywords and
type names ignore case. Forward calls and names such as `math.sum(...)` work.
The older `FUNCTION next($previous: INTEGER) RETURNS INTEGER DO ... END` spelling
remains supported, as do colon-style parameters and optional `$` prefixes.

```sfml
name "Typed player functions"
state total = 0;

integer math.sum(integer[3] values) {
  let sum = 0;
  for value in values {
    sum = sum + value;
  }
  return sum;
}

integer next(integer previous) {
  return previous + 1;
}

void updateTotal(integer[3] values) {
  total = next(math.sum(values));
  print("total", total);
  return;
}

every 20 ticks {
  updateTotal([2, 4, 8]);
}
```

This stores fifteen and prints it when capture is enabled. Signature types are
`integer`, `boolean`, `string`, `label_ref`, `resource_selector`, `item_key`,
`item_contents`, `item_locations`, `fluid_key`, `fluid_contents`, fixed arrays
such as `integer[3]`, dynamic arrays such as `integer[]`, and records such as
`{count: integer, name: string}`.
Argument and return shapes must match exactly. `void` is a procedure return type,
not a parameter, variable or collection type. Handles/snapshots cannot persist
in STATE, even when a function returns them.

Parameters without REF are values. Assigning a parameter or one of its array/struct members
changes that function's local value, not the caller's original. Functions cannot
implicitly read caller locals or program-wide LET; pass those as arguments.
STATE is directly visible: value-returning functions may read it, while void
procedures may also assign it, including indexed/field updates. There are no
closures. Each manager uses its own live STATE even when sharing a published build.

```sfml
state batches = 0;

integer nextBatch() {
  return batches + 1;
}

void recordBatch() {
  batches = nextBatch();
  print("batches", batches);
}

every 20 ticks {
  recordBatch();
}
```

Value-returning functions may calculate, query, use bounded IF/FOR/WHILE blocks,
read STATE and call `wait(n)`. Every reachable path must return the declared type.
A loop may execute zero times, so provide a return after it. A return exits nested
loops/branches; statements after an unconditional return are rejected.

Void procedures can use PRINT, item/fluid transfers, INPUT/OUTPUT, FORGET,
redstone output and SETCIRCUIT, and call other void procedures. Call them as
statements, never as values or conditions. Bare `return;` ends a procedure early;
reaching its closing brace also finishes it. Value functions cannot call void
procedures or perform those effects. Typed port queries work in either kind.

## Reference parameters

Ordinary parameters remain value copies. Either a void procedure or a value-returning function can opt into a writable
reference with `ref` on both the parameter and argument:

```sfml
state count = 0;

void increment(ref integer value) {
  value = value + 1;
}

every 20 ticks {
  let counters = [1, 2];
  let job = {amount: 0};
  increment(ref count);
  increment(ref counters[0]);
  increment(ref job.amount);
  print(count, counters[0], job.amount);
}
```

A reference target can be a writable whole variable, array element or struct
field. It must have exactly the parameter's type; literals, computed values and
read-only targets are rejected. Omitting REF on either side is an error, not an
implicit conversion.

```sfml
integer takeNext(ref integer cursor) {
  let previous = cursor;
  cursor = cursor + 1;
  return previous;
}

every 20 ticks {
  let next = 0;
  let selected = takeNext(ref next);
  print(selected, next);
}
```

Calls with direct or indirect REF effects cannot run in program-wide LET
initializers or named-redstone subscription expressions. Value functions cannot
directly acquire a STATE reference: a trigger or void procedure must explicitly
pass it. Forwarding a received reference is allowed. Native actions still belong
in triggers or void procedures. Disabled PRINT skips its arguments, so put a
mutating function call in its own assignment first when it must always run.

Writes update the caller's target immediately. Two references to the same target
observe each other's writes; they are not copies written back at return. Target
indices are evaluated once at the call, so changing the index variable while a
call waits does not redirect its reference. Non-reference parameters still copy
their values, even alongside reference parameters.

WAIT retains references safely with the suspended call in RAM; it does not
persist a call stack or reference. Unload/reload cancels the call as usual.
Already completed writes remain subject to normal variable/STATE save rules.

Arguments evaluate once, left to right; AND/OR short-circuit. Queries observe
current loaded providers and can see earlier transfers. Computed LABEL names
use the invocation's bounded label snapshot. Functions receive no direct world
or Java access. See [Waiting between steps](/automation/scheduling.md) for suspended
calls, frozen operands and fresh observations after a wait.

For functions shared between programs, Studio can publish your own
[functions-only library](/advanced/libraries.md) and insert an import pinned to
its exact revision. Libraries cannot implicitly access a root program's STATE
or globals; pass values or explicit REF arguments.

Direct/indirect recursion, duplicate names, overloading, optional/variadic
parameters, nested library imports and extension methods are unsupported. Constructor/action
names and boolean operator spellings are reserved. RETURN belongs inside a
function. Use Text/Studio for definitions and calls; Visual refuses unsupported
constructs without rewriting their source.

Limits are 128 definitions, 16 parameters per definition, 256 local bindings per
frame, 256 calls per manager invocation, call depth 16 and 4,096 active function
local cells. Calls and due triggers share expression, allocation, string, query
and loop allowances. Function entry does not reset them. WAIT additionally bounds
retained stacks; suspension does not permit unlimited memory or work in one tick.
These limits do not preempt slow third-party callbacks.

Related: [default API](/reference/functions.md), [variables and STATE](variables.md), and [loops](loops.md).
