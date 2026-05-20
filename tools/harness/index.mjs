import { fileURLToPath } from 'node:url'

import { run as runBoundaries } from './check-boundaries.mjs'
import { run as runNoEmoji } from './check-no-emoji.mjs'
import { run as runDocsFresh } from './check-docs-fresh.mjs'

export function run() {
  const results = [runBoundaries(), runNoEmoji(), runDocsFresh()]
  return results.some((code) => code !== 0) ? 1 : 0
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  process.exitCode = run()
}
