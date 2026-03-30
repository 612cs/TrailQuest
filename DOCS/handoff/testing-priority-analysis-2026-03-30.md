# TrailQuest 测试优先级分析（2026-03-30）

## 1. 结论摘要

当前项目已经从功能原型期进入“高频改动 + 多链路联动”的阶段，但自动化测试覆盖仍然明显不足。

现状判断：

- 后端已有少量单元测试，主要覆盖了路线服务、天气聚合、景观预测器。
- 前端目前没有建立正式测试工具链，缺少组件测试、Store 测试和 E2E 测试。
- 项目中最容易出问题的模块，不再是静态页面，而是“前后端 + 第三方服务 + 异步上传/流式交互”这类复杂链路。
- 因此测试优先级应该以“主业务闭环”和“高事故率模块”为先，而不是平均铺开。

建议总体顺序：

1. 先补后端高风险服务的单元测试与集成测试
2. 再补前端关键 Store 与页面主链路的 E2E 测试
3. 最后补相对稳定的展示型页面和低风险辅助模块

配套阅读：

- [冒烟测试与回归测试方案](./smoke-and-regression-testing-plan-2026-03-30.md)

## 2. 当前测试现状

### 2.1 已有测试

后端当前已有这些测试：

- `apps/api/src/test/java/com/sheng/hikingbackend/HikingBackendApplicationTests.java`
- `apps/api/src/test/java/com/sheng/hikingbackend/service/impl/TrailServiceImplTest.java`
- `apps/api/src/test/java/com/sheng/hikingbackend/service/impl/TrailWeatherServiceImplTest.java`
- `apps/api/src/test/java/com/sheng/hikingbackend/service/landscape/predictor/LandscapePredictorsTest.java`

这些测试已经覆盖了：

- 路线更新 / 删除的部分服务逻辑
- 路线天气聚合的主分支与降级逻辑
- 景观预测器的基础输出结构

### 2.2 明显缺口

目前还缺：

- 前端测试工具链（Vitest / Vue Test Utils / Playwright 均未接入）
- AI 对话后端的单元测试与集成测试
- 上传与发布链路的自动化测试
- Geo / Weather / Landscape / AI 的接口级集成测试
- WebSocket AI 聊天链路测试
- 数据库迁移/兼容性测试
- 跨模块 E2E 测试

## 3. 风险最高的模块

结合代码结构、最近的实际故障和第三方依赖情况，最需要优先测试的模块是以下几类。

### 3.1 第一梯队：高风险核心主链路

这些模块最容易出事故，而且一旦出问题会直接影响主功能。

#### A. 发布路线链路

涉及文件：

- `apps/web/src/views/PublishView.vue`
- `apps/web/src/stores/usePublishUploadStore.ts`
- `apps/web/src/api/uploads.ts`
- `apps/web/src/api/trails.ts`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/UploadController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/TrailController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/UploadServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrailServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrackParseServiceImpl.java`

高风险原因：

- 涉及本地文件、OSS、STS、后台上传任务、轨迹解析、地点回填、路线创建多个步骤
- 任一中间步骤失败都可能导致用户“看起来填完了但发布失败”
- 最近已出现数据库字段缺失、上传链路、轨迹解析等真实问题

建议测试类型：

- 单元测试：中高优先级
- 集成测试：最高优先级
- E2E 测试：最高优先级

#### B. AI 对话与路线推荐链路

涉及文件：

- `apps/web/src/views/ChatView.vue`
- `apps/web/src/stores/chat.ts`
- `apps/web/src/api/ai.ts`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/AiController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/websocket/AiWebSocketHandler.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/AiConversationServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/ai/AiRouteRecommendationServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/ai/DashScopeChatServiceImpl.java`

高风险原因：

- 同时涉及数据库、WebSocket、模型服务、结构化推荐、会话状态同步
- 已发生过会话删除状态错误、流式收尾误判失败、推荐地点命中不稳定等问题
- 普通问答与路线推荐走不同分支，回归时容易漏测

