# Claude Code 常用命令

本文档记录 TrailQuest 项目中常用的 Claude Code 命令。

## 提交代码

```bash
# 查看当前改动
git status

# 查看具体改动
git diff

# 暂存文件并提交
git add <文件路径>
git commit -m "提交信息"
```

## 分支管理

```bash
# 查看所有分支
git branch

# 切换分支
git checkout <分支名>

# 删除本地分支
git branch -D <分支名>

# 合并分支到当前分支
git merge <分支名> --no-edit
```

## 同步远程

```bash
# 拉取远程更新
git pull --rebase origin main

# 推送本地提交
git push origin main
```

## 代码同步问题处理

当出现 "divergent branches" 错误时：

```bash
# 方式1: rebase（推荐保持线性历史）
git pull --rebase origin main

# 方式2: merge（会产生合并提交）
git pull --merge origin main

# 解决冲突后继续
git add <冲突文件>
git rebase --continue

# 或者放弃 rebase
git rebase --abort
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

## 文件操作

```bash
# 搜索文件
Glob <pattern>

# 搜索内容
Grep <pattern>

# 读取文件
Read <file_path>
```

## 常用快捷操作

- `git status` - 查看当前状态
- `git log --oneline -5` - 查看最近5条提交
- `git branch -a` - 查看所有分支（包括远程）