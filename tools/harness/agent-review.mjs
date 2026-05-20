import { execFileSync } from 'node:child_process'
import fs from 'node:fs'
import path from 'node:path'
import process from 'node:process'

const MARKER = '<!-- trailquest-agent-review -->'
const MAX_DIFF_LINES = 2000
const MAX_DIFF_BYTES = 50 * 1024
const MAX_TOKENS = 4096
const ANTHROPIC_VERSION = '2023-06-01'
const PROVIDERS = {
  anthropic: {
    defaultBaseUrl: 'https://api.anthropic.com',
    defaultModel: 'claude-sonnet-4-6',
  },
  openai: {
    defaultBaseUrl: 'https://api.openai.com/v1',
    defaultModel: 'gpt-4o-mini',
  },
}
const REVIEW_FILES = [
  'AGENTS.md',
  'DOCS/agents/architecture/boundaries.md',
  'DOCS/agents/harness/checklists.md',
  'DOCS/agents/harness/review-loop.md',
  'DOCS/agents/conventions/README.md',
]

async function main() {
  const context = loadGitHubContext()
  const config = loadProviderConfig()

  if (!config.apiKey) {
    warn(`Agent review API key is missing for provider=${config.provider}, skipping agent review.`)
    return
  }

  let pr
  try {
    pr = loadPullRequest(context.prNumber)
  } catch (error) {
    warn(`Unable to load PR #${context.prNumber}: ${error.message}`)
    return
  }

  const reviewInputs = buildReviewInputs(pr)

  let rawResponseText
  let usage
  let stopReason
  let completionStatus

  try {
    const result = await requestReview(config, reviewInputs)
    rawResponseText = result.text
    usage = result.usage
    stopReason = result.stopReason
  } catch (error) {
    warn(`Agent review API request failed for provider=${config.provider}: ${error.message}`)
    return
  }

  completionStatus = classifyStopReason(config.provider, stopReason)

  if (completionStatus === 'invalid') {
    warn(`Provider stopped early with reason=${String(stopReason || 'unknown')}`)
    return
  }

  let review
  try {
    review = normalizeReview(parseReviewJson(rawResponseText), reviewInputs, {
      completionStatus,
      stopReason,
    })
  } catch (error) {
    warn(`Failed to parse agent review JSON: ${error.message}`)
    return
  }

  const commentBody = renderComment(pr, review, reviewInputs, usage, config)

  try {
    upsertPullRequestComment(context, commentBody)
  } catch (error) {
    warn(`Failed to publish PR comment: ${error.message}`)
    return
  }

  writeStepSummary(pr, review, reviewInputs, usage, config)
  console.log(`Published agent review for PR #${pr.number} with verdict: ${review.verdict}`)
}

function loadGitHubContext() {
  const eventPath = process.env.GITHUB_EVENT_PATH
  const repo = process.env.GITHUB_REPOSITORY

  if (!eventPath || !fs.existsSync(eventPath)) {
    throw new Error('GITHUB_EVENT_PATH is missing')
  }

  if (!repo) {
    throw new Error('GITHUB_REPOSITORY is missing')
  }

  const event = JSON.parse(fs.readFileSync(eventPath, 'utf8'))
  const prNumberFromEnv = process.env.PR_NUMBER ? Number(process.env.PR_NUMBER) : null
  const prNumberFromEvent = event.pull_request?.number ?? event.inputs?.pr_number ?? event.number
  const prNumber = prNumberFromEnv || Number(prNumberFromEvent)

  if (!Number.isInteger(prNumber) || prNumber <= 0) {
    throw new Error('Unable to infer PR number from GitHub event payload')
  }

  return { prNumber, repo }
}

