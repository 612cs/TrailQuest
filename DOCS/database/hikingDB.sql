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

 Date: 24/03/2026 20:16:15
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
-- Table structure for ai_messages
-- ----------------------------
DROP TABLE IF EXISTS `ai_messages`;
CREATE TABLE `ai_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `role` enum('user','assistant','system') NOT NULL COMMENT '消息角色',
  `content` text NOT NULL COMMENT '消息内容',
  `tokens` int DEFAULT '0' COMMENT '估算token数',
  `metadata_json` json DEFAULT NULL COMMENT '结构化消息元数据',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_messages_conversation_created` (`conversation_id`,`created_at`),
  CONSTRAINT `ai_messages_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 消息表';

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
  `biz_type` enum('avatar','trail_cover','trail_gallery','trail_track','review') NOT NULL COMMENT '业务类型',
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论图片表';

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
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(20) NOT NULL COMMENT '标签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线收藏表';

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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线图片表';

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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线点赞表';

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
-- Table structure for trail_tracks
-- ----------------------------
DROP TABLE IF EXISTS `trail_tracks`;
CREATE TABLE `trail_tracks` (
  `id` bigint NOT NULL COMMENT '轨迹记录ID',
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `media_file_id` bigint NOT NULL COMMENT '原始轨迹文件ID',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `source_format` enum('gpx','kml') NOT NULL COMMENT '轨迹来源格式',
  `original_file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `track_geojson` json NOT NULL COMMENT '标准化轨迹 GeoJSON',
  `track_points_count` int NOT NULL DEFAULT '0' COMMENT '轨迹点数量',
  `waypoint_count` int NOT NULL DEFAULT '0' COMMENT '航点数量',
  `start_lng` decimal(10,6) DEFAULT NULL COMMENT '起点经度',
  `start_lat` decimal(10,6) DEFAULT NULL COMMENT '起点纬度',
  `end_lng` decimal(10,6) DEFAULT NULL COMMENT '终点经度',
  `end_lat` decimal(10,6) DEFAULT NULL COMMENT '终点纬度',
  `bbox_min_lng` decimal(10,6) DEFAULT NULL COMMENT '边界框最小经度',
  `bbox_min_lat` decimal(10,6) DEFAULT NULL COMMENT '边界框最小纬度',
  `bbox_max_lng` decimal(10,6) DEFAULT NULL COMMENT '边界框最大经度',
  `bbox_max_lat` decimal(10,6) DEFAULT NULL COMMENT '边界框最大纬度',
  `distance_meters` decimal(12,2) DEFAULT NULL COMMENT '总距离(米)',
  `elevation_min_meters` decimal(12,2) DEFAULT NULL COMMENT '最低海拔(米)',
  `elevation_peak_meters` decimal(12,2) DEFAULT NULL COMMENT '最高海拔(米)',
  `elevation_gain_meters` decimal(12,2) DEFAULT NULL COMMENT '累计爬升(米)',
  `elevation_loss_meters` decimal(12,2) DEFAULT NULL COMMENT '累计下降(米)',
  `duration_seconds` bigint DEFAULT NULL COMMENT '轨迹时长(秒)',
  `status` enum('parsed','parse_failed') NOT NULL DEFAULT 'parsed' COMMENT '解析状态',
  `parse_error_message` varchar(255) DEFAULT NULL COMMENT '解析失败信息',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trail_tracks_trail_id` (`trail_id`),
  KEY `idx_trail_tracks_user_created` (`user_id`,`created_at`),
  KEY `idx_trail_tracks_media` (`media_file_id`),
  CONSTRAINT `trail_tracks_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `trail_tracks_ibfk_2` FOREIGN KEY (`media_file_id`) REFERENCES `media_files` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `trail_tracks_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线轨迹解析表';

-- ----------------------------
-- Table structure for landscape_feature_snapshots
-- ----------------------------
DROP TABLE IF EXISTS `landscape_feature_snapshots`;
CREATE TABLE `landscape_feature_snapshots` (
  `id` bigint NOT NULL COMMENT '特征快照ID',
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `prediction_date` date NOT NULL COMMENT '预测日期',
  `landscape_type` enum('cloud_sea','rime','icicle') NOT NULL COMMENT '景观类型',
  `feature_version` varchar(32) NOT NULL DEFAULT 'v1' COMMENT '特征版本',
  `feature_payload` json NOT NULL COMMENT '特征向量快照',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_landscape_feature_trail_date` (`trail_id`,`prediction_date`),
  KEY `idx_landscape_feature_type_date` (`landscape_type`,`prediction_date`),
  CONSTRAINT `landscape_feature_snapshots_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='景观预测训练/调试特征快照表';

-- ----------------------------
-- Table structure for landscape_observation_labels
-- ----------------------------
DROP TABLE IF EXISTS `landscape_observation_labels`;
CREATE TABLE `landscape_observation_labels` (
  `id` bigint NOT NULL COMMENT '景观标签ID',
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `user_id` bigint DEFAULT NULL COMMENT '标注用户ID',
  `observation_date` date NOT NULL COMMENT '观测日期',
  `landscape_type` enum('cloud_sea','rime','icicle','sunrise_sunset','milky_way') NOT NULL COMMENT '景观类型',
  `observed` tinyint(1) NOT NULL COMMENT '是否观测到',
  `confidence` decimal(4,2) DEFAULT NULL COMMENT '人工标注置信度',
  `evidence` json DEFAULT NULL COMMENT '图片、备注等证据',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_landscape_label_trail_date` (`trail_id`,`observation_date`),
  KEY `idx_landscape_label_type_date` (`landscape_type`,`observation_date`),
  KEY `idx_landscape_label_user` (`user_id`),
  CONSTRAINT `landscape_observation_labels_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `landscape_observation_labels_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='景观观测人工标签表';

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
  `start_lng` decimal(10,6) DEFAULT NULL COMMENT '路线起点经度',
  `start_lat` decimal(10,6) DEFAULT NULL COMMENT '路线起点纬度',
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
  `status` enum('active','deleted') NOT NULL DEFAULT 'active' COMMENT '路线状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_trails_author_created` (`author_id`,`created_at`),
  KEY `idx_trails_status_created` (`status`,`created_at`),
  CONSTRAINT `trails_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线表';

-- ----------------------------
-- Table structure for upload_checkpoints
-- ----------------------------
DROP TABLE IF EXISTS `upload_checkpoints`;
CREATE TABLE `upload_checkpoints` (
  `id` bigint NOT NULL COMMENT '断点记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `biz_type` enum('avatar','trail_cover','trail_gallery','trail_track','review') NOT NULL COMMENT '业务类型',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小（字节）',
  `object_key` varchar(255) NOT NULL COMMENT 'OSS 对象Key',
  `upload_id` varchar(200) NOT NULL COMMENT 'OSS 分片上传ID',
  `done_parts_json` json DEFAULT NULL COMMENT '已完成分片信息（JSON）',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_checkpoint_user_biz_file` (`user_id`,`biz_type`,`file_name`),
  KEY `idx_upload_checkpoint_expires` (`expires_at`),
  KEY `idx_upload_checkpoint_object_key` (`object_key`),
  CONSTRAINT `upload_checkpoints_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OSS 上传断点续传记录表';

-- ----------------------------
-- Table structure for user_hiking_profiles
-- ----------------------------
DROP TABLE IF EXISTS `user_hiking_profiles`;
CREATE TABLE `user_hiking_profiles` (
  `id` bigint NOT NULL COMMENT '画像ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `experience_level` enum('beginner','intermediate','expert') NOT NULL COMMENT '徒步经验等级',
  `trail_style` enum('city_weekend','long_distance','both') NOT NULL COMMENT '常走路线类型',
  `pack_preference` enum('light','heavy','both') NOT NULL COMMENT '负重偏好',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_hiking_profiles_user_id` (`user_id`),
  CONSTRAINT `user_hiking_profiles_ibfk_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户徒步画像表';

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
  `bio` varchar(300) DEFAULT NULL COMMENT '个人简介',
  `location` varchar(100) DEFAULT NULL COMMENT '所在地',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_avatar_media_id` (`avatar_media_id`),
  CONSTRAINT `users_ibfk_avatar_media` FOREIGN KEY (`avatar_media_id`) REFERENCES `media_files` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

SET FOREIGN_KEY_CHECKS = 1;
