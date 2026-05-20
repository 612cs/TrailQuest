import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

import {
  findPackageJson,
  isMarkdownFile,
  readText,
  repoRoot,
  toRepoRelative,
  walkFiles,
} from './_utils.mjs'

const PNPM_BUILTINS = new Set(['install', 'exec', 'dlx'])

export function run() {
  const docFiles = collectDocFiles()
  const violations = []

  for (const filePath of docFiles) {
    const fileDir = path.dirname(filePath)
    const content = readText(filePath)

    for (const candidatePath of extractPathCandidates(content)) {
      const resolvedPath = resolveDocPath(candidatePath, fileDir)

      if (!resolvedPath) {
        continue
      }

      if (!fs.existsSync(resolvedPath)) {
        violations.push(`${toRepoRelative(filePath)} references missing path ${candidatePath}`)
      }
    }

    for (const command of extractCommandCandidates(content)) {
      const validationError = validateCommand(command, fileDir)
      if (validationError) {
        violations.push(
          `${toRepoRelative(filePath)} references invalid command ${command}: ${validationError}`,
        )
      }
    }
  }

  if (violations.length > 0) {
    console.error('Documentation freshness check failed:')
    for (const violation of violations) {
      console.error(`- ${violation}`)
    }
    return 1
  }

  console.log(`Documentation freshness check passed for ${docFiles.length} files.`)
  return 0
}

function collectDocFiles() {
  return [
    path.join(repoRoot, 'AGENTS.md'),
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
}

function extractPathCandidates(content) {
  const paths = new Set()
  const inlinePattern = /`([^`\n]+)`/g
  const linkPattern = /\[[^\]]+\]\(([^)]+)\)/g

  for (const match of content.matchAll(inlinePattern)) {
    const token = sanitizeCandidate(match[1])
    if (looksLikePath(token)) {
      paths.add(token)
    }
  }

  for (const match of content.matchAll(linkPattern)) {
    const token = sanitizeCandidate(match[1])
    if (looksLikePath(token)) {
      paths.add(token)
    }
  }

  for (const line of extractCodeLines(content)) {
    const token = sanitizeCandidate(line)
    if (looksLikePath(token)) {
      paths.add(token)
    }
  }

  return [...paths]
}

function extractCommandCandidates(content) {
  const commands = new Set()
  const inlinePattern = /`([^`\n]+)`/g

  for (const match of content.matchAll(inlinePattern)) {
    const token = sanitizeCandidate(match[1])
    if (looksLikeCommand(token)) {
      commands.add(token)
    }
  }

  for (const line of extractCodeLines(content)) {
    const token = sanitizeCandidate(line)
    if (looksLikeCommand(token)) {
      commands.add(token)
    }
  }

  return [...commands]
}

function extractCodeLines(content) {
  const lines = []
  const fencePattern = /```(?:\w+)?\n([\s\S]*?)```/g

  for (const match of content.matchAll(fencePattern)) {
    for (const line of match[1].split('\n')) {
      const trimmedLine = line.trim()
      if (!trimmedLine || trimmedLine.startsWith('#')) {
        continue
      }
      lines.push(trimmedLine)
    }
  }

  return lines
}

function sanitizeCandidate(candidate) {
  return candidate.trim().replace(/^<|>$/g, '')
}

function looksLikePath(candidate) {
  if (!candidate || candidate.startsWith('http://') || candidate.startsWith('https://')) {
    return false
  }

  if (/^[├│└]/u.test(candidate) || /\s/u.test(candidate) || /[\u3400-\u9fff]/u.test(candidate)) {
    return false
  }

  if (looksLikeCommand(candidate)) {
    return false
  }

  if (candidate.includes('*') || candidate.includes('|') || candidate.includes('...')) {
    return false
  }

  return (
    candidate.startsWith('./') ||
    candidate.startsWith('../') ||
    (candidate.startsWith('apps/') && hasFileExtension(candidate)) ||
    (candidate.startsWith('packages/') && hasFileExtension(candidate)) ||
    (candidate.startsWith('DOCS/') && hasFileExtension(candidate)) ||
    (candidate.startsWith('.github/') && hasFileExtension(candidate)) ||
    (candidate.startsWith('tools/') && hasFileExtension(candidate)) ||
    candidate.startsWith('/') ||
    candidate === 'package.json' ||
    candidate === 'pnpm-workspace.yaml' ||
    candidate === 'AGENTS.md' ||
    candidate === 'CLAUDE.md'
  )
}

function hasFileExtension(candidate) {
  return /\.[A-Za-z0-9]+$/.test(candidate)
}

function looksLikeCommand(candidate) {
  return /^(pnpm|npm|node|git|\.\/mvnw)\b/.test(candidate)
}

function resolveDocPath(candidatePath, fileDir) {
  const withoutAnchor = candidatePath.split('#')[0]

  if (!withoutAnchor) {
    return null
  }

  if (path.isAbsolute(withoutAnchor)) {
    return withoutAnchor
  }

  const relativeToDoc = path.resolve(fileDir, withoutAnchor)
  if (fs.existsSync(relativeToDoc)) {
    return relativeToDoc
  }

  return path.resolve(repoRoot, withoutAnchor)
}

function validateCommand(command, fileDir) {
  if (command.includes(' / ') || command.includes(' | ')) {
    return null
  }

  const parts = command.split(/\s+/)
  const [tool] = parts

  if (tool === 'pnpm') {
    return validatePnpmCommand(parts.slice(1), fileDir)
  }

  if (tool === './mvnw') {
    const mvnwPath = path.resolve(fileDir, './mvnw')
    return fs.existsSync(mvnwPath) ? null : 'missing ./mvnw'
  }

  return null
}

function validatePnpmCommand(args, fileDir) {
  if (args.length === 0) {
    return null
  }

  let packageDir = fileDir
  let commandIndex = 0

  if (args[0] === '--dir' && args[1]) {
    packageDir = path.resolve(fileDir, args[1])
    commandIndex = 2
  }

  const commandName = args[commandIndex]
  if (!commandName) {
    return 'missing pnpm subcommand'
  }

  if (PNPM_BUILTINS.has(commandName)) {
    return null
  }

  if (commandName === 'run' && args[commandIndex + 1]) {
    return hasPackageScript(packageDir, args[commandIndex + 1])
      ? null
      : 'missing package.json script'
  }

  return hasPackageScript(packageDir, commandName) ? null : 'missing package.json script'
}

function hasPackageScript(packageDir, scriptName) {
  const packageJsonCandidates = [findPackageJson(packageDir), path.join(repoRoot, 'package.json')]

  for (const packageJsonPath of packageJsonCandidates) {
    const packageJson = JSON.parse(readText(packageJsonPath))
    if (packageJson.scripts?.[scriptName]) {
      return true
    }
  }

  return false
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  process.exitCode = run()
}
