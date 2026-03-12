# TrailQuest Backend

TrailQuest 后端位于 `apps/api`，当前已经完成基础环境搭建，可以开始进入正式业务开发。

## 技术栈

- Spring Boot `3.5.11`
- Java `17`
- MyBatis-Plus `3.5.15`
- MyBatis-Plus Pagination `3.5.15`
- Spring Security
- Spring Validation
- Spring Data Redis
- MySQL Connector/J
- Springdoc OpenAPI + Swagger UI `2.8.16`
- Lombok

## 关键依赖

- `com.baomidou:mybatis-plus-spring-boot3-starter`
- `com.baomidou:mybatis-plus-jsqlparser`
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-jdbc`
- `org.springframework.boot:spring-boot-starter-security`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.boot:spring-boot-starter-data-redis`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `com.mysql:mysql-connector-j`

## 项目基础信息

- GroupId：`com.sheng`
- ArtifactId：`hiking-backend`
- 包名：`com.sheng.hikingbackend`
- 启动类：`com.sheng.hikingbackend.HikingBackendApplication`

## 当前配置概览

- MySQL：
  - 数据库：`hikingDB`
  - 连接配置位于 `src/main/resources/application.properties`
- Redis：
  - 本地 Docker Redis，默认 `localhost:6379`
- MyBatis-Plus：
  - 已开启下划线转驼峰
  - 已接入 MySQL 分页拦截器
- Security：
  - 当前为开发模式，已关闭默认拦截
  - 已放行 Swagger 文档路径
  - 当前仍会打印默认生成密码日志，但不影响联调
- CORS：
  - 已放行 `http://localhost:5173`
  - 已放行 `http://127.0.0.1:5173`

## 常用命令

在仓库根目录执行：

```bash
pnpm dev:api
pnpm build:api
pnpm test:api
```

或者在 `apps/api` 目录内执行：

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw clean package -DskipTests
```

## 当前可用接口

- `GET /hello`
- `GET /test/db`
- `GET /test/users-count`

## 文档入口

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 当前代码结构

```text
apps/api/
  src/main/java/com/sheng/hikingbackend
    HikingBackendApplication.java
    TestController.java
    config/
      MybatisPlusConfig.java
      OpenApiConfig.java
      SecurityConfig.java
    controller/
      DbTestController.java
```

## 已完成事项

- Spring Boot 项目迁移进 monorepo
- MySQL 与 Redis 连接打通
- 数据库测试接口已可用
- 开发期安全放行
- MyBatis-Plus 与分页依赖已安装并通过测试
- Swagger / OpenAPI 已安装并通过测试

## 推荐的后续开发顺序

1. 建立标准包结构：`entity`、`mapper`、`service`、`service.impl`、`controller`、`dto`
2. 先完成 `users` 和 `trails` 两个基础模块
3. 再补 `favorites`、`reviews`、`chat`
4. 最后再逐步把前端 mock 数据替换成真实接口

## 当前注意点

- 目前还没有真正的 MyBatis `mapper` 接口，所以启动时会有 “No MyBatis mapper was found” 的提示
- `application.properties` 里是本地开发配置，后面建议拆成本地环境文件或环境变量
- 如果后续要上生产，需要重新收紧 Security 与 CORS 配置
