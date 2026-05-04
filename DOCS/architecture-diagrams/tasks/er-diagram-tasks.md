# 数据库 ER 图绘制任务清单

## 已完成 ✅

- [x] **用户表 (users)** - 用户基本信息、角色、状态、封禁信息
- [x] **路线表 (trails)** - 路线信息、难度、距离、海拔、审核状态
- [x] **评论表 (reviews)** - 评论内容、评分、回复关系、治理信息
- [x] **媒体文件表 (media_files)** - 文件存储、OSS 信息、业务类型
- [x] **表关系总览图** - 核心表之间的关系（users/trails/reviews/media/tags/ai_conversations）
- [x] **标签表 (tags)** - 标签名
- [x] **路线标签关联表 (trail_tags)** - 路线与标签的多对多关联
- [x] **路线图片表 (trail_images)** - 路线图片、排序
- [x] **路线收藏表 (trail_favorites)** - 用户收藏路线
- [x] **路线点赞表 (trail_likes)** - 用户点赞路线
- [x] **AI 消息表 (ai_messages)** - AI 消息内容、角色、Token 数
- [x] **用户徒步画像表 (user_hiking_profiles)** - 徒步等级、总距离、完成路线数
- [x] **评论图片表 (review_images)** - 评论图片、排序
- [x] **路线轨迹表 (trail_tracks)** - GPX/KML 轨迹、轨迹点数、距离、爬升
- [x] **后台操作日志表 (admin_operation_logs)** - 操作类型、目标、IP 地址
- [x] **应用配置表 (app_settings)** - 配置键值对
- [x] **上传检查点表 (upload_checkpoints)** - 分片上传状态
- [x] **景观特征快照表 (landscape_feature_snapshots)** - 路线景观特征
- [x] **景观观察标签表 (landscape_observation_labels)** - 景观标签、置信度
- [x] **系统选项组表 (system_option_groups)** - 选项组编码、名称
- [x] **系统选项项表 (system_option_items)** - 选项编码、标签、值

---

## 绘制规范

### 图形说明
| 图形 | 含义 |
|------|------|
| 矩形 | 数据表 |
| 椭圆 | 表字段 |
| 菱形 | 表之间的关系 |
| 黄色椭圆 | 外键字段（关联其他表） |

### 关系标注
- **1:n** - 一对多关系
- **1:1** - 一对一关系
- **n:m** - 多对多关系（通过中间表）

---

## 表关系映射

```
users (1) ───< (n) trails              [发布]
users (1) ───< (n) reviews             [发表]
users (1) ───< (n) trail_favorites     [收藏]
users (1) ───< (n) trail_likes         [点赞]
users (1) ───< (n) media_files         [上传]
users (1) ───< (n) ai_conversations    [创建会话]
users (1) ───< (1) user_hiking_profiles [画像]
users (1) ───< (n) admin_operation_logs [操作]

trails (1) ───< (n) reviews            [被评论]
trails (1) ───< (n) trail_images       [图片]
trails (1) ───< (n) trail_tags         [标签关联]
trails (1) ───< (n) trail_favorites    [被收藏]
trails (1) ───< (n) trail_likes        [被点赞]
trails (1) ───< (n) trail_tracks       [轨迹]

reviews (1) ───< (n) review_images     [图片]
reviews (1) ───< (n) reviews           [回复/自引用]

tags (1) ───< (n) trail_tags           [标记路线]

ai_conversations (1) ───< (n) ai_messages [消息]

media_files (1) ───< (1) users         [头像]
media_files (1) ───< (n) trail_tracks  [轨迹文件]

system_option_groups (1) ───< (n) system_option_items [选项项]

landscape_feature_snapshots (1) ───< (n) landscape_observation_labels [标签]
```

---

## 文件命名规范

```
DOCS/architecture-diagrams/sources/
├── 用户表.drawio
├── 路线表.drawio
├── 评论表.drawio
├── 媒体文件表.drawio
├── 数据库关系总图.drawio
├── 标签表.drawio
├── 路线标签关联表.drawio
├── 路线图片表.drawio
├── 路线收藏表.drawio
├── 路线点赞表.drawio
├── 路线轨迹表.drawio
├── 评论图片表.drawio
├── AI消息表.drawio
├── 用户徒步画像表.drawio
├── 后台操作日志表.drawio
├── 应用设置表.drawio
├── 上传检查点表.drawio
├── 景观特征快照表.drawio
├── 景观观察标签表.drawio
├── 系统选项组表.drawio
└── 系统选项项表.drawio
```
