---
description: 以 TrailQuest 仓库规则评审一个 PR，必要时自动贴回评论
argument-hint: <pr-number> [--dry-run]
allowed-tools: Bash, Read
---

你将扮演 TrailQuest 仓库的 Agent 评审者，针对 GitHub PR `$ARGUMENTS` 给出可执行的代码评审。

## 输入参数

- 第一个参数：PR 编号（必填）
- 第二个参数：`--dry-run`（可选）。出现时只打印评审结果，不调用 `gh pr comment`。

请先解析参数；若缺少 PR 编号，直接告诉用户用法 `/review-pr <pr-number> [--dry-run]` 后退出，不要继续。

## 步骤

1. **拉取上下文**（并行执行，使用 Bash 工具）：
   - `gh pr view <pr> --json number,title,author,state,baseRefName,headRefName,changedFiles,additions,deletions,body`
   - `gh pr diff <pr>`
   - `gh pr view <pr> --json files --jq '.files[].path'`
2. **加载仓库硬规则**（并行执行，使用 Read 工具）：
   - `AGENTS.md`
   - `DOCS/agents/architecture/boundaries.md`
   - `DOCS/agents/harness/checklists.md`
   - `DOCS/agents/conventions/README.md`
3. **静态判断 diff**：
   - 仅评审 diff 中实际变化的部分，不要去翻整个仓库的代码。
   - 命中以下任意硬规则的，标 `blocking`：
     - 跨 app import：`apps/web` 与 `apps/admin` 互相 import；其它 `apps/*` 之间互相 import。
     - 反向依赖：`packages/ui` import 了 `apps/*`。
     - 分层错位：`src/components/*` import `src/views/*`；`src/stores/*` import `src/components/*`；`src/mock/*` import 出去到非 `src/mock` / `src/types`。
     - 后端 API 没有用统一 `{ success, data, message, code }` 信封。
     - 文档零 emoji 区域（根 AGENTS.md、CLAUDE.md、子包 AGENTS.md、`DOCS/agents/**`）出现 emoji。
     - 文档新鲜度：新增/改动文档里写了已不存在的 `pnpm` 命令或文件路径。
     - 前端 UI 用了硬编码颜色而非 CSS 变量，或没有暗色模式适配。
   - 其它建议（命名不一致、可读性、潜在的边界问题、缺测试等）放 `non_blocking`。
4. **生成结构化评审**：用如下 JSON 严格输出（可以包在 \`\`\`json 围栏里给用户先看一眼）：
   ```json
   {
     "verdict": "approve | request_changes | comment",
     "summary": "一段话概述本次评审的核心结论",
     "blocking_issues": [
       { "file": "path/to/file", "line": 12, "rule": "boundary/cross-app", "detail": "..." }
     ],
     "non_blocking_notes": [
       { "file": "path/to/file", "line": 34, "detail": "..." }
     ]
   }
   ```
   - `verdict` 取值规则：有任何 `blocking_issues` → `request_changes`；零 blocking 且整体值得放行 → `approve`；不确定但想留观察意见 → `comment`。
   - `line` 字段尽量给 diff 中真实命中的行号；拿不到就省略该字段，不要瞎编。
5. **回贴 PR 评论**（除非用户传了 `--dry-run`）：
   把评审写成 Markdown，前缀固定为 `[Agent Review: <verdict>]`，正文包含 summary + blocking 列表 + non-blocking 列表。然后用：
   ```
   gh pr comment <pr> --body "$(cat <<'EOF'
   [Agent Review: ...]
   ...
   EOF
   )"
   ```
   注意：用 heredoc，不要把 body 拼到命令行里，避免引号转义出问题。
6. **结束**：在最后一段告诉用户评审已贴到 PR #<pr>，并给出 `gh pr view <pr>` 的链接提示；若是 `--dry-run`，明确说明没有发送评论。

## 纪律

- 不修改仓库内任何文件，只做读取 + 评论。
- 不要扩到 PR 之外的代码。命中的硬规则要指明 diff 里的具体文件路径与原文片段。
- 如果 diff 超过 2000 行，先告诉用户体量较大，给出粗粒度评审（仅 summary + 关键文件清单），不要逐行评。
- 找不到 PR / 仓库没绑 GitHub / `gh` 未登录：直接报错给用户，不要尝试从其它地方猜。
