ALTER TABLE users
    ADD COLUMN status ENUM('active','banned','deleted') NOT NULL DEFAULT 'active' COMMENT '账号状态' AFTER role,
    ADD COLUMN ban_reason VARCHAR(255) NULL COMMENT '封禁原因' AFTER status,
    ADD COLUMN banned_by BIGINT NULL COMMENT '封禁管理员ID' AFTER ban_reason,
    ADD COLUMN banned_at DATETIME NULL COMMENT '封禁时间' AFTER banned_by,
    ADD COLUMN deleted_by BIGINT NULL COMMENT '软删除管理员ID' AFTER banned_at,
    ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间' AFTER deleted_by,
    ADD KEY idx_users_status_created (status, created_at),
    ADD KEY idx_users_banned_by (banned_by),
    ADD KEY idx_users_deleted_by (deleted_by),
    ADD CONSTRAINT users_ibfk_banned_by FOREIGN KEY (banned_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT users_ibfk_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE TABLE admin_operation_logs (
    id BIGINT NOT NULL COMMENT '主键ID',
    action_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(64) NOT NULL COMMENT '操作对象类型',
    target_id BIGINT NOT NULL COMMENT '操作对象ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    remark VARCHAR(255) NULL COMMENT '操作备注',
    metadata_json JSON NULL COMMENT '扩展元数据',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_admin_logs_target (target_type, target_id, created_at),
    KEY idx_admin_logs_operator (operator_id, created_at),
    CONSTRAINT admin_logs_ibfk_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE RESTRICT
) COMMENT='后台操作日志';
