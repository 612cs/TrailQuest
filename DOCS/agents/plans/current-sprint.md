本文记录当前迭代的 Agent 任务，作为 Agent 在「我接下来该做什么」时的入口。每个迭代收口后归档到 `DOCS/handoff/`。

## 迭代窗口

- 起始日期：2026-05-20
- 主题：搭建 Harness 工程骨架，建立 Agent 协作基线

## 已完成（本迭代）

- 重写根 AGENTS.md 为地图模式，建立 `DOCS/agents/` 骨架。
- 落地 `tools/harness/`：边界检查、emoji 检查、文档新鲜度检查与聚合入口 `index.mjs`。
- 根 `package.json` 增加 `typecheck`、`harness:check`、`check` 聚合命令。
- 新增 `.github/workflows/harness.yml`，PR 自动跑 `pnpm check`。
- 修复阻塞 typecheck 的两处现存问题：`apps/web/tsconfig.app.json` 的 `ignoreDeprecations`、`apps/admin/src/components/dashboard/DashboardOverviewCards.vue` 未使用导入。

## 进行中

- 子包 AGENTS.md 补齐：apps/web、apps/admin、packages/ui。
- 失败案例库 DOCS/agents/harness/failures.md 持续录入。

## 下一阶段候选

- 评论模块全量后端化，接入真实数据（前后端协同）。
- 路线封面 / 相册 / 头像编辑接入真实 OSS 上传。
- 个人页 “我的发布 / 我的收藏 / 统计信息” 接入真实数据。
- 搜索页地图视图（列表 / 地图切换）补全。
- 前端核心流程的单元测试与 E2E 覆盖。

## 维护

新增任务请在「下一阶段候选」追加；任务进入实做时移到「进行中」；完成后移到「已完成（本迭代）」并简述结果。迭代结束把整文件归档到 DOCS/handoff/，新建空白迭代文件。
