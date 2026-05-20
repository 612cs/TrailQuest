本目录是 TrailQuest 的 Agent 操作手册集合，作为根 AGENTS.md 的延伸。仓库内 AI Agent 按需检索这里的章节，避免把所有规则塞进单一入口文件。

## 目录结构

```text
DOCS/agents/
├── architecture/
│   ├── overview.md       系统架构总览
│   └── boundaries.md     模块边界与依赖规则（CI 强制）
├── conventions/
│   ├── README.md         规范总览
│   ├── naming.md         命名规范
│   └── testing.md        测试规范
├── harness/
│   ├── failures.md       Agent 失败案例库（持续维护）
│   ├── checklists.md     PR 自检清单
│   └── review-loop.md    Agent-to-Agent 互评流程
└── plans/
    └── current-sprint.md  当前迭代任务
```

## 维护原则

- 文档仅在 50-100 行量级，超出就拆分。
- 不写百科式描述，只写硬约束、地图链接、失败案例。
- 任何变更都必须能让 `pnpm harness:check` 通过：路径与命令引用必须真实存在，无 emoji。
- 失败案例只追加不删除；规则演进通过新案例覆盖旧案例。

## 与根 AGENTS.md 的关系

根 AGENTS.md 是地图，本目录是地图各节点的详情页。子包 AGENTS.md（apps/web、apps/admin、apps/api、packages/ui）只覆盖该子包独有的规则与命令，通用规则放在这里。
