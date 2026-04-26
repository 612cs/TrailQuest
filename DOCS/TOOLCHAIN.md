# TrailQuest 工具链文档

本文档介绍项目使用的代码质量工具及其配置。

## Oxc 工具链

[Oxc](https://oxc.rs/) 是一个用 Rust 编写的高性能 JavaScript/TypeScript 工具集合，目标是提供极速的 JS 工具链。

### 包含工具

| 工具 | 用途 | 替代方案 |
|------|------|----------|
| `oxlint` | 代码检查 (Linter) | ESLint |
| `oxfmt` | 代码格式化 (Formatter) | Prettier |
| `rolldown` | 打包工具 (Bundler) | Rollup, esbuild |

**核心优势**: 用 Rust 编写，比传统 JS 工具快 10-100 倍。

---

## 项目配置

### oxlint (代码检查)

**配置文件**: `.oxlintrc.json`

**启用插件**:
- `eslint` - 基础 ESLint 规则
- `typescript` - TypeScript 支持
- `unicorn` - 代码质量最佳实践
- `oxc` - Oxc 内置规则
- `import` - 导入/导出检查
- `vitest` - 测试文件支持
- `vue` - Vue 单文件组件支持

**规则配置**:
- `correctness` 级别: `error` (正确性问题视为错误)
- `suspicious` 级别: `warn` (可疑代码发出警告)
- 允许 `_` 开头的未使用变量
- Vue 文件支持

**使用命令**:
```bash
pnpm lint      # 检查代码
pnpm lint:fix  # 自动修复问题
```

---

### oxfmt (代码格式化)

**配置文件**: `.oxfmtrc.json`

**格式化规则**:
| 配置项 | 值 | 说明 |
|--------|-----|------|
| `printWidth` | 100 | 每行最大 100 字符 |
| `semi` | `false` | 不使用分号 |
| `singleQuote` | `true` | 使用单引号 |
| `trailingComma` | `"all"` | 尽可能使用尾随逗号 |
| `endOfLine` | `"lf"` | 使用 LF 换行符 |
| `sortTailwindcss` | `true` | 自动排序 Tailwind 类名 |

**使用命令**:
```bash
pnpm format       # 格式化代码
pnpm format:check # 检查格式
```

---

### rolldown (打包工具)

本项目**目前没有直接使用 rolldown**。项目使用 **Vite 8** 作为构建工具，而 Vite 5+ 底层已逐步集成 rolldown 来替代 esbuild/rollup。

---

## 忽略模式

以下目录被 oxlint 和 oxfmt 忽略:
- `**/node_modules/**`
- `**/dist/**`
- `**/coverage/**`
- `**/.vite/**`
- `apps/api/**` (Java 后端)
- `DOCS/**`
- `logs/**`

---

## 相关文档

- [Oxc 官方文档](https://oxc.rs/)
- [oxlint 规则列表](https://oxc.rs/docs/guide/usage/linter/rules.html)
- [AGENTS.md](../AGENTS.md) - 项目开发指南
