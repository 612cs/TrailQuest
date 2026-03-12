# TrailQuest Monorepo

TrailQuest 是一个徒步路线发现与分享项目，当前采用 monorepo 结构统一管理前端、后端与数据库文档。

## 项目结构

```text
hiking/
  apps/
    web/     # Vue 3 + TypeScript + Vite 前端
    api/     # Spring Boot 3 + MyBatis-Plus 后端
  DOCS/      # 数据库设计、测试数据、阶段总结
```

## 当前技术栈

- 前端：Vue 3、TypeScript、Vite 7、Pinia、Vue Router 5、Tailwind CSS 4
- 后端：Spring Boot 3.5.11、Java 17、MyBatis-Plus 3.5.15、MySQL、Redis、Spring Security、Springdoc OpenAPI
- 数据库：MySQL `hikingDB`
- 缓存：Redis（Docker）

## 常用命令

在仓库根目录执行：

```bash
pnpm install

pnpm dev
pnpm build
pnpm preview

pnpm dev:api
pnpm build:api
pnpm test:api
```

## 访问地址

- 前端开发环境：`http://localhost:5173`
- 后端开发环境：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 当前进度

- 前端已迁移到 `apps/web`
- Spring Boot 后端已迁移到 `apps/api`
- MySQL `hikingDB` 已建库建表并已导入测试数据
- Spring Boot 已连通 MySQL 与 Redis
- 开发期安全拦截已放开，便于前后端联调
- MyBatis-Plus 与分页插件已安装
- Swagger / OpenAPI 已接入
- 已提供数据库测试接口：
  - `GET /hello`
  - `GET /test/db`
  - `GET /test/users-count`

## 重点文档

- [后端阶段总结](./DOCS/backend-handoff.md)
- [MySQL 建表](./DOCS/mysql-schema.md)
- [MySQL 测试数据](./DOCS/mysql-test-data.md)
- [前端 UI 指南](./DOCS/UI_GUIDELINES.md)

## 下一步推荐

1. 建立 `entity / mapper / service / controller` 标准目录
2. 先从 `users` 或 `trails` 模块落第一套真实业务接口
3. 去掉 Spring Security 默认生成密码的日志噪音
4. 把数据库账号密码迁到本地环境配置，避免直接写在仓库里
