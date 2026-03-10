# MySQL 建表（含字段注释）

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
  `publish_time` VARCHAR(20) NOT NULL COMMENT '发布时间文案',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
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
```
