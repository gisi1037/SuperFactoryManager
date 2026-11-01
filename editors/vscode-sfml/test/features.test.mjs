import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import { readFile } from 'node:fs/promises';
import test from 'node:test';
const require = createRequire(import.meta.url);
const { analyze, hover } = require('../src/language.cjs');
const { Compiler } = require('../src/compiler.cjs');
const catalog = require('../data/functions.json');

test('variable colors exclude comments and escaped/multiline strings', () => {
  const source = 'let $a = "$hidden \\\" $alsoHidden\n$stillHidden" -- $comment\n$a = 2';
  assert.deepEqual(analyze(source).variables.map(token => source.slice(token.start, token.end)), ['$a', '$a']);
  assert.equal(analyze('x'.repeat(262145)).variables.length, 0);
});

test('all built-in functions and methods expose signatures and examples', () => {
  for (const [kind, entries] of Object.entries(catalog)) for (const [name, entry] of Object.entries(entries)) {
    const source = (kind === 'methods' ? '$source.' : '') + name.toUpperCase() + '()';
    const result = hover(analyze(source), source.indexOf(name.toUpperCase()) + 1);
    assert.deepEqual(result.signatures, entry.signatures, name);
    assert.ok(result.description.length > 20, name);
    assert.ok(result.example.length > 0, name);
  }
});

test('hover distinguishes selectors, snapshots and exact NBT keys', () => {
  assert.match(hover(analyze('item("*")'), 1).description, /RESOURCE_SELECTOR/);
  assert.match(hover(analyze('$a.contents()'), 5).description, /ITEM_KEY/);
  assert.equal(hover(analyze('$a.contains()'), 5), undefined);
});

test('dynamic-array hovers describe exact element types, REF effects and structural invalidation', () => {
  assert.deepEqual(hover(analyze('array("integer")'), 1).signatures,
    ['array("type literal") -> T[]', 'array(values: sequence<T>) -> T[]']);
  assert.match(hover(analyze('len(values)'), 1).signatures.join('\n'), /len\(values: T\[\]\)/);
  const push = hover(analyze('PUSH(ref values, 1)'), 1);
  const pop = hover(analyze('pop(ref values)'), 1);
  assert.deepEqual(push.signatures, ['push(ref values: T[], value: T) -> void']);
  assert.deepEqual(pop.signatures, ['pop(ref values: T[]) -> T']);
  assert.match(push.description, /256/);
  assert.match(pop.description, /4096/);
  assert.match(push.description, /same-length or ABA/);
  assert.match(pop.description, /whole-root rebind/);
  assert.equal(hover(analyze('"array(\\"integer\\")" -- push(ref values, 1)'), 2), undefined);
});

test('collection helper hovers distinguish dynamic mutators from exact typed sequence readers', () => {
  const signatures = {
    extend: 'extend(ref values: T[], sequence: sequence<T>) -> void',
    insert: 'insert(ref values: T[], index: integer, value: T) -> void',
    removeat: 'removeAt(ref values: T[], index: integer) -> T',
    clear: 'clear(ref values: T[]) -> void',
    peek: 'peek(values: sequence<T>) -> T',
    contains: 'contains(values: sequence<T>, item: T) -> boolean',
    indexof: 'indexOf(values: sequence<T>, item: T) -> integer'
  };
  for (const [name, signature] of Object.entries(signatures))
    assert.deepEqual(hover(analyze(name.toUpperCase() + '()'), 1).signatures, [signature]);
  assert.match(catalog.functions.array.description, /Any sequence/);
  assert.match(catalog.functions.extend.description, /empty source is a no-op/);
  assert.match(catalog.functions.clear.description, /does not change the structural epoch/);
  assert.match(catalog.functions.peek.description, /empty sequence faults/);
  assert.match(catalog.functions.contains.description, /NBT keys/);
  assert.match(catalog.functions.contains.description, /does not add collection equality/);
  assert.match(catalog.functions.indexof.description, /first zero-based/);
  assert.match(catalog.functions.indexof.description, /-1/);
  assert.equal(hover(analyze('$source.contains()'), 10), undefined);
});

test('hover ignores comments, strings, fields and identifiers that are not calls', () => {
  for (const source of ['-- transfer()', '"transfer()"', '$transfer', '$row.total']) {
    const offset = source.search(/transfer|min|total/);
    assert.equal(hover(analyze(source), offset + 1), undefined, source);
  }
});

