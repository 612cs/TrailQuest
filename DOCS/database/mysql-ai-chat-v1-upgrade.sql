ALTER TABLE ai_messages
    ADD COLUMN metadata_json JSON NULL COMMENT '结构化消息元数据' AFTER tokens;
