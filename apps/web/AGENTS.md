本文是 `apps/web` 用户端 SPA 的 Agent 操作手册。通用规范见根 AGENTS.md 与 DOCS/agents/。

## 架构

Vue 3 SPA + Composition API，Tailwind CSS v4，Pinia 3，Vue Router 5，Vite 7。

```text
src/
├── api/            统一 Axios 封装与后端接口适配
├── components/     按特性域组织（auth、chat、common、community、home、profile、search、trail）
├── composables/    无副作用逻辑复用
├── mock/           开发期假数据与 TypeScript 接口
├── router/         路由配置
├── stores/         Pinia store
├── types/          类型定义
├── utils/          纯工具函数
└── views/          路由级页面
```

## 命令

```bash
pnpm dev                 仅启动用户端
pnpm test:unit           Vitest 单元测试
pnpm test:e2e            Playwright E2E
pnpm build               生产构建
pnpm preview             本地预览构建产物
```

根目录入口：`pnpm dev`、`pnpm test:web:unit`、`pnpm build:web`。

## 关键约定

- View 负责数据获取与布局，子组件只渲染 props。
- 所有 HTTP 调用走 `src/api/`，组件不直接 `axios`。
- 鉴权 token 与请求头由 `src/api/http.ts` 统一注入，业务代码不得手写。
- `ApiResponse<T>` 信封由 `http.ts` 自动拆包，组件拿到的是 `data` 本身。
- Mock 数据集中在 `src/mock/`，并保持与 `src/types/` 同步，方便切换真实 API。
- 路由缓存（`<keep-alive>`）覆盖 Home / Search / Community。

## 边界与硬规则

由 `pnpm harness:check` 强制：

- 不得 import `apps/admin` 的源码；只能依赖自身 `src` 与 `@trailquest/ui`。
- `src/components/*` 不得 import `src/views/*`。
- `src/stores/*` 不得 import `src/components/*`。
- `src/mock/*` 只能 import `src/mock/*` 与 `src/types/*`。

详见 DOCS/agents/architecture/boundaries.md。

## UI 纪律

- 所有 UI 必须同时适配明暗模式，使用 CSS 变量（`var(--primary-500)` 等）。
- 图标统一 `lucide-vue-next`，`stroke-width="2"`。
- 不使用 emoji；用图标传递语义。
- 卡片用 `.card`、玻璃头 `.glass-header`、难度徽章 `.badge-easy / .badge-moderate / .badge-hard`、入场动画 `animate-fade-in-up`。

## 失败案例参考

- 文档新鲜度：DOCS/agents/harness/failures.md
- PR 自检清单：DOCS/agents/harness/checklists.md
