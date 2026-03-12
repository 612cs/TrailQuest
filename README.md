# TrailQuest Monorepo

TrailQuest 现在采用 monorepo 结构管理前端与后端：

```text
hiking/
  apps/
    web/   # Vue 3 + TypeScript + Vite
    api/   # Spring Boot 3
  DOCS/    # 数据库与产品文档
```

## Workspace Commands

```bash
pnpm install
pnpm dev
pnpm build
pnpm preview
```

以上根命令默认作用于 `apps/web`。

## Frontend

```bash
pnpm --dir apps/web dev
pnpm --dir apps/web build
pnpm --dir apps/web preview
```

## Backend

Spring Boot 3 后端已迁移到 `apps/api`。

```bash
pnpm dev:api
pnpm build:api
pnpm test:api
```

如果你更习惯直接使用 Maven Wrapper：

```bash
./apps/api/mvnw -f apps/api/pom.xml spring-boot:run
```

补充说明：

- 后端当前包名为 `com.sheng.hikingbackend`
- 应用名配置在 `apps/api/src/main/resources/application.properties`
- 原始 `hiking-backend` 目录现在只保留独立仓库元数据和构建产物残留，业务工程已经迁入 `apps/api`

- `apps/api/README.md`
