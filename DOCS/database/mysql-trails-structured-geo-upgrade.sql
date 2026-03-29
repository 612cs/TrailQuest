ALTER TABLE `trails`
  ADD COLUMN `geo_country` varchar(64) DEFAULT NULL COMMENT '结构化地点-国家' AFTER `start_lat`,
  ADD COLUMN `geo_province` varchar(64) DEFAULT NULL COMMENT '结构化地点-省' AFTER `geo_country`,
  ADD COLUMN `geo_city` varchar(64) DEFAULT NULL COMMENT '结构化地点-市' AFTER `geo_province`,
  ADD COLUMN `geo_district` varchar(64) DEFAULT NULL COMMENT '结构化地点-区县' AFTER `geo_city`,
  ADD COLUMN `geo_source` varchar(32) DEFAULT NULL COMMENT '结构化地点来源' AFTER `geo_district`,
  ADD KEY `idx_trails_geo_province` (`geo_province`),
  ADD KEY `idx_trails_geo_city` (`geo_city`),
  ADD KEY `idx_trails_geo_district` (`geo_district`);