function loadProviderConfig() {
  const provider = process.env.AGENT_REVIEW_PROVIDER || 'anthropic'
  const defaults = PROVIDERS[provider]

  if (!defaults) {
    throw new Error(`Unsupported AGENT_REVIEW_PROVIDER: ${provider}`)
  }

  const apiKey =
    process.env.AGENT_REVIEW_API_KEY ||
    (provider === 'anthropic' ? process.env.ANTHROPIC_API_KEY : process.env.OPENAI_API_KEY) ||
    ''

  return {
    provider,
    baseUrl: process.env.AGENT_REVIEW_BASE_URL || defaults.defaultBaseUrl,
    model: process.env.AGENT_REVIEW_MODEL || defaults.defaultModel,
    apiKey,
  }
}

function loadPullRequest(prNumber) {
  const viewJson = runGh([
    'pr',
    'view',
    String(prNumber),
    '--json',
    'number,title,url,body,changedFiles,additions,deletions,baseRefName,headRefName,files',
  ])

  const pr = JSON.parse(viewJson)
  pr.diff = runGh(['pr', 'diff', String(prNumber), '--patch'])
  pr.changedPaths = Array.isArray(pr.files) ? pr.files.map((file) => file.path) : []
  return pr
}

function buildReviewInputs(pr) {
  const docs = REVIEW_FILES.map((filePath) => ({
    filePath,
    content: fs.readFileSync(path.resolve(filePath), 'utf8'),
  }))

  const diffStats = summarizeDiff(pr.diff)
  const coarseMode = diffStats.lines > MAX_DIFF_LINES || diffStats.bytes > MAX_DIFF_BYTES
  const diffContent = coarseMode ? truncateDiff(pr.diff, MAX_DIFF_LINES, MAX_DIFF_BYTES) : pr.diff

  return {
    docs,
    diffContent,
    diffStats,
    coarseMode,
    metadata: {
      number: pr.number,
      title: pr.title,
      url: pr.url,
      body: pr.body ?? '',
      changedFiles: pr.changedFiles,
      additions: pr.additions,
      deletions: pr.deletions,
      baseRefName: pr.baseRefName,
      headRefName: pr.headRefName,
      changedPaths: pr.changedPaths,
    },
  }
}

function buildPromptTexts(reviewInputs) {
  const systemText = [
    'You are TrailQuest Agent Review, a strict PR reviewer.',
    'Only review the provided diff and repository guardrail documents.',
    'Return valid JSON only. Do not wrap it in markdown fences.',
    'Respond in Simplified Chinese for "summary" and "detail" fields. Keep "rule" identifiers in English snake_case.',
    'If the diff is incomplete or coarse_mode is true, do not approve the PR.',
    'Use request_changes only for clear blocking issues grounded in the diff and rules.',
    'Use comment when the diff is too large for precise review or confidence is limited.',
    '',
    ...reviewInputs.docs.map((doc) => `## ${doc.filePath}\n${doc.content}`),
  ].join('\n')

  const userText = [
    'Review this pull request diff against the repository rules.',
    '',
    'Output JSON schema:',
    '{"verdict":"approve|request_changes|comment","summary":"string","blocking_issues":[{"file":"string","line":0,"rule":"string","detail":"string"}],"non_blocking_notes":[{"file":"string","line":0,"detail":"string"}]}',
    '',
    'Rules:',
    '- Only cite files and lines that are visible in the diff.',
    '- Keep blocking issues focused on hard rules and real regressions.',
    '- If coarse_mode is true, set verdict to comment unless there is an obvious blocking issue visible in the truncated diff.',
    '- Do not mention unavailable repository context. Work only from these inputs.',
    '',
    `coarse_mode: ${reviewInputs.coarseMode ? 'true' : 'false'}`,
    `diff_lines: ${reviewInputs.diffStats.lines}`,
    `diff_bytes: ${reviewInputs.diffStats.bytes}`,
    '',
    'PR metadata:',
    JSON.stringify(reviewInputs.metadata, null, 2),
    '',
    'Unified diff:',
    reviewInputs.diffContent,
  ].join('\n')

  return { systemText, userText }
}

