# TrailQuest 后台评论治理模块设计文档

日期：2026-03-31

## 1. 背景

当前后台已经有评论管理页面，但能力非常轻，主要停留在“搜索 + 删除”。

现状代码：

- [apps/admin/src/views/ReviewManageView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/ReviewManageView.vue)
- [apps/admin/src/api/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/api/admin.ts)
- [apps/admin/src/types/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/types/admin.ts)

目前存在的明显问题：

1. 评论只有列表和删除，没有详情上下文
2. 删除使用 `window.confirm`，交互过于简陋
3. 看不到评论所属楼层、父评论、回复关系
4. 看不到评论关联举报
5. 不支持批量处理
6. 不支持“隐藏”与“恢复”，只有硬删除接口
7. 缺少删除原因、治理备注、操作留痕

这意味着当前模块更像临时工具，不像正式社区治理工作台。

## 2. 本期目标

本期目标不是把评论系统做成复杂的论坛审核平台，而是先把后台评论治理补成一个可持续使用的模块。

一期目标：

1. 评论列表可看懂上下文
2. 评论治理从“直接删除”升级为“隐藏优先、删除谨慎”
3. 支持查看评论详情和关联对象
4. 支持处理原因和治理备注
5. 支持批量治理
6. 接入操作日志

## 3. 设计原则

### 3.1 治理优先于物理删除

评论是社区行为记录，默认不建议普通管理员直接物理删除。

优先级建议：

1. 隐藏评论
2. 恢复评论
3. 删除评论

其中“删除评论”应作为更高风险动作，建议后置或限制权限。

### 3.2 必须保留上下文

管理员做评论判断时，不能只看一段孤立文本，需要看到：

- 评论作者
- 所属路线
- 父评论
- 楼层关系
- 发布时间
- 举报原因摘要

### 3.3 治理动作必须有理由

隐藏、删除、恢复等动作都应允许填写处理原因，并进入审计日志。

## 4. 当前代码现状

当前评论管理模块只有以下能力：

- 按评论内容、路线名、作者搜索
- 列表展示评论文本、作者、路线、时间
- 删除评论

对应实现见：

- [apps/admin/src/views/ReviewManageView.vue](/Users/sheng/Documents/code/hiking/apps/admin/src/views/ReviewManageView.vue)

后端接口当前只有：

- `GET /api/admin/reviews`
- `DELETE /api/admin/reviews/{id}`

对应实现入口：

- [apps/admin/src/api/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/api/admin.ts)

这说明当前治理模型过于单一，无法支撑后续社区增长。

## 5. 范围定义

### 5.1 本期必须支持

1. 评论列表页增强
2. 评论详情查看
3. 隐藏评论
4. 恢复评论
5. 删除评论
6. 批量隐藏
7. 批量恢复
8. 显示治理原因
9. 显示评论上下文
10. 写入操作日志

### 5.2 本期可选

1. 批量删除
2. 与举报中心联动跳转
3. 评论风险标签

### 5.3 本期不做

1. AI 自动审核
2. 敏感词实时命中面板
3. 多级审核流

## 6. 治理状态设计

建议为评论补充状态字段：

- `status = active | hidden | deleted`

字段建议：

- `moderation_reason VARCHAR(500) NULL`
- `moderated_by BIGINT NULL`
- `moderated_at DATETIME NULL`
- `deleted_by BIGINT NULL`
- `deleted_at DATETIME NULL`

含义建议：

- `active`：正常可见
- `hidden`：前台不可见，但后台保留，支持恢复
- `deleted`：软删除，不再作为正常评论参与前台展示

如果现有数据模型不方便快速调整，也可以先用：

- `visible` 布尔字段
- `deleted_at`

但从可扩展性看，推荐直接上 `status`。

## 7. 评论列表设计

### 7.1 筛选条件

建议支持：

- 评论内容关键词
- 路线关键词
- 作者关键词
- 状态筛选
- 是否被举报
- 时间范围

### 7.2 列表字段

建议字段：

- 评论内容摘要
- 作者
- 所属路线
- 父评论摘要
- 状态
- 举报次数
- 发布时间
- 最近治理时间
- 操作

### 7.3 行尾操作

建议提供：

- 查看详情
- 隐藏
- 恢复
- 删除

注意：

- `active` 状态显示“隐藏”
- `hidden` 状态显示“恢复”
- “删除”放在二级危险操作中

## 8. 评论详情设计

建议通过弹窗、抽屉或独立详情页展示以下内容：

1. 评论完整正文
2. 作者信息
3. 所属路线
4. 父评论内容
5. 子回复列表摘要
6. 举报记录摘要
7. 当前治理状态
8. 历史处理备注

