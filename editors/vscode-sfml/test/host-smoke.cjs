const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vscode = require('vscode');

async function waitFor(predicate) {
  const deadline = Date.now() + 20000;
  while (Date.now() < deadline) {
    if (predicate()) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error('Timed out waiting for SFML diagnostics');
}

exports.run = async () => {
  const report = path.resolve(__dirname, '../dist/host-smoke.json');
  try {
    const config = vscode.workspace.getConfiguration('sfml');
    await config.update('compiler.manifestPath', path.resolve(__dirname, '../../../build/sfml-compiler.json'), vscode.ConfigurationTarget.Global);
    await config.update('compiler.enabled', true, vscode.ConfigurationTarget.Global);
    await vscode.extensions.getExtension('gisi1037.sfml-language').activate();
    const source = 'function math.double($n: integer) returns integer do return $n * 2 end\n'
      + 'every 20 ticks do\n  let $src = label("inputs")\n  print($src.contents())\n  let $n = math.double(2)\nend';
    const doc = await vscode.workspace.openTextDocument({ language: 'sfml', content: source });
    await vscode.window.showTextDocument(doc);
    const hoverText = async word => {
      const position = doc.positionAt(source.lastIndexOf(word) + 1);
      const hovers = await vscode.commands.executeCommand('vscode.executeHoverProvider', doc.uri, position);
      return hovers.flatMap(result => result.contents.map(content => content.value || String(content))).join('\n');
    };
    assert.match(await hoverText('contents'), /item_contents/);
    assert.match(await hoverText('math.double'), /Player-defined/);
    assert.equal(vscode.workspace.getConfiguration('sfml', doc.uri).get('variableHighlighting.enabled'), true);
    const edit = new vscode.WorkspaceEdit();
    edit.insert(doc.uri, new vscode.Position(3, 0), '  $missing = 1\n');
    await vscode.workspace.applyEdit(edit);
    await waitFor(() => vscode.languages.getDiagnostics(doc.uri).some(d => d.source === 'SFML compiler' && /Unknown variable/.test(d.message)));
    const errors = vscode.languages.getDiagnostics(doc.uri);
    assert.ok(errors.every(d => d.source !== 'SFML tooling'), JSON.stringify(errors));
    const restore = new vscode.WorkspaceEdit();
    restore.replace(doc.uri, new vscode.Range(new vscode.Position(0, 0), doc.positionAt(doc.getText().length)), source);
    await vscode.workspace.applyEdit(restore);
    // Give the restored version time to reach Java; clearing on edit alone is not the assertion.
    await new Promise(resolve => setTimeout(resolve, 1500));
    assert.deepEqual(vscode.languages.getDiagnostics(doc.uri), []);
    fs.writeFileSync(report, JSON.stringify({passed:true, checks:['built-in hover','player function hover','variable color enabled','real compiler diagnostic','diagnostics cleared after correction']}, null, 2));
  } catch (error) {
    fs.writeFileSync(report, JSON.stringify({passed:false,error:error.stack}, null, 2));
    throw error;
  }
};
