# TrailQuest Monorepo

TrailQuest 是一个徒步路线发现与分享项目，当前采用 monorepo 结构统一管理前端、后端与数据库文档。

## 项目结构

```text
hiking/
  apps/
    web/     # Vue 3 + TypeScript + Vite 前端
    api/     # Spring Boot 3 + MyBatis-Plus 后端
  DOCS/      # 交接、后端、数据库与前端规范文档
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
- `media_files` 通用媒体表已设计并落地
- 阿里云 OSS STS 上传链路已打通
- 已提供最小上传测试页：`/oss-upload-test`
- 已提供数据库测试接口：
  - `GET /hello`
  - `GET /test/db`
  - `GET /test/users-count`

## 重点文档

- [DOCS 索引](./DOCS/README.md)
- [最新交接文档（2026-03-16）](./DOCS/handoff/current-progress-handoff-2026-03-16.md)
- [后端开发清单](./DOCS/backend/backend-development-checklist.md)
- [MySQL 建表](./DOCS/database/mysql-schema.md)
- [MySQL 媒体升级脚本](./DOCS/database/mysql-media-files-upgrade.sql)
- [前端 UI 指南](./DOCS/frontend/UI_GUIDELINES.md)

## 下一步推荐

1. 优先完成评论模块后端化：`GET /api/trails/{id}/reviews`、`POST /api/reviews`
2. 把评论图片真实接入 `review` 上传链路并同步写入 `review_images`
3. 继续接头像编辑页和路线封面/相册的真实上传
4. 在个人页补齐“我的发布 / 我的收藏 / 统计信息”真实数据