async function requestReview(config, reviewInputs) {
  const { systemText, userText } = buildPromptTexts(reviewInputs)

  if (config.provider === 'anthropic') {
    return callAnthropic({
      baseUrl: config.baseUrl,
      model: config.model,
      apiKey: config.apiKey,
      system: systemText,
      user: userText,
      maxTokens: MAX_TOKENS,
    })
  }

  if (config.provider === 'openai') {
    return callOpenAI({
      baseUrl: config.baseUrl,
      model: config.model,
      apiKey: config.apiKey,
      system: systemText,
      user: userText,
      maxTokens: MAX_TOKENS,
    })
  }

  throw new Error(`Unsupported provider: ${config.provider}`)
}

async function callAnthropic({ baseUrl, model, apiKey, system, user, maxTokens }) {
  const response = await fetch(joinApiUrl(baseUrl, '/v1/messages'), {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      'x-api-key': apiKey,
      'anthropic-version': ANTHROPIC_VERSION,
    },
    body: JSON.stringify({
      model,
      max_tokens: maxTokens,
      temperature: 0,
      system: [
        {
          type: 'text',
          text: system,
          cache_control: { type: 'ephemeral' },
        },
      ],
      messages: [
        {
          role: 'user',
          content: [{ type: 'text', text: user }],
        },
      ],
    }),
  })

  if (!response.ok) {
    const errorBody = await response.text()
    throw new Error(`HTTP ${response.status}: ${errorBody}`)
  }

  const responseJson = await response.json()
  const text = Array.isArray(responseJson.content)
    ? responseJson.content
        .filter((block) => block.type === 'text')
        .map((block) => block.text)
        .join('\n')
    : ''

  return {
    text,
    usage: responseJson.usage ?? null,
    stopReason: responseJson.stop_reason ?? null,
  }
}

async function callOpenAI({ baseUrl, model, apiKey, system, user, maxTokens }) {
  const requestBody = {
    model,
    messages: [
      { role: 'system', content: system },
      { role: 'user', content: user },
    ],
    max_tokens: maxTokens,
    temperature: 0,
    response_format: { type: 'json_object' },
  }

  let response = await fetch(joinApiUrl(baseUrl, '/chat/completions'), {
    method: 'POST',
    headers: {
      'content-type': 'application/json',
      Authorization: `Bearer ${apiKey}`,
    },
    body: JSON.stringify(requestBody),
  })

  if (!response.ok) {
    const errorBody = await response.text()

    if (response.status === 400 && /response_format/i.test(errorBody)) {
      response = await fetch(joinApiUrl(baseUrl, '/chat/completions'), {
        method: 'POST',
        headers: {
          'content-type': 'application/json',
          Authorization: `Bearer ${apiKey}`,
        },
        body: JSON.stringify({
          model,
          messages: requestBody.messages,
          max_tokens: maxTokens,
          temperature: 0,
        }),
      })

      if (!response.ok) {
        const fallbackErrorBody = await response.text()
        throw new Error(`HTTP ${response.status}: ${fallbackErrorBody}`)
      }
    } else {
      throw new Error(`HTTP ${response.status}: ${errorBody}`)
    }
  }

  const responseJson = await response.json()
  const choice = Array.isArray(responseJson.choices) ? responseJson.choices[0] : null
  const text = choice?.message?.content

  if (typeof text !== 'string' || !text.trim()) {
    throw new Error('OpenAI-compatible provider returned empty message content')
  }

  return {
    text,
    usage: responseJson.usage ?? null,
    stopReason: choice?.finish_reason ?? null,
  }
}

function parseReviewJson(rawText) {
  const trimmed = rawText.trim()
  const fencedMatch = trimmed.match(/```json\s*([\s\S]*?)```/i)
  if (fencedMatch) {
    return JSON.parse(fencedMatch[1].trim())
  }

  try {
    return JSON.parse(trimmed)
  } catch {
    const braceMatch = trimmed.match(/\{[\s\S]*\}/)
    if (!braceMatch) {
      throw new Error('No JSON object found in provider response')
    }
    return JSON.parse(braceMatch[0].trim())
  }
}

