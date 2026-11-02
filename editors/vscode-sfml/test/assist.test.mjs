import assert from 'node:assert/strict';
import { createRequire } from 'node:module';
import test from 'node:test';
const require = createRequire(import.meta.url);
const { completions, signatureHelp, parameters } = require('../src/assist.cjs');

test('completion replaces just the current token and separates receivers and functions', () => {
  let text = 'every 20 ticks { let source = label("in") source.em';
  const result = completions(text, text.length);
  assert.ok(result.some(item => item.name === 'empty'));
  assert.ok(result.every(item => item.kind === 'method'));
  assert.equal(result[0].start, text.length - 2);
  assert.equal(completions('print("em', 9).length, 0);
  assert.equal(completions('-- em', 5).length, 0);
  assert.equal(completions('item:em', 7).length, 0);
  assert.equal(completions('x'.repeat(30001), 30001).length, 0);
});

test('variables are lexical, functions do not receive RAM globals, and legacy sigils remain supported', () => {
  const text = 'let global = 1 state shared = 2 integer f(integer n) { let local = 3 lo } every 20 ticks { gl }';
  assert.ok(completions(text, text.indexOf('lo }') + 2).some(item => item.name === 'local'));
  const body = completions(text, text.indexOf('lo }'));
  assert.ok(body.some(item => item.name === 'shared'));
  assert.ok(!body.some(item => item.name === 'global'));
  const trigger = completions(text, text.indexOf('gl }') + 2);
  assert.ok(trigger.some(item => item.name === 'global'));
  assert.ok(!trigger.some(item => item.name === 'local'));
  const legacy = 'every 20 ticks { let value = 1 print($va';
  assert.ok(completions(legacy, legacy.length).some(item => item.name === '$value'));
});

test('qualified player functions and nested call signature help preserve exact names', () => {
  const source = 'integer math.Need(integer n) { return n } every 20 ticks { math.Ne';
  assert.ok(completions(source, source.length).some(item => item.name === 'Need'));
  const call = 'transfer(label("x,y"), label("out"), (1 + 2)';
  const help = signatureHelp(call, call.length);
  assert.equal(help.argument, 2);
  assert.match(help.entry.signatures[0], /transfer\(/);
  assert.equal(signatureHelp('print(1)', 8), undefined);
  assert.equal(signatureHelp('print( -- comment', 17), undefined);
  assert.equal(signatureHelp('label("open', 11).argument, 0);
  assert.equal(signatureHelp('label("source").slots(0, ', 25).argument, 1);
});

test('signature parameter ranges retain nested struct and sequence types', () => {
  const signature = 'integer fn({a: integer, b: string} value, label_ref[] targets)';
  assert.deepEqual(parameters(signature).map(([start,end]) => signature.slice(start,end).trim()),
    ['{a: integer, b: string} value', 'label_ref[] targets']);
});
