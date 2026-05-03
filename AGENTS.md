# AGENTS.md

本文件为 AI Agent 在此仓库中工作时提供指导。

## 项目概述

TrailQuest 是一个面向中文用户的徒步路线发现与分享平台，采用 pnpm monorepo 统一管理前端、管理后台、后端与共享 UI 包。

## 常用命令

```bash
pnpm install              # 安装所有依赖

pnpm dev                  # 启动用户端前端 (apps/web)
pnpm dev:admin            # 启动管理后台 (apps/admin)
pnpm dev:api              # 启动后端 API

pnpm build / build:admin / build:api
pnpm test:web:unit / test:web:e2e / test:api
```

## 架构

```text
hiking/
├── apps/
│   ├── web/       # 用户端 SPA — Vue 3 + TypeScript + Tailwind v4
│   ├── admin/     # 管理后台 SPA — Vue 3 + TypeScript
│   └── api/       # Spring Boot 3 + MyBatis-Plus 后端
├── packages/
│   └── ui/        # 共享 UI 组件库 (@trailquest/ui)
└── DOCS/          # 项目文档
```

各子包的架构细节、约定和 Guardrails，见对应目录下的 `AGENTS.md`。

## 代码提交

- **流程**：不自动提交代码，由用户手动触发。
- **提交信息**：遵循 Conventional Commits，使用中文（`feat:`, `fix:`, `docs:` 等）。
- **工具优先**：优先使用 GitHub MCP 提交。

## 相关文档

- [DOCS/TOOLCHAIN.md](./DOCS/TOOLCHAIN.md) - 代码质量工具链配置（oxlint、oxfmt、Oxc）
- [DOCS/INTERNSHIP_OBJECTIVES.md](./DOCS/INTERNSHIP_OBJECTIVES.md) - 实习目的与目标
