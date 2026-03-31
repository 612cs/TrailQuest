CREATE TABLE IF NOT EXISTS system_option_groups (
    id BIGINT PRIMARY KEY,
    group_code VARCHAR(64) NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_option_groups_code (group_code)
);

CREATE TABLE IF NOT EXISTS system_option_items (
    id BIGINT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    item_code VARCHAR(64) NOT NULL,
    item_label VARCHAR(100) NOT NULL,
    item_sub_label VARCHAR(100) NULL,
    description VARCHAR(255) NULL,
    icon_name VARCHAR(64) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    is_builtin TINYINT(1) NOT NULL DEFAULT 1,
    extra_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_system_option_items_group FOREIGN KEY (group_id) REFERENCES system_option_groups (id),
    UNIQUE KEY uk_system_option_items_group_code (group_id, item_code),
    KEY idx_system_option_items_group_sort (group_id, sort_order)
);

INSERT INTO system_option_groups (id, group_code, group_name, description, status)
VALUES
    (2037000000000000001, 'hiking_experience_level', '徒步经验', '用户画像中的经验等级选项', 'active'),
    (2037000000000000002, 'hiking_trail_style', '常走路线类型', '用户画像中的常走路线类型选项', 'active'),
    (2037000000000000003, 'hiking_pack_preference', '负重偏好', '用户画像中的负重偏好选项', 'active'),
    (2037000000000000004, 'home_activity', '首页活动入口', '首页按活动探索卡片', 'active'),
    (2037000000000000005, 'search_difficulty', '搜索难度筛选', '搜索页难度筛选项', 'active'),
    (2037000000000000006, 'search_distance', '搜索长度筛选', '搜索页路线长度筛选项', 'active'),
    (2037000000000000007, 'search_pack_type', '搜索装备筛选', '搜索页装备类型筛选项', 'active'),
    (2037000000000000008, 'search_duration_type', '搜索耗时筛选', '搜索页耗时类型筛选项', 'active')
ON DUPLICATE KEY UPDATE
    group_name = VALUES(group_name),
    description = VALUES(description),
    status = VALUES(status);

INSERT INTO system_option_items (
    id, group_id, item_code, item_label, item_sub_label, description, icon_name, sort_order, enabled, is_builtin, extra_json
)
VALUES
    (2037000000000000101, 2037000000000000001, 'beginner', '新手', NULL, '适合刚开始接触徒步的用户', 'Sprout', 1, 1, 1, NULL),
    (2037000000000000102, 2037000000000000001, 'intermediate', '进阶', NULL, '有一定徒步经验，能适应中等强度路线', 'Mountain', 2, 1, 1, NULL),
    (2037000000000000103, 2037000000000000001, 'expert', '老驴', NULL, '具备长线或复杂路线经验', 'Compass', 3, 1, 1, NULL),

    (2037000000000000201, 2037000000000000002, 'city_weekend', '城市周边', NULL, '更常走城市周边短线或周末路线', 'Trees', 1, 1, 1, NULL),
    (2037000000000000202, 2037000000000000002, 'long_distance', '长线徒步', NULL, '更偏好多日或长距离徒步', 'Map', 2, 1, 1, NULL),
    (2037000000000000203, 2037000000000000002, 'both', '都走', NULL, '短线长线都可以接受', 'Route', 3, 1, 1, NULL),

    (2037000000000000301, 2037000000000000003, 'light', '轻装', NULL, '偏好轻装出行', 'Backpack', 1, 1, 1, NULL),
    (2037000000000000302, 2037000000000000003, 'heavy', '重装', NULL, '偏好多日重装或露营型路线', 'Package2', 2, 1, 1, NULL),
    (2037000000000000303, 2037000000000000003, 'both', '都可以', NULL, '轻装重装都可以接受', 'Scale', 3, 1, 1, NULL),

    (2037000000000000401, 2037000000000000004, 'hiking', '徒步', NULL, '首页活动入口', 'Footprints', 1, 1, 1, JSON_OBJECT('query', '徒步')),
    (2037000000000000402, 2037000000000000004, 'running', '跑步', NULL, '首页活动入口', 'Activity', 2, 1, 1, JSON_OBJECT('query', '跑步')),
    (2037000000000000403, 2037000000000000004, 'cycling', '骑行', NULL, '首页活动入口', 'Bike', 3, 1, 1, JSON_OBJECT('query', '骑行')),
    (2037000000000000404, 2037000000000000004, 'pet_friendly', '宠物友好', NULL, '首页活动入口', 'PawPrint', 4, 1, 1, JSON_OBJECT('query', '宠物友好')),
    (2037000000000000405, 2037000000000000004, 'backpacking', '背包客', NULL, '首页活动入口', 'Backpack', 5, 1, 1, JSON_OBJECT('query', '背包客')),
    (2037000000000000406, 2037000000000000004, 'wheelchair_friendly', '轮椅友好', NULL, '首页活动入口', 'Accessibility', 6, 1, 1, JSON_OBJECT('query', '轮椅友好')),

    (2037000000000000501, 2037000000000000005, 'easy', '简单', NULL, '适合新手或轻松路线', NULL, 1, 1, 1, NULL),
    (2037000000000000502, 2037000000000000005, 'moderate', '适中', NULL, '中等强度路线', NULL, 2, 1, 1, NULL),
    (2037000000000000503, 2037000000000000005, 'hard', '困难', NULL, '高强度或高难度路线', NULL, 3, 1, 1, NULL),

    (2037000000000000601, 2037000000000000006, 'short', '5km以内', NULL, '短距离路线', NULL, 1, 1, 1, NULL),
    (2037000000000000602, 2037000000000000006, 'medium', '5-10km', NULL, '中等距离路线', NULL, 2, 1, 1, NULL),
    (2037000000000000603, 2037000000000000006, 'long', '10km以上', NULL, '长距离路线', NULL, 3, 1, 1, NULL),

    (2037000000000000701, 2037000000000000007, 'light', '轻装', NULL, '搜索页装备类型筛选', NULL, 1, 1, 1, NULL),
    (2037000000000000702, 2037000000000000007, 'heavy', '重装', NULL, '搜索页装备类型筛选', NULL, 2, 1, 1, NULL),
    (2037000000000000703, 2037000000000000007, 'both', '轻重皆可', NULL, '搜索页装备类型筛选', NULL, 3, 1, 1, NULL),

    (2037000000000000801, 2037000000000000008, 'single_day', '单日', NULL, '搜索页耗时筛选', NULL, 1, 1, 1, NULL),
    (2037000000000000802, 2037000000000000008, 'multi_day', '多日', NULL, '搜索页耗时筛选', NULL, 2, 1, 1, NULL)
ON DUPLICATE KEY UPDATE
    item_label = VALUES(item_label),
    item_sub_label = VALUES(item_sub_label),
    description = VALUES(description),
    icon_name = VALUES(icon_name),
    sort_order = VALUES(sort_order),
    enabled = VALUES(enabled),
    extra_json = VALUES(extra_json);
