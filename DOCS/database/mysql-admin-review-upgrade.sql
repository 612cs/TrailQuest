ALTER TABLE trails
    ADD COLUMN review_status ENUM('pending','approved','rejected') NOT NULL DEFAULT 'approved' COMMENT '审核状态' AFTER status,
    ADD COLUMN review_remark VARCHAR(255) NULL COMMENT '审核备注' AFTER review_status,
    ADD COLUMN reviewed_by BIGINT NULL COMMENT '审核管理员ID' AFTER review_remark,
    ADD COLUMN reviewed_at DATETIME NULL COMMENT '审核时间' AFTER reviewed_by,
    ADD KEY idx_trails_review_status_created (review_status, created_at),
    ADD KEY idx_trails_reviewed_by (reviewed_by),
    ADD CONSTRAINT trails_ibfk_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL;