建议测试类型：

- 单元测试：最高优先级
- 集成测试：最高优先级
- E2E 测试：最高优先级

#### C. 天气 / 地理 / 景观预测聚合链路

涉及文件：

- `apps/api/src/main/java/com/sheng/hikingbackend/controller/GeoController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/GeoServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/WeatherServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrailWeatherServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/LandscapeContextServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/LandscapePredictionServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/landscape/*`

高风险原因：

- 强依赖和风天气多个 API、JWT、专属 Host、gzip 解析
- 需要兼容“有起点坐标 / 只有 location / 天文数据不完整”等多种降级路径
- 景观预测对小时天气、天文、海拔等数据链路非常敏感

建议测试类型：

- 单元测试：最高优先级
- 集成测试：最高优先级
- E2E 测试：中优先级

### 3.2 第二梯队：高频用户交互链路

#### D. 路线详情互动链路

涉及文件：

- `apps/web/src/views/TrailDetailView.vue`
- `apps/web/src/stores/useTrailInteractionStore.ts`
- `apps/web/src/api/trails.ts`
- `apps/web/src/api/reviews.ts`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/TrailController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/ReviewController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/ReviewServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/TrailServiceImpl.java`

高风险原因：

- 包含点赞、收藏、评论、删除评论、加载更多、用户卡片等多交互状态
- 需要保证跨页面状态一致
- 与登录态、本人/非本人权限密切相关

建议测试类型：

- 单元测试：中优先级
- 集成测试：高优先级
- E2E 测试：高优先级

#### E. 认证与用户资料链路

涉及文件：

- `apps/web/src/stores/useUserStore.ts`
- `apps/web/src/components/auth/AuthModal.vue`
- `apps/web/src/views/ProfileView.vue`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/AuthController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/UserController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/AuthServiceImpl.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/service/impl/UserServiceImpl.java`

高风险原因：

- 登录态是 AI、发布、互动等功能的前置条件
- 用户画像又会影响 AI 推荐
- 资料编辑和头像上传也涉及 OSS

建议测试类型：

- 单元测试：中优先级
- 集成测试：高优先级
- E2E 测试：中优先级

### 3.3 第三梯队：中低风险模块

#### F. 首页 / 搜索 / 社区内容展示

涉及文件：

- `apps/web/src/views/HomeView.vue`
- `apps/web/src/views/SearchView.vue`
- `apps/web/src/views/CommunityView.vue`
- `apps/api/src/main/java/com/sheng/hikingbackend/controller/TrailController.java`
- `apps/api/src/main/java/com/sheng/hikingbackend/mapper/TrailMapper.java`

风险原因：

- 业务重要，但多数问题更偏“结果不准”或“展示不一致”，不如发布和 AI 那样容易直接阻断主链路
- 仍然需要测试筛选、排序、分页、地区检索等关键行为

建议测试类型：

- 单元测试：中优先级
- 集成测试：中优先级
- E2E 测试：中优先级

## 4. 建议的测试优先级

### P0：必须优先补

### 4.1 先写单元测试

#### 4.1.1 `TrailServiceImpl`

优先原因：

- 承担发布、更新、删除路线的核心业务
- 同时处理封面、相册、轨迹、结构化地点、编辑窗口、状态更新

重点覆盖：

- 创建路线成功分支
- 更新路线成功分支
- 替换轨迹时的结构化地点回填
- 无轨迹时 location lookup 回退
- 媒体归属校验失败
- 编辑超时 / 非本人修改

#### 4.1.2 `AiConversationServiceImpl`

优先原因：

- 已发生过删除状态值与数据库枚举不匹配的问题
- 会话列表、删除、消息追加都直接影响 AI 体验

重点覆盖：

- 创建会话
- 列表只返回 active
- 删除会话归档为 archived
- 追加用户消息 / assistant 消息会刷新 updatedAt

