# 后端开发清单

> 说明：这份清单保留为后端阶段性开发路线参考，并已按 2026-05-20 仓库代码做过一次状态校准。它不再描述“业务层未开始”的早期阶段，当前规划请同时参考 `DOCS/agents/plans/current-sprint.md` 与 `DOCS/handoff/next-stage-product-roadmap-2026-03-28.md`。

这份清单整合了 `DOCS/backend/backend-handoff.md` 与 `apps/api/README.md`，目标是把“当前状态、下一步顺序、实际交付物”整理成一份可以直接执行的开发指南。

## 1. 当前状态总览

后端项目已完成基础环境搭建，并具备以下核心能力：

- 后端目录已迁移到 `apps/api`
- 可通过 `pnpm` 在 monorepo 根目录统一管理命令
- Spring Boot 项目可正常启动
- 已连接 MySQL 数据库 `hikingDB`
- 已连接本地 Docker Redis
- 已接入 MyBatis-Plus
- 已接入 MyBatis-Plus 分页拦截器
- 已接入 Springdoc OpenAPI / Swagger UI
- 已具备统一返回体、分页对象、异常处理与基础鉴权能力
- 已提供路线、评论、收藏、上传、AI、天气、景观预测、后台治理等业务接口

当前可访问的接口与文档包括：

- `GET /hello`
- `GET /test/db`
- `GET /test/users-count`
- `GET /api/trails`
- `GET /api/trails/{id}`
- `GET /api/trails/{id}/reviews`
- `POST /api/reviews`
- `GET /api/trails/{id}/weather`
- `GET /api/trails/{id}/landscape-prediction`
- `GET /api/ai/conversations`
- `POST /api/ai/chat/stream`
- `GET /api/admin/dashboard/summary`
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
- 评论、评论图片、上传媒体、AI 会话 / 消息、后台治理日志已具备对应结构
- AI Chat 与景观 / 天气相关结构以后端表设计与服务实现为准

相关文档：

- `DOCS/database/mysql-schema.md`
- `DOCS/database/mysql-test-data.md`

## 4. 还未完成的核心内容

当前后端已经具备较完整的业务骨架，剩余重点不再是“补齐基础层”，而是继续做产品闭环与质量提升：

- 前台举报上报接口与后台举报治理闭环
- 通知中心 / 消息反馈模型
- AI 推荐质量、解释性与工具链路优化
- 天气能力的缓存、降级与第三方依赖治理
- 景观预测扩展到更多景观类型
- 埋点、错误监控与接口稳定性观测
- 开发期安全、配置外置与生产收口

## 5. 推荐开发顺序

### 第一阶段：补齐仍缺的产品闭环接口

推荐顺序：

1. 举报上报链路
2. 通知 / 消息中心
3. 搜索地图视图所需的接口补强

这样安排的原因：

- 后台治理入口已经在，前台举报来源仍缺
- 用户反馈闭环会直接影响留存与社区体验
- 搜索地图属于前端现成入口但后续能力仍待补足

### 第二阶段：增强现有差异化能力

推荐顺序：

1. AI 推荐质量优化
2. 景观预测扩容
3. 天气能力缓存与稳定性治理

这样安排的原因：

- AI、景观、天气已经有首版，更适合做质量升级
- 三者之间已有数据依赖，连续推进收益更高

### 第三阶段：观测与上线治理

推荐最先补齐的方向：

1. 错误监控
2. 接口稳定性观测
3. 配置与安全收口

优先打通的链路：

- 上传
- AI 对话
- 天气与景观预测
- 后台治理接口

## 6. 最推荐的近期落地路线

如果按“尽快补齐产品闭环”的目标推进，推荐按下面顺序执行：

1. 补前台举报上报接口
2. 统一举报数据模型与后台治理页联调
3. 建立通知中心最小消息模型
4. 为 AI / 天气 / 景观接口补埋点与错误定位
5. 再做推荐质量和预测模型扩展

如果当前只能优先做一件事，最推荐：

- 先完成前台举报链路

原因：

- 后台举报处理页已经存在
- 这是当前最明显的治理闭环缺口
- 实现完成后可直接提升社区治理完整度

## 7. 建议直接拆成的开发任务

### 任务 A：治理闭环

- 设计举报上报 DTO / VO
- 提供前台举报接口
- 联调后台举报处理页

### 任务 B：通知与消息

- 设计最小通知模型
- 提供通知列表 / 已读接口
- 为评论、回复、点赞、收藏等事件接入通知

### 任务 C：AI 与景观质量增强

- 优化 AI 推荐结果结构
- 补充路线推荐解释字段
- 扩展景观预测类型与提示说明

### 任务 D：天气与稳定性

- 为天气能力增加缓存与失败降级
- 统一第三方依赖错误码
- 增加关键链路监控点

## 8. 当前已知注意点

### Security 与开发期放行策略

现状：

- 当前接口体系已经较完整，不再是纯开发骨架
- Spring Security 与开发期放行策略仍需继续收口

建议：

- 明确哪些接口继续保留开发期放行
- 在进入生产前完成权限边界与白名单收紧

### 本地配置外置

现状：

- 本地开发仍依赖 MySQL、Redis、OSS、AI 等配置

建议：

- 继续统一 `application-local.properties` 与环境变量注入边界
- 把第三方依赖错误信息做成更清晰的可观测输出

### 生产前还需收紧配置

后续如果上生产环境，需要回头处理：

- Security 权限控制
- CORS 白名单
- 数据库与 Redis 配置外置
- 第三方密钥与监控告警策略

## 9. 建议作为下一步的最小里程碑

建议把第一个可验收里程碑定义为：

- 完成前台举报接口
- 完成后台举报治理联调
- 完成通知中心最小数据模型
- 至少一条高风险链路接入错误监控
- Swagger 中可直接调试新增接口

## 10. 一句话结论

当前后端已经不是“从零开始”，而是处于“主链路已成型，等待闭环补齐与质量增强”的阶段。

最值得立刻开始的工作不是继续补基础骨架，而是优先补齐举报、通知、观测这类当前最容易影响产品完整度的能力。