function normalizeReview(review, reviewInputs, providerResult) {
  const allowedVerdicts = new Set(['approve', 'request_changes', 'comment'])
  const verdict = allowedVerdicts.has(review.verdict) ? review.verdict : 'comment'
  const normalized = {
    verdict,
    summary:
      String(review.summary || '').trim() || 'Agent review completed without a usable summary.',
    blocking_issues: normalizeNotes(review.blocking_issues, true),
    non_blocking_notes: normalizeNotes(review.non_blocking_notes, false),
  }

  if (reviewInputs.coarseMode && normalized.verdict === 'approve') {
    normalized.verdict = 'comment'
    normalized.non_blocking_notes.unshift({
      detail: `Diff exceeded review budget (${reviewInputs.diffStats.lines} lines / ${reviewInputs.diffStats.bytes} bytes), so the agent review was downgraded to a coarse comment.`,
    })
  }

  if (normalized.blocking_issues.length > 0) {
    normalized.verdict = 'request_changes'
  }

  if (providerResult.completionStatus === 'truncated') {
    normalized.verdict = 'comment'
    normalized.non_blocking_notes.unshift({
      detail: `Provider 截断输出（reason=${String(providerResult.stopReason || 'unknown')}），评审结果可能不完整。`,
    })
  }

  return normalized
}

function normalizeNotes(notes, includeRule) {
  if (!Array.isArray(notes)) {
    return []
  }

  return notes
    .filter((note) => note && typeof note === 'object')
    .map((note) => {
      const normalized = {
        file: note.file ? String(note.file) : undefined,
        line: Number.isInteger(note.line) && note.line > 0 ? note.line : undefined,
        detail: String(note.detail || '').trim(),
      }

      if (includeRule) {
        normalized.rule = note.rule ? String(note.rule) : 'unspecified'
      }

      return normalized
    })
    .filter((note) => note.detail)
}

function renderComment(pr, review, reviewInputs, usage, config) {
  const lines = [
    MARKER,
    `[Agent Review: ${review.verdict.toUpperCase()}]`,
    '',
    `PR #${pr.number}: ${pr.title}`,
    '',
    review.summary,
    '',
  ]

  if (review.blocking_issues.length > 0) {
    lines.push('**Blocking issues**')
    for (const issue of review.blocking_issues) {
      lines.push(`- ${formatNote(issue, true)}`)
    }
    lines.push('')
  }

  if (review.non_blocking_notes.length > 0) {
    lines.push('**Non-blocking notes**')
    for (const note of review.non_blocking_notes) {
      lines.push(`- ${formatNote(note, false)}`)
    }
    lines.push('')
  }

  if (review.blocking_issues.length === 0 && review.non_blocking_notes.length === 0) {
    lines.push('No actionable issues were found in the provided diff.')
    lines.push('')
  }

  const modeSummary = reviewInputs.coarseMode
    ? `Coarse review: diff truncated at ${MAX_DIFF_LINES} lines / ${MAX_DIFF_BYTES} bytes.`
    : `Full diff review: ${reviewInputs.diffStats.lines} lines / ${reviewInputs.diffStats.bytes} bytes.`
  lines.push(`_Review mode_: ${modeSummary}`)

  if (usage) {
    const usageParts = []
    const inputTokens = usage.input_tokens ?? usage.prompt_tokens
    const outputTokens = usage.output_tokens ?? usage.completion_tokens

    if (inputTokens != null) usageParts.push(`input ${inputTokens}`)
    if (outputTokens != null) usageParts.push(`output ${outputTokens}`)
    if (usage.cache_read_input_tokens != null)
      usageParts.push(`cache read ${usage.cache_read_input_tokens}`)
    if (usage.cache_creation_input_tokens != null)
      usageParts.push(`cache write ${usage.cache_creation_input_tokens}`)
    if (usageParts.length > 0) {
      lines.push(`_Provider usage_: ${usageParts.join(', ')}`)
    }
  }

  lines.push(`_Provider_: \`${config.provider}\``)
  lines.push(`_Model_: \`${config.model}\``)
  return lines.join('\n')
}

