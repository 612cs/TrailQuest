# MySQL 建表（含字段注释）

当前建模约定：

- `trails` 就是“社区动态”和“路线发布”的本体，不额外拆 `posts` / `publishes` 表。
- “我的发布”直接查询 `trails.author_id`。
- “我的收藏”使用 `trail_favorites(trail_id, user_id)` 关系表。
- 路线发布时间统一使用 `trails.created_at`，前端自行格式化为“几小时前”。
- AI Chat 以后端表结构为准，前端联调时再把 `id/timestamp` 做字段映射。

```sql
-- 用户表
CREATE TABLE `users` (
  `id` BIGINT PRIMARY KEY COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `avatar` VARCHAR(16) NOT NULL COMMENT '头像缩写',
  `avatar_bg` VARCHAR(16) NOT NULL COMMENT '头像背景色',
  `email` VARCHAR(100) COMMENT '邮箱',
  `password_hash` VARCHAR(255) COMMENT '密码哈希',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 路线表
CREATE TABLE `trails` (
  `id` BIGINT PRIMARY KEY COMMENT '路线ID',
  `image` VARCHAR(255) NOT NULL COMMENT '封面图',
  `name` VARCHAR(100) NOT NULL COMMENT '路线名称',
  `location` VARCHAR(100) NOT NULL COMMENT '位置描述',
  `ip` VARCHAR(45) NOT NULL COMMENT '用于定位的IP',
  `difficulty` ENUM('easy','moderate','hard') NOT NULL COMMENT '难度枚举',
  `difficulty_label` VARCHAR(10) NOT NULL COMMENT '难度中文',
  `pack_type` ENUM('light','heavy','both') NOT NULL COMMENT '轻装/重装/皆可',
  `duration_type` ENUM('single_day','multi_day') NOT NULL COMMENT '单日/多日',
  `rating` DECIMAL(2,1) NOT NULL DEFAULT 0 COMMENT '评分',
  `review_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `distance` VARCHAR(20) NOT NULL COMMENT '距离',
  `elevation` VARCHAR(20) NOT NULL COMMENT '海拔/爬升',
  `duration` VARCHAR(20) NOT NULL COMMENT '耗时',
  `description` TEXT NOT NULL COMMENT '路线介绍',
  `favorites` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `author_id` BIGINT NOT NULL COMMENT '发布者ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_trails_author_created` (`author_id`, `created_at`),
  FOREIGN KEY (`author_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线表';

-- 标签表
CREATE TABLE `tags` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(20) NOT NULL UNIQUE COMMENT '标签名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 路线-标签关联表
CREATE TABLE `trail_tags` (
  `trail_id` BIGINT NOT NULL COMMENT '路线ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`trail_id`, `tag_id`),
  FOREIGN KEY (`trail_id`) REFERENCES `trails`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`tag_id`) REFERENCES `tags`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线标签关联表';

-- 路线图片表
CREATE TABLE `trail_images` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '路线图片ID',
  `trail_id` BIGINT NOT NULL COMMENT '路线ID',
  `image` VARCHAR(255) NOT NULL COMMENT '图片地址',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`trail_id`) REFERENCES `trails`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线图片表';

-- 路线收藏明细
CREATE TABLE `trail_favorites` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
  `trail_id` BIGINT NOT NULL COMMENT '路线ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_trail_fav` (`trail_id`, `user_id`),
  KEY `idx_trail_favorites_user_created` (`user_id`, `created_at`),
  FOREIGN KEY (`trail_id`) REFERENCES `trails`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线收藏表';

-- 路线点赞明细
CREATE TABLE `trail_likes` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
  `trail_id` BIGINT NOT NULL COMMENT '路线ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_trail_like` (`trail_id`, `user_id`),
  FOREIGN KEY (`trail_id`) REFERENCES `trails`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线点赞表';

-- 评论表（支持多级回复）
CREATE TABLE `reviews` (
  `id` BIGINT PRIMARY KEY COMMENT '评论ID',
  `trail_id` BIGINT COMMENT '路线ID（顶级评论有，回复可为空）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID',
  `rating` TINYINT COMMENT '评分（仅顶级评论）',
  `time_label` VARCHAR(20) NOT NULL COMMENT '时间文案（如：3天前）',
  `text` TEXT NOT NULL COMMENT '评论内容',
  `reply_to` VARCHAR(50) COMMENT '回复对象用户名',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_reviews_trail_parent_created` (`trail_id`, `parent_id`, `created_at`),
  FOREIGN KEY (`trail_id`) REFERENCES `trails`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`parent_id`) REFERENCES `reviews`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 评论图片表
CREATE TABLE `review_images` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论图片ID',
  `review_id` BIGINT NOT NULL COMMENT '评论ID',
  `image` VARCHAR(255) NOT NULL COMMENT '图片地址',
  FOREIGN KEY (`review_id`) REFERENCES `reviews`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论图片表';

-- AI 会话表
CREATE TABLE `ai_conversations` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '会话标题',
  `model` VARCHAR(50) NOT NULL COMMENT '模型名称',
  `status` ENUM('active','archived') NOT NULL DEFAULT 'active' COMMENT '会话状态',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话表';

-- AI 消息表
CREATE TABLE `ai_messages` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `role` ENUM('user','assistant','system') NOT NULL COMMENT '消息角色',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `tokens` INT DEFAULT 0 COMMENT '估算token数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_ai_messages_conversation_created` (`conversation_id`, `created_at`),
  FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversations`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 消息表';
```

## 典型查询

```sql
-- 1) 我的发布
SELECT *
FROM `trails`
WHERE `author_id` = 101
ORDER BY `created_at` DESC;

-- 2) 我的收藏
SELECT t.*
FROM `trail_favorites` tf
JOIN `trails` t ON t.id = tf.trail_id
WHERE tf.user_id = 101
ORDER BY tf.created_at DESC;

-- 3) 社区页（路线 + 作者）
SELECT
  t.*,
  u.username,
  u.avatar,
  u.avatar_bg
FROM `trails` t
JOIN `users` u ON u.id = t.author_id
ORDER BY t.created_at DESC;

-- 4) 某条路线的顶级评论
SELECT
  r.*,
  u.username,
  u.avatar,
  u.avatar_bg
FROM `reviews` r
JOIN `users` u ON u.id = r.user_id
WHERE r.trail_id = 1 AND r.parent_id IS NULL
ORDER BY r.created_at DESC;

-- 5) 某条评论的回复
SELECT
  r.*,
  u.username,
  u.avatar,
  u.avatar_bg
FROM `reviews` r
JOIN `users` u ON u.id = r.user_id
WHERE r.parent_id = 1
ORDER BY r.created_at ASC;

-- 6) 某个 AI 会话的消息列表
SELECT *
FROM `ai_messages`
WHERE `conversation_id` = 1
ORDER BY `created_at` ASC;
```
