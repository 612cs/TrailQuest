# Claude Code 常用命令

本文档记录 Claude Code CLI 的常用命令和操作。

## 启动与基本操作

```bash
# 启动 Claude Code（当前目录）
claude

# 在指定目录启动
claude <目录路径>

# 使用指定模型
claude --model opus

# 断点续话（恢复之前的对话）
claude --resume

# 查看帮助
claude --help
```

## Slash Commands

在对话中直接使用：

| 命令 | 功能 |
|------|------|
| `/resume` | 断点续话 |
| `/compact` | 压缩对话上下文 |
| `/redo` | 重做最近一次操作 |
| `/clear` | 清除对话历史 |
| `/test` | 运行测试 |
| `/review` | 代码审查模式 |
| `/commit` | 提交代码 |
| `/pr` | 创建 Pull Request |
| `/help` | 显示帮助 |

## Git 操作

```bash
# 查看当前状态
git status

# 查看具体改动
git diff

# 暂存并提交
git add <文件>
git commit -m "<提交信息>"

# 切换分支
git checkout <分支名>

# 创建新分支
git checkout -b <分支名>

# 拉取远程更新
git pull --rebase origin <分支名>

# 推送提交
git push origin <分支名>
```

## 处理冲突

当出现 "divergent branches" 错误时：

```bash
# 方式1: rebase（推荐）
git pull --rebase origin <分支名>

# 方式2: merge
git pull --merge origin <分支名>

# 解决冲突后继续
git add <冲突文件>
git rebase --continue

# 放弃 rebase
git rebase --abort
```

## 快捷操作

- `git status` - 查看当前状态
- `git log --oneline -5` - 查看最近5条提交
- `git branch -a` - 查看所有分支
- `Glob <pattern>` - 搜索文件
- `Grep <pattern>` - 搜索内容
- `Read <file>` - 读取文件

## 配置 MCP 服务器

MCP 服务器在 `~/.claude/settings.json` 中配置：

```json
{
  "mcpServers": {
    "server-name": {
      "command": "npx",
      "args": ["-y", "server-package"]
    }
  }
}
```

## 项目运行

```bash
# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev

# 构建生产版本
pnpm build
```