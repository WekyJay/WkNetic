-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
  `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int(2) DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int(1) DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`oper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_config` (
  `config_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置主键',
  `config_key` varchar(100) NOT NULL COMMENT '配置键名（如：site.logo, site.name）',
  `config_value` text COMMENT '配置值',
  `config_type` varchar(20) DEFAULT 'string' COMMENT '配置类型（string, number, boolean, json, image）',
  `config_group` varchar(50) DEFAULT 'system' COMMENT '配置分组（system, site, email, upload等）',
  `config_label` varchar(100) DEFAULT '' COMMENT '配置标签（用于前端显示）',
  `config_desc` varchar(500) DEFAULT '' COMMENT '配置描述',
  `is_system` tinyint(1) DEFAULT 0 COMMENT '是否系统内置（0否 1是，系统内置不可删除）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态（0停用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化系统配置数据
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `config_group`, `config_label`, `config_desc`, `is_system`, `sort_order`) VALUES
('site.name', 'WkNetic', 'string', 'site', '站点名称', '网站的名称', 1, 1),
('site.logo', '/assets/logo.png', 'image', 'site', '站点Logo', '网站的Logo图片地址', 1, 2),
('site.favicon', '/favicon.ico', 'image', 'site', '网站图标', '浏览器标签页图标', 1, 3),
('site.keywords', 'WkNetic,社区,论坛', 'string', 'site', 'SEO关键词', '网站SEO关键词', 1, 4),
('site.description', 'WkNetic社区平台', 'string', 'site', 'SEO描述', '网站SEO描述', 1, 5),
('site.copyright', '© 2026 WkNetic. All rights reserved.', 'string', 'site', '版权信息', '网站底部版权信息', 1, 6),
('site.icp', '', 'string', 'site', 'ICP备案号', '网站ICP备案号', 1, 7),
('system.upload.max_size', '10485760', 'number', 'upload', '上传文件大小限制', '单位：字节，默认10MB', 1, 10),
('system.upload.allowed_types', 'jpg,jpeg,png,gif,pdf,doc,docx', 'string', 'upload', '允许上传的文件类型', '多个类型用逗号分隔', 1, 11),
('security.captcha.type', 'simple', 'string', 'security', '验证码类型', 'simple-简易验证码, cloudflare-Cloudflare Turnstile, none-无验证', 1, 20),
('security.captcha.cloudflare.site_key', '', 'string', 'security', 'Cloudflare Site Key', 'Cloudflare Turnstile 站点密钥', 0, 21),
('security.captcha.cloudflare.secret_key', '', 'string', 'security', 'Cloudflare Secret Key', 'Cloudflare Turnstile 密钥', 0, 22);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `location` varchar(100) DEFAULT NULL COMMENT '所在地',
  `website` varchar(255) DEFAULT NULL COMMENT '个人网站',
  `gender` tinyint(1) DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID（外键关联sys_role.role_id）',
  `role` varchar(20) DEFAULT 'USER' COMMENT '用户角色：ADMIN/MODERATOR/USER/VIP/BANNED（兼容字段，逐步废弃）',
  `minecraft_uuid` varchar(36) DEFAULT NULL COMMENT 'Minecraft账号UUID',
  `minecraft_username` varchar(16) DEFAULT NULL COMMENT 'Minecraft游戏用户名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_minecraft_uuid` (`minecraft_uuid`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`role_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
  
-- 注意：由于外键约束，需要先创建sys_role表，再初始化管理员账号

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码（如：ADMIN, MODERATOR, USER, VIP）',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_desc` varchar(500) DEFAULT NULL COMMENT '角色描述',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认角色（0否 1是，新用户注册时自动分配）',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 初始化默认角色
INSERT IGNORE INTO `sys_role` (`role_code`, `role_name`, `role_desc`, `sort_order`, `is_default`, `status`) VALUES
('ADMIN', '管理员', '拥有系统最高权限，可以管理所有功能', 100, 0, 1),
('MODERATOR', '审核员', '可以审核和管理用户内容', 50, 0, 1),
('VIP', 'VIP会员', 'VIP用户，享有特殊权限', 20, 0, 1),
('USER', '普通用户', '普通注册用户', 10, 1, 1),
('BANNED', '已封禁', '被封禁的用户，无法使用系统功能', 0, 0, 1);

-- 初始化管理员账号（密码：123456）
-- 使用子查询获取ADMIN角色ID
INSERT IGNORE INTO `sys_user` (`username`, `password`, `nickname`, `email`, `role`, `role_id`, `status`) 
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin@wknetic.com', 'ADMIN', role_id, 1
FROM `sys_role` WHERE role_code = 'ADMIN';

-- ----------------------------
-- Table structure for user_plugins
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_plugins` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `plugin_id` varchar(100) NOT NULL COMMENT '插件ID',
  `plugin_name` varchar(200) NOT NULL COMMENT '插件名称',
  `plugin_version` varchar(50) NOT NULL COMMENT '插件版本',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用（0禁用 1启用）',
  `granted_permissions` text COMMENT '已授予的权限（JSON数组）',
  `installed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_plugin` (`user_id`, `plugin_id`),
  KEY `idx_user_enabled` (`user_id`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户插件表';

-- 你可以在这里继续添加其他表的 CREATE 语句

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` bigint(20) NOT NULL COMMENT '关注者用户ID',
  `following_id` bigint(20) NOT NULL COMMENT '被关注者用户ID',
  `status` tinyint(1) DEFAULT 1 COMMENT '关注状态（1关注 0已取消）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_following_id` (`following_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_follow_following` FOREIGN KEY (`following_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';