#### 4.1.3 `AiRouteRecommendationServiceImpl`

优先原因：

- 推荐结果质量直接决定 AI 是否可信
- 结构化地点检索最近刚做过改造，回归风险高

重点覆盖：

- 普通问答不触发路线推荐
- “江西”命中 `geoProvince`
- “萍乡”命中 `geoCity`
- “芦溪”命中 `geoDistrict`
- 结构化检索为空时回退到 keyword

#### 4.1.4 `GeoServiceImpl` / `TrailWeatherServiceImpl`

优先原因：

- 最近已出现 JWT、Host、gzip、降级链路等复杂问题

重点覆盖：

- reverse geo 成功解析
- location lookup 成功解析
- 坐标优先 / location 回退
- 和风异常时的错误码与降级行为

### 4.2 需要优先补集成测试

#### 4.2.1 发布路线接口集成测试

目标接口：

- `POST /api/trails`
- `PUT /api/trails/{id}`

原因：

- 这是最重的核心主链路
- 单元测试无法覆盖请求 DTO、参数校验、数据库字段兼容、Mapper SQL 拼装问题

重点覆盖：

- 最小必填发布成功
- 带轨迹发布成功
- 带结构化地点发布成功
- 轨迹字段缺失时报错清晰
- 数据库字段不匹配时能尽早暴露

#### 4.2.2 AI 会话接口集成测试

目标接口：

- `GET /api/ai/conversations`
- `POST /api/ai/conversations`
- `GET /api/ai/conversations/{id}/messages`
- `DELETE /api/ai/conversations/{id}`

原因：

- 删除会话这类问题只有到数据库层才会暴露

#### 4.2.3 Geo / Weather / Landscape 接口集成测试

目标接口：

- `POST /api/geo/reverse`
- `GET /api/trails/{id}/weather`
- `GET /api/trails/{id}/landscape-prediction`

原因：

- 这些接口聚合了第三方服务和内部数据
- 非常适合做 mock provider 的集成测试

### 4.3 需要优先补 E2E 测试

#### 4.3.1 发布路线主链路

原因：

- 最复杂、最容易出阻断性问题

建议流程：

1. 登录
2. 进入发布页
3. 上传封面
4. 上传轨迹
5. 等待地点自动回填
6. 点击发布
7. 路线创建成功
8. 跳转/进入详情页验证

#### 4.3.2 AI 聊天主链路

建议流程：

1. 登录
2. 进入 AI 聊天页
3. 新建会话
4. 发送普通问题
5. 发送推荐路线问题
6. 查看卡片返回
7. 删除会话

#### 4.3.3 路线详情互动主链路

建议流程：

1. 进入路线详情页
2. 点赞 / 收藏
3. 发表评论
4. 删除自己的评论
5. 刷新页面后状态保持一致

### P1：第二批补齐

### 5.1 单元测试

优先模块：

- `UploadServiceImpl`
- `ReviewServiceImpl`
- `usePublishUploadStore`
- `chat.ts`
- `useUserStore`
- `useTrailInteractionStore`

原因：

- 这些模块承担状态机、异步流程和边界逻辑
- 非常适合单元测试

重点：

- Store 状态流转
- API 异常时的错误处理
- 按钮禁用/重试/回滚逻辑

### 5.2 集成测试

优先模块：

- `UploadController` + `UploadServiceImpl`
- `ReviewController` + `ReviewServiceImpl`
- `UserController` + `UserServiceImpl`

### 5.3 E2E 测试

优先页面：

- 登录 / 注册 / 编辑资料
- 搜索筛选 / 切换排序
- 个人中心编辑路线 / 删除路线

### P2：后续完善

### 6.1 单元测试

- 展示型组件
- 纯工具函数
- 静态适配器
- 格式化函数

### 6.2 E2E 测试

- 首页推荐流
- 社区浏览流
- 非关键动画与沉浸式展示页面

## 5. 建议采用的测试分层

### 5.1 单元测试：适合什么

