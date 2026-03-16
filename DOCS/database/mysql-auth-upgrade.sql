-- 为用户认证与管理员能力升级 users 表
ALTER TABLE `users`
  MODIFY COLUMN `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  MODIFY COLUMN `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  ADD COLUMN `role` ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '用户角色' AFTER `password_hash`,
  ADD UNIQUE KEY `uk_users_email` (`email`);

-- 如果你保留了旧测试账号，又希望先让它们可登录，可以把密码改成 BCrypt 哈希。
-- 下方示例密码明文为：password123
UPDATE `users`
SET `password_hash` = '$2a$10$XQ4J0sWlD0pW3Gm0k0M8Fu2u92G/Ik2Vq6KYFAsktwVDqveufqdp2'
WHERE `email` IN ('sarah@example.com', 'yunyou@example.com', 'queen@example.com');

-- 指定某个账号为管理员。
-- 建议先通过注册接口创建普通用户，再执行这条 SQL 提升为管理员。
-- 示例：
-- UPDATE `users` SET `role` = 'ADMIN' WHERE `email` = 'admin@trailquest.com';