test('plain variables and optional sigils share declarations, with scoped reference hovers', () => {
  const source = 'state runs = 0;\n'
    + 'every 20 ticks { let source = label("source"); let row = {source: 3};\n'
    + 'print(source.count(item("minecraft:stone")), row.source); $runs = runs + 1;\n'
    + 'if (true) { let hidden = 1; print(hidden); } print(hidden); }\n'
    + '-- runs source\n"runs source"';
  const result = analyze(source);
  assert.deepEqual(result.variables.map(token => source.slice(token.start, token.end)),
    ['runs', 'source', 'row', 'source', 'row', '$runs', 'runs', 'hidden', 'hidden']);
  assert.match(hover(result, source.indexOf('$runs') + 1).description, /Persistent disk STATE/);
  assert.deepEqual(hover(result, source.indexOf('source.count') + 2).signatures, ['let source']);
  assert.deepEqual(hover(result, source.indexOf('.count') + 2).signatures, catalog.methods.count.signatures);
  assert.equal(hover(result, source.indexOf('row.source') + 5), undefined);
  assert.equal(hover(result, source.lastIndexOf('hidden') + 1), undefined);
});

test('typed brace functions support spaced parameters, structs, arrays, void and aliases', () => {
  const source = 'integer[2] math.copy(integer[2] values, {n: integer} row) { return values; }\n'
    + 'void publish(string message) { print(message); return; }\n'
    + 'function old(value: integer) returns integer { return value; }\n'
    + 'every 20 ticks { let values = [1,2]; math.copy(values, {n: 3}); publish("ok"); old(1); }';
  const result = analyze(source);
  assert.equal(result.functions.size, 3);
  assert.match(hover(result, source.lastIndexOf('math.copy') + 6).signatures[0], /^integer\[2\] math.copy/);
  assert.match(hover(result, source.lastIndexOf('publish') + 1).signatures[0], /^void publish\(string message\)/);
  assert.match(hover(result, source.lastIndexOf('old') + 1).signatures[0], /^function old/);
  assert.deepEqual(hover(result, source.indexOf('return values') + 8).signatures, ['integer[2] values']);
  assert.deepEqual(hover(result, source.indexOf('print(message)') + 7).signatures, ['string message']);
  assert.equal(hover(result, source.indexOf('integer[2]') + 1), undefined);
});

test('dynamic, fixed and nested struct dimensions retain complete function and parameter hovers', () => {
  const source = 'integer[][2] rows.copy(ref integer[][2] values, {tags: string[], fixed: integer[2]} row) { return values; }\n'
    + '{rows: {value: integer}[][2]}[] records(ref {rows: {value: integer}[][2]}[] values) { return values; }\n'
    + 'function legacy(ref values: integer[2][]) returns integer[2][] do return values end\n'
    + 'every 20 ticks { rows.copy(ref pairs, record); records(ref entries); legacy(ref groups); }';
  const result = analyze(source);
  assert.equal(result.functions.size, 3);
  assert.match(hover(result, source.lastIndexOf('rows.copy') + 6).signatures[0], /^integer\[\]\[2\] rows.copy/);
  assert.match(hover(result, source.lastIndexOf('records') + 1).signatures[0], /^\{rows: \{value: integer\}\[\]\[2\]\}\[\] records/);
  assert.match(hover(result, source.lastIndexOf('legacy') + 1).signatures[0], /returns integer\[2\]\[\]/);
  assert.deepEqual(hover(result, source.indexOf('return values') + 8).signatures, ['ref integer[][2] values']);
  assert.match(hover(result, source.indexOf('return values') + 8).description, /Reference parameter/);
});

test('legacy blocks, loop bindings, result declarations and shadowing remain visible only in scope', () => {
  const source = 'let amount = 1\nevery 20 ticks do\n'
    + 'if (true) then let amount = 2 print(amount) else print(amount) end\n'
    + 'for index = 0 to 2 do print(index) end print(index)\n'
    + 'transfer(label("source"), label("dest")) into result print(result.moved)\nend';
  const result = analyze(source);
  const amounts = result.variables.filter(token => token.text === 'amount');
  assert.equal(amounts.length, 4);
  assert.equal(amounts[2].binding, amounts[1].binding);
  assert.equal(amounts[3].binding, amounts[0].binding);
  assert.equal(hover(result, source.lastIndexOf('index') + 1), undefined);
  assert.deepEqual(hover(result, source.indexOf('result.moved') + 1).signatures, ['into result']);
});

test('partial headers and hostile nesting stay presentation-only without throwing', () => {
  for (const source of ['void f(integer', 'integer[2] f({x: integer', 'integer[][ f(ref integer[] values)',
    'function f(x: integer) returns',
    '{x:'.repeat(2000) + 'integer' + '}'.repeat(2000)]) {
    assert.doesNotThrow(() => analyze(source));
  }
});

test('struct values and collection-loop headers preserve enclosing bindings', () => {
  const source = 'every 20 ticks { let amount = 3; let rows = [{amount:amount}]; '
    + 'for row in [{amount:amount}] { print(row.amount); } print(row); }';
  const result = analyze(source);
  assert.equal(result.variables.filter(token => token.text === 'amount').length, 3);
  assert.equal(result.variables.filter(token => token.text === 'row').length, 2);
  assert.equal(hover(result, source.lastIndexOf('row') + 1), undefined);
});

