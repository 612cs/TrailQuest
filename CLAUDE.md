本文是 Claude Code 在 TrailQuest 仓库内工作时的入口指引。详细规则请按 AGENTS.md 的导航表检索 `DOCS/agents/`。

## 项目简介

TrailQuest 是 Vue 3 + TypeScript 徒步路线发现与分享平台，pnpm monorepo。

## 入口文档

- 根 [AGENTS.md](./AGENTS.md) — Agent 操作地图与硬性规则
- [DOCS/harness.md](./DOCS/harness.md) — Harness 工程理论
- [DOCS/agents/README.md](./DOCS/agents/README.md) — Agent 操作手册集合
- [DOCS/agents/architecture/boundaries.md](./DOCS/agents/architecture/boundaries.md) — 模块边界与依赖规则
- [DOCS/agents/conventions/README.md](./DOCS/agents/conventions/README.md) — 通用编码规范
- [DOCS/agents/harness/checklists.md](./DOCS/agents/harness/checklists.md) — PR 自检清单
- [DOCS/agents/harness/failures.md](./DOCS/agents/harness/failures.md) — 失败案例库

## 常用命令

```bash
pnpm install
pnpm dev
pnpm dev:admin
pnpm dev:api
pnpm typecheck
pnpm lint
pnpm format:check
pnpm harness:check
pnpm test
pnpm check
```

## 提交纪律

- 不自动提交代码，由用户手动触发。
- 提交信息使用中文 Conventional Commits（`feat:`、`fix:`、`docs:`、`refactor:`、`chore:`）。
- 优先使用 GitHub MCP 提交。
- 提交前必须 `pnpm check` 通过。

## 失败即学习

任何 Agent 在仓库内出错的案例都写入 `DOCS/agents/harness/failures.md`，必要时把规则编码到 `tools/harness/` 或 `.oxlintrc.json`，让下一个 Agent 不会再犯同样的错。
