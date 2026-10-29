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

test('hover ignores comments, strings, fields and identifiers that are not calls', () => {
  for (const source of ['-- transfer()', '"transfer()"', '$transfer', '$row.total', 'let $min = 1']) {
    const offset = source.search(/transfer|min|total/);
    assert.equal(hover(analyze(source), offset + 1), undefined, source);
  }
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
  } finally { compiler.dispose(); }
});