一期不要求做完整评论线程树，但至少要能看到：

- 当前评论
- 父评论
- 最近几条子回复

## 9. 治理动作设计

### 9.1 隐藏评论

适用场景：

- 争议内容
- 低质灌水
- 疑似违规但暂不需要彻底删除

规则：

- 前台不可见
- 后台可恢复
- 必填处理原因
- 写入操作日志

### 9.2 恢复评论

适用场景：

- 误判纠正
- 申诉通过

规则：

- `hidden -> active`
- 保留最后一次治理历史
- 写入操作日志

### 9.3 删除评论

适用场景：

- 明显违规
- 法务要求删除
- 已确认高风险内容

规则：

- 默认作为高风险动作
- 强制二次确认
- 建议必填删除原因
- 写入操作日志

## 10. 批量处理设计

### 10.1 一期建议支持

- 批量隐藏
- 批量恢复

### 10.2 暂缓支持

- 批量删除

原因：

- 删除是高风险不可逆动作
- 误操作成本高

### 10.3 交互建议

列表支持复选框，顶部出现批量操作栏：

- 已选 N 条
- 批量隐藏
- 批量恢复
- 清空选择

## 11. 后端接口设计

### 11.1 列表接口增强

`GET /api/admin/reviews`

建议查询参数增加：

- `status`
- `reported`
- `dateFrom`
- `dateTo`

建议返回字段增加：

- `status`
- `parentReviewId`
- `parentReviewText`
- `reportCount`
- `moderationReason`
- `moderatedAt`

### 11.2 评论详情接口

`GET /api/admin/reviews/{id}`

返回：

- 评论完整信息
- 父评论摘要
- 子回复摘要
- 举报摘要
- 治理历史摘要

### 11.3 评论隐藏接口

`POST /api/admin/reviews/{id}/hide`

请求体：

```json
{
  "reason": "包含攻击性内容"
}
```

### 11.4 评论恢复接口

`POST /api/admin/reviews/{id}/restore`

### 11.5 评论删除接口

保留：

- `DELETE /api/admin/reviews/{id}`

或改造成更安全的：

- `POST /api/admin/reviews/{id}/delete`

若保留 `DELETE`，也建议支持请求体或请求头传递治理原因；如果框架处理不方便，建议改为 `POST` 动作接口。

### 11.6 批量接口

建议新增：

- `POST /api/admin/reviews/batch-hide`
- `POST /api/admin/reviews/batch-restore`

## 12. 前端改造方案

### 12.1 API 层

在 [apps/admin/src/api/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/api/admin.ts) 中补充：

- `fetchAdminReviewDetail`
- `hideAdminReview`
- `restoreAdminReview`
- `batchHideAdminReviews`
- `batchRestoreAdminReviews`

### 12.2 类型层

在 [apps/admin/src/types/admin.ts](/Users/sheng/Documents/code/hiking/apps/admin/src/types/admin.ts) 中补充：

- `AdminReviewDetail`
- `AdminReviewModerationRequest`
- `AdminReviewBatchActionRequest`

并扩展现有 `AdminReviewListItem`：

- `status`
- `reportCount`
- `parentReviewText`
- `moderationReason`
- `moderatedAt`

### 12.3 页面与组件

建议新增或重构：

- `apps/admin/src/views/ReviewManageView.vue`
- `apps/admin/src/components/reviews/ReviewManagementTable.vue`
- `apps/admin/src/components/reviews/ReviewDetailDialog.vue`
- `apps/admin/src/components/reviews/ReviewBatchActionBar.vue`
- `apps/admin/src/composables/useReviewManagement.ts`

### 12.4 交互改造

需要替换当前 `window.confirm`，统一改为：

- `AdminConfirmDialog`
- `AdminNoticeDialog`

这样可以与现有后台风格保持一致。

## 13. 与举报中心联动建议

后续建议让举报中心可跳到评论治理详情：

- 举报对象类型为 `review` 时，支持从举报记录跳转评论详情
- 评论详情显示该评论关联的举报摘要

一期不必做很深的联动，但建议接口结构预留 `reportCount` 与举报摘要字段。

## 14. 验收标准

满足以下条件视为一期完成：

1. 评论列表支持按状态筛选
2. 评论可隐藏、恢复、删除
3. 隐藏和删除支持填写原因
4. 评论详情可看到上下文信息
5. 列表支持批量隐藏和批量恢复
6. 所有治理动作写入操作日志
7. 前台只展示 `active` 评论

## 15. 迭代建议

### P1

- 举报联动跳转
- 评论处理历史时间线
- 敏感词命中标签

### P2

- 自动审核策略
- AI 风险评分
- 评论申诉与人工复核流

