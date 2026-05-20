本文是 TrailQuest 的测试纪律。Agent 完成开发后，必须自行运行下列命令并确认通过，再交由人类或 Codex 审核。

## 工具

| 层 | 工具 | 入口 |
|----|------|------|
| 前端单元 | Vitest | `pnpm test:web:unit` |
| 前端 E2E | Playwright | `pnpm test:web:e2e` |
| 后端 | Maven + Spring Boot Test | `pnpm test:api` |
| 全量自检 | Harness | `pnpm check` |

## 单元测试纪律

- 关键 store / composable / utils 必须有单元测试。
- 组件测试使用 `@vue/test-utils`，覆盖 props、emit、关键交互。
- 不使用真实网络；axios 用 vi.mock 或 MSW（按需引入）替换。
- 单文件测试不超过 200 行；超出按特性域拆分。

## 后端测试纪律

- Controller 层使用 `@SpringBootTest` + `MockMvc`，覆盖参数校验与响应信封。
- Service 层使用 Mockito 隔离 Mapper，覆盖业务分支。
- 数据库密集逻辑使用 Testcontainers 或本地 MySQL，不允许只在 H2 上验证。
- 不允许只跑 happy path。新增方法必须覆盖至少一个失败分支。

## E2E 与冒烟

- E2E 用例集中在 `apps/web/e2e/`，覆盖关键金路径：搜索 → 详情 → 收藏；发布流程；AI 聊天。
- 改 UI 必须本地起 dev server 看一眼，不能仅靠 Vitest 通过就声明完成。

## 提交前自检

```bash
pnpm typecheck
pnpm lint
pnpm format:check
pnpm harness:check
pnpm test
```

或一次性：`pnpm check`。

## CI

`.github/workflows/harness.yml` 提供两个 job：

- `check`：跑 `pnpm check`，串 `typecheck → lint → format:check → harness:check → test:web:unit`，作为 PR 的快速主信号。
- `api-test`：拉起 MySQL 8 与 Redis 7 services，跑 `pnpm test:api`，独立暴露后端集成测试结果。

任意 job 失败即阻止合并。`pnpm check` 只串纯前端能跑通的环节，避免开发者本地因外部依赖跑不通而被噪音淹没。需要全量信号时跑 `pnpm test`。

## 失败处理

- 同一类失败连续出现两次以上，写入 DOCS/agents/harness/failures.md，并尝试编码到 `tools/harness/` 或 `.oxlintrc.json`。
- 不允许跳过 hook（`--no-verify`）或绕过 CI；只能修问题本身。
