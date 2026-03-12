# 后端阶段总结与交接

这份文档用于快速回顾当前后端进度，方便在新窗口继续开发。

## 已完成内容

### 1. monorepo 结构调整

- 前端迁移到 `apps/web`
- Spring Boot 后端迁移到 `apps/api`
- 根目录统一通过 `pnpm` 管理常用命令

### 2. 数据库设计落地

- 确认了后端建模方向：
  - `trails` 直接作为“路线发布 + 社区动态”本体
  - `trail_favorites` 作为“我的收藏”关系表
  - 不单独为“我的发布”建中间表
  - AI Chat 以后端表结构为准
- 已整理 MySQL 建表文档：
  - `DOCS/mysql-schema.md`
- 已整理测试数据文档：
  - `DOCS/mysql-test-data.md`

### 3. Docker 中的 MySQL / Redis 联通

- MySQL 数据库 `hikingDB` 已建立
- 表结构与测试数据已导入
- Spring Boot 已成功连接 MySQL
- Redis 已配置为本地 Docker Redis

### 4. 后端基础能力已接入

- 已新增数据库测试接口：
  - `GET /hello`
  - `GET /test/db`
  - `GET /test/users-count`
- 已关闭开发期默认安全拦截，方便前后端联调
- 已放行前端开发地址的跨域请求

### 5. MyBatis-Plus 与 API 文档

- 已安装并验证：
  - `mybatis-plus-spring-boot3-starter`
  - `mybatis-plus-jsqlparser`
  - `springdoc-openapi-starter-webmvc-ui`
- 已新增：
  - MyBatis-Plus 分页拦截器
  - OpenAPI 基础信息配置
- 已可访问：
  - Swagger UI：`http://localhost:8080/swagger-ui.html`
  - OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 当前后端状态

后端已经完成“从 0 到可联调基础环境”的阶段，当前不是空项目了，已经具备：

- 可启动
- 可连 MySQL
- 可连 Redis
- 可访问 Swagger
- 可写 MyBatis-Plus 业务代码

## 当前还没做的业务层

目前还没有正式业务模块，只完成了基础设施。以下内容还没开始：

- `entity`
- `mapper`
- `service`
- `service.impl`
- `dto/vo`
- 正式业务控制器
- 统一返回体
- 全局异常处理
- 真正的登录鉴权

## 建议的后续开发顺序

### 第一阶段：先搭后端标准骨架

建议先建立这些包：

```text
com.sheng.hikingbackend
  common
  config
  controller
  dto
  entity
  mapper
  service
  service.impl
```

推荐同时补：

- 统一返回体 `ApiResponse`
- 全局异常处理 `GlobalExceptionHandler`
- 基础分页请求/响应对象

### 第二阶段：优先做基础模块

推荐顺序：

1. `users`
2. `trails`
3. `trail_favorites`
4. `reviews`
5. `ai_conversations` / `ai_messages`

原因：

- `users` 和 `trails` 是全站主干
- `favorites` 和 `reviews` 依赖前两者
- `chat` 可以稍晚，因为前端目前已有 mock 兜底

### 第三阶段：开始替换前端 mock

推荐接口优先级：

1. `GET /api/trails`
2. `GET /api/trails/{id}`
3. `GET /api/trails/{id}/reviews`
4. `GET /api/users/{id}/favorites`
5. `POST /api/reviews`

这样可以最先打通：

- 首页
- 搜索页
- 社区页
- 路线详情页

## 当前注意点

### 1. Security 默认密码日志仍会出现

虽然接口已经放行，不影响开发，但 Spring Security 仍会输出默认密码日志。

建议后续做法：

- 要么继续保留 `spring-security`，后面正式接登录鉴权
- 要么在开发期进一步关闭默认 `UserDetailsService` 自动配置，减少日志噪音

### 2. MyBatis Mapper 警告是正常的

当前启动时会看到：

```text
No MyBatis mapper was found in '[com.sheng.hikingbackend.mapper]'
```

这是因为还没有创建真正的 `mapper` 接口，不是错误。

### 3. 本地配置后面建议外置

当前 `application.properties` 中包含本地 MySQL / Redis 配置。

后续建议：

- 本地开发使用 `application-local.properties`
- 密码改为环境变量注入

## 明天继续时建议先做什么

最推荐的起手顺序：

1. 建立统一返回体与全局异常处理
2. 建 `users` 实体、Mapper、Service、Controller
3. 建 `trails` 实体、Mapper、Service、Controller
4. 把前端 `getTrailsWithAuthor()` 对应的数据接口先跑通

如果只做一件事，最推荐先做：

`trails` 模块的列表接口和详情接口

因为它能最快带动首页、搜索页、社区页、详情页四个前端页面联动。
