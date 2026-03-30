CREATE TABLE IF NOT EXISTS app_settings (
    id BIGINT NOT NULL COMMENT '配置ID',
    setting_key VARCHAR(100) NOT NULL COMMENT '配置键',
    setting_value TEXT NULL COMMENT '配置值',
    description VARCHAR(255) NULL COMMENT '配置说明',
    updated_by BIGINT NULL COMMENT '最后更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_settings_key (setting_key),
    KEY idx_app_settings_updated_by (updated_by),
    CONSTRAINT app_settings_ibfk_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点运营配置表';
