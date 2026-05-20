import path from 'node:path'
import { readText } from './_utils.mjs'
import { fileURLToPath } from 'node:url'

import { isMarkdownFile, repoRoot, toRepoRelative, walkFiles } from './_utils.mjs'

const EMOJI_PATTERN = /\p{Extended_Pictographic}/u

export function run() {
  const docFiles = collectDocFiles()
  const violations = []

  for (const filePath of docFiles) {
    const lines = readText(filePath).split('\n')

    for (const [lineIndex, line] of lines.entries()) {
      if (EMOJI_PATTERN.test(line)) {
        violations.push(`${toRepoRelative(filePath)}:${lineIndex + 1}`)
      }
    }
  }

  if (violations.length > 0) {
    console.error('Emoji check failed:')
    for (const violation of violations) {
      console.error(`- ${violation}`)
    }
    return 1
  }

  console.log(`Emoji check passed for ${docFiles.length} documentation files.`)
  return 0
}

function collectDocFiles() {
  const roots = [
    path.join(repoRoot, 'AGENTS.md'),
    path.join(repoRoot, 'CLAUDE.md'),
    ...walkFiles(
      path.join(repoRoot, 'apps'),
      (filePath) => path.basename(filePath) === 'AGENTS.md',
    ),
    ...walkFiles(
      path.join(repoRoot, 'packages'),
      (filePath) => path.basename(filePath) === 'AGENTS.md',
    ),
    ...walkFiles(path.join(repoRoot, 'DOCS/agents'), isMarkdownFile),
  ]

  return roots.filter((filePath, index) => roots.indexOf(filePath) === index)
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  process.exitCode = run()
}
