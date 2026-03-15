ALTER TABLE `users`
ADD COLUMN `avatar_media_id` bigint DEFAULT NULL COMMENT '头像媒体文件ID' AFTER `avatar_bg`,
ADD KEY `idx_users_avatar_media_id` (`avatar_media_id`);

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

ALTER TABLE `users`
ADD CONSTRAINT `users_ibfk_avatar_media`
  FOREIGN KEY (`avatar_media_id`) REFERENCES `media_files` (`id`) ON DELETE SET NULL;
