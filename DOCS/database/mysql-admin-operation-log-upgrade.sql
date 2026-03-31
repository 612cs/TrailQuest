CREATE TABLE IF NOT EXISTS admin_operation_logs (
    id BIGINT NOT NULL COMMENT '主键ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) NULL COMMENT '操作人名称',
    operator_role VARCHAR(32) NULL COMMENT '操作人角色',
    module_code VARCHAR(64) NULL COMMENT '模块编码',
    action_code VARCHAR(64) NULL COMMENT '动作编码',
    target_type VARCHAR(64) NOT NULL COMMENT '操作对象类型',
    target_id BIGINT NOT NULL COMMENT '操作对象ID',
    target_title VARCHAR(255) NULL COMMENT '操作对象标题',
    reason VARCHAR(500) NULL COMMENT '操作原因',
    result_status VARCHAR(32) NULL COMMENT '结果状态',
    before_snapshot JSON NULL COMMENT '变更前快照',
    after_snapshot JSON NULL COMMENT '变更后快照',
    request_id VARCHAR(64) NULL COMMENT '请求ID',
    ip_address VARCHAR(64) NULL COMMENT 'IP地址',
    user_agent VARCHAR(500) NULL COMMENT 'User-Agent',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_admin_logs_target (target_type, target_id, created_at),
    KEY idx_admin_logs_operator (operator_id, created_at),
    CONSTRAINT admin_logs_ibfk_operator FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE RESTRICT
) COMMENT='后台操作日志';

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'action_type'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN action_type VARCHAR(64) NULL COMMENT '旧版操作类型' AFTER operator_id"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'remark'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN remark VARCHAR(255) NULL COMMENT '旧版备注' AFTER action_type"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'metadata_json'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN metadata_json JSON NULL COMMENT '旧版元数据' AFTER remark"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'operator_name'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN operator_name VARCHAR(100) NULL COMMENT '操作人名称' AFTER operator_id"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'operator_role'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN operator_role VARCHAR(32) NULL COMMENT '操作人角色' AFTER operator_name"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'module_code'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN module_code VARCHAR(64) NULL COMMENT '模块编码' AFTER operator_role"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'action_code'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN action_code VARCHAR(64) NULL COMMENT '动作编码' AFTER module_code"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'target_title'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN target_title VARCHAR(255) NULL COMMENT '操作对象标题' AFTER target_id"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'reason'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN reason VARCHAR(500) NULL COMMENT '操作原因' AFTER target_title"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'result_status'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN result_status VARCHAR(32) NULL COMMENT '结果状态' AFTER reason"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'before_snapshot'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN before_snapshot JSON NULL COMMENT '变更前快照' AFTER result_status"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'after_snapshot'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN after_snapshot JSON NULL COMMENT '变更后快照' AFTER before_snapshot"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'request_id'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN request_id VARCHAR(64) NULL COMMENT '请求ID' AFTER after_snapshot"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'ip_address'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN ip_address VARCHAR(64) NULL COMMENT 'IP地址' AFTER request_id"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND COLUMN_NAME = 'user_agent'
    ),
    'SELECT 1',
    "ALTER TABLE admin_operation_logs ADD COLUMN user_agent VARCHAR(500) NULL COMMENT 'User-Agent' AFTER ip_address"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND INDEX_NAME = 'idx_admin_logs_module_created'
    ),
    'SELECT 1',
    'ALTER TABLE admin_operation_logs ADD KEY idx_admin_logs_module_created (module_code, created_at)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'admin_operation_logs'
          AND INDEX_NAME = 'idx_admin_logs_action_created'
    ),
    'SELECT 1',
    'ALTER TABLE admin_operation_logs ADD KEY idx_admin_logs_action_created (action_code, created_at)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE admin_operation_logs logs
LEFT JOIN users u ON u.id = logs.operator_id
SET
    logs.operator_name = COALESCE(logs.operator_name, u.username),
    logs.operator_role = COALESCE(logs.operator_role, u.role),
    logs.module_code = COALESCE(logs.module_code,
        CASE
            WHEN logs.action_type LIKE 'user.%' THEN 'user_management'
            WHEN logs.action_type LIKE 'trail.approve' OR logs.action_type LIKE 'trail.reject' THEN 'trail_review'
            WHEN logs.action_type LIKE 'trail.offline' OR logs.action_type LIKE 'trail.restore' THEN 'trail_management'
            WHEN logs.action_type LIKE 'review.%' THEN 'review_management'
            WHEN logs.action_type LIKE 'report.%' THEN 'report_management'
            WHEN logs.action_type LIKE 'setting.%' THEN 'setting_management'
            WHEN logs.action_type LIKE 'config.option.%' THEN 'config_center'
            ELSE NULL
        END),
    logs.action_code = COALESCE(logs.action_code,
        CASE logs.action_type
            WHEN 'user.ban' THEN 'user_ban'
            WHEN 'user.unban' THEN 'user_unban'
            WHEN 'trail.approve' THEN 'trail_approve'
            WHEN 'trail.reject' THEN 'trail_reject'
            WHEN 'trail.offline' THEN 'trail_offline'
            WHEN 'trail.restore' THEN 'trail_restore'
            WHEN 'review.hide' THEN 'review_hide'
            WHEN 'review.restore' THEN 'review_restore'
            WHEN 'review.delete' THEN 'review_delete'
            WHEN 'report.resolve' THEN 'report_resolve'
            WHEN 'setting.home_hero.update' THEN 'home_hero_update'
            WHEN 'config.option.create' THEN 'option_item_create'
            WHEN 'config.option.update' THEN 'option_item_update'
            WHEN 'config.option.toggle' THEN 'option_item_toggle'
            ELSE NULL
        END),
    logs.reason = COALESCE(logs.reason, logs.remark),
    logs.result_status = COALESCE(logs.result_status, 'success'),
    logs.after_snapshot = COALESCE(logs.after_snapshot, logs.metadata_json)
WHERE logs.operator_name IS NULL
   OR logs.operator_role IS NULL
   OR logs.module_code IS NULL
   OR logs.action_code IS NULL
   OR logs.reason IS NULL
   OR logs.result_status IS NULL
   OR logs.after_snapshot IS NULL;
