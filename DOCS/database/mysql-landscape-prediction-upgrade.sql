ALTER TABLE trail_tracks
    ADD COLUMN IF NOT EXISTS elevation_min_meters DECIMAL(12, 2) NULL COMMENT '最低海拔(米)' AFTER distance_meters,
    ADD COLUMN IF NOT EXISTS elevation_peak_meters DECIMAL(12, 2) NULL COMMENT '最高海拔(米)' AFTER elevation_min_meters;

CREATE TABLE IF NOT EXISTS landscape_feature_snapshots (
    id BIGINT NOT NULL COMMENT '特征快照ID',
    trail_id BIGINT NOT NULL COMMENT '路线ID',
    prediction_date DATE NOT NULL COMMENT '预测日期',
    landscape_type ENUM('cloud_sea', 'rime', 'icicle') NOT NULL COMMENT '景观类型',
    feature_version VARCHAR(32) NOT NULL DEFAULT 'v1' COMMENT '特征版本',
    feature_payload JSON NOT NULL COMMENT '特征向量快照',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_landscape_feature_trail_date (trail_id, prediction_date),
    KEY idx_landscape_feature_type_date (landscape_type, prediction_date),
    CONSTRAINT landscape_feature_snapshots_ibfk_1 FOREIGN KEY (trail_id) REFERENCES trails (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景观预测训练/调试特征快照表';

CREATE TABLE IF NOT EXISTS landscape_observation_labels (
    id BIGINT NOT NULL COMMENT '景观标签ID',
    trail_id BIGINT NOT NULL COMMENT '路线ID',
    user_id BIGINT DEFAULT NULL COMMENT '标注用户ID',
    observation_date DATE NOT NULL COMMENT '观测日期',
    landscape_type ENUM('cloud_sea', 'rime', 'icicle', 'sunrise_sunset', 'milky_way') NOT NULL COMMENT '景观类型',
    observed TINYINT(1) NOT NULL COMMENT '是否观测到',
    confidence DECIMAL(4, 2) DEFAULT NULL COMMENT '人工标注置信度',
    evidence JSON DEFAULT NULL COMMENT '图片、备注等证据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_landscape_label_trail_date (trail_id, observation_date),
    KEY idx_landscape_label_type_date (landscape_type, observation_date),
    KEY idx_landscape_label_user (user_id),
    CONSTRAINT landscape_observation_labels_ibfk_1 FOREIGN KEY (trail_id) REFERENCES trails (id) ON DELETE CASCADE,
    CONSTRAINT landscape_observation_labels_ibfk_2 FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '景观观测人工标签表';
