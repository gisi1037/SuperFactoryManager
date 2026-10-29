const fs = require('node:fs/promises');
const os = require('node:os');
const path = require('node:path');
const { spawn } = require('node:child_process');

class Compiler {
  constructor(manifestPath) {
    this.manifestPath = manifestPath;
    this.queue = new Map();
    this.id = 0;
    this.disposed = false;
  }

  check(key, source) {
    if (this.disposed) return Promise.reject(new Error('Compiler was stopped'));
    return new Promise((resolve, reject) => {
      this.queue.get(key)?.resolve(null);
      this.queue.delete(key);
      if (this.queue.size >= 16) {
        const oldest = this.queue.keys().next().value;
        this.queue.get(oldest).resolve(null);
        this.queue.delete(oldest);
      }
      // The compiler itself reports its source-length limit; do not send an arbitrarily large file.
      this.queue.set(key, { source: source.slice(0, 32768), resolve, reject });
      this.pump();
    });
  }

  async start() {
    if ((await fs.stat(this.manifestPath)).size > 262144) throw new Error('Compiler manifest is too large');
    const input = await fs.readFile(this.manifestPath, 'utf8');
    if (input.length > 262144) throw new Error('Compiler manifest is too large');
    const manifest = JSON.parse(input);
    if (typeof manifest.javaExecutable !== 'string' || !Array.isArray(manifest.classpath)
      || !manifest.classpath.length || manifest.classpath.length > 512
      || manifest.classpath.some(entry => typeof entry !== 'string' || /[\r\n]/.test(entry) || entry.length > 8192)
      || manifest.mainClass !== 'ca.teamdman.sfm.tooling.SfmlCompilerServer') throw new Error('Invalid SFML compiler manifest');
    this.temp = await fs.mkdtemp(path.join(os.tmpdir(), 'sfml-vscode-'));
    this.argsFile = path.join(this.temp, 'compiler.args');
    const quoted = value => '"' + value.replace(/\\/g, '\\\\').replace(/"/g, '\\"') + '"';
    // Java argument files avoid Windows' command-line length limit with the development classpath.
    await fs.writeFile(this.argsFile, ['-Xms16m', '-Xmx256m', '-Djava.awt.headless=true', '-cp',
      quoted(manifest.classpath.join(path.delimiter)), manifest.mainClass].join('\n'), 'utf8');
    if (this.disposed) { await this.cleanup(); throw new Error('Compiler was stopped'); }
    this.output = '';
    const child = this.child = spawn(manifest.javaExecutable, ['@' + this.argsFile], {
      windowsHide: true, stdio: ['pipe', 'pipe', 'pipe']
    });
    child.stdout.setEncoding('utf8');
    child.stdout.on('data', chunk => { if (this.child === child) this.receive(chunk); });
    child.stderr.on('data', () => {}); // Drain library logs; they are never presented as source diagnostics.
    child.stdin.on('error', error => { if (this.child === child) this.fail(error); });
    child.on('error', error => { if (this.child === child) this.fail(error); });
    child.on('exit', code => { if (!this.disposed && this.child === child) this.fail(new Error(`SFML compiler exited (${code})`)); });
  }

  async pump() {
    if (this.busy || this.disposed || !this.queue.size) return;
    this.busy = true;
    try {
      if (Date.now() < (this.retryAfter || 0)) throw new Error('Compiler unavailable; use SFML: Restart Compiler after preparing the backend');
      if (!this.child) await this.start();
      if (this.disposed) return;
      const key = this.queue.keys().next().value;
      this.active = this.queue.get(key);
      this.queue.delete(key);
      this.active.id = ++this.id;
      this.timeout = setTimeout(() => this.fail(new Error('SFML compiler timed out')), 15000);
      this.child.stdin.write(JSON.stringify({ id: this.active.id, source: this.active.source }) + '\n');
    } catch (error) { this.fail(error); }
  }

  receive(chunk) {
    this.output += chunk;
    if (this.output.length > 262144) return this.fail(new Error('SFML compiler response exceeded the limit'));
    let end;
    while ((end = this.output.indexOf('\n')) >= 0) {
      const line = this.output.slice(0, end);
      this.output = this.output.slice(end + 1);
      if (!line.trim()) continue;
      try {
        const reply = JSON.parse(line);
        if (!this.active || reply.id !== this.active.id || !Array.isArray(reply.diagnostics)
          || reply.diagnostics.length > 32) throw new Error('Invalid SFML compiler response');
        clearTimeout(this.timeout);
        const active = this.active;
        this.active = null;
        this.busy = false;
        if (reply.error) active.reject(new Error(String(reply.error)));
        else active.resolve(reply.diagnostics);
        this.pump();
      } catch (error) { this.fail(error); return; }
    }
  }

  fail(error) {
    clearTimeout(this.timeout);
    this.active?.reject(error);
    this.active = null;
    for (const entry of this.queue.values()) entry.reject(error);
    this.queue.clear();
    const child = this.child;
    this.child = null;
    child?.removeAllListeners('exit');
    child?.kill();
    this.busy = false;
    this.retryAfter = Date.now() + 10000;
    this.cleanup();
  }

  async cleanup() {
    // Delete only the file/directory this connection created; never recurse through user paths.
    const file = this.argsFile, directory = this.temp;
    this.argsFile = this.temp = null;
    if (file) await fs.unlink(file).catch(() => {});
    if (directory) await fs.rmdir(directory).catch(() => {});
  }

  dispose() { this.disposed = true; this.fail(new Error('Compiler was stopped')); }
}

module.exports = { Compiler };
