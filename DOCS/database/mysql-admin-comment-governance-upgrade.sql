ALTER TABLE reviews
    ADD COLUMN status ENUM('active','hidden','deleted') NOT NULL DEFAULT 'active' COMMENT '评论治理状态' AFTER reply_to,
    ADD COLUMN moderation_reason VARCHAR(255) NULL COMMENT '治理原因' AFTER status,
    ADD COLUMN moderated_by BIGINT NULL COMMENT '最后一次治理管理员ID' AFTER moderation_reason,
    ADD COLUMN moderated_at DATETIME NULL COMMENT '最后一次治理时间' AFTER moderated_by,
    ADD KEY idx_reviews_status_created (status, created_at),
    ADD KEY idx_reviews_moderated_by (moderated_by),
    ADD CONSTRAINT reviews_ibfk_moderated_by FOREIGN KEY (moderated_by) REFERENCES users(id) ON DELETE SET NULL;

UPDATE reviews
SET status = 'active'
WHERE status IS NULL;
