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

 Date: 16/03/2026 20:49:17
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
-- Table structure for media_files
-- ----------------------------
DROP TABLE IF EXISTS `media_files`;
CREATE TABLE `media_files` (
  `id` bigint NOT NULL COMMENT '媒体文件ID',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `storage_provider` enum('aliyun_oss') NOT NULL DEFAULT 'aliyun_oss' COMMENT '存储服务提供商',
  `bucket_name` varchar(100) NOT NULL COMMENT 'OSS Bucket 名称',
  `object_key` varchar(255) NOT NULL COMMENT 'OSS 对象Key',
  `url` varchar(500) NOT NULL COMMENT '文件访问地址',
  `biz_type` enum('avatar','trail_cover','trail_gallery','review') NOT NULL COMMENT '业务类型',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `mime_type` varchar(100) NOT NULL COMMENT '文件MIME类型',
  `size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小（字节）',
  `width` int DEFAULT NULL COMMENT '图片宽度',
  `height` int DEFAULT NULL COMMENT '图片高度',
  `status` enum('uploaded','active','deleted') NOT NULL DEFAULT 'active' COMMENT '文件状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_media_bucket_object` (`bucket_name`,`object_key`),
  KEY `idx_media_user_biz_created` (`user_id`,`biz_type`,`created_at`),
  KEY `idx_media_biz_status_created` (`biz_type`,`status`,`created_at`),
  CONSTRAINT `media_files_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用媒体文件表';

-- ----------------------------
-- Records of media_files
-- ----------------------------
BEGIN;
INSERT INTO `media_files` (`id`, `user_id`, `storage_provider`, `bucket_name`, `object_key`, `url`, `biz_type`, `original_name`, `extension`, `mime_type`, `size`, `width`, `height`, `status`, `created_at`, `updated_at`) VALUES (2033216883554000897, 2033210514809671681, 'aliyun_oss', 'trailquest-prod-media', 'avatar/2033210514809671681/2026/03/16ca1ca7e71c4b81be3b5a671442566d.png', 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/avatar/2033210514809671681/2026/03/16ca1ca7e71c4b81be3b5a671442566d.png', 'avatar', 'trail-pine.png', 'png', 'image/png', 1023422, 1200, 800, 'active', '2026-03-15 16:20:55', '2026-03-15 16:20:55');
INSERT INTO `media_files` (`id`, `user_id`, `storage_provider`, `bucket_name`, `object_key`, `url`, `biz_type`, `original_name`, `extension`, `mime_type`, `size`, `width`, `height`, `status`, `created_at`, `updated_at`) VALUES (2033217283673841666, 2033122436212338690, 'aliyun_oss', 'trailquest-prod-media', 'trail_cover/2033122436212338690/2026/03/29665af0ed5c4c089dbac68c6f61a5a1.png', 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/trail_cover/2033122436212338690/2026/03/29665af0ed5c4c089dbac68c6f61a5a1.png', 'trail_cover', '把腿张开_-lowres__bad_anatomy__bad_hands__extra_limbs__mutated__deformed__blurry__worst_quality__low_quality__watermark__text__signature__ugly__poorly_drawn_face__extra_fingers__fus_1276895851.png', 'png', 'image/png', 732303, 1024, 576, 'active', '2026-03-15 16:22:30', '2026-03-15 16:22:30');
INSERT INTO `media_files` (`id`, `user_id`, `storage_provider`, `bucket_name`, `object_key`, `url`, `biz_type`, `original_name`, `extension`, `mime_type`, `size`, `width`, `height`, `status`, `created_at`, `updated_at`) VALUES (2033225575586447361, 2033122436212338690, 'aliyun_oss', 'trailquest-prod-media', 'avatar/2033122436212338690/2026/03/bfa0b77cbf2b4b47a7e8c41b69584232.png', 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/avatar/2033122436212338690/2026/03/bfa0b77cbf2b4b47a7e8c41b69584232.png', 'avatar', 'image3.png', 'png', 'image/png', 1195909, 1024, 1024, 'active', '2026-03-15 16:55:27', '2026-03-15 16:55:27');
INSERT INTO `media_files` (`id`, `user_id`, `storage_provider`, `bucket_name`, `object_key`, `url`, `biz_type`, `original_name`, `extension`, `mime_type`, `size`, `width`, `height`, `status`, `created_at`, `updated_at`) VALUES (2033490707202904065, 2033122436212338690, 'aliyun_oss', 'trailquest-prod-media', 'review/2033122436212338690/2026/03/d313e74eab2f4c13af0be7e717edca6a.png', 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/review/2033122436212338690/2026/03/d313e74eab2f4c13af0be7e717edca6a.png', 'review', '一_主体设定_1_人物描述_根据参考图_年轻女性_五官精致_脸型小巧柔和_整体妆容参考韩系女演员风格_画面呈现深夜卧室随手自拍的自然感与_1276895851(1).png', 'png', 'image/png', 1239135, 1024, 1024, 'active', '2026-03-16 10:28:59', '2026-03-16 10:28:59');
INSERT INTO `media_files` (`id`, `user_id`, `storage_provider`, `bucket_name`, `object_key`, `url`, `biz_type`, `original_name`, `extension`, `mime_type`, `size`, `width`, `height`, `status`, `created_at`, `updated_at`) VALUES (2033490710457683970, 2033122436212338690, 'aliyun_oss', 'trailquest-prod-media', 'review/2033122436212338690/2026/03/6427ae2be0cf48e1b798af61600c7e10.png', 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/review/2033122436212338690/2026/03/6427ae2be0cf48e1b798af61600c7e10.png', 'review', 'image6.png', 'png', 'image/png', 1610818, 768, 1344, 'active', '2026-03-16 10:29:00', '2026-03-16 10:29:00');
INSERT INTO `media_files` (`id`, `user_id`, `storage_provider`, `bucket_name`, `object_key`, `url`, `biz_type`, `original_name`, `extension`, `mime_type`, `size`, `width`, `height`, `status`, `created_at`, `updated_at`) VALUES (2033515323863506946, 2033496679581421570, 'aliyun_oss', 'trailquest-prod-media', 'review/2033496679581421570/2026/03/21daeff024384ee4bb2d2883bc19ce41.png', 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/review/2033496679581421570/2026/03/21daeff024384ee4bb2d2883bc19ce41.png', 'review', '把腿张开_-lowres__bad_anatomy__bad_hands__extra_limbs__mutated__deformed__blurry__worst_quality__low_quality__watermark__text__signature__ugly__poorly_drawn_face__extra_fingers__fus_1276895851.png', 'png', 'image/png', 732303, 1024, 576, 'active', '2026-03-16 12:06:48', '2026-03-16 12:06:48');
INSERT INTO `media_files` (`id`, `user_id`, `storage_provider`, `bucket_name`, `object_key`, `url`, `biz_type`, `original_name`, `extension`, `mime_type`, `size`, `width`, `height`, `status`, `created_at`, `updated_at`) VALUES (2033516264947884034, 2033496679581421570, 'aliyun_oss', 'trailquest-prod-media', 'review/2033496679581421570/2026/03/3fbed628be924f6fa24f082660e0b937.png', 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/review/2033496679581421570/2026/03/3fbed628be924f6fa24f082660e0b937.png', 'review', 'image5.png', 'png', 'image/png', 1204106, 1344, 768, 'active', '2026-03-16 12:10:33', '2026-03-16 12:10:33');
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论图片表';

-- ----------------------------
-- Records of review_images
-- ----------------------------
BEGIN;
INSERT INTO `review_images` (`id`, `review_id`, `image`) VALUES (1, 1, '/trail-pine.png');
INSERT INTO `review_images` (`id`, `review_id`, `image`) VALUES (2, 1, '/trail-lake.png');
INSERT INTO `review_images` (`id`, `review_id`, `image`) VALUES (3, 2033490733455052801, 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/review/2033122436212338690/2026/03/d313e74eab2f4c13af0be7e717edca6a.png');
INSERT INTO `review_images` (`id`, `review_id`, `image`) VALUES (4, 2033490733455052801, 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/review/2033122436212338690/2026/03/6427ae2be0cf48e1b798af61600c7e10.png');
INSERT INTO `review_images` (`id`, `review_id`, `image`) VALUES (5, 2033516281989341186, 'https://trailquest-prod-media.oss-cn-shenzhen.aliyuncs.com/review/2033496679581421570/2026/03/3fbed628be924f6fa24f082660e0b937.png');
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
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033490733455052801, 3, 2033122436212338690, NULL, 5, '刚刚', '很好看', NULL, '2026-03-16 18:29:06');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033514612840898562, 3, 2033496679581421570, NULL, 5, '刚刚', '好得很', NULL, '2026-03-16 20:03:59');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033515837493780481, 3, 2033496679581421570, 2033490733455052801, NULL, '刚刚', '凑凑的', '猪猪侠', '2026-03-16 20:08:51');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033515877381611522, 3, 2033496679581421570, 2033515837493780481, NULL, '刚刚', '你才凑凑的', 'SHENG', '2026-03-16 20:09:01');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033515909140881409, 3, 2033496679581421570, 2033515837493780481, NULL, '刚刚', '凑凑', 'SHENG', '2026-03-16 20:09:08');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033515944826019842, 3, 2033496679581421570, 2033514612840898562, NULL, '刚刚', '有多好', 'SHENG', '2026-03-16 20:09:17');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033516281989341186, 3, 2033496679581421570, 2033515837493780481, NULL, '刚刚', '骚得很', 'SHENG', '2026-03-16 20:10:37');
INSERT INTO `reviews` (`id`, `trail_id`, `user_id`, `parent_id`, `rating`, `time_label`, `text`, `reply_to`, `created_at`) VALUES (2033523400641781762, 2, 2033496679581421570, NULL, 3, '刚刚', '111', NULL, '2026-03-16 20:38:54');
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
INSERT INTO `trails` (`id`, `image`, `name`, `location`, `ip`, `difficulty`, `difficulty_label`, `pack_type`, `duration_type`, `rating`, `review_count`, `distance`, `elevation`, `duration`, `description`, `favorites`, `likes`, `author_id`, `created_at`, `updated_at`) VALUES (2, '/trail-lake.png', '镜湖环线', '云南 大理 苍山', '222.88.90.10', 'easy', '简单', 'light', 'single_day', 3.0, 1, '3.4 km', '+50 m', '45m', '轻松的环湖步道，适合全家出行。湖水清澈见底，四周被苍翠的山林环绕。', 5126, 2430, 102, '2026-03-11 12:00:00', '2026-03-12 15:58:38');
INSERT INTO `trails` (`id`, `image`, `name`, `location`, `ip`, `difficulty`, `difficulty_label`, `pack_type`, `duration_type`, `rating`, `review_count`, `distance`, `elevation`, `duration`, `description`, `favorites`, `likes`, `author_id`, `created_at`, `updated_at`) VALUES (3, '/trail-foggy.png', '布莱克伍德峡谷', '四川 雅安 牛背山', '118.125.88.20', 'hard', '困难', 'heavy', 'multi_day', 5.0, 2, '18.0 km', '+1,200 m', '6h 20m', '极具挑战性的峡谷穿越路线，需要较好的体力和经验。', 8930, 5620, 104, '2026-03-10 09:00:00', '2026-03-12 15:58:38');
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
  `avatar_media_id` bigint DEFAULT NULL COMMENT '头像媒体文件ID',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_avatar_media_id` (`avatar_media_id`),
  CONSTRAINT `users_ibfk_avatar_media` FOREIGN KEY (`avatar_media_id`) REFERENCES `media_files` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `avatar_media_id`, `email`, `password_hash`, `role`, `created_at`) VALUES (101, 'Sarah M.', 'SM', '#8b5cf6', NULL, 'sarah@example.com', '$2a$10$XQ4J0sWlD0pW3Gm0k0M8Fu2u92G/Ik2Vq6KYFAsktwVDqveufqdp2', 'USER', '2026-03-12 15:58:38');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `avatar_media_id`, `email`, `password_hash`, `role`, `created_at`) VALUES (102, '云游者', 'YY', '#0891b2', NULL, 'yunyou@example.com', '$2a$10$XQ4J0sWlD0pW3Gm0k0M8Fu2u92G/Ik2Vq6KYFAsktwVDqveufqdp2', 'ADMIN', '2026-03-12 15:58:38');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `avatar_media_id`, `email`, `password_hash`, `role`, `created_at`) VALUES (104, '@hiking_queen', 'HQ', '#8b5cf6', NULL, 'queen@example.com', '$2a$10$XQ4J0sWlD0pW3Gm0k0M8Fu2u92G/Ik2Vq6KYFAsktwVDqveufqdp2', 'USER', '2026-03-12 15:58:38');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `avatar_media_id`, `email`, `password_hash`, `role`, `created_at`) VALUES (2033122436212338690, '猪猪侠', '猪猪', '#ea580c', 2033225575586447361, 'admin@qq.com', '$2a$10$tmpYeUZ8Fm5L.7SErKaz8OljlRb/bjid38pOAZp2YY3AEqujWm5Zi', 'USER', '2026-03-15 10:05:37');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `avatar_media_id`, `email`, `password_hash`, `role`, `created_at`) VALUES (2033210514809671681, 'oss_test_user', 'OS', '#059669', 2033216883554000897, 'oss-test-20260315@example.com', '$2a$10$npo1CdxmpmY1GugYYCNF6uZbann0a27.ynQoJivY7OJNOmxqKaIWO', 'USER', '2026-03-15 15:55:36');
INSERT INTO `users` (`id`, `username`, `avatar`, `avatar_bg`, `avatar_media_id`, `email`, `password_hash`, `role`, `created_at`) VALUES (2033496679581421570, 'SHENG', 'SH', '#059669', NULL, '2516824318@qq.com', '$2a$10$h9luatk4n5Jpm3lwzcbgaOlc4AXj3.0c0m/zVeVfVHiYzIN3lklqa', 'USER', '2026-03-16 10:52:43');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
