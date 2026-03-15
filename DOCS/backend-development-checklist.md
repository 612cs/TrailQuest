# 后端开发清单

这份清单整合了 `DOCS/backend-handoff.md` 与 `apps/api/README.md`，目标是把“当前状态、下一步顺序、实际交付物”整理成一份可以直接执行的开发指南。

## 1. 当前状态总览

后端项目已完成基础环境搭建，当前具备以下能力：

- 后端目录已迁移到 `apps/api`
- 可通过 `pnpm` 在 monorepo 根目录统一管理命令
- Spring Boot 项目可正常启动
- 已连接 MySQL 数据库 `hikingDB`
- 已连接本地 Docker Redis
- 已接入 MyBatis-Plus
- 已接入 MyBatis-Plus 分页拦截器
- 已接入 Springdoc OpenAPI / Swagger UI
- 已开放开发期 Security 拦截，便于联调
- 已放行前端开发地址跨域请求

当前可访问的接口与文档：

- `GET /hello`
- `GET /test/db`
- `GET /test/users-count`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 2. 技术与项目基础信息

- Java：`17`
- Spring Boot：`3.5.11`
- MyBatis-Plus：`3.5.15`
- Swagger UI：`2.8.16`
- GroupId：`com.sheng`
- ArtifactId：`hiking-backend`
- 包名：`com.sheng.hikingbackend`
- 启动类：`com.sheng.hikingbackend.HikingBackendApplication`

常用命令：

```bash
# 仓库根目录
pnpm dev:api
pnpm build:api
pnpm test:api
```

```bash
# apps/api 目录
./mvnw spring-boot:run
./mvnw test
./mvnw clean package -DskipTests
```

## 3. 数据建模结论

当前后端建模方向已经确定：

- `trails` 作为“路线发布 + 社区动态”主表
- `trail_favorites` 作为“我的收藏”关系表
- 不单独为“我的发布”建立中间表
- AI Chat 相关结构以后端表设计为准

相关文档：

- `DOCS/mysql-schema.md`
- `DOCS/mysql-test-data.md`

## 4. 还未完成的核心内容

目前仍处于“基础设施已完成，业务层未开始”的阶段，以下内容还需要补齐：

- `entity`
- `mapper`
- `service`
- `service.impl`
- `dto/vo`
- 正式业务 controller
- 统一返回体
- 全局异常处理
- 分页请求与分页响应对象
- 真正的登录鉴权

## 5. 推荐开发顺序

### 第一阶段：先补标准后端骨架

建议先建立统一包结构：

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

这一阶段建议同时完成：

- `ApiResponse` 统一返回体
- `GlobalExceptionHandler` 全局异常处理
- 基础分页请求对象
- 基础分页响应对象

交付完成标准：

- 业务接口统一返回格式
- 异常能被统一处理
- 后续业务模块可以直接复用分页能力

### 第二阶段：优先做基础业务模块

推荐顺序：

1. `users`
2. `trails`
3. `trail_favorites`
4. `reviews`
5. `ai_conversations` / `ai_messages`

这样安排的原因：

- `users` 和 `trails` 是全站核心主干
- `favorites` 与 `reviews` 依赖前两者
- `chat` 当前可以继续由前端 mock 暂时兜底

### 第三阶段：优先替换前端最关键的 mock

推荐最先提供这批接口：

1. `GET /api/trails`
2. `GET /api/trails/{id}`
3. `GET /api/trails/{id}/reviews`
4. `GET /api/users/{id}/favorites`
5. `POST /api/reviews`

优先打通的页面：

- 首页
- 搜索页
- 社区页
- 路线详情页

## 6. 最推荐的近期落地路线

如果按“尽快见到联调成果”的目标推进，推荐按下面顺序执行：

1. 建立统一返回体与全局异常处理
2. 建立 `users` 模块基础骨架
3. 建立 `trails` 模块基础骨架
4. 先实现 `trails` 列表接口
5. 再实现 `trails` 详情接口
6. 对接前端 `getTrailsWithAuthor()` 所需数据结构
7. 再补 `reviews` 和 `favorites`

如果当前只能优先做一件事，最推荐：

- 先完成 `trails` 模块的列表接口和详情接口

原因：

- 能最快支撑首页
- 能最快支撑搜索页
- 能最快支撑社区页
- 能最快支撑路线详情页

## 7. 建议直接拆成的开发任务

### 任务 A：基础规范层

- 新建 `common` 包
- 实现 `ApiResponse`
- 实现通用错误码或错误消息结构
- 实现 `GlobalExceptionHandler`
- 实现分页请求/响应对象

### 任务 B：用户模块

- 建 `User` 实体
- 建 `UserMapper`
- 建 `UserService` 与实现类
- 建用户相关 controller
- 提供基础查询接口

### 任务 C：路线模块

- 建 `Trail` 实体
- 建 `TrailMapper`
- 建 `TrailService` 与实现类
- 建路线相关 controller
- 提供列表接口
- 提供详情接口

### 任务 D：评论与收藏模块

- 建 `Review` 相关实体与接口
- 建 `TrailFavorite` 相关实体与接口
- 提供评论列表与新增评论接口
- 提供收藏列表接口

### 任务 E：聊天模块

- 建 `ai_conversations`
- 建 `ai_messages`
- 再逐步替换前端 chat mock

## 8. 当前已知注意点

### Security 默认密码日志

现状：

- 当前接口已放行，不影响开发联调
- Spring Security 仍可能输出默认生成密码日志

建议：

- 如果后续会做正式鉴权，可以保留 `spring-security`
- 如果当前只想减少噪音，可以继续关闭开发期默认用户相关自动配置

### MyBatis Mapper 警告

现状：

```text
No MyBatis mapper was found in '[com.sheng.hikingbackend.mapper]'
```

说明：

- 这是因为还没有创建真正的 `mapper` 接口
- 当前属于正常现象，不是故障

### 本地配置外置

现状：

- `application.properties` 中包含本地 MySQL / Redis 配置

建议：

- 拆分为 `application-local.properties`
- 密码与敏感配置改为环境变量注入

### 生产前还需收紧配置

后续如果上生产环境，需要回头处理：

- Security 权限控制
- CORS 白名单
- 数据库与 Redis 配置外置

## 9. 建议作为下一步的最小里程碑

建议把第一个可验收里程碑定义为：

- 完成统一返回体
- 完成全局异常处理
- 完成 `trails` 列表接口
- 完成 `trails` 详情接口
- Swagger 中可直接调试
- 前端至少一个页面开始脱离 mock

## 10. 一句话结论

当前后端已经不是“从零开始”，而是处于“基础设施就绪，等待业务模块落地”的阶段。

最值得立刻开始的工作不是继续调环境，而是尽快把 `trails` 模块和统一接口规范落地，让前后端开始真正联调。
