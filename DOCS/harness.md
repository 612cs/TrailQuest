> Neko-chan 整理 | 2026-05-19

---

## 一、什么是 Harness Engineering？

**Harness Engineering（驾驭工程）** 是 2026 年初由 HashiCorp 联合创始人 Mitchell Hashimoto 提出的新概念，随后被 OpenAI、Martin Fowler 等采纳，一个月内席卷开发者社区。

### 核心定义

> **围绕 AI Agent 设计和构建约束机制、反馈回路、工作流控制和持续改进循环的系统工程实践。**

它不优化模型本身，而是优化**模型运行的环境**。

### 一句话总结

> **Anytime you find an agent makes a mistake, you take the time to engineer a solution such that the agent will not make that mistake again in the future.**
> — Mitchell Hashimoto

**Agent 每一次失败，都是环境设计不完善的信号。正确的回应不是换更强的模型，而是重新设计它运行的环境。**

### 类比：Prompt → Context → Harness 的进化

```
Prompt Engineering（提示词工程 2023-2024）
  → 对马喊话的技巧
  → 优化输入措辞

Context Engineering（上下文工程 2025）
  → 给马看的地图
  → 优化信息输入

Harness Engineering（驾驭工程 2026+）
  → 给马造一条高速公路，配上护栏、限速牌和加油站
  → 优化运行环境
```

---

## 二、为什么需要它？

### 真实数据

| 案例 | 改变 | 效果 |
|------|------|------|
| **LangChain** | 仅优化 Harness（文档结构、验证回路、追踪系统） | Terminal Bench 从 **30名→5名**，得分 52.8%→66.5% |
| **Can Boluk** | 仅改变代码编辑格式（从 patch 改为 Hashline） | 基准得分从 **6.7%→68.3%** |
| **OpenAI 团队** | 5个月构建 Harness，100万行代码产出 | **0 行**工程师手写代码 |

**一个格式的改变 = 十个模型升级。**

### Agent 常见失败模式

| 失败模式 | 表现 | 原因 |
|---------|------|------|
| One-shotting | 试图一个会话做完所有功能，上下文溢出 | 没有任务分解机制 |
| 过早宣布胜利 | 部分功能完成后直接宣布任务完成 | 缺少验证标准 |
| 过早标记完成 | 写完代码就标记为完成，没做端到端测试 | 测试不自动 |
| 模式复制放大 | 包括坏模式和架构漂移都被复制 | 缺少 Linter 约束 |

---

## 三、四大护栏

```
Harness Engineering
    │
    ├─ 📋 上下文工程（Context Engineering）
    │   AGENTS.md 活文档、按需检索
    │   解决：Agent不知道该看什么
    │
    ├─ 🔒 架构约束（Architecture Constraints）
    │   分层依赖、自定义Linter、CI阻断
    │   解决：Agent复制并放大坏模式
    │
    ├─ 🔄 反馈循环（Feedback Loop）
    │   Agent审Agent、自动测试、错误回传
    │   解决：Agent不知道自己错了
    │
    └─ 🗑️ 熵管理（Entropy Management）
        Doc-gardening Agent、持续垃圾回收
        解决：技术债务和文档腐烂
```

### 1️⃣ 上下文工程

不是一本静态的 1000 页说明书！上下文是稀缺资源，过多指导会挤掉任务空间。

正确做法：
- 一个小而稳定的入口点（AGENTS.md）
- 教 Agent 按需检索更多上下文
- AGENTS.md 里每一行都对应一个历史 Agent 失败案例

### 2️⃣ 架构约束

OpenAI 的层级依赖模型：
```
Types → Config → Repo → Service → Runtime → UI
```
下层不能反向依赖上层。所有规则编码为**自定义 Linter 规则**，违反即 CI 阻止合并。

关键细节：Linter 的错误信息不仅是报错，还解释为什么、正确做法是什么，让 Agent 能自我修正。

### 3️⃣ 反馈循环

从人类 Code Review → **Agent-to-Agent Review**：
- Codex 在本地审核自身更改
- 失败时带着错误信息循环回到模型
- 如果 AI 写的测试通过了有 Bug 的代码，Harness 判定测试无效

### 4️⃣ 熵管理

技术债务就像高息贷款，需要持续小额偿还：
- 定期运行后台 Agent 扫描偏差、发起重构 PR
- **Doc-gardening Agent**（文档园丁）自动扫描文档与代码之间的不一致
- Agent 为 Agent 维护文档

---

## 四、六大行业共识

| # | 共识 | 核心 |
|---|------|------|
| 1 | **瓶颈在基础设施，不在模型** | 仅改格式，得分从6.7%→68.3% |
| 2 | **文档必须是活的反馈循环** | 静态文档是坟场，后台Agent定期清理 |
| 3 | **思考与执行分离** | Orchestrator + Worker 分层架构 |
| 4 | **上下文不是越多越好** | 按需检索、动态注入 |
| 5 | **约束必须自动化** | 编码为 Linter、CI、类型系统 |
| 6 | **工程师角色在转变** | 从代码编写者 → 环境建筑师 |

---

## 附：AGENTS.md 的正确写法（地图模式）

