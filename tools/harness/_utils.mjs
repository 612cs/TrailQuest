import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const THIS_DIR = path.dirname(fileURLToPath(import.meta.url))

export const repoRoot = path.resolve(THIS_DIR, '..', '..')

export function toRepoRelative(targetPath) {
  return path.relative(repoRoot, targetPath).split(path.sep).join('/')
}

export function walkFiles(rootDir, predicate) {
  if (!fs.existsSync(rootDir)) {
    return []
  }

  const entries = fs.readdirSync(rootDir, { withFileTypes: true })
  const files = []

  for (const entry of entries) {
    const entryPath = path.join(rootDir, entry.name)

    if (entry.isDirectory()) {
      files.push(...walkFiles(entryPath, predicate))
      continue
    }

    if (!predicate || predicate(entryPath)) {
      files.push(entryPath)
    }
  }

  return files
}

export function readText(filePath) {
  return fs.readFileSync(filePath, 'utf8')
}

export function resolveWorkspaceImport(importerPath, specifier) {
  if (specifier.startsWith('@trailquest/ui/')) {
    return path.resolve(repoRoot, 'packages/ui/src', specifier.slice('@trailquest/ui/'.length))
  }

  if (specifier === '@trailquest/ui') {
    return path.resolve(repoRoot, 'packages/ui/src/index.ts')
  }

  if (specifier.startsWith('./') || specifier.startsWith('../')) {
    return path.resolve(path.dirname(importerPath), specifier)
  }

  if (specifier.startsWith('/')) {
    return path.resolve(repoRoot, `.${specifier}`)
  }

  return null
}

export function collectImports(sourceText) {
  const pattern =
    /\b(?:import|export)\s+(?:[^'"`]*?\s+from\s+)?['"]([^'"]+)['"]|\bimport\(\s*['"]([^'"]+)['"]\s*\)/g

  const imports = []

  for (const match of sourceText.matchAll(pattern)) {
    const specifier = match[1] ?? match[2]
    if (specifier) {
      imports.push(specifier)
    }
  }

  return imports
}

export function isSourceFile(filePath) {
  return /\.(?:[cm]?[jt]sx?|vue)$/.test(filePath)
}

export function isMarkdownFile(filePath) {
  return filePath.endsWith('.md')
}

export function projectForPath(targetPath) {
  const normalized = toRepoRelative(targetPath)

  if (normalized.startsWith('apps/web/')) {
    return 'apps/web'
  }

  if (normalized.startsWith('apps/admin/')) {
    return 'apps/admin'
  }

  if (normalized.startsWith('apps/api/')) {
    return 'apps/api'
  }

  if (normalized.startsWith('packages/ui/')) {
    return 'packages/ui'
  }

  return null
}

export function sourceArea(targetPath) {
  const normalized = toRepoRelative(targetPath)
  const marker = '/src/'
  const markerIndex = normalized.indexOf(marker)

  if (markerIndex === -1) {
    return null
  }

  const afterSrc = normalized.slice(markerIndex + marker.length)
  const [area] = afterSrc.split('/')
  return area ?? null
}

export function resolveExistingPath(candidatePath) {
  const directCandidates = [
    candidatePath,
    `${candidatePath}.ts`,
    `${candidatePath}.tsx`,
    `${candidatePath}.js`,
    `${candidatePath}.jsx`,
    `${candidatePath}.mjs`,
    `${candidatePath}.cjs`,
    `${candidatePath}.vue`,
    path.join(candidatePath, 'index.ts'),
    path.join(candidatePath, 'index.tsx'),
    path.join(candidatePath, 'index.js'),
    path.join(candidatePath, 'index.vue'),
  ]

  for (const directCandidate of directCandidates) {
    if (fs.existsSync(directCandidate)) {
      return directCandidate
    }
  }

  return candidatePath
}

export function findPackageJson(startDir) {
  let currentDir = startDir

  while (currentDir.startsWith(repoRoot)) {
    const packageJsonPath = path.join(currentDir, 'package.json')
    if (fs.existsSync(packageJsonPath)) {
      return packageJsonPath
    }

    if (currentDir === repoRoot) {
      break
    }

    currentDir = path.dirname(currentDir)
  }

  return path.join(repoRoot, 'package.json')
}
