# 当前开发进度与交接

这份文档用于记录 2026-03-16 当前已经完成的工作、真实可用能力、未完成项以及明早最推荐继续推进的方向，方便下一次开发时直接接上。

## 1. 今天已完成的内容

### 1.1 通用媒体表与上传基础设施已落地

已完成：

- `media_files` 通用媒体表设计
- `users.avatar_media_id` 头像媒体字段
- 媒体文件后端实体、Mapper、DTO、VO
- OSS 配置对象 `OssProperties`
- 上传服务 `UploadService`

数据库相关文档与脚本：

- `DOCS/database/mysql-media-files-upgrade.sql`
- `DOCS/database/mysql-schema.md`
- `DOCS/database/hikingDB.sql`

说明：

- `media_files` 当前定位是“媒体资产表”，不直接承担通用业务关联
- 业务关联仍由 `trails.image`、`trail_images`、`review_images`、`users.avatar_media_id` 等字段负责

### 1.2 阿里云 OSS STS 上传链路已打通

已完成：

- `POST /api/uploads/sts`
- `POST /api/uploads/complete`
- RAM 用户 + STS AssumeRole 权限打通
- OSS 临时凭证申请成功
- 真实文件上传与落库验证成功

当前上传策略：

- 后端生成 `objectKey`
- 前端通过 STS 临时凭证直传 OSS
- 上传完成后调用 `/api/uploads/complete`
- 后端将文件信息写入 `media_files`

当前支持的业务类型：

- `avatar`
- `trail_cover`
- `trail_gallery`
- `review`

### 1.3 前端最小上传测试页已可用

已完成：

- 新增前端最小上传测试页 `/oss-upload-test`
- 真实接入 STS -> OSS -> complete 完整链路
- 本地图片预览
- 上传进度展示
- 上传结果展示

说明：

- 这页已经不是 mock，而是真正上传 OSS
- 用于后续接头像、评论图片、路线封面/相册的开发验证

### 1.4 头像真实图片能力已接入当前用户链路

已完成：

- `users.avatar_media_id` 已进入后端模型
- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/me`

已支持：

- 当前用户返回 `avatarMediaId`
- 当前用户返回 `avatarMediaUrl`
- 若用户已上传头像，则前端个人页和头部优先显示真实头像图
- 若未上传头像，则继续显示 `avatar + avatarBg`

### 1.5 本地配置安全化已完成

已完成：

- `application.properties` 中 OSS 密钥改为环境变量 / profile 覆盖方式
- 新增 `application-local.properties.example`
- 新增本地 `application-local.properties` 方案
- `.gitignore` 已忽略本地 `application-local.properties`

说明：

- 仓库内不再保存真实 OSS AK/SK
- 本地开发建议使用 `local` profile

### 1.6 Git 历史中的泄露密钥提交已清理

已完成：

- 将含真实 OSS 密钥的历史提交从当前 `main` 分支重写清理
- 合并为新的干净提交
- 保留本地备份分支以防回溯

说明：

- 当前主分支历史不应再包含那次泄露的 AK/SK
- 原旧密钥需要确保已经在阿里云侧禁用或删除

## 2. 当前已经可用的真实链路

目前已经确认可用的真实链路包括：

- 用户注册
- 用户登录
- 登录态恢复
- 首页路线列表
- 搜索页路线筛选
- 社区页路线流
- 路线详情主数据展示
- OSS STS 凭证申请
- OSS 真实图片上传
- `media_files` 文件记录落库
- 当前用户真实头像 URL 返回

换句话说，目前已经跑通了：

- `auth + trails`
- `OSS 上传测试链路`

## 3. 当前还没开始或还没完成的部分

### 3.1 评论模块仍未后端化

目前仍未完成：

- `GET /api/trails/{id}/reviews`
- `POST /api/reviews`
- 评论树构建
- 二级/三级回复真实提交
- 评论图片与 `review_images` 的业务落库联动

当前现状：

- 详情页评论区仍然使用 `mockData.ts`
- 评论新增、回复、评论图片仍是前端本地 mock 行为

### 3.2 路线图片业务联动仍未接入

目前仍未完成：

- 发布路线时把 `trail_cover` 同步写入 `trails.image`
- 发布路线时把 `trail_gallery` 同步写入 `trail_images`
- 路线编辑或替换图片时的更新逻辑

### 3.3 头像编辑页仍未接真实上传

目前仍未完成：

- 个人页“编辑头像”入口
- 上传头像并实时刷新当前用户资料
- 头像替换后的用户体验细节

### 3.4 收藏/点赞与个人页仍未后端化

目前未完成：

- 收藏列表接口
- 收藏/取消收藏接口
- 点赞/取消点赞接口
- 我的发布
- 我的收藏
- 个人资料真实统计
- 管理员后台页面

### 3.5 天气能力仍在前端直连高德

当前现状：

- 路线定位与天气查询仍在前端调用高德接口
- 暂未迁移到后端

当前建议：

- 短期保留前端直连即可
- 中长期若要做缓存、隐藏 key、统一多端能力，再迁到后端

## 4. 当前代码层面的核心判断

### 已经稳定的部分

- 认证骨架
- JWT 鉴权
- trails 列表与详情接口
- 前端统一请求层与登录态处理
- 首页 / 搜索页 / 社区页真实数据接入
- `media_files` 表结构与落库能力
- OSS STS 上传链路
- 最小上传测试页
- 当前用户头像真实 URL 返回

### 尚未稳定的部分

- 评论模块设计与接口
- 评论图片与 `review_images` 的业务联动
- 路线封面 / 相册的真实上传接入
- 头像编辑交互
- 收藏/点赞交互
- 个人页和后台页的数据结构

## 5. 明早最推荐的下一步

最推荐马上开始的任务是：

1. 评论接口后端化
2. 详情页评论区接真实接口
3. 评论图片接 `review` 上传链路并同步写入 `review_images`

推荐原因：

- 主数据、认证、上传基础设施都已经准备好了
- 现在最明显的缺口就是详情页评论区仍在 mock
- 一旦评论接完，“列表页 -> 详情页 -> 评论互动 -> 图片上传”主链路就闭环了

### 推荐先做的接口

优先建议：

- `GET /api/trails/{id}/reviews`
- `POST /api/reviews`

其中评论模块需要明确支持：

- 顶级评论
- 二级回复
- 三级回复
- 二级/三级图片回复

### 次优先任务

如果评论暂时不做，第二推荐是：

1. 个人页头像编辑接真实上传
2. 路线发布页封面图 / 相册图接真实上传

## 6. 当前注意点

### 6.1 本地启动需使用 `local` profile

当前建议启动方式：

```bash
./apps/api/mvnw -f apps/api/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
```

说明：

- `application-local.properties` 不提交到仓库
- `.example` 文件只保留占位符说明

### 6.2 若启动失败，先检查 8080 端口占用

常见排查：

```bash
lsof -i :8080
kill -9 <PID>
```

### 6.3 OSS 配置不完整时，上传接口会直接失败

当前行为：

- 若缺少本地 OSS 配置，后端会返回 `OSS_CONFIG_MISSING`
- `/oss-upload-test` 页面会直接提示配置不完整

### 6.4 GitHub MCP 目前仍不可依赖

当前已知情况：

- 之前验证时 GitHub MCP 认证为 `401 Bad credentials`
- 当前提交流程仍以本地 git 为准

## 7. 一句话总结

当前项目已经完成了“认证 + trails + OSS 上传基础设施”的真实化，明早最值得继续推进的是评论模块后端化，把路线详情页评论区和图片上传真正闭环。
