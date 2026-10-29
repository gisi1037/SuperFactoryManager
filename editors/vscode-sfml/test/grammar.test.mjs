import assert from "node:assert/strict";
import { readFile, readdir } from "node:fs/promises";
import { createRequire } from "node:module";
import { fileURLToPath } from "node:url";
import test from "node:test";
import textmate from "vscode-textmate";
import oniguruma from "vscode-oniguruma";

const require = createRequire(import.meta.url);
const root = new URL("../", import.meta.url);
const readJson = async (path) => JSON.parse(await readFile(new URL(path, root), "utf8"));
const manifest = await readJson("package.json");
const configuration = await readJson("language-configuration.json");
const rawGrammar = await readFile(new URL("syntaxes/sfml.tmLanguage.json", root), "utf8");
const wasm = await readFile(require.resolve("vscode-oniguruma/release/onig.wasm"));
await oniguruma.loadWASM(wasm.buffer.slice(wasm.byteOffset, wasm.byteOffset + wasm.byteLength));
const registry = new textmate.Registry({
  onigLib: Promise.resolve({
    createOnigScanner: (patterns) => new oniguruma.OnigScanner(patterns),
    createOnigString: (text) => new oniguruma.OnigString(text)
  }),
  loadGrammar: async (scope) => scope === "source.sfml"
    ? textmate.parseRawGrammar(rawGrammar, "sfml.tmLanguage.json") : null
});
const grammar = await registry.loadGrammar("source.sfml");

function tokenScope(line, text, start = 0) {
  const position = line.indexOf(text, start);
  assert.notEqual(position, -1, `Missing fixture text ${text}`);
  const { tokens } = grammar.tokenizeLine(line);
  const token = tokens.find((entry) => position >= entry.startIndex && position < entry.endIndex);
  assert.ok(token, `No token for ${text}`);
  return token.scopes;
}

function expectScope(line, text, scope, start = 0) {
  assert.ok(tokenScope(line, text, start).includes(scope), `${text} in ${line}: expected ${scope}`);
}

test("the manifest registers the SFML grammar and local editing runtime", async () => {
  assert.equal(manifest.contributes.languages[0].id, "sfml");
  assert.deepEqual(manifest.contributes.languages[0].extensions, [".sfml"]);
  assert.equal(manifest.contributes.grammars[0].scopeName, JSON.parse(rawGrammar).scopeName);
  assert.equal(manifest.main, "./src/extension.cjs");
  assert.equal(manifest.dependencies, undefined);
  for (const path of [manifest.contributes.languages[0].configuration,
    manifest.contributes.grammars[0].path, manifest.contributes.snippets[0].path]) {
    assert.ok((await readFile(new URL(path, root))).length > 0);
  }
});

test("all alphabetic lexer keywords are colored in upper, lower and mixed case", async () => {
  const source = await readFile(new URL("../../src/main/resources/assets/superfactorymanager/grammar/SFML.g4", root), "utf8");
  const names = [...source.matchAll(/^[A-Z_]+\s*:\s*([A-Z](?:\s+[A-Z])+)(?=\s*[;|])/gm)]
    .map((match) => match[1].replace(/\s/g, ""));
  assert.ok(names.length > 60);
  for (const name of [...names, "GLOBAL", "G"]) {
    for (const spelling of [name, name.toLowerCase(), [...name].map((c, i) => i % 2 ? c.toLowerCase() : c).join("")]) {
      assert.ok(tokenScope(spelling, spelling).some((scope) => scope !== "source.sfml"), spelling);
    }
  }
});

test("variables, function definitions, methods and members have distinct scopes", () => {
  expectScope("let $working = false", "$working", "variable.other.readwrite.sfml");
  expectScope("function stock.total($items: item_contents) returns integer do", "stock.total", "entity.name.function.sfml");
  expectScope("function stock.total($items: item_contents) returns integer do", "item_contents", "storage.type.sfml");
  expectScope("let $items = $source.contents()", "contents", "support.function.sfml");
  expectScope("$jobs[0].amount = 8", "amount", "variable.other.property.sfml");
  expectScope("let $s = {if: 1, count: 3, inner: {ready: true}}", "if", "variable.other.property.sfml");
  expectScope("let $s = {if: 1, count: 3, inner: {ready: true}}", "ready", "variable.other.property.sfml");
  expectScope("let $n = math.double(8)", "math.double", "entity.name.function.sfml");
});

test("new boolean and bitwise operators retain their complete token spans", () => {
  for (const operator of ["!", "!=", "==", "&&", "||", "~", "&", "|", "^", "<<", ">>", ">>>", ">=", "<=", "%", "=", "+", "-", "*", "/"]) {
    const line = `$a ${operator} $b`;
    const result = grammar.tokenizeLine(line);
    const token = result.tokens.find((entry) => entry.startIndex === 3);
    assert.ok(token?.scopes.includes("keyword.operator.sfml"), operator);
    assert.equal(line.slice(token.startIndex, token.endIndex), operator);
  }
  expectScope("if (!$working && $phase != 2) then", "if", "keyword.control.sfml");
});

