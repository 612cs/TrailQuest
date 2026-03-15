/*
 Navicat Premium Dump SQL

 Source Server         : hiking
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : hikingDB

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 15/03/2026 23:14:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_conversations
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversations`;
CREATE TABLE `ai_conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '会话标题',
  `model` varchar(50) NOT NULL COMMENT '模型名称',
  `status` enum('active','archived') NOT NULL DEFAULT 'active' COMMENT '会话状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `ai_conversations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 会话表';

-- ----------------------------
-- Records of ai_conversations
-- ----------------------------
BEGIN;
INSERT INTO `ai_conversations` (`id`, `user_id`, `title`, `model`, `status`, `created_at`, `updated_at`) VALUES (1, 101, '杭州周边徒步推荐', 'gpt-4.1', 'active', '2026-03-12 19:00:00', '2026-03-12 19:03:00');
INSERT INTO `ai_conversations` (`id`, `user_id`, `title`, `model`, `status`, `created_at`, `updated_at`) VALUES (2, 102, '一日轻装线路规划', 'gpt-4.1', 'active', '2026-03-11 18:00:00', '2026-03-11 18:02:00');
COMMIT;

-- ----------------------------
-- Table structure for ai_messages
-- ----------------------------
DROP TABLE IF EXISTS `ai_messages`;
CREATE TABLE `ai_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `role` enum('user','assistant','system') NOT NULL COMMENT '消息角色',
  `content` text NOT NULL COMMENT '消息内容',
  `tokens` int DEFAULT '0' COMMENT '估算token数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_messages_conversation_created` (`conversation_id`,`created_at`),
  CONSTRAINT `ai_messages_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 消息表';

-- ----------------------------
-- Records of ai_messages
-- ----------------------------
BEGIN;
INSERT INTO `ai_messages` (`id`, `conversation_id`, `role`, `content`, `tokens`, `created_at`) VALUES (1, 1, 'user', '帮我推荐杭州周边适合周末的一日徒步路线', 56, '2026-03-12 19:00:30');
INSERT INTO `ai_messages` (`id`, `conversation_id`, `role`, `content`, `tokens`, `created_at`) VALUES (2, 1, 'assistant', '推荐你去临安附近的老鹰峰顶，路线适中，风景好，周末人也不会太多。', 84, '2026-03-12 19:01:10');
INSERT INTO `ai_messages` (`id`, `conversation_id`, `role`, `content`, `tokens`, `created_at`) VALUES (3, 2, 'user', '大理附近轻装一日线路有哪些？', 42, '2026-03-11 18:00:20');
INSERT INTO `ai_messages` (`id`, `conversation_id`, `role`, `content`, `tokens`, `created_at`) VALUES (4, 2, 'assistant', '镜湖环线比较适合轻装一日行程，路况平缓，适合拍照与休闲。', 76, '2026-03-11 18:01:00');
COMMIT;

-- ----------------------------
-- Table structure for review_images
-- ----------------------------
DROP TABLE IF EXISTS `review_images`;
CREATE TABLE `review_images` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论图片ID',
  `review_id` bigint NOT NULL COMMENT '评论ID',
  `image` varchar(255) NOT NULL COMMENT '图片地址',
  PRIMARY KEY (`id`),
  KEY `review_id` (`review_id`),
  CONSTRAINT `review_images_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论图片表';

-- ----------------------------
-- Records of review_images
-- ----------------------------
BEGIN;
INSERT INTO `review_images` (`id`, `review_id`, `image`) VALUES (1, 1, '/trail-pine.png');
INSERT INTO `review_images` (`id`, `review_id`, `image`) VALUES (2, 1, '/trail-lake.png');
COMMIT;

-- ----------------------------
-- Table structure for reviews
-- ----------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL COMMENT '评论ID',
  `trail_id` bigint DEFAULT NULL COMMENT '路线ID（顶级评论有，回复可为空）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
  `rating` tinyint DEFAULT NULL COMMENT '评分（仅顶级评论）',
  `time_label` varchar(20) NOT NULL COMMENT '时间文案（如：3天前）',
  `text` text NOT NULL COMMENT '评论内容',
  `reply_to` varchar(50) DEFAULT NULL COMMENT '回复对象用户名',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_reviews_trail_parent_created` (`trail_id`,`parent_id`,`created_at`),
  KEY `user_id` (`user_id`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

-- ----------------------------
-- Records of reviews
-- ----------------------------
BEGIN;
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (1, 1, 101, NULL, 5, '1 周前', '壮观的景色！山顶的日出令人叹为观止。', NULL, '2026-03-05 08:00:00');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (101, 1, 104, 1, NULL, '6 天前', '同意！日出真的太美了。', 'Sarah M.', '2026-03-06 09:30:00');
COMMIT;

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(20) NOT NULL COMMENT '标签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

-- ----------------------------
-- Records of tags
-- ----------------------------
BEGIN;
INSERT INTO `tags` (`id`, `name`) VALUES (6, '休闲');
INSERT INTO `tags` (`id`, `name`) VALUES (5, '家庭');
INSERT INTO `tags` (`id`, `name`) VALUES (2, '山顶');
INSERT INTO `tags` (`id`, `name`) VALUES (3, '摄影');
INSERT INTO `tags` (`id`, `name`) VALUES (1, '日出');
INSERT INTO `tags` (`id`, `name`) VALUES (4, '湖泊');
COMMIT;

-- ----------------------------
-- Table structure for trail_favorites
-- ----------------------------
DROP TABLE IF EXISTS `trail_favorites`;
CREATE TABLE `trail_favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trail_fav` (`trail_id`,`user_id`),
  KEY `idx_trail_favorites_user_created` (`user_id`,`created_at`),
  CONSTRAINT `trail_favorites_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `trail_favorites_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线收藏表';

-- ----------------------------
-- Records of trail_favorites
-- ----------------------------
BEGIN;
INSERT INTO `trail_favorites` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (1, 1, 101, '2026-03-12 18:30:00');
INSERT INTO `trail_favorites` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (2, 1, 102, '2026-03-11 20:00:00');
INSERT INTO `trail_favorites` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (3, 2, 104, '2026-03-10 21:00:00');
COMMIT;

-- ----------------------------
-- Table structure for trail_images
-- ----------------------------
DROP TABLE IF EXISTS `trail_images`;
CREATE TABLE `trail_images` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '路线图片ID',
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `image` varchar(255) NOT NULL COMMENT '图片地址',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `trail_id` (`trail_id`),
  CONSTRAINT `trail_images_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线图片表';

-- ----------------------------
-- Records of trail_images
-- ----------------------------
BEGIN;
INSERT INTO `trail_images` (`id`, `trail_id`, `image`, `sort_order`, `created_at`) VALUES (1, 1, '/trail-pine.png', 1, '2026-03-12 15:58:38');
INSERT INTO `trail_images` (`id`, `trail_id`, `image`, `sort_order`, `created_at`) VALUES (2, 1, '/trail-foggy.png', 2, '2026-03-12 15:58:38');
INSERT INTO `trail_images` (`id`, `trail_id`, `image`, `sort_order`, `created_at`) VALUES (3, 2, '/trail-lake.png', 1, '2026-03-12 15:58:38');
INSERT INTO `trail_images` (`id`, `trail_id`, `image`, `sort_order`, `created_at`) VALUES (4, 3, '/trail-foggy.png', 1, '2026-03-12 15:58:38');
COMMIT;

-- ----------------------------
-- Table structure for trail_likes
-- ----------------------------
DROP TABLE IF EXISTS `trail_likes`;
CREATE TABLE `trail_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trail_like` (`trail_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `trail_likes_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `trail_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线点赞表';

-- ----------------------------
-- Records of trail_likes
-- ----------------------------
BEGIN;
INSERT INTO `trail_likes` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (1, 1, 101, '2026-03-12 15:58:38');
INSERT INTO `trail_likes` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (2, 1, 102, '2026-03-12 15:58:38');
INSERT INTO `trail_likes` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (3, 1, 104, '2026-03-12 15:58:38');
INSERT INTO `trail_likes` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (4, 2, 102, '2026-03-12 15:58:38');
INSERT INTO `trail_likes` (`id`, `trail_id`, `user_id`, `created_at`) VALUES (5, 3, 104, '2026-03-12 15:58:38');
COMMIT;

-- ----------------------------
-- Table structure for trail_tags
-- ----------------------------
DROP TABLE IF EXISTS `trail_tags`;
CREATE TABLE `trail_tags` (
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`trail_id`,`tag_id`),
  KEY `tag_id` (`tag_id`),
  CONSTRAINT `trail_tags_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `trail_tags_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线标签关联表';

-- ----------------------------
-- Records of trail_tags
-- ----------------------------
BEGIN;
INSERT INTO `trail_tags` (`trail_id`, `tag_id`) VALUES (1, 1);
INSERT INTO `trail_tags` (`trail_id`, `tag_id`) VALUES (1, 2);
INSERT INTO `trail_tags` (`trail_id`, `tag_id`) VALUES (1, 3);
INSERT INTO `trail_tags` (`trail_id`, `tag_id`) VALUES (2, 4);
INSERT INTO `trail_tags` (`trail_id`, `tag_id`) VALUES (2, 5);
INSERT INTO `trail_tags` (`trail_id`, `tag_id`) VALUES (2, 6);
COMMIT;

-- ----------------------------
-- Table structure for trails
-- ----------------------------
DROP TABLE IF EXISTS `trails`;
CREATE TABLE `trails` (
  `id` bigint NOT NULL COMMENT '路线ID',
  `image` varchar(255) NOT NULL COMMENT '封面图',
  `name` varchar(100) NOT NULL COMMENT '路线名称',
  `location` varchar(100) NOT NULL COMMENT '位置描述',
  `ip` varchar(45) NOT NULL COMMENT '用于定位的IP',
  `difficulty` enum('easy','moderate','hard') NOT NULL COMMENT '难度枚举',
  `difficulty_label` varchar(10) NOT NULL COMMENT '难度中文',
  `pack_type` enum('light','heavy','both') NOT NULL COMMENT '轻装/重装/皆可',
  `duration_type` enum('single_day','multi_day') NOT NULL COMMENT '单日/多日',
  `rating` decimal(2,1) NOT NULL DEFAULT '0.0' COMMENT '评分',
  `review_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `distance` varchar(20) NOT NULL COMMENT '距离',
  `elevation` varchar(20) NOT NULL COMMENT '海拔/爬升',
  `duration` varchar(20) NOT NULL COMMENT '耗时',
  `description` text NOT NULL COMMENT '路线介绍',
  `favorites` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `likes` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `author_id` bigint NOT NULL COMMENT '发布者ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_trails_author_created` (`author_id`,`created_at`),
  CONSTRAINT `trails_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线表';

-- ----------------------------
-- Records of trails
-- ----------------------------
BEGIN;
INSERT INTO `trails` (`id`, `image`, `name`, `location`, `ip`, `difficulty`, `difficulty_label`, `pack_type`, `duration_type`, `rating`, `review_count`, `distance`, `elevation`, `duration`, `description`, `favorites`, `likes`, `author_id`, `created_at`, `updated_at`) VALUES (1, '/trail-pine.png', '老鹰峰顶', '浙江 杭州 临安', '121.41.32.100', 'moderate', '适中', 'light', 'single_day', 4.9, 1200, '6.4 km', '+420 m', '3h 15m', '老鹰峰顶是临安地区最受欢迎的徒步路线之一，沿途可欣赏壮丽的山谷景色。', 3842, 1256, 101, '2026-03-12 16:00:00', '2026-03-12 15:58:38');
INSERT INTO `trails` (`id`, `image`, `name`, `location`, `ip`, `difficulty`, `difficulty_label`, `pack_type`, `duration_type`, `rating`, `review_count`, `distance`, `elevation`, `duration`, `description`, `favorites`, `likes`, `author_id`, `created_at`, `updated_at`) VALUES (2, '/trail-lake.png', '镜湖环线', '云南 大理 苍山', '222.88.90.10', 'easy', '简单', 'light', 'single_day', 4.7, 856, '3.4 km', '+50 m', '45m', '轻松的环湖步道，适合全家出行。湖水清澈见底，四周被苍翠的山林环绕。', 5126, 2430, 102, '2026-03-11 12:00:00', '2026-03-12 15:58:38');
INSERT INTO `trails` (`id`, `image`, `name`, `location`, `ip`, `difficulty`, `difficulty_label`, `pack_type`, `duration_type`, `rating`, `review_count`, `distance`, `elevation`, `duration`, `description`, `favorites`, `likes`, `author_id`, `created_at`, `updated_at`) VALUES (3, '/trail-foggy.png', '布莱克伍德峡谷', '四川 雅安 牛背山', '118.125.88.20', 'hard', '困难', 'heavy', 'multi_day', 4.8, 2400, '18.0 km', '+1,200 m', '6h 20m', '极具挑战性的峡谷穿越路线，需要较好的体力和经验。', 8930, 5620, 104, '2026-03-10 09:00:00', '2026-03-12 15:58:38');
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `avatar` varchar(16) NOT NULL COMMENT '头像缩写',
  `avatar_bg` varchar(16) NOT NULL COMMENT '头像背景色',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `email`, `password_hash`, `role`, `created_at`) VALUES (101, 'Sarah M.', 'SM', '#8b5cf6', 'sarah@example.com', '$2a$10$XQ4J0sWlD0pW3Gm0k0M8Fu2u92G/Ik2Vq6KYFAsktwVDqveufqdp2', 'USER', '2026-03-12 15:58:38');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `email`, `password_hash`, `role`, `created_at`) VALUES (102, '云游者', 'YY', '#0891b2', 'yunyou@example.com', '$2a$10$XQ4J0sWlD0pW3Gm0k0M8Fu2u92G/Ik2Vq6KYFAsktwVDqveufqdp2', 'ADMIN', '2026-03-12 15:58:38');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `email`, `password_hash`, `role`, `created_at`) VALUES (104, '@hiking_queen', 'HQ', '#8b5cf6', 'queen@example.com', '$2a$10$XQ4J0sWlD0pW3Gm0k0M8Fu2u92G/Ik2Vq6KYFAsktwVDqveufqdp2', 'USER', '2026-03-12 15:58:38');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `email`, `password_hash`, `role`, `created_at`) VALUES (2033122436212338690, '猪猪侠', '猪猪', '#ea580c', 'admin@qq.com', '$2a$10$tmpYeUZ8Fm5L.7SErKaz8OljlRb/bjid38pOAZp2YY3AEqujWm5Zi', 'USER', '2026-03-15 10:05:37');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
