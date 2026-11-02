import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import vm from 'node:vm';
import test from 'node:test';
const require = createRequire(import.meta.url);

function host(trusted = true, settings = {}) {
  const listeners = {}, timers = new Map(), diagnostics = new Map(), requests = [], colors = [], watchers = [];
  const disposable = {dispose() {}};
  const doc = {languageId:'sfml', version:1, isClosed:false, text:'let $x = 1 every 20 ticks do print($x) end',
    uri:{toString:()=> 'file:///test.sfml',fsPath:'/test.sfml'}, getText() { return this.text; },
    positionAt:offset=>({line:0,character:offset}), offsetAt:position=>position.character,
    lineCount:1, lineAt() { return {text:this.text}; }};
  const event = name => callback => { listeners[name] = callback; return disposable; };
  class Range { constructor(...args) { this.args = args; } }
  const vscode = {
    Uri:{file:fsPath=>({fsPath})},
    RelativePattern:class {constructor(base,pattern){Object.assign(this,{base,pattern});}},
    ThemeColor: class {constructor(id){this.id=id;}}, Range,
    Diagnostic: class {constructor(range,message,severity){Object.assign(this,{range,message,severity});}},
    DiagnosticSeverity:{Error:0,Warning:1},
    MarkdownString: class {constructor(){this.value='';} appendCodeblock(text){this.value+=text;return this;}
      appendText(text){this.value+=text;return this;}},
    Hover: class {constructor(contents,range){Object.assign(this,{contents,range});}},
    CompletionItemKind:{Variable:0,Method:1,Function:2},
    CompletionItem:class {constructor(label,kind){Object.assign(this,{label,kind});}},
    SignatureHelp:class {},
    SignatureInformation:class {constructor(label,documentation){Object.assign(this,{label,documentation});}},
    ParameterInformation:class {constructor(label){this.label=label;}},
    languages:{createDiagnosticCollection:()=>({...disposable,set:(uri,value)=>diagnostics.set(uri.toString(),value),
      delete:uri=>diagnostics.delete(uri.toString()),clear:()=>diagnostics.clear()}),
      registerHoverProvider:(_,value)=>{listeners.hover=value;return disposable;},
      registerCompletionItemProvider:(_,value)=>{listeners.completion=value;return disposable;},
      registerSignatureHelpProvider:(_,value)=>{listeners.signature=value;return disposable;}},
    commands:{registerCommand:(_,callback)=>{listeners.restart=callback;return disposable;}},
    window:{visibleTextEditors:[{document:doc,setDecorations:(_,ranges)=>colors.push(ranges)}],
      createTextEditorDecorationType:()=>disposable,onDidChangeVisibleTextEditors:event('visible')},
    workspace:{isTrusted:trusted,textDocuments:[doc],createFileSystemWatcher:pattern=>{
      const watcher={pattern,disposed:false,dispose(){this.disposed=true;},
        onDidChange(fn){this.change=fn;return disposable;},onDidCreate(fn){this.create=fn;return disposable;},
        onDidDelete(fn){this.delete=fn;return disposable;}};
      watchers.push(watcher);return watcher;
    },getConfiguration:()=>({get:(name,fallback)=>
      Object.hasOwn(settings,name)?settings[name]:name==='compiler.manifestPath'?'/manifest.json':fallback}),
      getWorkspaceFolder:()=>undefined,onDidOpenTextDocument:event('open'),onDidChangeTextDocument:event('change'),
      onDidCloseTextDocument:event('close'),onDidChangeConfiguration:event('config'),onDidGrantWorkspaceTrust:event('trust')}
  };
  class Compiler {check(key,source){return new Promise(resolve=>requests.push({key,source,resolve}));} dispose(){}}
  const context = {subscriptions:[]}, module = {exports:{}};
  vm.runInNewContext(readFileSync(new URL('../src/extension.cjs', import.meta.url),'utf8'), {
    module,require:name=>name==='vscode'?vscode:name==='./compiler.cjs'?{Compiler}:
      name==='./language.cjs'?require('../src/language.cjs'):name==='./assist.cjs'?require('../src/assist.cjs'):require(name),
    setTimeout:callback=>{const id={};timers.set(id,callback);return id;},clearTimeout:id=>timers.delete(id)
  });
  module.exports.activate(context);
  return {doc,vscode,listeners,diagnostics,requests,colors,watchers,
    async flush(){for(const [id,fn] of [...timers]){timers.delete(id);fn();}await new Promise(setImmediate);},
    dispose(){context.subscriptions.forEach(value=>value.dispose());}};
}

