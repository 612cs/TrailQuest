# TrailQuest 后台操作审计日志设计文档

日期：2026-03-31

## 1. 背景

当前 `apps/admin` 已经具备一批高风险治理动作：

- 封禁用户 / 解封用户
- 路线审核通过 / 驳回
- 路线下架 / 恢复
- 评论删除
- 举报处理
- 首页大图修改
- 配置中心配置项创建 / 更新

这些动作已经覆盖账号治理、内容治理、运营配置三大类核心后台能力，但现阶段没有任何“谁在什么时间对什么对象执行了什么操作”的留痕能力。

当前代码现状可以从这些文件看出：

- [apps/admin/src/api/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/api/admin.ts)
- [apps/admin/src/views/UserManageView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/UserManageView.vue)
- [apps/admin/src/views/TrailReviewDetailView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/TrailReviewDetailView.vue)
- [apps/admin/src/views/TrailManagementDetailView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/TrailManagementDetailView.vue)
- [apps/admin/src/views/ReviewManageView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/ReviewManageView.vue)
- [apps/admin/src/views/ReportManageView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/ReportManageView.vue)
- [apps/admin/src/views/SettingView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/SettingView.vue)
- [apps/admin/src/views/ConfigCenterView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/ConfigCenterView.vue)

这会带来几个直接风险：

- 误操作后无法追溯责任人
- 管理员之间无法确认是谁做过某次治理
- 无法为封禁、驳回、删除等动作提供合规审查依据
- 后续做权限细分时，缺少行为历史数据支撑

因此需要补一版后台操作审计日志模块，先解决“关键操作必留痕、可搜索、可追溯”。

## 2. 本期目标

本期目标不是做一套复杂的企业审计平台，而是先做后台一期可落地的审计日志最小闭环。

一期目标：

1. 所有高风险后台操作自动写入审计日志
2. 后台新增“操作日志”列表页，支持按人、按模块、按动作、按时间检索
3. 关键日志可查看操作前后摘要
4. 审计日志默认不可修改、不可删除
5. 为后续 RBAC、风控、告警提供统一日志基础设施

## 3. 设计原则

### 3.1 以服务端记账为准

审计日志必须由后端在治理接口成功执行后写入，不能以前端埋点代替。

原因：

- 前端埋点可被绕过
- 后端更容易拿到真实操作者身份
- 后端能感知接口是否真正执行成功

### 3.2 只记录关键治理动作

一期不记录所有页面浏览和普通查询，只记录会改变业务状态或配置结果的操作。

### 3.3 日志不可篡改

普通管理员不能编辑和删除审计日志。即使后续支持归档，也不允许业务侧随意覆盖原始内容。

### 3.4 先保留摘要，不追求全量快照

一期先记录结构化摘要：

- 操作对象
- 操作类型
- 操作人
- 操作结果
- 操作理由
- 关键字段变更前后摘要

不要求一开始就把完整对象 JSON 做成大而全的版本对比系统。

## 4. 范围定义

### 4.1 本期必须纳入审计的动作

#### 账号治理

- 封禁用户
- 解封用户

#### 路线治理

- 路线审核通过
- 路线审核驳回
- 路线下架
- 路线恢复

#### 社区治理

- 评论删除
- 举报处理

#### 运营配置

- 首页大图更新
- 配置项创建
- 配置项更新
- 配置项启用 / 停用

### 4.2 本期暂不纳入

- 普通列表查询
- 登录页输入行为
- 页面访问轨迹
- 非管理员前台用户行为日志

## 5. 数据设计

建议新增一张主表：`admin_operation_logs`

建议字段：

- `id BIGINT PRIMARY KEY`
- `operator_id BIGINT NOT NULL`
- `operator_name VARCHAR(100) NOT NULL`
- `operator_role VARCHAR(32) NOT NULL`
- `module_code VARCHAR(64) NOT NULL`
- `action_code VARCHAR(64) NOT NULL`
- `target_type VARCHAR(64) NOT NULL`
- `target_id VARCHAR(64) NOT NULL`
- `target_title VARCHAR(255) NULL`
- `reason VARCHAR(500) NULL`
- `result_status VARCHAR(32) NOT NULL`
- `before_snapshot JSON NULL`
- `after_snapshot JSON NULL`
- `request_id VARCHAR(64) NULL`
- `ip_address VARCHAR(64) NULL`
- `user_agent VARCHAR(500) NULL`
- `created_at DATETIME NOT NULL`

建议索引：

- `KEY idx_operator_created (operator_id, created_at DESC)`
- `KEY idx_module_created (module_code, created_at DESC)`
- `KEY idx_target (target_type, target_id, created_at DESC)`
- `KEY idx_action_created (action_code, created_at DESC)`

## 6. 枚举建议

### 6.1 模块枚举 `module_code`

- `user_management`
- `trail_review`
- `trail_management`
- `review_management`
- `report_management`
- `setting_management`
- `config_center`

### 6.2 动作枚举 `action_code`

- `user_ban`
- `user_unban`
- `trail_approve`
- `trail_reject`
- `trail_offline`
- `trail_restore`
- `review_delete`
- `report_resolve`
- `home_hero_update`
- `option_item_create`
- `option_item_update`
- `option_item_toggle`

