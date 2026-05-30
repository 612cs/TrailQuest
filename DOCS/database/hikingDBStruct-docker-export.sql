-- MySQL dump 10.13  Distrib 8.0.45, for Linux (aarch64)
--
-- Host: localhost    Database: hikingDB
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_operation_logs`
--

DROP TABLE IF EXISTS `admin_operation_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_operation_logs` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `action_type` varchar(64) DEFAULT NULL COMMENT 'æ—§ç‰ˆæ“ä½œç±»åž‹',
  `target_type` varchar(64) NOT NULL COMMENT '操作对象类型',
  `target_id` bigint NOT NULL COMMENT '操作对象ID',
  `target_title` varchar(255) DEFAULT NULL COMMENT 'æ“ä½œå¯¹è±¡æ ‡é¢˜',
  `reason` varchar(500) DEFAULT NULL COMMENT 'æ“ä½œåŽŸå› ',
  `result_status` varchar(32) DEFAULT NULL COMMENT 'ç»“æžœçŠ¶æ€',
  `before_snapshot` json DEFAULT NULL COMMENT 'å˜æ›´å‰å¿«ç…§',
  `after_snapshot` json DEFAULT NULL COMMENT 'å˜æ›´åŽå¿«ç…§',
  `request_id` varchar(64) DEFAULT NULL COMMENT 'è¯·æ±‚ID',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IPåœ°å€',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT 'æ“ä½œäººåç§°',
  `operator_role` varchar(32) DEFAULT NULL COMMENT 'æ“ä½œäººè§’è‰²',
  `module_code` varchar(64) DEFAULT NULL COMMENT 'æ¨¡å—ç¼–ç ',
  `action_code` varchar(64) DEFAULT NULL COMMENT 'åŠ¨ä½œç¼–ç ',
  `remark` varchar(255) DEFAULT NULL COMMENT '操作备注',
  `metadata_json` json DEFAULT NULL COMMENT '扩展元数据',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_logs_target` (`target_type`,`target_id`,`created_at`),
  KEY `idx_admin_logs_operator` (`operator_id`,`created_at`),
  KEY `idx_admin_logs_module_created` (`module_code`,`created_at`),
  KEY `idx_admin_logs_action_created` (`action_code`,`created_at`),
  CONSTRAINT `admin_logs_ibfk_operator` FOREIGN KEY (`operator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_conversations`
--

DROP TABLE IF EXISTS `ai_conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_messages`
--

DROP TABLE IF EXISTS `ai_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `app_settings`
--

DROP TABLE IF EXISTS `app_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_settings` (
  `id` bigint NOT NULL COMMENT '配置ID',
  `setting_key` varchar(100) NOT NULL COMMENT '配置键',
  `setting_value` text COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '配置说明',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_settings_key` (`setting_key`),
  KEY `idx_app_settings_updated_by` (`updated_by`),
  CONSTRAINT `app_settings_ibfk_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点运营配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `landscape_feature_snapshots`
--

DROP TABLE IF EXISTS `landscape_feature_snapshots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `landscape_observation_labels`
--

DROP TABLE IF EXISTS `landscape_observation_labels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `media_files`
--

DROP TABLE IF EXISTS `media_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `media_files` (
  `id` bigint NOT NULL COMMENT '媒体文件ID',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `storage_provider` enum('aliyun_oss') NOT NULL DEFAULT 'aliyun_oss' COMMENT '存储服务提供商',
  `bucket_name` varchar(100) NOT NULL COMMENT 'OSS Bucket 名称',
  `object_key` varchar(255) NOT NULL COMMENT 'OSS 对象Key',
  `url` varchar(500) NOT NULL COMMENT '文件访问地址',
  `biz_type` enum('avatar','home_hero','trail_cover','trail_gallery','trail_track','review') NOT NULL COMMENT '业务类型',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `review_images`
--

DROP TABLE IF EXISTS `review_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_images` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论图片ID',
  `review_id` bigint NOT NULL COMMENT '评论ID',
  `image` varchar(255) NOT NULL COMMENT '图片地址',
  PRIMARY KEY (`id`),
  KEY `review_id` (`review_id`),
  CONSTRAINT `review_images_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论图片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL COMMENT '评论ID',
  `trail_id` bigint DEFAULT NULL COMMENT '路线ID（顶级评论有，回复可为空）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
  `rating` tinyint DEFAULT NULL COMMENT '评分（仅顶级评论）',
  `time_label` varchar(20) NOT NULL COMMENT '时间文案（如：3天前）',
  `text` text NOT NULL COMMENT '评论内容',
  `reply_to` varchar(50) DEFAULT NULL COMMENT '回复对象用户名',
  `status` enum('active','hidden','deleted') NOT NULL DEFAULT 'active' COMMENT 'è¯„è®ºæ²»ç†çŠ¶æ€',
  `moderation_reason` varchar(255) DEFAULT NULL COMMENT 'æ²»ç†åŽŸå› ',
  `moderated_by` bigint DEFAULT NULL COMMENT 'æœ€åŽä¸€æ¬¡æ²»ç†ç®¡ç†å‘˜ID',
  `moderated_at` datetime DEFAULT NULL COMMENT 'æœ€åŽä¸€æ¬¡æ²»ç†æ—¶é—´',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_reviews_trail_parent_created` (`trail_id`,`parent_id`,`created_at`),
  KEY `user_id` (`user_id`),
  KEY `parent_id` (`parent_id`),
  KEY `idx_reviews_status_created` (`status`,`created_at`),
  KEY `idx_reviews_moderated_by` (`moderated_by`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_moderated_by` FOREIGN KEY (`moderated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_option_groups`
--

DROP TABLE IF EXISTS `system_option_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_option_groups` (
  `id` bigint NOT NULL,
  `group_code` varchar(64) NOT NULL,
  `group_name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` enum('active','inactive') NOT NULL DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_option_groups_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_option_items`
--

DROP TABLE IF EXISTS `system_option_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_option_items` (
  `id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_label` varchar(100) NOT NULL,
  `item_sub_label` varchar(100) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `icon_name` varchar(64) DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `is_builtin` tinyint(1) NOT NULL DEFAULT '1',
  `extra_json` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_option_items_group_code` (`group_id`,`item_code`),
  KEY `idx_system_option_items_group_sort` (`group_id`,`sort_order`),
  CONSTRAINT `fk_system_option_items_group` FOREIGN KEY (`group_id`) REFERENCES `system_option_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(20) NOT NULL COMMENT '标签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trail_favorites`
--

DROP TABLE IF EXISTS `trail_favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trail_images`
--

DROP TABLE IF EXISTS `trail_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trail_images` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '路线图片ID',
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `image` varchar(255) NOT NULL COMMENT '图片地址',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `trail_id` (`trail_id`),
  CONSTRAINT `trail_images_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线图片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trail_likes`
--

DROP TABLE IF EXISTS `trail_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trail_tags`
--

DROP TABLE IF EXISTS `trail_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trail_tags` (
  `trail_id` bigint NOT NULL COMMENT '路线ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`trail_id`,`tag_id`),
  KEY `tag_id` (`tag_id`),
  CONSTRAINT `trail_tags_ibfk_1` FOREIGN KEY (`trail_id`) REFERENCES `trails` (`id`) ON DELETE CASCADE,
  CONSTRAINT `trail_tags_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trail_tracks`
--

DROP TABLE IF EXISTS `trail_tracks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trails`
--

DROP TABLE IF EXISTS `trails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trails` (
  `id` bigint NOT NULL COMMENT '路线ID',
  `image` varchar(255) NOT NULL COMMENT '封面图',
  `name` varchar(100) NOT NULL COMMENT '路线名称',
  `location` varchar(100) NOT NULL COMMENT '位置描述',
  `start_lng` decimal(10,6) DEFAULT NULL COMMENT '轨迹起点经度',
  `start_lat` decimal(10,6) DEFAULT NULL COMMENT '轨迹起点纬度',
  `geo_country` varchar(64) DEFAULT NULL COMMENT '结构化地点-国家',
  `geo_province` varchar(64) DEFAULT NULL COMMENT '结构化地点-省',
  `geo_city` varchar(64) DEFAULT NULL COMMENT '结构化地点-市',
  `geo_district` varchar(64) DEFAULT NULL COMMENT '结构化地点-区县',
  `geo_source` varchar(32) DEFAULT NULL COMMENT '结构化地点来源',
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
  `source_type` varchar(32) NOT NULL DEFAULT 'user_upload' COMMENT '来源类型',
  `source_site` varchar(128) DEFAULT NULL COMMENT '来源站点',
  `source_url` varchar(500) DEFAULT NULL COMMENT '来源链接',
  `source_confidence` decimal(4,3) DEFAULT NULL COMMENT '来源可信度',
  `import_batch_no` varchar(64) DEFAULT NULL COMMENT '导入批次号',
  `status` enum('active','deleted') NOT NULL DEFAULT 'active' COMMENT '路线状态',
  `review_status` enum('pending','approved','rejected') NOT NULL DEFAULT 'approved' COMMENT '审核状态',
  `ai_review_status` varchar(32) NOT NULL DEFAULT 'pending' COMMENT 'AI审核状态',
  `ai_review_reason` varchar(500) DEFAULT NULL COMMENT 'AI审核原因',
  `ai_review_risk_level` varchar(32) DEFAULT NULL COMMENT 'AI风险等级',
  `ai_review_categories_json` json DEFAULT NULL COMMENT 'AI风险分类JSON',
  `ai_review_model` varchar(64) DEFAULT NULL COMMENT 'AI审核模型',
  `ai_reviewed_at` datetime DEFAULT NULL COMMENT 'AI审核时间',
  `ai_review_trace_id` varchar(128) DEFAULT NULL COMMENT 'AI审核追踪ID',
  `review_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审核管理员ID',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_trails_author_created` (`author_id`,`created_at`),
  KEY `idx_trails_status_created` (`status`,`created_at`),
  KEY `idx_trails_geo_province` (`geo_province`),
  KEY `idx_trails_geo_city` (`geo_city`),
  KEY `idx_trails_geo_district` (`geo_district`),
  KEY `idx_trails_review_status_created` (`review_status`,`created_at`),
  KEY `idx_trails_reviewed_by` (`reviewed_by`),
  KEY `idx_trails_source_type_created` (`source_type`,`created_at`),
  KEY `idx_trails_ai_review_status_created` (`ai_review_status`,`created_at`),
  KEY `idx_trails_source_site` (`source_site`),
  CONSTRAINT `trails_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
  CONSTRAINT `trails_ibfk_reviewed_by` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='路线表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `upload_checkpoints`
--

DROP TABLE IF EXISTS `upload_checkpoints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `upload_checkpoints` (
  `id` bigint NOT NULL COMMENT '断点记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `biz_type` enum('avatar','home_hero','trail_cover','trail_gallery','trail_track','review') NOT NULL COMMENT '业务类型',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_hiking_profiles`
--

DROP TABLE IF EXISTS `user_hiking_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  `status` enum('active','banned','deleted') NOT NULL DEFAULT 'active' COMMENT '账号状态',
  `ban_reason` varchar(255) DEFAULT NULL COMMENT '封禁原因',
  `banned_by` bigint DEFAULT NULL COMMENT '封禁管理员ID',
  `banned_at` datetime DEFAULT NULL COMMENT '封禁时间',
  `deleted_by` bigint DEFAULT NULL COMMENT '软删除管理员ID',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_avatar_media_id` (`avatar_media_id`),
  KEY `idx_users_status_created` (`status`,`created_at`),
  KEY `idx_users_banned_by` (`banned_by`),
  KEY `idx_users_deleted_by` (`deleted_by`),
  CONSTRAINT `users_ibfk_avatar_media` FOREIGN KEY (`avatar_media_id`) REFERENCES `media_files` (`id`) ON DELETE SET NULL,
  CONSTRAINT `users_ibfk_banned_by` FOREIGN KEY (`banned_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `users_ibfk_deleted_by` FOREIGN KEY (`deleted_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'hikingDB'
--

--
-- Dumping routines for database 'hikingDB'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-30 14:41:28