test('the editor adapter colors variables and supplies untrusted, non-HTML function hovers', () => {
  const h=host();
  try {
    assert.equal(h.colors[0].length,2);
    const result=h.listeners.hover.provideHover(h.doc,{character:h.doc.text.indexOf('print')+1});
    assert.match(result.contents.value,/print\(/);
    assert.equal(result.contents.isTrusted,false);
    assert.equal(result.contents.supportHtml,false);
  } finally {h.dispose();}
});

test('completion and signature providers expose bounded replacements and safe documentation', () => {
  const h = host();
  try {
    h.doc.text = 'every 20 ticks { let source = label("in") source.em';
    const items = h.listeners.completion.provideCompletionItems(h.doc, {character:h.doc.text.length});
    const empty = items.find(item => item.label === 'empty');
    assert.ok(empty);
    assert.equal(empty.insertText, 'empty');
    assert.equal(empty.documentation.isTrusted, false);
    assert.equal(empty.documentation.supportHtml, false);
    h.doc.text = 'transfer(label("in"), label("out"), ';
    h.doc.version++;
    const help = h.listeners.signature.provideSignatureHelp(h.doc, {character:h.doc.text.length});
    assert.equal(help.activeParameter, 2);
    assert.ok(help.signatures.length > 0);
    assert.match(help.signatures[help.activeSignature].label, /transfer\(/);
  } finally { h.dispose(); }
});

test('editing to plain variables refreshes decorations and declaration hovers', async () => {
  const h = host();
  try {
    h.doc.text = 'state progress = 0; every 20 ticks { progress = progress + 1; }';
    h.doc.version++;
    h.listeners.change({document:h.doc});
    await h.flush();
    assert.equal(h.colors.at(-1).length, 3);
    const result = h.listeners.hover.provideHover(h.doc, {character:h.doc.text.lastIndexOf('progress') + 1});
    assert.match(result.contents.value, /state progress/);
    assert.match(result.contents.value, /Persistent disk STATE/);
    assert.equal(result.contents.isTrusted, false);
  } finally { h.dispose(); }
});

test('untrusted workspaces explain Restricted Mode without submitting compiler requests', async () => {
  const h=host(false);
  try {
    await h.flush();assert.equal(h.requests.length,0);
    const warning=h.diagnostics.get(h.doc.uri.toString())[0];
    assert.equal(warning.source,'SFML tooling');
    assert.equal(warning.severity,1);
    assert.match(warning.message,/Restricted Mode/);
    assert.match(warning.message,/Manage Workspace Trust/);
    h.vscode.workspace.isTrusted=true;h.listeners.trust();await h.flush();
    assert.equal(h.requests.length,1);
    h.requests[0].resolve([{message:'Unknown variable: $missing',line:0,column:4}]);
    await new Promise(setImmediate);
    assert.equal(h.diagnostics.get(h.doc.uri.toString())[0].source,'SFML compiler');
  } finally {h.dispose();}
});

test('missing workspace configuration is visible and intentionally disabled checking remains quiet', async () => {
  const missing=host(true,{'compiler.manifestPath':'build/sfml-compiler.json'});
  try {
    await missing.flush();assert.equal(missing.requests.length,0);
    assert.match(missing.diagnostics.get(missing.doc.uri.toString())[0].message,/not configured/);
  } finally {missing.dispose();}
  const disabled=host(false,{'compiler.enabled':false});
  try {
    await disabled.flush();assert.equal(disabled.requests.length,0);assert.equal(disabled.diagnostics.size,0);
  } finally {disabled.dispose();}
});

test('old document versions and closed documents cannot publish errors', async () => {
  const h=host();
  try {
    await h.flush();assert.equal(h.requests.length,1);
    h.doc.version++;h.listeners.change({document:h.doc});
    await h.flush();assert.equal(h.requests.length,2);
    h.requests[0].resolve([{message:'stale',line:0,column:0}]);await new Promise(setImmediate);
    assert.equal(h.diagnostics.size,0);
    h.requests[1].resolve([{message:'current',line:0,column:4}]);await new Promise(setImmediate);
    assert.equal(h.diagnostics.get(h.doc.uri.toString())[0].message,'current');
    h.doc.version++;h.listeners.change({document:h.doc});await h.flush();
    h.doc.isClosed=true;h.listeners.close(h.doc);
    h.requests[2].resolve([{message:'closed',line:0,column:0}]);await new Promise(setImmediate);
    assert.equal(h.diagnostics.size,0);
  } finally {h.dispose();}
});

test('restarting the backend invalidates results from its previous generation', async () => {
  const h=host();
  try {
    await h.flush();h.listeners.restart();await h.flush();
    h.requests[0].resolve([{message:'old backend',line:0,column:0}]);await new Promise(setImmediate);
    assert.equal(h.diagnostics.size,0);
    h.requests[1].resolve([]);await new Promise(setImmediate);
    assert.deepEqual(h.diagnostics.get(h.doc.uri.toString()),[]);
  } finally {h.dispose();}
});

test('regenerating the configured manifest refreshes the compiler and rejects old results', async () => {
  const h=host();
  try {
    await h.flush();
    const watcher=h.watchers[0];
    assert.equal(watcher.pattern.pattern,'manifest.json');
    watcher.change(); watcher.create();
    await h.flush();
    assert.equal(watcher.disposed,false);
    h.requests[0].resolve([{message:'old backend',line:0,column:0}]);
    await new Promise(setImmediate);
    assert.equal(h.diagnostics.size,0);
    await h.flush();
    assert.equal(h.requests.length,2);
    h.requests[1].resolve([]); await new Promise(setImmediate);
    assert.deepEqual(h.diagnostics.get(h.doc.uri.toString()),[]);
    assert.equal(h.watchers.length,1);
  } finally {h.dispose();}
  assert.ok(h.watchers.every(watcher=>watcher.disposed));
});

test('restricted and disabled compiler checks create no manifest watchers', async () => {
  for (const h of [host(false),host(true,{'compiler.enabled':false})]) {
    try {await h.flush();assert.equal(h.watchers.length,0);}
    finally {h.dispose();}
  }
});