test('Java-style loop initializer hovers are mutable and end with the loop scope', () => {
  const source = 'every 20 ticks { let values = [1,2]; '
    + 'for (let Index = 0; $Index < len(values); Index = Index + 1) { '
    + 'let child = {n: Index}; print(Index, child.n); } print(Index, child, values); }';
  const result = analyze(source);
  const indices = result.variables.filter(token => token.text === 'Index' || token.text === '$Index');
  assert.equal(indices.length, 6);
  assert.ok(indices.every(token => token.binding === indices[0].binding));
  assert.equal(indices[0].binding.readOnly, false);
  assert.deepEqual(hover(result, source.indexOf('$Index') + 1).signatures, ['let Index']);
  assert.match(hover(result, source.indexOf('$Index') + 1).description, /Mutable FOR initializer/);
  assert.equal(hover(result, source.lastIndexOf('Index') + 1), undefined);
  assert.equal(hover(result, source.lastIndexOf('child') + 1), undefined);
  assert.deepEqual(hover(result, source.lastIndexOf('values') + 1).signatures, ['let values']);
});

test('indexed foreach declarations bind only the body and preserve source and outer bindings', () => {
  const source = 'every 20 ticks do let Index = [1,2] '
    + 'for (Index, $Value) in Index do print($Index, Value) end print(Index, Value) '
    + 'for ($position, row) in [{n: Index}] do print(position, row.n) end print(position, row) end';
  const result = analyze(source);
  const indices = result.variables.filter(token => token.text === 'Index' || token.text === '$Index');
  assert.equal(indices.length, 6);
  assert.equal(indices[2].binding, indices[0].binding, 'source resolves the outer binding');
  assert.equal(indices[3].binding, indices[1].binding, 'body resolves the index binding');
  assert.equal(indices[4].binding, indices[0].binding, 'outer binding returns after the loop');
  assert.equal(indices[1].binding.readOnly, true);
  assert.deepEqual(hover(result, source.indexOf('print($Index') + 8).signatures, ['for Index: integer']);
  assert.match(hover(result, source.indexOf('Value) end') + 1).description, /Read-only loop value/);
  assert.equal(hover(result, source.lastIndexOf('Value') + 1), undefined);
  assert.equal(hover(result, source.lastIndexOf('position') + 1), undefined);
  assert.equal(hover(result, source.lastIndexOf('row') + 1), undefined);
});

test('nested new loop scopes, case sensitivity and partial headers remain bounded', () => {
  const source = 'every 20 ticks { for (let Index = 0; Index < 2; Index = Index + 1) { '
    + 'for (position, value) in [Index] { print(position, value, Index, index); } '
    + 'print(position, value, Index); } print(Index); }';
  const result = analyze(source);
  assert.equal(hover(result, source.indexOf(', index') + 3), undefined);
  assert.equal(hover(result, source.indexOf('print(position, value, Index);') + 7), undefined);
  assert.equal(hover(result, source.lastIndexOf('Index') + 1), undefined);
  for (const header of ['for (let index =', 'for (index,', 'for (' + '('.repeat(2000),
    'for (let index = {n: 0}; index.n < 2; index.n = index.n + 1) { print(index); }'])
    assert.doesNotThrow(() => analyze(header));
});

test('ref parameters retain signatures and distinguish writable references from value copies', () => {
  const source = 'void increment(ref integer value, integer amount) { value = value + amount; }\n'
    + 'function legacy(ref $value: integer) returns void do $value = $value + 1 end\n'
    + 'every 20 ticks { let count = 0; increment(ref count, 1); legacy(ref count); }';
  const result = analyze(source);
  assert.equal(result.functions.size, 2);
  assert.match(hover(result, source.lastIndexOf('increment') + 1).signatures[0], /ref integer value/);
  assert.match(hover(result, source.lastIndexOf('legacy') + 1).signatures[0], /ref \$value: integer/);
  assert.deepEqual(hover(result, source.indexOf('value =') + 1).signatures, ['ref integer value']);
  assert.match(hover(result, source.indexOf('value =') + 1).description, /immediately/);
  assert.match(hover(result, source.indexOf('+ amount') + 3).description, /local copy/);
  assert.deepEqual(hover(result, source.lastIndexOf('count') + 1).signatures, ['let count']);
  assert.equal(hover(result, source.indexOf('ref count') + 1), undefined);
});

