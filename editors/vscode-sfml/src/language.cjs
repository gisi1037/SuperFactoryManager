const catalog = require('../data/functions.json');
const typeNames = new Set('integer boolean string void label_ref resource_selector item_key item_contents item_locations fluid_key fluid_contents'.split(' '));
const bindingName = token => token.text.replace(/^\$/, '');
const isName = token => token && (token.kind === 'word' || token.kind === 'variable');

// A small lexical scan for presentation only. All diagnostics come from Java's ProgramBuilder.
function analyze(text, completionOffset) {
  const tokens = [], variables = [], functions = new Map();
  if (text.length > 262144) return { tokens, variables, functions };
  for (let i = 0; i < text.length;) {
    const start = i, c = text[i];
    if (/\s/.test(c)) { i++; continue; }
    if (text.startsWith('--', i)) {
      while (i < text.length && !/[\r\n]/.test(text[i])) i++;
      continue;
    }
    if (c === '"') {
      i++;
      while (i < text.length) {
        if (text[i++] === '\\') i++;
        else if (text[i - 1] === '"') break;
      }
      tokens.push({ start, end: Math.min(i, text.length), kind: 'string' });
      continue;
    }
    const variable = c === '$' && /[A-Za-z_]/.test(text[i + 1] || '');
    const word = /[A-Za-z_]/.test(c);
    if (variable || word) {
      i += variable ? 2 : 1;
      while (i < text.length && /[A-Za-z0-9_]/.test(text[i])) i++;
      const token = { start, end: i, kind: variable ? 'variable' : 'word', text: text.slice(start, i) };
      tokens.push(token);
    } else if (/[0-9]/.test(c)) {
      while (i < text.length && /[0-9]/.test(text[i])) i++;
      tokens.push({ start, end: i, kind: 'number', text: text.slice(start, i) });
    } else { tokens.push({ start, end: ++i, kind: 'symbol', text: c }); }
  }
  // This is deliberately a presentation scan, not a second type checker. Match
  // bounded type/header shapes and leave malformed/incomplete code to Java.
  function typeEnd(start, depth = 0) {
    if (depth > 64) return -1;
    let j = start;
    if (tokens[j]?.text === '{') {
      j++;
      while (tokens[j] && tokens[j].text !== '}') {
        if (tokens[j]?.kind !== 'word' || tokens[j + 1]?.text !== ':') return -1;
        j = typeEnd(j + 2, depth + 1);
        if (j < 0) return -1;
        if (tokens[j]?.text === '}') break;
        if (tokens[j++]?.text !== ',') return -1;
      }
      if (tokens[j++]?.text !== '}') return -1;
    } else if (tokens[j]?.kind === 'word' && typeNames.has(tokens[j].text.toLowerCase())) j++;
    else return -1;
    while (tokens[j]?.text === '[') {
      j++;
      if (tokens[j]?.kind === 'number') j++;
      if (tokens[j++]?.text !== ']') return -1;
    }
    return j;
  }
  const headers = new Map();
  for (let i = 0; i < tokens.length; i++) {
    if (tokens[i - 1]?.text === '.') continue;
    const alias = tokens[i].text?.toLowerCase() === 'function';
    let j = alias ? i + 1 : typeEnd(i);
    if (j < 0) continue;
    if (tokens[j]?.kind !== 'word') continue;
    let name = tokens[j++].text;
    while (tokens[j]?.text === '.' && tokens[j + 1]?.kind === 'word') {
      name += '.' + tokens[j + 1].text;
      j += 2;
    }
    if (tokens[j]?.text !== '(') continue;
    const parameters = [];
    j++;
    while (tokens[j] && tokens[j].text !== ')' && j - i <= 1024) {
      const start = j;
      const reference = tokens[j]?.text?.toLowerCase() === 'ref';
      if (reference) j++;
      let parameter;
      if (isName(tokens[j]) && tokens[j + 1]?.text === ':') {
        parameter = tokens[j];
        j = typeEnd(j + 2);
      } else {
        j = typeEnd(j);
        parameter = tokens[j];
        if (isName(parameter)) j++;
        else j = -1;
      }
      if (j < 0) break;
      parameters.push({ token: parameter, signatures: [text.slice(tokens[start].start, tokens[j - 1].end)],
        description: reference
          ? 'Reference parameter. The caller must pass ref with a writable target of exactly this type; writes update that target immediately. Supported by void and value-returning functions.'
          : 'Value parameter. Assignment changes this local copy. Names are case-sensitive; the $ prefix is optional.' });
      if (tokens[j]?.text === ')') break;
      if (tokens[j++]?.text !== ',') { j = -1; break; }
    }
    if (j < 0 || tokens[j++]?.text !== ')') continue;
    if (alias) {
      if (tokens[j++]?.text?.toLowerCase() !== 'returns') continue;
      j = typeEnd(j);
    }
    if (j < 0 || !['{', 'do', 'then'].includes(tokens[j]?.text?.toLowerCase())) continue;
    functions.set(name, { signatures: [text.slice(tokens[i].start, tokens[j].end).slice(0, 2048)],
      description: 'Player-defined function in this file. Parameter and return types are explicit; names are case-sensitive. Void procedures perform effects and may return without a value.' });
    headers.set(i, { end: j, parameters });
    i = j;
  }
  const scopes = [{ kind: 'root', bindings: new Map() }];
  let visible;
  function captureVisible() {
    const names = new Map();
    const inFunction = scopes.some(scope => scope.function);
    for (let s = scopes.length - 1; s >= 0 && s >= scopes.length - 64; s--) {
      for (const [name, entry] of scopes[s].bindings) {
        if (s === 0 && inFunction && !entry.persistent) continue;
        if (!names.has(name) && names.size < 256) names.set(name, entry);
      }
    }
    visible = [...names.values()];
  }
  let pendingParameters = [], pendingLoop;
  const loopDeclarations = new Map();
  function loopEntry(token, index = false) {
    return { token, readOnly: true, signatures: [`for ${token.text}${index ? ': integer' : ''}`],
      description: index
        ? 'Read-only zero-based integer loop index. Bound only inside this loop body; the source is captured once.'
        : 'Read-only loop value. Bound only inside this loop body; iteration uses one captured sequence snapshot.' };
  }
  function javaHeader(start) {
    let depth = 0, semicolons = 0;
    for (let j = start; j < tokens.length && j - start <= 1024; j++) {
      if (tokens[j].text === '(' && ++depth > 64) return false;
      if (tokens[j].text === ')' && --depth === 0) return semicolons === 2;
      if (depth === 1 && tokens[j].text === ';') semicolons++;
    }
    return false;
  }
  function declare(token, entry, scope = scopes.at(-1)) {
    token.binding = entry;
    scope.bindings.set(bindingName(token), entry);
  }
  for (let i = 0; i < tokens.length; i++) {
    if (visible === undefined && completionOffset !== undefined && tokens[i].start >= completionOffset) captureVisible();
    const header = headers.get(i);
    if (header) {
      pendingParameters = header.parameters;
      for (const parameter of pendingParameters) {
        parameter.token.binding = parameter;
        variables.push(parameter.token);
      }
      i = header.end;
    }
    const token = tokens[i], lower = token.text?.toLowerCase();
    const field = tokens[i - 1]?.text === '.' || tokens[i + 1]?.text === ':';
    if (!field && lower === 'for' && tokens[i + 1]?.text === '(') {
      if (isName(tokens[i + 2]) && tokens[i + 3]?.text === ',' && isName(tokens[i + 4])
        && tokens[i + 5]?.text === ')' && tokens[i + 6]?.text?.toLowerCase() === 'in') {
        const entries = [loopEntry(tokens[i + 2], true), loopEntry(tokens[i + 4])];
        loopDeclarations.set(i + 2, entries[0]);
        loopDeclarations.set(i + 4, entries[1]);
        pendingLoop = { entries };
      } else if (javaHeader(i + 1)) {
        const scope = { kind: 'for-header', bindings: new Map() };
        scopes.push(scope);
        pendingLoop = { entries: [], header: scope };
      }
    }
    if (!field && (lower === 'end' || lower === '}')) {
      if (scopes.length > 1) {
        const closed = scopes.pop();
        if (closed.header && scopes.at(-1) === closed.header) scopes.pop();
      }
    } else if (!field && lower === 'else' && scopes.at(-1).kind === 'legacy') {
      scopes.pop();
      if (tokens[i + 1]?.text?.toLowerCase() !== 'if') scopes.push({ kind: 'legacy', bindings: new Map() });
    } else if (!field && ['{', 'do', 'then'].includes(lower)) {
      const structure = lower === '{' && tokens[i + 1]?.kind === 'word' && tokens[i + 2]?.text === ':';
      scopes.push({ kind: structure ? 'structure' : lower === '{' ? 'brace' : 'legacy', bindings: new Map(), function: !!header });
      for (const parameter of pendingParameters) declare(parameter.token, parameter);
      pendingParameters = [];
      if (pendingLoop && !structure) {
        for (const entry of pendingLoop.entries) declare(entry.token, entry);
        scopes.at(-1).header = pendingLoop.header;
        pendingLoop = undefined;
      }
    }
    if (!isName(token) || field) continue;
    const previous = tokens[i - 1]?.text?.toLowerCase();
    if (loopDeclarations.has(i)) token.binding = loopDeclarations.get(i);
    else if (['let', 'state', 'into', 'for'].includes(previous) && tokens[i - 2]?.text !== '.') {
      const javaInitializer = previous === 'let' && scopes.at(-1).kind === 'for-header';
      const entry = { token, persistent: previous === 'state', signatures: [`${previous} ${token.text}`],
        description: previous === 'state' ? 'Persistent disk STATE binding. Names are case-sensitive; the $ prefix is optional.'
          : javaInitializer ? 'Mutable FOR initializer binding. Visible in the condition, body and update, and out of scope after the loop. Names are case-sensitive; the $ prefix is optional.'
          : 'Local or program RAM binding. Names are case-sensitive; the $ prefix is optional. Types are checked by the Java compiler.' };
      if (javaInitializer) entry.readOnly = false;
      if (previous === 'for') {
        entry.readOnly = true;
        entry.description = 'Read-only loop binding, visible only in this loop body. Iteration bounds or sequence are captured before entry.';
        token.binding = entry;
        pendingLoop = { entries: [entry] };
      }
      else declare(token, entry);
    } else if (tokens[i + 1]?.text !== '(' && (tokens[i - 1]?.text !== ':' || scopes.at(-1).kind === 'structure')) {
      // Keep editing malformed, deeply nested source bounded as well.
      for (let s = scopes.length - 1; s >= 0 && s >= scopes.length - 64; s--) {
        const entry = scopes[s].bindings.get(bindingName(token));
        if (entry) { token.binding = entry; break; }
      }
    }
    if (token.kind === 'variable' || token.binding) variables.push(token);
  }
  if (visible === undefined && completionOffset !== undefined) captureVisible();
  return { tokens, variables, functions, visible };
}