> OpenAI Codex 团队实践：**AGENTS.md 是地图，不是百科全书。**

### 核心原则

| ❌ 错误示范 | ✅ 正确做法 |
|-----------|-----------|
| 500 行的 AGENTS.md | **50-100 行**的 AGENTS.md |
| 所有内容塞进一个文件 | 只放导航表 + 核心硬规则 |
| Agent 找不到重点 | Agent 按图索骥，按需检索 |
| 挤占任务上下文窗口 | 给正事留空间 |

### AGENTS.md 标准模板

```markdown
# AGENTS.md

## 项目简介
[一句话：这是什么项目，什么技术栈]

## 快速导航
| 你想做什么 | 去哪里看 |
|-----------|---------|
| 了解系统架构 | docs/architecture/overview.md |
| 了解模块边界和依赖规则 | docs/architecture/boundaries.md |
| 了解编码规范 | docs/conventions/README.md |
| 了解当前迭代任务 | docs/plans/current-sprint.md |
| 了解 API 规范 | docs/reference/api-spec.yaml |
| 了解错误码 | docs/reference/error-codes.md |
| 了解测试规范 | docs/conventions/testing.md |

## 硬性规则（必须遵守，CI 会验证）
1. 依赖方向：types/ → config/ → repo/ → service/ → runtime/ → ui/
2. ...
```

### docs/ 目录标准结构

```
docs/
├── architecture/              # 架构层（很少变）
│   ├── overview.md            # 系统架构总览
│   └── boundaries.md          # 模块边界和依赖规则
├── conventions/               # 规范层（偶尔更新）
│   ├── README.md              # 规范总览
│   ├── naming.md              # 命名规范
│   └── testing.md             # 测试规范
├── design/                    # 设计层（按功能组织）
│   ├── feature-auth.md        # Status: ✅ Implemented
│   └── feature-search.md      # Status: 📋 Approved
├── plans/                     # 计划层（频繁变）
│   └── current-sprint.md
└── reference/                 # 参考层（自动生成）
    ├── api-spec.yaml
    └── error-codes.md
```

### 什么时候用多模块文档

| 场景 | 做法 |
|------|------|
| 小项目（几个文件） | 一个 AGENTS.md 就够了 |
| 中型项目（多个目录） | AGENTS.md + docs/ 目录 |
| 大型项目（多模块/多团队） | 顶层 AGENTS.md 导航 + 每个模块自己的 doc |

---

## 五、如何在项目中实际使用

### Step 1：写 AGENTS.md（活的，不是死的，50-100 行地图模式）

```
# 项目名

## 架构规则
- 分层依赖：Types → Config → Service → UI
- 不允许反向依赖

## 编码规范
- 所有公共 API 必须有 JSDoc
- 测试覆盖率不低于 80%

## 常见错误（持续更新）
- ❌ 不要直接在 Service 层操作数据库，走 Repo 层
- ❌ 不要在 UI 层调用 API，走 ViewModel
```

### Step 2：配置 Linter + CI 自动化

```bash
# .eslintrc / biome.json 中自定义规则
# CI 中配置：
- bun run typecheck    # 类型检查
- bun run lint         # Lint 检查
- bun test             # 测试
- bun run check        # 自定义 Harness 检查
```

### Step 3：Agent-to-Agent Review 流程

```
Claude Code 提交 PR
    ↓
Codex 自动 Review（Agent审Agent）
    ↓
有 Bug → 带着错误信息返回 Claude
    ↓
Claude 修复 → 再次 Review
    ↓
无 Bug → CI 跑自动化测试
    ↓
通过 → 合并
```

### Step 4：熵管理自动化

```
每周运行的后台脚本：
1. 扫描过时的文档/注释
2. 检查代码与文档不一致
3. 自动提交修复 PR
4. Agent 审 Agent 确认
```

### Step 5：持续学习与迭代

> 每次 Agent 犯错 → 不换模型 → 改环境
> 把教训写入 AGENTS.md 和 Linter 规则
> 让下一次 Agent 不会再犯同样的错

---

## 六、和你的项目结合

你已经有了：
- ✅ **Hermes Agent**（你现在的 AI 助手）
- ✅ **Claude Code + Codex 通过 AgentBridge 协作**
- ✅ **Obsidian 第二大脑**

可以做的：
1. 在你的项目里创建 AGENTS.md，写清楚架构规则
2. 配置 Linter + CI 自动化检查
3. AgentBridge 已经实现了 Agent-to-Agent Review
4. 每次 Agent 犯错，把教训记到 Obsidian 第二大脑

---

## 七、金句总结

> Harness Engineering 不是削弱 AI 的能力，而是为它打造一套黄金缰绳，让它跑得又快又稳。

> 就像高速公路上的护栏——正是因为有护栏，你才敢踩到 120 码。

> 增加信任需要的不是更多自由，而是更多限制。

> 工程师的价值正在从执行者转变为赋能者和系统思考者——从构建产品转向构建能够构建产品的工厂。

---

> 参考：Mitchell Hashimoto《My AI Adoption Journey》、OpenAI《Harness Engineering: leveraging Codex in an agent-first world》、Martin Fowler 深度分析、LangChain Terminal Bench 2.0