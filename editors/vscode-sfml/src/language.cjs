const catalog = require('../data/functions.json');

// A small lexical scan for presentation only. All diagnostics come from Java's ProgramBuilder.
function analyze(text) {
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
      if (variable) variables.push(token);
    } else { tokens.push({ start, end: ++i, kind: 'symbol', text: c }); }
  }
  for (let i = 0; i < tokens.length; i++) {
    if (tokens[i].kind !== 'word' || tokens[i].text.toLowerCase() !== 'function' || tokens[i - 1]?.text === '.') continue;
    let j = i + 1;
    if (tokens[j]?.kind !== 'word') continue;
    let name = tokens[j++].text;
    while (tokens[j]?.text === '.' && tokens[j + 1]?.kind === 'word') {
      name += '.' + tokens[j + 1].text;
      j += 2;
    }
    if (tokens[j]?.text !== '(') continue;
    let depth = 0;
    do {
      if (tokens[j]?.text === '(') depth++;
      if (tokens[j]?.text === ')') depth--;
      j++;
    } while (j < tokens.length && depth);
    if (depth || tokens[j++]?.text?.toLowerCase() !== 'returns') continue;
    let braces = 0;
    for (; j < tokens.length; j++) {
      const token = tokens[j];
      if (token.text === '{' || token.text === '[') braces++;
      if (token.text === '}' || token.text === ']') braces--;
      if (braces === 0 && token.kind === 'word' && token.text.toLowerCase() === 'do') {
        functions.set(name, { signatures: [text.slice(tokens[i].start, token.end).slice(0, 2048)],
          description: 'Player-defined function in this file. Parameter and return types are explicit; names are case-sensitive.' });
        break;
      }
      if (j - i > 1024) break;
    }
  }
  return { tokens, variables, functions };
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
  if (index < 0 || tokens[index].kind !== 'word') return;
  let first = index, last = index;
  while (tokens[first - 1]?.text === '.' && tokens[first - 2]?.kind === 'word') first -= 2;
  while (tokens[last + 1]?.text === '.' && tokens[last + 2]?.kind === 'word') last += 2;
  if (tokens[last + 1]?.text !== '(') return;
  const name = tokens.slice(first, last + 1).map(token => token.text).join('');
  const method = tokens[first - 1]?.text === '.';
  const entry = !method && functions.get(name) || (method ? catalog.methods[name.toLowerCase()]
    : catalog.functions[name.toLowerCase()]);
  if (entry) return { ...entry, start: tokens[first].start, end: tokens[last].end };
}

module.exports = { analyze, hover };
