本文是 TrailQuest 仓库内 Agent 真实出现过的失败案例库。每一条都对应 `tools/harness/`、`.oxlintrc.json` 或 AGENTS.md 中的一条规则。规则不是空降的，是教训沉淀。

> 录入新案例时，使用同样的格式：场景、错误模式、根因、补救动作。一条只覆盖一个失败模式。

## 模板

```markdown
### [日期] 简短标题

- 场景：发生在哪个文件 / 哪个流程。
- 错误模式：Agent 做了什么错事，肉眼表现是什么。
- 根因：为什么会发生（环境缺陷而非模型问题）。
- 补救：在 Harness 里加了什么规则 / 在 AGENTS 里加了什么约束 / 哪个失败案例已编码。
```

## 已记录案例

### 2026-05-20 根 AGENTS.md 引用了已删除的 INTERNSHIP_OBJECTIVES.md

- 场景：根 AGENTS.md 末尾的「相关文档」段落指向已删除的实习目标文档（路径形式 DOCS 下的实习目标 .md，此处用文字描述以避免文档新鲜度检查反向命中）。
- 错误模式：文件早被删除，文档链接长期腐烂；后续 Agent 按链接索引会拉空。
- 根因：缺少文档新鲜度检查；删文件没有同步删引用。
- 补救：
  1. 重写根 `AGENTS.md` 为地图模式，移除失效引用。
  2. `tools/harness/check-docs-fresh.mjs` 自动扫描 AGENTS.md 与 `DOCS/agents/**` 中的路径与命令引用。
  3. 这条规则被纳入 `pnpm harness:check`，CI 强制。

### 2026-05-20 apps/web/tsconfig.app.json 的 ignoreDeprecations 写死成 6.0

- 场景：`apps/web/tsconfig.app.json` 的 `ignoreDeprecations` 字段值为 `"6.0"`。
- 错误模式：TypeScript 5.9 拒绝该值，根 `pnpm typecheck` 全量失败。
- 根因：跨大版本升级时配置没有跟进。
- 补救：改为 `"5.0"`；后续 TS 升级把 typecheck 加入 `pnpm check` 长期门禁。

### 2026-05-20 apps/admin 未使用导入 TriangleAlert

- 场景：`apps/admin/src/components/dashboard/DashboardOverviewCards.vue` 顶部 import 了 `TriangleAlert` 但未使用。
- 错误模式：`vue-tsc` 报错，根 `pnpm typecheck` 全量失败。
- 根因：lucide 图标替换时旧 import 未清理；oxlint 没在 admin 分支被严格执行过。
- 补救：直接删除未使用 import；`pnpm check` 现在每次 PR 都跑，未来同类问题在 PR 阶段被拦截。
