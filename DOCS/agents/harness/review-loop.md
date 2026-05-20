本文描述 TrailQuest 的 Agent-to-Agent Review 流程。Claude Code 与 Codex 通过 AgentBridge 协作时按此流程推进。

## 角色

- Claude Code：地图、规划、文档、跨模块决策。
- Codex：实现、本地验证、CI / 工具脚本、回归。
- 人类：方向决策、最终合并。

## 单回合循环

```text
1. 用户分配任务给主 Agent
2. 主 Agent 拆分任务并通过 AgentBridge 与对端分工
3. 双方并行落地（实现 / 文档 / 脚本）
4. 实现方跑 pnpm check 自检
5. 评审方读 diff，按 DOCS/agents/harness/checklists.md 验收
6. 任一项不通过即回到第 3 步，并把教训写入 DOCS/agents/harness/failures.md
7. 全部通过 → 主 Agent 汇报用户，等待手动提交
```

## 沟通语言

- 使用显式表态：`My independent view is:`、`I agree on:`、`I disagree on:`、`Current consensus:`。
- 状态通知用 STATUS 前缀，便于过滤。
- 重要决定（边界规则、目录调整、提交策略）必须双方在 AgentBridge 上对齐，不在 commit message 里悄悄改。

## 回归与失败处理

- 任何 `pnpm check` 失败的项都需要在合并前修复，不允许跳过。
- 同类失败连续两次以上：暂停继续推进，先把案例写入 DOCS/agents/harness/failures.md，再编码到工具链。
- 发现 Agent 写出的测试明明通过却掩盖了 Bug：判定该测试无效，必须重写。

## 评审入口

### 本地手动触发（Claude Code）

在 Claude Code 内对一个 PR 跑评审：

```text
/review-pr <pr-number>           # 评审并把结果回贴到 PR 评论
/review-pr <pr-number> --dry-run # 只打印评审，不发送评论
```

实现：`.claude/commands/review-pr.md`。命令会拉 `gh pr diff`、读仓库硬规则文档、产出结构化评审（`verdict / summary / blocking_issues / non_blocking_notes`），并通过 `gh pr comment` 回贴。前缀固定为 `[Agent Review: <verdict>]`，便于过滤与后续聚合。

`verdict` 仅作信号，v1 不直接调 `gh pr review --approve / --request-changes`，避免误判后还要人工撤销。

### CI 自动触发

GitHub Actions 在 PR `opened / synchronize` 时跑 Agent 评审脚本（位于 tools/harness 目录下，由 Codex 落地）。脚本输入仅含 PR diff + 硬规则文档，不扩到全仓代码。输出走相同的 PR 评论协议。审查 provider 的 key 缺失或 API 异常时输出 warning 并跳过，不阻断主门禁。

### 审查 Agent 可配置

CI 侧审查 Agent 通过环境变量切换 provider 与模型，不把任何 key 写入仓库：

- `AGENT_REVIEW_PROVIDER`：`anthropic` 或 `openai`。默认 `anthropic`。
- `AGENT_REVIEW_BASE_URL`：可选。缺省时按 provider 取官方入口；也可以改成兼容服务的公开入口，例如 DeepSeek 的 `https://api.deepseek.com/anthropic` 或 `https://api.deepseek.com`。
- `AGENT_REVIEW_MODEL`：可选。默认 `claude-sonnet-4-6`（anthropic）或 `gpt-4o-mini`（openai）。
- `AGENT_REVIEW_API_KEY`：可选。优先级高于 provider 专属 key；若未设置，则 `anthropic` 回落到 `ANTHROPIC_API_KEY`，`openai` 回落到 `OPENAI_API_KEY`。

v1 的行为保持不变：只发 / 更新 PR 评论，不自动切 GitHub review state。provider 异常或 key 缺失时 soft-fail，只输出 warning。

## 与 Harness 工程的关系

- 流程层面：本文。
- 检查层面：`tools/harness/`。
- 文档层面：`DOCS/agents/**` 与各子包 AGENTS.md。
- CI 层面：`.github/workflows/harness.yml`，以及 PR Agent 评审 workflow（由 Codex 在 B 阶段落地）。
- 评审层面：`.claude/commands/review-pr.md` 与 CI 侧的 Agent 评审脚本。

各层一起构成「Agent 失败一次，环境就被加固一次」的反馈闭环。
