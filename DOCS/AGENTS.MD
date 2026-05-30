本文是 `DOCS` 目录的检索导航，面向 Agent 与人类维护者。Harness 工程相关导航请优先看根 [AGENTS.md](../AGENTS.md) 与 [DOCS/agents/README.md](./agents/README.md)。

## 顶层目录

| 目录 | 用途 |
|------|------|
| `agents/` | Agent 操作手册（Harness 工程文档体系入口） |
| `architecture-diagrams/` | 工程图表源文件与导出（Draw.io 源 + PNG / SVG） |
| `archive/` | 历史与编辑器自动备份资料 |
| `backend/` | 后端需求、方案、交接、开发清单 |
| `database/` | 数据库初始化、结构与升级脚本 |
| `frontend/` | 前端 UI、交互与主题规范 |
| `handoff/` | 阶段交接、测试优先级、阶段计划 |

## 常用入口

- 当前迭代任务：[agents/plans/current-sprint.md](./agents/plans/current-sprint.md)
- 系统架构总览：[agents/architecture/overview.md](./agents/architecture/overview.md)
- 模块边界与依赖：[agents/architecture/boundaries.md](./agents/architecture/boundaries.md)
- 通用编码规范：[agents/conventions/README.md](./agents/conventions/README.md)
- 测试规范：[agents/conventions/testing.md](./agents/conventions/testing.md)
- 命名规范：[agents/conventions/naming.md](./agents/conventions/naming.md)
- 失败案例库：[agents/harness/failures.md](./agents/harness/failures.md)
- PR 自检清单：[agents/harness/checklists.md](./agents/harness/checklists.md)
- Agent 互评流程：[agents/harness/review-loop.md](./agents/harness/review-loop.md)
- 工具链（oxlint / oxfmt）：[TOOLCHAIN.md](./TOOLCHAIN.md)
- Harness 工程理论：[harness.md](./harness.md)
- DOCS 索引：[README.md](./README.md)

## 维护规则

- 新增开发交接放入 `handoff/`，文件名建议含日期。
- 新增数据库脚本放入 `database/`，升级脚本命名 `mysql-<topic>-upgrade.sql`。
- 新增工程图表源文件放入 `architecture-diagrams/sources/`，导出图按格式分到 `exports/png/`、`exports/svg/`。
- `.DS_Store` 与编辑器锁文件不作为主资料保留。
- 新增 Agent 操作规则放入 `agents/`；触发 CI 强制时同步更新 `tools/harness/` 与根 [AGENTS.md](../AGENTS.md)。
