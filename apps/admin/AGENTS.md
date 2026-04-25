# apps/admin — 管理后台

## 架构

Vue 3 SPA，独立于用户端的管理后台，使用原始 CSS（非 Tailwind）。

```text
src/
├── api/             # 5 个 API 模块（admin, auth, constants, http, uploads）
├── components/      # 20 个组件，按功能目录组织
│   ├── common/      # 通用基础组件
│   ├── dashboard/   # 仪表盘
│   ├── layout/      # 布局（Shell, Sidebar, Topbar）
│   ├── logs/        # 操作日志
│   ├── reviews/     # 评论管理
│   ├── trails/      # 路线管理
│   └── users/       # 用户管理
├── composables/     # 6 个组合式函数
├── router/          # 路由配置（含 ADMIN 角色守卫）
├── stores/          # 3 个 Pinia Store（auth, pinia, theme）
├── types/           # 5 个类型文件
├── utils/           # 2 个工具模块
└── views/           # 12 个页面级组件
```

## 命令

```bash
pnpm dev              # Vite 开发服务器
pnpm build            # 生产构建
```

## 关键约定

- 独立认证流程，使用 ADMIN 角色验证，独立 localStorage key
- 路由守卫强制 ADMIN 角色，非管理员重定向到登录页
- API 约 25 个端点：仪表盘、审核、管理、日志、配置等
- 使用原始 CSS，不使用 Tailwind
- 布局组件在 `components/layout/`（Shell, Sidebar, Topbar）

## Guardrails

- **不要**绕过路由守卫 — 所有管理页面必须验证 ADMIN 角色
- **不要**与用户端 API 混用 — 使用 `src/api/admin.ts` 独立端点
- **不要**在 Store 中硬编码角色判断 — 使用 auth store 的角色验证方法
- **不要**跳过操作日志 — 所有管理操作应记录审计日志