### 6.3 对象类型 `target_type`

- `user`
- `trail`
- `review`
- `report`
- `setting`
- `option_item`

### 6.4 结果状态 `result_status`

- `success`
- `failed`

一期建议只记录成功日志。若实现成本可接受，也可以补失败日志，但失败日志应与成功日志区分，避免误解为状态已生效。

## 7. 快照设计

### 7.1 一期快照内容建议

#### 用户封禁/解封

`before_snapshot` 示例：

```json
{
  "status": "active"
}
```

`after_snapshot` 示例：

```json
{
  "status": "banned",
  "banReason": "多次发布违规内容"
}
```

#### 路线审核

```json
{
  "reviewStatus": "pending",
  "reviewRemark": null
}
```

```json
{
  "reviewStatus": "rejected",
  "reviewRemark": "轨迹信息不完整"
}
```

#### 首页大图更新

```json
{
  "imageUrl": "old-url"
}
```

```json
{
  "imageUrl": "new-url"
}
```

### 7.2 不建议记录的内容

- 用户密码
- Access Token
- 完整请求头
- 过大的媒体元数据
- 可能包含敏感个人信息的无关字段

## 8. 后端接口设计

### 8.1 日志列表接口

`GET /api/admin/operation-logs`

查询参数建议：

- `pageNum`
- `pageSize`
- `moduleCode`
- `actionCode`
- `operatorKeyword`
- `targetType`
- `targetId`
- `dateFrom`
- `dateTo`

返回字段建议：

- `id`
- `operatorName`
- `moduleCode`
- `actionCode`
- `targetType`
- `targetId`
- `targetTitle`
- `reason`
- `resultStatus`
- `createdAt`

### 8.2 日志详情接口

`GET /api/admin/operation-logs/{id}`

返回字段在列表基础上补充：

- `beforeSnapshot`
- `afterSnapshot`
- `operatorId`
- `operatorRole`
- `requestId`
- `ipAddress`
- `userAgent`

## 9. 后端落地方案

### 9.1 记录方式建议

优先方案：

1. 在每个治理 service 方法成功提交后显式调用日志记录器
2. 日志记录器统一收口为 `AdminOperationLogService`

不建议一期就强依赖 AOP 全自动切面。原因是：

- 当前后台治理动作数量有限
- 显式调用更清晰
- 对快照内容的控制更直接

### 9.2 推荐封装结构

- `AdminOperationLogEntity`
- `AdminOperationLogMapper`
- `AdminOperationLogService`
- `AdminOperationLogController`
- `AdminOperationLogAssembler`

可补一个统一写日志方法：

```java
record(
  operator,
  moduleCode,
  actionCode,
  targetType,
  targetId,
  targetTitle,
  reason,
  beforeSnapshot,
  afterSnapshot
)
```

## 10. 后台页面设计

### 10.1 新增菜单

建议在 `apps/admin` 侧边栏新增：

- `操作日志`

路由建议：

- `/operation-logs`

### 10.2 列表页字段

建议字段：

- 时间
- 操作人
- 模块
- 动作
- 对象类型
- 对象标识
- 对象标题
- 结果
- 原因摘要
- 操作

行尾操作：

- 查看详情

### 10.3 筛选项

建议支持：

- 操作人关键词
- 模块筛选
- 动作筛选
- 对象类型筛选
- 日期范围

### 10.4 详情弹窗/详情页

建议展示：

- 基本信息
- 操作原因
- 变更前摘要
- 变更后摘要
- 请求信息

一期可以先做抽屉或弹窗，不必单独做复杂详情页。

## 11. 前端改造范围

### 11.1 新增 API

在 [apps/admin/src/api/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/api/admin.ts) 中补充：

- `fetchAdminOperationLogs`
- `fetchAdminOperationLogDetail`

### 11.2 新增类型

在 [apps/admin/src/types/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/types/admin.ts) 中补充：

- `AdminOperationLogListItem`
- `AdminOperationLogDetail`
- `AdminOperationLogPageQuery`

### 11.3 新增页面

建议新增：

- `apps/admin/src/views/OperationLogView.vue`
- `apps/admin/src/components/logs/OperationLogTable.vue`
- `apps/admin/src/components/logs/OperationLogDetailDialog.vue`
- `apps/admin/src/composables/useOperationLogList.ts`

### 11.4 菜单与路由

需要同步改造：

- [apps/admin/src/router/index.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/router/index.ts)
- [apps/admin/src/components/layout/AdminSidebar.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/components/layout/AdminSidebar.vue)

## 12. 验收标准

满足以下条件视为一期完成：

1. 用户封禁/解封后可在操作日志看到记录
2. 路线审核、下架、恢复后可在操作日志看到记录
3. 评论删除、举报处理、首页大图更新、配置项修改后可在操作日志看到记录
4. 日志支持分页、筛选、查看详情
5. 日志默认不可编辑、不可删除
6. 详情页能看到至少一组变更前后摘要

## 13. 迭代建议

### P1

- 支持失败日志
- 支持导出 CSV
- 支持按对象回看历史操作时间线

### P2

- 支持告警规则
- 支持高风险操作二次确认与工单号关联
- 支持与权限系统联动的风险审查面板