test("strings hide keywords, operators and comment markers and support escapes", () => {
  const line = String.raw`print("if -- $name != \"quoted\" \\ path") -- actual comment`;
  expectScope(line, "if", "string.quoted.double.sfml");
  expectScope(line, "$name", "string.quoted.double.sfml");
  expectScope(line, String.raw`\"`, "constant.character.escape.sfml");
  expectScope(line, "actual", "comment.line.double-dash.sfml");
  const comment = grammar.tokenizeLine("-- if ($x) then");
  assert.ok(grammar.tokenizeLine("let $x = 1", comment.ruleStack).tokens
    .every((entry) => !entry.scopes.includes("comment.line.double-dash.sfml")));
});

test("escaped backslashes and multiline strings match the SFML lexer", () => {
  const first = grammar.tokenizeLine('let $text = "first line');
  const second = grammar.tokenizeLine('end -- still a string" -- comment', first.ruleStack);
  assert.ok(second.tokens[0].scopes.includes("string.quoted.double.sfml"));
  assert.ok(second.tokens.at(-1).scopes.includes("comment.line.double-dash.sfml"));
  expectScope(String.raw`let $s = "path\\"`, String.raw`\\`, "constant.character.escape.sfml");
});

test("resource selectors and numeric literals stay separate from data fields", () => {
  for (const resource of ["item:*", "item:*:*", "minecraft:iron_ingot", "item:minecraft:wool:14", "fluid:water", "#ore:ingotIron"]) {
    assert.ok(tokenScope(`input ${resource} from source`, resource).some((scope) => scope.startsWith("support.constant.")), resource);
  }
  expectScope("every 20g+1 ticks do", "20g", "constant.numeric.integer.sfml");
  expectScope("let $n = -9223372036854775808", "9223372036854775808", "constant.numeric.integer.sfml");
  expectScope("let $x = {amount:8}", "amount", "variable.other.property.sfml");
});

test("comments and word selection use the real SFML forms", () => {
  assert.equal(configuration.comments.lineComment, "--");
  assert.equal(configuration.comments.blockComment, undefined);
  const words = new RegExp(configuration.wordPattern, "g");
  assert.deepEqual("$working $stock[0].item item:minecraft:iron_ingot".match(words),
    ["$working", "$stock", "0", "item", "item:minecraft:iron_ingot"]);
  assert.ok(configuration.autoClosingPairs.every((pair) => pair.notIn.includes("comment")));
});

test("indentation covers mixed case blocks and else without indenting comments", () => {
  const increase = new RegExp(configuration.indentationRules.increaseIndentPattern);
  const decrease = new RegExp(configuration.indentationRules.decreaseIndentPattern);
  for (const line of ["every 20 ticks do", "EvErY REDSTONE PULSE DO", "if ($ready) then", "else if ($other) then",
    "else", "function f($n: integer) returns integer do", "for $i = 0 to 3 do", "while (false) do -- test"]) {
    assert.ok(increase.test(line), line);
  }
  for (const line of ["end", " END -- end block", "else", "Else If ($x) THEN"]) assert.ok(decrease.test(line), line);
  for (const line of ['print("end")', "-- if (true) then", "let $end = 1"]) {
    assert.ok(!increase.test(line), line);
    assert.ok(!decrease.test(line), line);
  }
});

test("fold markers nest blocks, without treating else as another opening block", () => {
  const start = new RegExp(configuration.folding.markers.start);
  const end = new RegExp(configuration.folding.markers.end);
  for (const line of ["every 20 ticks do", "if ($ready) then", "for $i in $items do", "-- region setup"]) assert.ok(start.test(line), line);
  for (const line of ["end", "END -- block", "-- endregion"]) assert.ok(end.test(line), line);
  assert.ok(!start.test("else if ($x) then"));
  assert.ok(!start.test("-- every 20 ticks do"));
});

test("every published example tokenizes with the real TextMate engine", async () => {
  const examples = new URL("../../examples/logistics/", root);
  const files = (await readdir(examples)).filter((path) => path.endsWith(".sfml"));
  assert.ok(files.length >= 5);
  for (const path of files) {
    let stack;
    for (const line of (await readFile(new URL(path, examples), "utf8")).split(/\r?\n/)) {
      const result = grammar.tokenizeLine(line, stack, 500);
      assert.ok(!result.stoppedEarly, `${path}: tokenizer budget exhausted`);
      assert.ok(result.tokens.every((token) => !token.scopes.some((scope) => scope.startsWith("invalid."))), `${path}: invalid token`);
      stack = result.ruleStack;
    }
  }
});

test("snippets preserve literal variable sigils and unique completion prefixes", async () => {
  const snippets = await readJson("snippets/sfml.json");
  assert.equal(Object.keys(snippets).length, 35);
  const prefixes = new Set();
  for (const [name, snippet] of Object.entries(snippets)) {
    assert.ok(!prefixes.has(snippet.prefix), name);
    prefixes.add(snippet.prefix);
    assert.ok(snippet.description.length > 0, name);
    const body = Array.isArray(snippet.body) ? snippet.body.join("\n") : snippet.body;
    assert.ok(!/(?<!\\)\$[A-Za-z_]/.test(body), `Unescaped snippet variable in ${name}`);
  }
  const declaration = snippets["Local declaration"].body;
  assert.ok(declaration.includes(String.raw`\$`));
});