test('same-file function signatures support namespaces, multiline types and exact-name overrides', () => {
  const source = 'function stock.sum(\n $values: {n: integer}[2]\n) returns integer do return 0 end\n'
    + 'let $n = stock.sum($values)';
  const result = hover(analyze(source), source.lastIndexOf('sum') + 1);
  assert.match(result.signatures[0], /\$values: \{n: integer\}\[2\]/);
  assert.match(result.description, /Player-defined/);
  const override = 'function min($n: integer) returns integer do return $n end\nmin(1) MIN(1,2)';
  assert.match(hover(analyze(override), override.lastIndexOf('min') + 1).description, /Player-defined/);
  assert.match(hover(analyze(override), override.lastIndexOf('MIN') + 1).description, /smaller/);
});

test('method chains and punctuation have precise hover ranges', () => {
  const source = 'label("stock").side("up").contents()';
  const result = hover(analyze(source), source.indexOf('contents') + 3);
  assert.equal(source.slice(result.start, result.end), 'contents');
  assert.equal(hover(analyze(source), source.indexOf('()')), undefined);
});

test('compiler queue keeps only the latest pending version per document', async () => {
  const compiler = new Compiler('unused');
  compiler.busy = true; // Hold the wire to inspect bounded pending requests independently of Java.
  const first = compiler.check('a', 'old');
  const latest = compiler.check('a', 'new');
  assert.equal(await first, null);
  assert.equal(compiler.queue.size, 1);
  assert.equal(compiler.queue.get('a').source, 'new');
  compiler.queue.get('a').resolve([]);
  assert.deepEqual(await latest, []);
  compiler.dispose();
});

test('compiler queue caps documents and source payloads', async () => {
  const compiler = new Compiler('unused');
  compiler.busy = true;
  const requests = Array.from({length: 17}, (_, i) => compiler.check(String(i), 'x'.repeat(40000)).catch(() => null));
  assert.equal(await requests[0], null);
  assert.equal(compiler.queue.size, 16);
  assert.ok([...compiler.queue.values()].every(entry => entry.source.length === 32768));
  compiler.dispose();
  await Promise.all(requests);
});

test('missing compiler manifests are tooling failures, not successful compilation', async () => {
  const compiler = new Compiler(new URL('does-not-exist.json', import.meta.url));
  try { await assert.rejects(compiler.check('a', 'every 20 ticks do end'), /ENOENT/); }
  finally { compiler.dispose(); }
});

test('the Node client communicates with the actual Java compiler', {skip: !process.env.SFML_COMPILER_MANIFEST}, async () => {
  const compiler = new Compiler(process.env.SFML_COMPILER_MANIFEST);
  try {
    assert.deepEqual(await compiler.check('a', 'every 20 ticks do end'), []);
    const errors = await compiler.check('a', 'every 20 ticks do\n $missing = 1\nend');
    assert.ok(errors.length > 0);
    assert.match(errors[0].message, /Unknown variable/);
    assert.ok((await compiler.check('a', 'every 20 ticks do let $n = 1 $n = true end')).length > 0);
    assert.deepEqual(await compiler.check('a', 'let $n = 0 every 20 ticks do $n = $n + 1 end'), []);
    assert.deepEqual(await compiler.check('a', 'every 20 ticks { let values = array("integer"); '
      + 'push(ref values, 1); let last = pop(ref values); let seeded = array([last, 2]); '
      + 'print(last, len(values), len(seeded)); }'), []);
    assert.deepEqual(await compiler.check('a', 'every 20 ticks { let values = array([1, 2]); '
      + 'extend(ref values, [3]); insert(ref values, 1, 9); let removed = removeAt(ref values, 0); '
      + 'print(peek(values), contains(values, 9), indexOf(values, 9), removed); clear(ref values); '
      + 'extend(ref values, array("integer")); let rows = array(label("source").contents()); }'), []);
    assert.deepEqual(await compiler.check('a', 'every 20 ticks { let values = [1, 2]; '
      + 'for (let index = 0; index < len(values); index = index + 1) { print(values[index]); } '
      + 'for (index, value) in values { print(index, value); } }'), []);
    assert.deepEqual(await compiler.check('a', 'integer sum(integer[2] values) { let total = 0; '
      + 'for (index, value) in values { total = total + index + value; } return total; } '
      + 'every 20 ticks do for (let index = 0; index < 2; index = index + 1) do print(sum([1,2])) end end'), []);
    for (const source of [
      'every 20 ticks { for (let index = 0; index < 2; index = index + 1) {} print(index); }',
      'every 20 ticks { for (let index = 0; index < 2; child = child + 1) { let child = 0; } }',
      'every 20 ticks { for (index, value) in [1,2] { index = 1; } }',
      'every 20 ticks { for (index, value) in [1,2] { value = 1; } }',
      'every 20 ticks { for (index, value) in value {} }'
    ]) assert.ok((await compiler.check('a', source)).length > 0, source);
  } finally { compiler.dispose(); }
});
