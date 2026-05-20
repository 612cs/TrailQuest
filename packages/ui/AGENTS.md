本文是 `packages/ui` 共享 UI 组件库的 Agent 操作手册。通用规范见根 AGENTS.md 与 DOCS/agents/。

## 架构

`@trailquest/ui` 共享组件库，被 `apps/web` 与 `apps/admin` 通过 Vite alias 直接引用源码。

```text
packages/ui/
├── package.json     dependencies: vue、lucide-vue-next
├── src/
│   ├── index.ts     公共导出入口
│   └── components/  共享组件
│       ├── ImagePreviewModal.vue
│       ├── ModalShell.vue
│       └── Pagination.vue
└── tsconfig.json
```

## 命令

`packages/ui` 不单独跑命令，被 `apps/web` 与 `apps/admin` 的 Vite / vue-tsc 一并编译。

根目录类型检查：`pnpm typecheck`。

## 关键约定

- 仅放跨 app 复用、且与业务无关的组件；业务组件留在各 app 自身的 `src/components/`。
- 所有公共组件通过 `src/index.ts` 导出，禁止跨包深路径 import。
- 不依赖业务 store 或 axios；只接收 props、抛出 emit。
- 样式通过 CSS 变量同时适配明暗模式；不绑定具体业务主题。
- 图标统一 `lucide-vue-next`，不使用 emoji。

## 边界与硬规则

由 `pnpm harness:check` 强制：

- 不得反向依赖任何 `apps/*` 源码（uiReverse 规则）。
- 仅依赖 `vue`、`lucide-vue-next` 与同包内文件。

详见 DOCS/agents/architecture/boundaries.md。

## 新增组件流程

1. 在 `src/components/` 新建组件，props 与 emit 必须有显式 TypeScript 类型。
2. 在 `src/index.ts` 增加 `export { default as Foo } from './components/Foo.vue'`。
3. 跑 `pnpm typecheck` 确认 `apps/web`、`apps/admin` 的消费端能编译。
4. 跑 `pnpm harness:check` 确认未引入反向依赖。
