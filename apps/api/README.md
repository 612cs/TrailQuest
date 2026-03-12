# Spring Boot API

TrailQuest 后端已经正式迁移到 `apps/api`。

## 当前项目信息

- Spring Boot: `3.5.11`
- Java: `17`
- GroupId: `com.sheng`
- ArtifactId: `hiking-backend`
- 包名: `com.sheng.hikingbackend`

## 常用命令

在仓库根目录执行：

```bash
pnpm dev:api
pnpm build:api
pnpm test:api
```

或者直接使用 Maven Wrapper：

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw clean package -DskipTests
```

## 当前目录结构

```text
apps/api/
  .mvn/
  mvnw
  mvnw.cmd
  pom.xml
  src/main/java/com/sheng/hikingbackend
  src/main/resources
  src/test/java/com/sheng/hikingbackend
```

## 说明

- `src/main/resources/application.properties` 当前只配置了 `spring.application.name=hiking-backend`
- 后续接入 MySQL、Redis、Spring Security 时，可以继续在这个目录内演进
- 原始 `hiking-backend` 目录目前只剩独立 `.git`、`.idea`、`target` 等残留，用于保留你原仓库历史，不参与 monorepo 运行
