本文件是 TrailQuest 项目的 Agent 入口地图。所有 AI Agent（Codex / Claude Code / 其它）在仓库内工作时，先读本文件，再按需检索 `DOCS/agents/` 下的详细文档。本文件保持精简，详细规范放在 `DOCS/agents/`。

## 项目简介

TrailQuest 是面向中文用户的徒步路线发现与分享平台，pnpm monorepo，前端 Vue 3 + TypeScript，后端 Spring Boot 3。

```text
hiking/
├── apps/web        用户端 SPA (Vue 3 + Vite 7)
├── apps/admin      管理后台 SPA (Vue 3 + Vite 7)
├── apps/api        Spring Boot 3 后端
├── packages/ui     共享 UI 组件库 (@trailquest/ui)
├── tools/harness   Harness 检查脚本（边界 / emoji / 文档新鲜度）
└── DOCS/           项目文档
```

## 快速导航

| 想做什么 | 去哪里看 |
|---------|---------|
| 了解 Harness 工程整体设计 | DOCS/harness.md |
| 了解模块边界与依赖规则 | DOCS/agents/architecture/boundaries.md |
| 了解系统架构总览 | DOCS/agents/architecture/overview.md |
| 了解通用编码规范 | DOCS/agents/conventions/README.md |
| 了解测试规范 | DOCS/agents/conventions/testing.md |
| 了解命名规范 | DOCS/agents/conventions/naming.md |
| 查看 Agent 失败案例库 | DOCS/agents/harness/failures.md |
| 查看 PR 自检清单 | DOCS/agents/harness/checklists.md |
| 查看 Agent 互评流程 | DOCS/agents/harness/review-loop.md |
| 查看当前迭代任务 | DOCS/agents/plans/current-sprint.md |
| 工具链（oxlint / oxfmt） | DOCS/TOOLCHAIN.md |
| DOCS 目录索引 | DOCS/README.md |
| 用户端规则 | apps/web/AGENTS.md |
| 管理后台规则 | apps/admin/AGENTS.md |
| 后端规则 | apps/api/AGENTS.md |
| 共享 UI 包规则 | packages/ui/AGENTS.md |

## 硬性规则（CI 会验证）

1. **依赖方向**：`packages/ui` 不得反向依赖 `apps/*`；`apps/*` 之间不得互相 import；`apps/web` 与 `apps/admin` 只能 import 自身 `src` 与 `packages/ui`。详见 DOCS/agents/architecture/boundaries.md。
2. **分层依赖**：`src/components/*` 不得 import `src/views/*`；`src/stores/*` 不得 import `src/components/*`；`src/mock/*` 只能 import `src/mock/*` 与 `src/types/*`。
3. **文档零 emoji**：根 AGENTS.md、CLAUDE.md、各子包 AGENTS.md、`DOCS/agents/**` 内不得使用 emoji。其它文档（如 `DOCS/harness.md`）不受限。
4. **文档新鲜度**：上述文档中引用的 `pnpm` 命令、文件路径必须真实存在，否则 `pnpm harness:check` 会失败。
5. **统一响应格式（后端）**：所有 API 返回 `{ success, data, message, code }`。详见 apps/api/AGENTS.md。
6. **主题适配（前端）**：所有 UI 必须同时适配明暗模式，使用 CSS 变量而非硬编码颜色。详见 apps/web/AGENTS.md。

## 常用命令（根目录执行）

```bash
pnpm install             # 安装依赖
pnpm dev                 # 启动用户端
pnpm dev:admin           # 启动管理后台
pnpm dev:api             # 启动后端
pnpm typecheck           # 类型检查（web + admin）
pnpm lint                # oxlint
pnpm format:check        # oxfmt 检查
pnpm harness:check       # Harness 自定义检查
pnpm check               # 一键全量自检（typecheck + lint + format:check + harness:check + test）
pnpm test                # 单元测试聚合
```

`pnpm check` 是 PR 合并前的快速闸门，串 `typecheck → lint → format:check → harness:check → test:web:unit`。后端集成测试 `pnpm test:api` 由独立的 CI job `api-test` 跑，依赖 MySQL 与 Redis 容器，详见 `.github/workflows/harness.yml`。

## 代码提交

- 不自动提交代码，由用户手动触发。
- 提交信息使用中文 Conventional Commits（`feat:`、`fix:`、`docs:`、`refactor:` 等）。
- 优先使用 GitHub MCP 工具提交，保证原子性与可追溯性。

## 失败即学习

每次 Agent 在本仓库出错（或人工 Review 拦下问题），将教训写入 DOCS/agents/harness/failures.md，必要时把规则编码到 `tools/harness/` 或 `.oxlintrc.json`，让下一个 Agent 不会再犯同样的错。
