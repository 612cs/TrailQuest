ALTER TABLE media_files
    MODIFY COLUMN biz_type ENUM('avatar','home_hero','trail_cover','trail_gallery','trail_track','review')
    NOT NULL COMMENT '业务类型';

ALTER TABLE upload_checkpoints
    MODIFY COLUMN biz_type ENUM('avatar','home_hero','trail_cover','trail_gallery','trail_track','review')
    NOT NULL COMMENT '业务类型';
