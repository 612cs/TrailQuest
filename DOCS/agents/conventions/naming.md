本文给出 TrailQuest 仓库内的命名约定，覆盖文件、目录、组件、状态、API 与提交信息。

## 文件与目录

| 类型 | 命名 | 示例 |
|------|------|------|
| 目录 | kebab-case，英文 | apps/web/src/components/trail-detail |
| Vue 组件文件 | PascalCase | `TrailCard.vue`、`AppHeader.vue` |
| 视图文件 | PascalCase + `View.vue` 后缀 | `HomeView.vue`、`SearchView.vue` |
| Composable | `use` 前缀 + camelCase | `useFlashStore.ts`、`useTrailFeed.ts` |
| Pinia store 文件 | camelCase + `.ts` | `chat.ts`、`useUserStore.ts` |
| Mock 数据文件 | camelCase | `mockData.ts` |
| 类型文件 | camelCase | `trail.ts`、`user.ts` |
| 工具函数 | camelCase | `formatDate.ts` |

## Vue 组件

- 组件名使用 PascalCase，文件名与组件名一致。
- props、emit 名称使用 camelCase，模板中使用 kebab-case。
- 单根组件，避免在模板顶层使用 fragment。

## 状态

- Pinia store id 使用 camelCase，与文件名一致。
- store 内部 state 使用 camelCase，action 使用动词开头（`fetchTrails`、`toggleFavorite`）。
- 派生数据用 `computed`，不要在 action 里维护派生字段。

## 后端

- Controller 类后缀 `Controller`，方法名动词开头（`listTrails`、`createTrail`）。
- Service 接口用名词，实现类后缀 `Impl`。
- Mapper 接口与表名同步，`TrailMapper.java` 对应 `trails` 表。
- DTO 后缀 `Dto`，请求 `Req`、响应 `Resp`。

## 数据库

- 表名小写复数 + 下划线（`trails`、`trail_reviews`）。
- 字段小写下划线（`created_at`、`user_id`）。
- 升级脚本命名 `mysql-<topic>-upgrade.sql`，详见 DOCS/database/。

## 提交信息

遵循中文 Conventional Commits：

```text
feat: 新增路线收藏接口
fix: 修复发布页草稿丢失
docs: 更新 AGENTS.md 导航
refactor: 抽出通用 ApiResponse 封装
chore: 升级 vite 至 8.0.10
```

不允许使用 emoji 前缀。

## 分支

- 主分支：`main`
- 功能分支：`feat/<scope>-<short-desc>`，如 `feat/trail-search-filter`
- 修复分支：`fix/<scope>-<short-desc>`
