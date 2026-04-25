# apps/api — Spring Boot 后端

## 架构

Spring Boot 3.5 + MyBatis-Plus + MySQL + Redis。

```text
src/main/java/
├── controller/      # REST 控制器
├── service/         # 业务逻辑层
├── mapper/          # MyBatis-Plus Mapper 接口
├── entity/          # 数据库实体
├── dto/             # 数据传输对象
├── config/          # 配置类（Security, Redis, OSS, etc.）
└── common/          # 通用工具、响应封装、异常处理
```

## 命令

```bash
./mvnw spring-boot:run           # 启动开发服务器
./mvnw package                   # 打包
./mvnw test                      # 运行测试
pnpm dev:api                     # 通过根目录脚本启动
```

## 关键约定

- 统一响应格式：`{ success, data, message, code }`
- MyBatis-Plus 分页插件已接入
- Spring Security 开发期拦截已放开（便于联调）
- 阿里云 OSS STS 上传链路已打通
- Swagger / OpenAPI 已接入

## 访问地址

| 服务 | 地址 |
|------|------|
| 后端 API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

## 数据库

- MySQL `hikingDB`，已建库建表并导入测试数据
- Redis（Docker）用于缓存
- 12 个 SQL 升级脚本在 `DOCS/database/`

## Guardrails

- **不要**直接返回数据库实体 — 使用 DTO 封装响应
- **不要**跳过统一响应格式 — 所有接口使用 `{ success, data, message, code }`
- **不要**在 Controller 中写业务逻辑 — 委托给 Service 层
- **不要**硬编码 SQL — 使用 MyBatis-Plus 的条件构造器
- **不要**提交包含密钥的配置文件 — 使用环境变量或 `.env`
