本文是 `apps/api` Spring Boot 后端的 Agent 操作手册。通用规范见根 AGENTS.md 与 DOCS/agents/。

## 架构

Spring Boot 3.5 + Java 17 + MyBatis-Plus 3.5 + MySQL + Redis。

```text
src/main/java/
├── controller/    REST 控制器
├── service/       业务逻辑层
├── mapper/        MyBatis-Plus Mapper 接口
├── entity/        数据库实体
├── dto/           请求 / 响应载体
├── config/        Spring 配置（Security、Redis、OSS 等）
└── common/        响应封装、异常处理、通用工具
```

依赖方向：`entity / dto → mapper → service → controller`。Controller 只做参数校验与委托，不直接操作数据库。

## 命令

```bash
./mvnw spring-boot:run             启动开发服务器
./mvnw package                     打包
./mvnw test                        运行测试
```

根目录入口：`pnpm dev:api`、`pnpm build:api`、`pnpm test:api`。

## 访问地址

| 服务 | 地址 |
|------|------|
| 后端 API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

## 数据与缓存

- MySQL 数据库 `hikingDB`，建表与升级 SQL 见 DOCS/database/。
- Redis 通过 Docker 启动，仅后端访问。
- `media_files` 通用媒体表已落地，OSS STS 直传链路打通。

## 硬性约定

- 统一响应信封：`{ success, data, message, code }`。
- Controller 不直接返回 entity，必须经 DTO。
- SQL 通过 MyBatis-Plus 条件构造器；不允许在 Java 字符串里硬拼 SQL。
- Spring Security 开发期已放开拦截器；上线前必须收紧。
- 不允许提交含密钥的配置；使用环境变量或 `.env`（已 gitignore）。

## 边界

- 后端代码不参与 TypeScript 边界扫描，但与前端约定的契约（DTO、错误码）必须双向同步。
- 修改响应字段时同步更新 `apps/web/src/types/` 与 `apps/admin/src/types/`。

## 失败案例参考

- DOCS/agents/harness/failures.md
- DOCS/agents/harness/checklists.md
