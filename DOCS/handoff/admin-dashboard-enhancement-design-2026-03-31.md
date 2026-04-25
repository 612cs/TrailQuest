# TrailQuest 后台首页仪表盘增强设计文档

日期：2026-03-31

## 1. 背景

当前后台首页已经有基础仪表盘，但整体仍偏展示型，不像真正能提升管理效率的工作台。

现状代码：

- [apps/admin/src/views/DashboardView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/DashboardView.vue)
- [apps/admin/src/api/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/api/admin.ts)
- [apps/admin/src/types/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/types/admin.ts)

当前首页只有：

1. 待审核路线数量
2. 评论总数
3. 待处理举报数量
4. 用户总数
5. 一组静态说明文案
6. 几个快捷入口链接

这会导致几个问题：

- 管理员进入后台后不知道“现在最该处理什么”
- 看不到趋势变化和异常信号
- 看不到最近待办和高风险数据
- 看不到治理效率和工作积压
- 仪表盘与具体业务模块之间的联动太弱

因此需要把首页从“欢迎页 + 四张卡片”升级为“真正的后台工作台”。

## 2. 本期目标

一期目标不是做复杂 BI，而是围绕后台治理效率，把首页做成一个真正的运营入口。

本期目标：

1. 首页能直接反映待处理工作量
2. 首页能看到最近异常与风险
3. 首页能展示近 7 天趋势
4. 首页能给出快速行动入口
5. 首页字段与后续治理模块保持一致

## 3. 设计原则

### 3.1 优先展示待办，而不是泛统计

后台首页首先要回答的是：

- 现在有哪些事情需要处理
- 哪些是高优先级
- 哪些模块积压了

而不是只展示平台总量。

### 3.2 指标必须能跳转到行动页

首页每个关键模块都应能直接跳到对应列表页，并尽量带上预设筛选条件。

### 3.3 趋势优先于绝对值

只有总数没有趋势，管理员很难判断系统是变好还是变坏。

一期至少补：

- 近 7 天新发布路线数
- 近 7 天新评论数
- 近 7 天新增举报数
- 近 7 天新增用户数

## 4. 当前代码现状

当前 `DashboardView` 主要由两块组成：

1. 治理概览卡片
2. 后台说明

对应实现见：

- [apps/admin/src/views/DashboardView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/DashboardView.vue)

当前接口：

- `GET /api/admin/dashboard/summary`

返回类型：

- `pendingTrailCount`
- `reviewCount`
- `pendingReportCount`
- `userCount`

对应类型定义：

- [apps/admin/src/types/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/types/admin.ts)

这说明首页数据模型仍偏“最小占位”，不够支撑高频运营。

## 5. 首页信息架构建议

建议把首页拆成 5 个区域。

### 5.1 顶部总览卡片

保留，但调整为更偏“治理待办”的指标：

1. 待审核路线
2. 待处理举报
3. 已隐藏评论
4. 今日新增用户

可选补充：

5. 今日新增路线
6. 今日新增评论

### 5.2 今日待办区

展示当前最需要处理的工作队列：

- 待审核路线数
- 高风险举报数
- 最近被集中举报的评论数
- 被封禁后申诉待处理数

一期如果没有申诉能力，可以先只保留前三项。

### 5.3 最近风险动态

建议展示最近 5 到 10 条：

- 新增举报
- 新驳回路线
- 新封禁用户
- 集中出现的异常评论

这个区域的目标不是“做日志页”，而是让管理员一进入后台就看到最近风险变化。

### 5.4 7 日趋势区

建议最少展示 4 条折线或柱状趋势：

- 近 7 天新增路线
- 近 7 天新增评论
- 近 7 天新增举报
- 近 7 天新增用户

如果实现成本有限，也可以先做一张多序列趋势图。

### 5.5 快捷操作区

建议保留并增强：

- 路线审核
- 路线管理
- 用户管理
- 评论治理
- 举报处理
- 配置中心
- 操作日志

## 6. 数据指标设计

### 6.1 概览指标

建议接口返回：

- `pendingTrailCount`
- `pendingReportCount`
- `hiddenReviewCount`
- `todayNewUserCount`
- `todayNewTrailCount`
- `todayNewReviewCount`

### 6.2 趋势指标

建议返回：

```json
[
  {
    "date": "2026-03-25",
    "newTrailCount": 12,
    "newReviewCount": 26,
    "newReportCount": 3,
    "newUserCount": 9
  }
]
```

### 6.3 风险动态指标

