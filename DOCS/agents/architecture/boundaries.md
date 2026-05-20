本文是 `tools/harness/check-boundaries.mjs` 实施的边界规则的人类可读版本。两边必须保持一致；调整规则时同步改动两处。

## 工作区边界

| 规则 | 含义 |
|------|------|
| crossApp | `apps/*` 之间不得互相 import 源码（apps/web 不能 import apps/admin，反之亦然） |
| appScope | `apps/web` 与 `apps/admin` 只能 import 自身 `src` 与 `packages/ui` |
| uiReverse | `packages/ui` 不得反向依赖任何 `apps/*` |

`apps/api` 是 Java 后端，不参与 TypeScript 边界检查，但同样禁止与前端共用源码（通过 .oxlintrc.json 的 `ignorePatterns` 排除）。

## 子包内部分层

| 规则 | 含义 |
|------|------|
| componentView | `src/components/*` 不得 import `src/views/*` |
| storeComponent | `src/stores/*` 不得 import `src/components/*` |
| mockIsolation | `src/mock/*` 只能 import `src/mock/*` 与 `src/types/*` |

分层意图：

- 视图（views）依赖组件（components），反之不行；组件被多个视图复用，引入视图会形成循环。
- Store 是状态层，被组件订阅，不得反向引用组件。
- Mock 数据只承载契约（types）与同层数据，不得连接业务逻辑，否则迁移到真实 API 时拆不掉。

## 跨层依赖方向（推荐，不强制）

```text
types  →  utils  →  mock / api  →  stores  →  composables / components  →  views
```

边界检查只对 `apps/web`、`apps/admin`、`packages/ui` 生效。`apps/api` 由 Java 编译器与 oxlint 的 ignorePatterns 排除。

## 当违反规则时

`pnpm harness:check` 会输出形如：

```text
Boundary check failed:
- apps/web/src/components/foo/Bar.vue -> ../../views/Baz.vue (src/components/* cannot import src/views/*)
```

修复方式：

1. 把被复用的逻辑下沉到 `composables/` 或 `stores/`。
2. 把跨 app 复用的纯组件提取到 `packages/ui`。
3. 把 mock 数据脱敏到 `src/types`，业务逻辑留在 store。

## 修改规则的流程

新增或调整规则时：

1. 更新 `tools/harness/check-boundaries.mjs` 中的 `RULES` 字典。
2. 同步更新本文件的「规则」表格。
3. 在 `DOCS/agents/harness/failures.md` 记录触发该规则的真实失败案例。
4. 跑 `pnpm harness:check` 确认现有代码可通过。
