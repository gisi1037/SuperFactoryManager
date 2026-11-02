const { analyze, hover } = require('./language.cjs');
const catalog = require('../data/functions.json');

// Presentation assistance only. Java remains the sole syntax/type diagnostic authority.
function context(text, offset) {
  if (text.length > 30000 || offset < 0 || offset > text.length) return;
  let string = false, comment = false;
  for (let i = 0; i < offset; i++) {
    if (comment) { if (text[i] === '\n' || text[i] === '\r') comment = false; }
    else if (string) { if (text[i] === '\\') i++; else if (text[i] === '"') string = false; }
    else if (text.startsWith('--', i)) { comment = true; i++; }
    else if (text[i] === '"') string = true;
  }
  return { string, comment };
}

function completions(text, offset) {
  const at = context(text, offset);
  if (!at || at.string || at.comment) return [];
  let start = offset, end = offset;
  while (start > 0 && /[A-Za-z0-9_$]/.test(text[start - 1])) start--;
  while (end < text.length && /[A-Za-z0-9_]/.test(text[end])) end++;
  const prefix = text.slice(start, offset), analysis = analyze(text, offset);
  const before = text.slice(0, start).trimEnd();
  if (before.endsWith(':')) return [];
  const receiver = before.endsWith('.');
  const entries = [];
  const add = (name, entry, kind) => {
    if (entries.length >= 128 || !name.toLowerCase().startsWith(prefix.toLowerCase())) return;
    entries.push({ name, entry, kind, start, end });
  };
  const qualified = receiver && /([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\.$/.exec(before);
  const namespace = qualified && [...analysis.functions.keys()].some(name => name.startsWith(qualified[1] + '.'));
  if (!receiver) {
    for (const entry of analysis.visible || []) {
      const name = entry.token.text.replace(/^\$/, '');
      add(prefix.startsWith('$') ? '$' + name : name, entry, 'variable');
    }
  }
  if (!prefix.startsWith('$')) {
    if (!receiver) for (const [name, entry] of Object.entries(catalog.functions)) {
      if (!analysis.functions.has(name)) add(name, entry, 'function');
    }
    if (receiver && !namespace) for (const [name, entry] of Object.entries(catalog.methods)) add(name, entry, 'method');
    for (const [name, entry] of analysis.functions) {
      if (!receiver) add(name, entry, 'function');
      else if (namespace && name.startsWith(qualified[1] + '.')) add(name.slice(qualified[1].length + 1), entry, 'function');
    }
  }
  return entries;
}

function signatureHelp(text, offset, analysis) {
  const at = context(text, offset);
  if (!at || at.comment) return;
  analysis ||= analyze(text);
  const stack = [];
  for (let i = 0; i < analysis.tokens.length && analysis.tokens[i].start < offset; i++) {
    const token = analysis.tokens[i], c = token.text;
    if (['(', '[', '{'].includes(c)) {
      if (stack.length >= 64) return;
      stack.push({ c, index: i, argument: 0 });
    } else if ([')', ']', '}'].includes(c)) {
      if (!stack.length || stack.pop().c !== {')':'(', ']':'[', '}':'{'}[c]) return;
    } else if (c === ',' && stack.length) stack.at(-1).argument++;
  }
  for (let i = stack.length - 1; i >= 0; i--) {
    const frame = stack[i];
    if (frame.c !== '(' || frame.index === 0) continue;
    const entry = hover(analysis, analysis.tokens[frame.index - 1].start);
    if (entry && !entry.token) return { entry, argument: frame.argument };
  }
}

// Parameter boundaries for VS Code's active-argument underline, including structs and nested array types.
function parameters(signature) {
  const open = signature.indexOf('(');
  if (open < 0) return [];
  let start = open + 1, depth = 0;
  const result = [];
  for (let i = start; i < signature.length; i++) {
    const c = signature[i];
    if ('({<'.includes(c)) depth++;
    else if (c === ')' && depth === 0) { if (signature.slice(start, i).trim()) result.push([start, i]); break; }
    else if (')}>'.includes(c)) depth--;
    else if (c === ',' && depth === 0) { result.push([start, i]); start = i + 1; }
  }
  return result;
}

module.exports = { completions, signatureHelp, parameters };
