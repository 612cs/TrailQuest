本文是 `apps/admin` 管理后台 SPA 的 Agent 操作手册。通用规范见根 AGENTS.md 与 DOCS/agents/。

## 架构

Vue 3 SPA，独立于用户端，使用原生 CSS（不引入 Tailwind），Pinia + Vue Router。

```text
src/
├── api/           按域拆分（admin、auth、constants、http、uploads）
├── components/    按功能目录组织（common、dashboard、layout、logs、reviews、trails、users 等）
├── composables/   组合式函数
├── router/        含 ADMIN 角色守卫
├── stores/        auth、pinia、theme
├── types/         类型定义
├── utils/         工具模块
└── views/         页面级组件
```

## 命令

```bash
pnpm dev                  Vite 开发服务器（端口 5174）
pnpm build                生产构建
pnpm preview              本地预览构建产物
```

根目录入口：`pnpm dev:admin`、`pnpm build:admin`、`pnpm preview:admin`。

## 关键约定

- 独立认证流程，使用 ADMIN 角色，独立 localStorage key，不与用户端共用。
- 所有路由由路由守卫强制 ADMIN 角色，未通过重定向到登录页。
- API 端点全部走 `src/api/admin.ts` 与 `src/api/http.ts`，不与用户端混用。
- 布局组件统一在 `components/layout/`（Shell、Sidebar、Topbar）。
- 所有管理操作必须落操作日志（后端审计表）。

## 边界与硬规则

由 `pnpm harness:check` 强制：

- 不得 import `apps/web` 的源码；只能依赖自身 `src` 与 `@trailquest/ui`。
- `src/components/*` 不得 import `src/views/*`。
- `src/stores/*` 不得 import `src/components/*`。

详见 DOCS/agents/architecture/boundaries.md。

## UI 纪律

- 即便不使用 Tailwind，仍需通过 CSS 变量同时适配明暗模式。
- 图标统一 `lucide-vue-next`，不使用 emoji。
- 风险动作（封禁 / 下架 / 删除）必须二次确认 + 日志记录。

## 失败案例参考

- 文档与 typecheck 失败案例：DOCS/agents/harness/failures.md
- PR 自检清单：DOCS/agents/harness/checklists.md
