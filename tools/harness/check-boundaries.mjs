import { fileURLToPath } from 'node:url'

import {
  collectImports,
  isSourceFile,
  projectForPath,
  readText,
  repoRoot,
  resolveExistingPath,
  resolveWorkspaceImport,
  sourceArea,
  toRepoRelative,
  walkFiles,
} from './_utils.mjs'

const RULES = {
  crossApp: 'apps/* cannot import another app source tree',
  appScope: 'apps/web and apps/admin can only import their own src and packages/ui',
  uiReverse: 'packages/ui cannot import apps/*',
  componentView: 'src/components/* cannot import src/views/*',
  storeComponent: 'src/stores/* cannot import src/components/*',
  mockIsolation: 'src/mock/* can only import src/mock/* and src/types/*',
}

export function run() {
  const files = [
    ...walkFiles(`${repoRoot}/apps/web/src`, isSourceFile),
    ...walkFiles(`${repoRoot}/apps/admin/src`, isSourceFile),
    ...walkFiles(`${repoRoot}/packages/ui/src`, isSourceFile),
  ]

  const violations = []

  for (const filePath of files) {
    const importerProject = projectForPath(filePath)
    const importerArea = sourceArea(filePath)

    for (const specifier of collectImports(readText(filePath))) {
      const targetPath = resolveWorkspaceImport(filePath, specifier)
      if (!targetPath) {
        continue
      }

      const existingTargetPath = resolveExistingPath(targetPath)
      const targetProject = projectForPath(existingTargetPath)
      const targetArea = sourceArea(existingTargetPath)

      if (!targetProject) {
        continue
      }

      if (
        importerProject?.startsWith('apps/') &&
        targetProject?.startsWith('apps/') &&
        importerProject !== targetProject
      ) {
        violations.push(formatViolation(filePath, specifier, RULES.crossApp))
      }

      if (
        (importerProject === 'apps/web' || importerProject === 'apps/admin') &&
        targetProject !== importerProject &&
        targetProject !== 'packages/ui'
      ) {
        violations.push(formatViolation(filePath, specifier, RULES.appScope))
      }

      if (importerProject === 'packages/ui' && targetProject.startsWith('apps/')) {
        violations.push(formatViolation(filePath, specifier, RULES.uiReverse))
      }

      if (
        importerArea === 'components' &&
        targetProject === importerProject &&
        targetArea === 'views'
      ) {
        violations.push(formatViolation(filePath, specifier, RULES.componentView))
      }

      if (
        importerArea === 'stores' &&
        targetProject === importerProject &&
        targetArea === 'components'
      ) {
        violations.push(formatViolation(filePath, specifier, RULES.storeComponent))
      }

      if (importerArea === 'mock' && targetProject === importerProject) {
        const allowedTargetAreas = new Set(['mock', 'types'])
        if (!allowedTargetAreas.has(targetArea)) {
          violations.push(formatViolation(filePath, specifier, RULES.mockIsolation))
        }
      }
    }
  }

  if (violations.length > 0) {
    console.error('Boundary check failed:')
    for (const violation of violations) {
      console.error(`- ${violation}`)
    }
    return 1
  }

  console.log(`Boundary check passed for ${files.length} files.`)
  return 0
}

function formatViolation(filePath, specifier, rule) {
  return `${toRepoRelative(filePath)} -> ${specifier} (${rule})`
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  process.exitCode = run()
}