function formatNote(note, includeRule) {
  const locationParts = []
  if (note.file) {
    locationParts.push(note.file)
  }
  if (note.line) {
    locationParts.push(`line ${note.line}`)
  }

  const prefixParts = []
  if (locationParts.length > 0) {
    prefixParts.push(locationParts.join(': '))
  }
  if (includeRule && note.rule) {
    prefixParts.push(`[${note.rule}]`)
  }

  return prefixParts.length > 0 ? `${prefixParts.join(' ')} ${note.detail}` : note.detail
}

function upsertPullRequestComment(context, body) {
  const commentsJson = runGh([
    'api',
    `repos/${context.repo}/issues/${context.prNumber}/comments`,
    '--paginate',
  ])

  const comments = JSON.parse(commentsJson)
  const existingComment = comments.find(
    (comment) => typeof comment.body === 'string' && comment.body.includes(MARKER),
  )

  if (existingComment) {
    runGh(
      [
        'api',
        `repos/${context.repo}/issues/comments/${existingComment.id}`,
        '--method',
        'PATCH',
        '--input',
        '-',
      ],
      JSON.stringify({ body }),
    )
    return
  }

  runGh(['pr', 'comment', String(context.prNumber), '--body', body])
}

function summarizeDiff(diffText) {
  return {
    lines: diffText.split('\n').length,
    bytes: Buffer.byteLength(diffText, 'utf8'),
  }
}

function truncateDiff(diffText, maxLines, maxBytes) {
  const output = []
  let byteCount = 0

  for (const line of diffText.split('\n')) {
    const nextLine = `${line}\n`
    const nextBytes = Buffer.byteLength(nextLine, 'utf8')

    if (output.length >= maxLines || byteCount + nextBytes > maxBytes) {
      output.push('... diff truncated for coarse review ...')
      break
    }

    output.push(line)
    byteCount += nextBytes
  }

  return output.join('\n')
}

function writeStepSummary(pr, review, reviewInputs, usage, config) {
  const summaryPath = process.env.GITHUB_STEP_SUMMARY
  if (!summaryPath) {
    return
  }

  const lines = [
    '# Agent Review',
    '',
    `- PR: #${pr.number} ${pr.title}`,
    `- Verdict: ${review.verdict}`,
    `- Provider: ${config.provider}`,
    `- Model: ${config.model}`,
    `- Coarse mode: ${reviewInputs.coarseMode ? 'yes' : 'no'}`,
    `- Diff size: ${reviewInputs.diffStats.lines} lines / ${reviewInputs.diffStats.bytes} bytes`,
    `- Blocking issues: ${review.blocking_issues.length}`,
    `- Non-blocking notes: ${review.non_blocking_notes.length}`,
  ]

  if (usage) {
    lines.push(`- Provider usage: ${JSON.stringify(usage)}`)
  }

  fs.appendFileSync(summaryPath, `${lines.join('\n')}\n`, 'utf8')
}

function runGh(args, input) {
  return execFileSync('gh', args, {
    encoding: 'utf8',
    input,
    env: {
      ...process.env,
      GH_TOKEN: process.env.GH_TOKEN || process.env.GITHUB_TOKEN,
    },
    stdio: ['pipe', 'pipe', 'pipe'],
  }).trim()
}

function joinApiUrl(baseUrl, suffix) {
  return `${baseUrl.replace(/\/+$/, '')}${suffix}`
}

function classifyStopReason(provider, stopReason) {
  if (provider === 'anthropic') {
    if (stopReason === 'end_turn') {
      return 'complete'
    }
    if (stopReason === 'max_tokens') {
      return 'truncated'
    }
    return 'invalid'
  }

  if (provider === 'openai') {
    if (stopReason === 'stop') {
      return 'complete'
    }
    if (stopReason === 'length') {
      return 'truncated'
    }
    return 'invalid'
  }

  return 'invalid'
}

function warn(message) {
  console.warn(`::warning::${message}`)
}

try {
  await main()
} catch (error) {
  console.error(error instanceof Error ? error.message : String(error))
  process.exitCode = 1
}
