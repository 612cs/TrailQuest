本文汇总 TrailQuest 的通用编码规范。子包独有的规范放在各自的 AGENTS.md。

## TypeScript / Vue

- 全部使用 Composition API + `<script setup>`；不写 Options API。
- `tsconfig.json` 严格模式开启；不允许 `any` 与隐式 `any`，用 `unknown` 加类型守卫替代。
- 所有数据结构都需要 interface / type，Pinia state、API 请求响应、组件 props 必须有显式类型。
- 公共导出统一通过子包的 `index.ts`，不要直接 import 深层路径。

## 样式

- 使用 Tailwind CSS v4，主题 token 在 `apps/web/src/style.css` 与 `apps/admin/src/style.css` 的 `@theme` 块。
- 使用 CSS 变量（如 `var(--primary-500)`）而不是硬编码颜色，所有 UI 必须同时适配明暗模式。
- 卡片用 `.card`、玻璃头 `.glass-header`、难度徽章 `.badge-easy / .badge-moderate / .badge-hard`。
- 入场动画统一用 `animate-fade-in-up`。

## 图标与文案

- 图标统一使用 `lucide-vue-next`，`stroke-width="2"`。
- 文档（AGENTS.md、`DOCS/agents/**`）不使用 emoji，由 `pnpm harness:check` 强制。
- UI 文案使用中文，面向用户。

## 后端

- 统一响应信封 `{ success, data, message, code }`，详见 apps/api/AGENTS.md。
- Controller 仅参数校验与委托，业务逻辑放 Service。
- 不直接返回 entity，使用 DTO。
- SQL 通过 MyBatis-Plus 条件构造器拼装，避免硬编码字符串。

## 通用纪律

- 不写无用注释；只在表达 “为什么” 而非 “是什么” 时写注释。
- 不为不可能发生的场景加防御代码。
- 不引入 backwards-compatibility shim；可以直接删除未使用代码。
- 修改公共 API 时同步更新调用方与文档。

## 对应规范

- 命名：DOCS/agents/conventions/naming.md
- 测试：DOCS/agents/conventions/testing.md
- 工具链（oxlint / oxfmt）：DOCS/TOOLCHAIN.md