适合：

- 纯业务规则
- 状态机逻辑
- 字段映射
- 推荐排序
- 地点解析优先级
- 天气/景观降级策略

不适合：

- 依赖真实数据库字段兼容性的问题
- 依赖 WebSocket / HTTP / 外部 API 联动的问题

### 5.2 集成测试：适合什么

适合：

- Controller + Service + Mapper + DB 之间的真实联动
- DTO 校验
- SQL 拼装
- 状态枚举与数据库 schema 的兼容性
- 事务行为

当前项目里，以下问题尤其需要集成测试才能拦住：

- 数据库缺字段
- enum/status 值不兼容
- 删除/软删逻辑写错
- 查询过滤条件和数据库字段不一致

### 5.3 E2E 测试：适合什么

适合：

- 真实用户主流程
- 跨页面状态一致性
- 上传、聊天、互动、发布这类多步操作
- 登录态与权限流转

当前项目最值得先补的 E2E，只需要三条：

1. 发布路线
2. AI 聊天
3. 路线详情互动

这三条就能覆盖项目当前最核心的产品价值。

## 6. 建议的落地顺序

### 第一阶段：先建立最小测试基础

前端：

- 接入 `vitest`
- 接入 `@vue/test-utils`
- 接入 `playwright`

后端：

- 保持 JUnit 5 + Mockito
- 增补 `MockMvc` / `SpringBootTest` 集成测试
- 如条件允许，接入测试数据库或至少 Testcontainers MySQL

### 第二阶段：先补最值钱的 10 个测试

建议顺序：

1. `TrailServiceImpl` 创建路线
2. `TrailServiceImpl` 更新路线
3. `AiConversationServiceImpl` 删除会话
4. `AiRouteRecommendationServiceImpl` 省市县结构化命中
5. `TrailWeatherServiceImpl` 坐标优先与回退
6. 发布路线接口集成测试
7. AI 会话删除接口集成测试
8. Geo reverse 接口集成测试
9. 发布路线 E2E
10. AI 聊天 E2E

### 第三阶段：补埋点和错误监控

严格说埋点不属于测试，但它是验证功能质量的必要补充。

优先建议埋点：

- 发布按钮点击数
- 发布成功数 / 失败数
- 上传封面失败率
- 上传轨迹失败率
- AI 发送次数
- AI 首 token 耗时
- AI 推荐命中数
- AI 会话删除成功率
- `/weather` 接口失败率
- `/landscape-prediction` 接口失败率

## 7. 面试表达建议

如果面试官继续追问“每次开发完功能你怎么测试”，建议直接回答成固定流程：

1. 先做静态检查和编译验证
2. 给核心业务规则补单元测试
3. 给高风险接口补集成测试
4. 对主链路补 E2E 测试
5. 上线后用埋点和错误监控验证真实表现

对于 TrailQuest，你可以举最好的例子就是：

- 发布路线
- AI 对话
- 天气/景观预测

因为它们都属于“跨前端、后端、第三方服务、数据库”的复杂链路。

## 8. 最终建议

如果只允许本项目当前先做最少量但最高价值的测试，我建议：

### 必须先做

- 发布路线：集成测试 + E2E
- AI 会话删除/读取：集成测试
- AI 推荐地点命中：单元测试
- 天气聚合：集成测试
- WebSocket AI 聊天：E2E

### 紧接着做

- `usePublishUploadStore` 单元测试
- `chat.ts` 单元测试
- 路线详情互动 E2E
- 用户登录/资料编辑 E2E

### 可以后补

- 首页、社区、一般展示组件测试
- 纯视觉组件快照测试

一句话总结：

TrailQuest 当前最需要的，不是“把所有模块都测一遍”，而是优先把 **发布、AI、天气/景观、详情互动** 这四条最关键、最容易出事故的链路测稳。

延伸文档：

- [冒烟测试与回归测试方案](./smoke-and-regression-testing-plan-2026-03-30.md)
