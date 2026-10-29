const vscode = require('vscode');
const path = require('node:path');
const { analyze, hover } = require('./language.cjs');
const { Compiler } = require('./compiler.cjs');

function activate(context) {
  const cache = new WeakMap(), compilers = new Map(), pending = new Map();
  const diagnostics = vscode.languages.createDiagnosticCollection('sfml');
  const color = vscode.window.createTextEditorDecorationType({ color: new vscode.ThemeColor('sfml.variableForeground') });
  let decorationTimer;
  const analysis = document => {
    let entry = cache.get(document);
    if (!entry || entry.version !== document.version) {
      entry = { version: document.version, value: analyze(document.getText()) };
      cache.set(document, entry);
    }
    return entry.value;
  };
  const updateColors = () => {
    for (const editor of vscode.window.visibleTextEditors) {
      if (editor.document.languageId !== 'sfml') continue;
      const enabled = vscode.workspace.getConfiguration('sfml', editor.document.uri).get('variableHighlighting.enabled', true);
      editor.setDecorations(color, enabled ? analysis(editor.document).variables.map(token =>
        new vscode.Range(editor.document.positionAt(token.start), editor.document.positionAt(token.end))) : []);
    }
  };
  const scheduleColors = () => { clearTimeout(decorationTimer); decorationTimer = setTimeout(updateColors, 100); };
  const compilerPath = document => {
    const setting = vscode.workspace.getConfiguration('sfml', document.uri).get('compiler.manifestPath', 'build/sfml-compiler.json');
    const folder = vscode.workspace.getWorkspaceFolder(document.uri) || vscode.workspace.workspaceFolders?.[0];
    return setting ? (path.isAbsolute(setting) ? setting : folder && path.resolve(folder.uri.fsPath, setting)) : null;
  };
  const current = (document, version, generation) => !document.isClosed && document.version === version
    && document.languageId === 'sfml' && generation === compilerGeneration && vscode.workspace.isTrusted;
  let compilerGeneration = 0;
  const toolingWarning = (document, message) => {
    const diagnostic = new vscode.Diagnostic(new vscode.Range(0, 0, 0, Math.min(1, document.lineAt(0).text.length)),
      message, vscode.DiagnosticSeverity.Warning);
    diagnostic.source = 'SFML tooling';
    diagnostics.set(document.uri, [diagnostic]);
  };
  async function check(document) {
    if (document.languageId !== 'sfml' || document.isClosed) return;
    const config = vscode.workspace.getConfiguration('sfml', document.uri);
    if (!config.get('compiler.enabled', true)) return;
    if (!vscode.workspace.isTrusted) {
      toolingWarning(document, 'SFML error checking is paused in Restricted Mode. Use Workspaces: Manage Workspace Trust to trust this folder and enable the compiler.');
      return;
    }
    const manifest = compilerPath(document);
    if (!manifest) {
      toolingWarning(document, 'SFML compiler is not configured. Open a workspace with a prepared backend or set sfml.compiler.manifestPath to an absolute compiler manifest path.');
      return;
    }
    const version = document.version, generation = compilerGeneration;
    try {
      let compiler = compilers.get(manifest);
      if (!compiler) { compiler = new Compiler(manifest); compilers.set(manifest, compiler); }
      const errors = await compiler.check(document.uri.toString(), document.getText());
      if (!errors || !current(document, version, generation)) return;
      diagnostics.set(document.uri, errors.map(error => {
        const line = Math.max(0, Math.min(document.lineCount - 1, Number(error.line) || 0));
        const length = document.lineAt(line).text.length;
        const column = Math.max(0, Math.min(length, Number(error.column) || 0));
        const result = new vscode.Diagnostic(new vscode.Range(line, column, line, Math.min(column + 1, length)),
          String(error.message).slice(0, 4096), vscode.DiagnosticSeverity.Error);
        result.source = 'SFML compiler';
        return result;
      }));
    } catch (error) {
      if (!current(document, version, generation)) return;
      const message = 'SFML compiler unavailable: ' + error.message + '. Run prepareSfmlCompiler and check sfml.compiler.manifestPath.';
      toolingWarning(document, message);
    }
  }
  const scheduleCheck = document => {
    if (document.languageId !== 'sfml') return;
    const key = document.uri.toString();
    clearTimeout(pending.get(key));
    diagnostics.delete(document.uri);
    pending.set(key, setTimeout(() => { pending.delete(key); check(document); }, 400));
  };
  const resetCompilers = () => {
    compilerGeneration++;
    for (const compiler of compilers.values()) compiler.dispose();
    compilers.clear();
    diagnostics.clear();
    for (const document of vscode.workspace.textDocuments) scheduleCheck(document);
  };
  context.subscriptions.push(color, diagnostics,
    vscode.languages.registerHoverProvider('sfml', { provideHover(document, position) {
      const entry = hover(analysis(document), document.offsetAt(position));
      if (!entry) return;
      const markdown = new vscode.MarkdownString();
      markdown.isTrusted = false;
      markdown.supportHtml = false;
      markdown.appendCodeblock(entry.signatures.join('\n'), 'sfml');
      markdown.appendText(entry.description);
      if (entry.example) markdown.appendCodeblock(entry.example, 'sfml');
      return new vscode.Hover(markdown, new vscode.Range(document.positionAt(entry.start), document.positionAt(entry.end)));
    }}),
    vscode.commands.registerCommand('sfml.restartCompiler', resetCompilers),
    vscode.workspace.onDidOpenTextDocument(scheduleCheck),
    vscode.workspace.onDidChangeTextDocument(event => { cache.delete(event.document); scheduleColors(); scheduleCheck(event.document); }),
    vscode.workspace.onDidCloseTextDocument(document => {
      const key = document.uri.toString();
      clearTimeout(pending.get(key)); pending.delete(key); cache.delete(document); diagnostics.delete(document.uri);
    }),
    vscode.workspace.onDidChangeConfiguration(event => {
      if (event.affectsConfiguration('sfml')) { scheduleColors(); resetCompilers(); }
    }),
    vscode.workspace.onDidGrantWorkspaceTrust(resetCompilers),
    vscode.window.onDidChangeVisibleTextEditors(scheduleColors),
    { dispose() {
      clearTimeout(decorationTimer);
      for (const timer of pending.values()) clearTimeout(timer);
      for (const compiler of compilers.values()) compiler.dispose();
    }});
  updateColors();
  for (const document of vscode.workspace.textDocuments) scheduleCheck(document);
}

module.exports = { activate };
