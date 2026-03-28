ALTER TABLE `trails`
  ADD COLUMN `start_lng` decimal(10,6) DEFAULT NULL COMMENT '路线起点经度' AFTER `ip`,
  ADD COLUMN `start_lat` decimal(10,6) DEFAULT NULL COMMENT '路线起点纬度' AFTER `start_lng`;