建议返回最近动态列表：

- `type`
- `title`
- `description`
- `targetType`
- `targetId`
- `createdAt`
- `priority`

事件类型建议：

- `new_report`
- `trail_rejected`
- `user_banned`
- `review_hidden`

### 6.4 待办队列指标

建议返回：

- `pendingTrailCount`
- `pendingReportCount`
- `reportedReviewCount`
- `offlineTrailCount`

## 7. 后端接口设计

### 7.1 首页总览接口升级

保留：

- `GET /api/admin/dashboard/summary`

建议把返回结构升级为：

- `overview`
- `todo`
- `trends`
- `recentRisks`

如果不想破坏现有接口，也可以拆成多个接口。

推荐拆分方式：

1. `GET /api/admin/dashboard/overview`
2. `GET /api/admin/dashboard/trends`
3. `GET /api/admin/dashboard/recent-risks`

### 7.2 返回结构建议

#### `overview`

```json
{
  "pendingTrailCount": 12,
  "pendingReportCount": 4,
  "hiddenReviewCount": 8,
  "todayNewUserCount": 6,
  "todayNewTrailCount": 10,
  "todayNewReviewCount": 18
}
```

#### `recent-risks`

```json
[
  {
    "type": "new_report",
    "title": "评论被举报",
    "description": "评论 23891 被举报 3 次",
    "targetType": "review",
    "targetId": "23891",
    "priority": "high",
    "createdAt": "2026-03-31T10:20:00"
  }
]
```

## 8. 前端页面设计

### 8.1 页面结构调整

当前首页只有两块大卡片，建议改为：

1. 顶部概览卡片区
2. 中部待办 + 风险双列区
3. 底部趋势图 + 快捷入口区

### 8.2 卡片建议

#### 顶部概览卡片

卡片字段建议：

- 名称
- 当前值
- 与昨日相比变化值
- 点击跳转入口

#### 今日待办

每一项显示：

- 待办标题
- 数量
- 说明
- 处理入口

#### 风险动态

每一项显示：

- 风险级别
- 事件标题
- 事件说明
- 时间
- 跳转按钮

#### 趋势图

图表建议：

- 优先折线图
- 支持 hover 查看每日值

一期若不引入图表库，也可以先做简化版趋势条形图组件。

## 9. 前端改造方案

### 9.1 API 层

在 [apps/admin/src/api/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/api/admin.ts) 中补充：

- `fetchDashboardOverview`
- `fetchDashboardTrends`
- `fetchDashboardRecentRisks`

如果坚持单接口，也需要扩展 `fetchDashboardSummary` 的返回结构。

### 9.2 类型层

在 [apps/admin/src/types/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/types/admin.ts) 中新增：

- `AdminDashboardOverview`
- `AdminDashboardTrendItem`
- `AdminDashboardRiskItem`
- `AdminDashboardTodoItem`

### 9.3 组件层

建议新增：

- `apps/admin/src/components/dashboard/DashboardOverviewCards.vue`
- `apps/admin/src/components/dashboard/DashboardTodoPanel.vue`
- `apps/admin/src/components/dashboard/DashboardRiskFeed.vue`
- `apps/admin/src/components/dashboard/DashboardTrendChart.vue`
- `apps/admin/src/components/dashboard/DashboardQuickLinks.vue`

### 9.4 页面层

重构：

- [apps/admin/src/views/DashboardView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/DashboardView.vue)

让页面只负责数据编排，具体展示拆到 dashboard 子组件中。

## 10. 指标与页面联动规则

建议至少支持以下跳转：

- 待审核路线卡片 -> `/trails/review?reviewStatus=pending`
- 待处理举报卡片 -> `/reports`
- 已隐藏评论卡片 -> `/reviews?status=hidden`
- 今日新增用户卡片 -> `/users`
- 风险动态中的评论事件 -> 评论详情
- 风险动态中的路线事件 -> 路线详情

## 11. 验收标准

满足以下条件视为一期完成：

1. 首页不再只有四张静态统计卡片
2. 首页能展示待办区、风险动态区、趋势区
3. 关键卡片支持跳转到对应模块
4. 趋势至少支持近 7 天四类核心指标
5. 风险动态至少能展示评论、举报、路线审核三类事件

## 12. 迭代建议

### P1

- 支持按管理员角色显示个性化待办
- 支持按日/周切换趋势
- 支持首页模块拖拽排序

### P2

- 接入更完整的 BI 指标
- 支持异常波动告警
- 支持基于操作日志的治理效率统计
