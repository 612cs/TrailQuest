-- 用户
INSERT INTO `users` (`id`,`username`,`avatar`,`avatar_bg`,`email`,`password_hash`) VALUES
(101,'Sarah M.','SM','#8b5cf6','sarah@example.com','hash_xxx'),
(102,'云游者','YY','#0891b2','yunyou@example.com','hash_xxx'),
(104,'@hiking_queen','HQ','#8b5cf6','queen@example.com','hash_xxx');

-- 路线
INSERT INTO `trails`
(`id`,`image`,`name`,`location`,`ip`,`difficulty`,`difficulty_label`,`pack_type`,`duration_type`,`rating`,`review_count`,
`distance`,`elevation`,`duration`,`description`,`favorites`,`likes`,`author_id`,`publish_time`) VALUES
(1,'/trail-pine.png','老鹰峰顶','浙江 杭州 临安','121.41.32.100','moderate','适中','light','single_day',4.9,1200,
 '6.4 km','+420 m','3h 15m','老鹰峰顶是临安地区最受欢迎的徒步路线之一，沿途可欣赏壮丽的山谷景色。',3842,1256,101,'5 小时前'),
(2,'/trail-lake.png','镜湖环线','云南 大理 苍山','222.88.90.10','easy','简单','light','single_day',4.7,856,
 '3.4 km','+50 m','45m','轻松的环湖步道，适合全家出行。湖水清澈见底，四周被苍翠的山林环绕。',5126,2430,102,'1 天前'),
(3,'/trail-foggy.png','布莱克伍德峡谷','四川 雅安 牛背山','118.125.88.20','hard','困难','heavy','multi_day',4.8,2400,
 '18.0 km','+1,200 m','6h 20m','极具挑战性的峡谷穿越路线，需要较好的体力和经验。',8930,5620,104,'2 天前');

-- 标签
INSERT INTO `tags` (`name`) VALUES
('日出'),('山顶'),('摄影'),('湖泊'),('家庭'),('休闲');

-- 路线标签关联
INSERT INTO `trail_tags` (`trail_id`,`tag_id`) VALUES
(1,1),(1,2),(1,3),(2,4),(2,5),(2,6);

-- 评论（顶级 + 回复）
INSERT INTO `reviews` (`id`,`trail_id`,`user_id`,`parent_id`,`rating`,`time_label`,`text`,`reply_to`) VALUES
(1,1,101,NULL,5,'1 周前','壮观的景色！山顶的日出令人叹为观止。',''),
(101,1,104,1,NULL,'6 天前','同意！日出真的太美了。','Sarah M.');

-- 评论图片
INSERT INTO `review_images` (`review_id`,`image`) VALUES
(1,'/trail-pine.png'),
(1,'/trail-lake.png');