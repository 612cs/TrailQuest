本文是 Agent 在本仓库交付任务前必须自查的清单。每项不通过就不要宣告完成。

## 编码自检

- [ ] 仅修改任务相关文件，不顺手清理无关代码。
- [ ] 没有引入未使用的 import / 变量。
- [ ] 没有用 `any` / 隐式 `any`。
- [ ] 改动了公共 API 时，调用方全部同步更新。
- [ ] 修改后端响应时，前端 type 与拆包路径同步更新。

## 文档自检

- [ ] 修改了命令、目录、子包结构时，同步更新根 `AGENTS.md` 与对应子包 `AGENTS.md`。
- [ ] 修改了硬性规则时，同步更新 `DOCS/agents/architecture/boundaries.md` 与 `tools/harness/check-boundaries.mjs`。
- [ ] 文档不包含 emoji（`DOCS/agents/**`、各 `AGENTS.md` 与 `CLAUDE.md`）。
- [ ] 文档引用的 `pnpm` 命令与文件路径都真实存在。

## 测试自检

- [ ] `pnpm typecheck` 通过。
- [ ] `pnpm lint` 通过。
- [ ] `pnpm format:check` 通过。
- [ ] `pnpm harness:check` 通过。
- [ ] `pnpm test` 通过；UI 改动同时本地起过 dev server 验证。

或者直接跑 `pnpm check`，一条命令完成全部门禁。

## 失败学习

- [ ] 任何一次失败修复后，问自己：这是模型问题还是环境问题？
- [ ] 若是环境问题，把案例写入 `DOCS/agents/harness/failures.md`。
- [ ] 若可机器化检测，再加一条规则到 `tools/harness/` 或 `.oxlintrc.json`。
