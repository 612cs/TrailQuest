# apps/web — 用户端前端

## 架构

Vue 3 SPA，Composition API + `<script setup>`，Tailwind CSS v4 样式。

```text
src/
├── api/             # 10 个 API 模块（统一 Axios 客户端）
├── components/      # 44 个组件，按 feature 目录组织
│   ├── auth/        # 登录/注册
│   ├── chat/        # AI 聊天
│   ├── common/      # 通用基础组件
│   ├── community/   # 社区
│   ├── home/        # 首页
│   ├── profile/     # 个人中心
│   ├── search/      # 搜索
│   └── trail/       # 路线详情
├── composables/     # 7 个组合式函数
├── mock/            # Mock 数据
├── router/          # 路由配置
├── stores/          # 9 个 Pinia Store
├── types/           # 15 个 TypeScript 类型文件
├── utils/           # 5 个工具模块
└── views/           # 9 个页面级组件
```

## 命令

```bash
pnpm dev              # Vite 开发服务器
pnpm build            # 生产构建
pnpm test:unit        # Vitest 单元测试
pnpm test:e2e         # Playwright E2E 测试
```

## 关键约定

- 组件按 feature 目录组织（`components/trail/`、`components/chat/` 等）
- View 负责布局和数据获取，委托给子组件
- API 模块统一使用 `http.ts` Axios 客户端，自动解包 `ApiResponse<T>`
- Store 使用 Pinia，按职责拆分（认证、聊天、主题、上传等）
- 类型文件与 API 模块一一对应
- Mock 数据统一在 `src/mock/` 目录
- 所有 UI 必须适配明/暗模式
- 不使用 emoji，使用 lucide-vue-next 图标

## Guardrails

- **不要**在组件中直接调用 Axios — 使用 `src/api/` 模块
- **不要**在 `src/api/` 之外手动处理 token — `http.ts` 统一注入
- **不要**使用硬编码颜色 — 使用 CSS 变量 `var(--primary-*)`
- **不要**跳过主题适配 — 所有新增 UI 必须同时适配明/暗模式
- **不要**在 View 之外做复杂数据获取 — View 负责获取，子组件负责渲染