function hover(analysis, offset) {
  const { tokens, functions } = analysis;
  let low = 0, high = tokens.length - 1, index = -1;
  while (low <= high) {
    const middle = low + high >> 1, token = tokens[middle];
    if (offset < token.start) high = middle - 1;
    else if (offset >= token.end) low = middle + 1;
    else { index = middle; break; }
  }
  if (index < 0) return;
  if (tokens[index].binding) return { ...tokens[index].binding, start: tokens[index].start, end: tokens[index].end };
  if (tokens[index].kind !== 'word') return;
  let first = index, last = index;
  while (tokens[first - 1]?.text === '.' && tokens[first - 2]?.kind === 'word' && !tokens[first - 2].binding) first -= 2;
  while (tokens[last + 1]?.text === '.' && tokens[last + 2]?.kind === 'word') last += 2;
  if (tokens[last + 1]?.text !== '(') return;
  const name = tokens.slice(first, last + 1).map(token => token.text).join('');
  const method = tokens[first - 1]?.text === '.';
  const entry = !method && functions.get(name) || (method ? catalog.methods[name.toLowerCase()]
    : catalog.functions[name.toLowerCase()]);
  if (entry) return { ...entry, start: tokens[first].start, end: tokens[last].end };
}

module.exports = { analyze, hover };
