import { mkdirSync } from "node:fs";
import { createRequire } from "node:module";
import { createVSIX } from "@vscode/vsce";

const require = createRequire(import.meta.url);
const { version, name } = require("../package.json");
mkdirSync("dist", { recursive: true });
await createVSIX({
  packagePath: `dist/${name}-${version}.vsix`,
  dependencies: false
});
