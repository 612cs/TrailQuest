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

## 与 Harness 工程的关系

- 流程层面：本文。
- 检查层面：`tools/harness/`。
- 文档层面：`DOCS/agents/**` 与各子包 AGENTS.md。
- CI 层面：`.github/workflows/harness.yml`。

四层一起构成「Agent 失败一次，环境就被加固一次」的反馈闭环。
