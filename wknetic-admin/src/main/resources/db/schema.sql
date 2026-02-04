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
  `is_public` tinyint(1) DEFAULT 0 COMMENT '是否公开（0私有仅管理员 1公开可被前端读取）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态（0停用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化系统配置数据
INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_type`, `config_group`, `config_label`, `config_desc`, `is_system`, `is_public`, `sort_order`) VALUES
('site.name', 'WkNetic', 'string', 'site', '站点名称', '网站的名称', 1, 1, 1),
('site.logo', '/assets/logo.png', 'image', 'site', '站点Logo', '网站的Logo图片地址', 1, 1, 2),
('site.favicon', '/favicon.ico', 'image', 'site', '网站图标', '浏览器标签页图标', 1, 1, 3),
('site.keywords', 'WkNetic,社区,论坛', 'string', 'site', 'SEO关键词', '网站SEO关键词', 1, 1, 4),
('site.description', 'WkNetic社区平台', 'string', 'site', 'SEO描述', '网站SEO描述', 1, 1, 5),
('site.copyright', '© 2026 WkNetic. All rights reserved.', 'string', 'site', '版权信息', '网站底部版权信息', 1, 1, 6),
('site.icp', '', 'string', 'site', 'ICP备案号', '网站ICP备案号', 1, 1, 7),
('system.upload.max_size', '10485760', 'number', 'upload', '上传文件大小限制', '单位：字节，默认10MB', 1, 0, 10),
('system.upload.allowed_types', 'jpg,jpeg,png,gif,pdf,doc,docx', 'string', 'upload', '允许上传的文件类型', '多个类型用逗号分隔', 1, 0, 11),
('security.captcha.type', 'simple', 'string', 'security', '验证码类型', 'simple-简易验证码, cloudflare-Cloudflare Turnstile, none-无验证', 1, 1, 20),
('security.captcha.cloudflare.site_key', '', 'string', 'security', 'Cloudflare Site Key', 'Cloudflare Turnstile 站点密钥', 0, 1, 21),
('security.captcha.cloudflare.secret_key', '', 'string', 'security', 'Cloudflare Secret Key', 'Cloudflare Turnstile 密钥', 0, 0, 22);

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

-- ============================
-- Forum Module Tables
-- ============================

-- ----------------------------
-- Table structure for forum_topic
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_topic` (
  `topic_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `topic_name` varchar(100) NOT NULL COMMENT '话题名称',
  `topic_desc` varchar(500) DEFAULT NULL COMMENT '话题描述',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标地址',
  `color` varchar(20) DEFAULT '#1890ff' COMMENT '主题颜色（HEX格式）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序权重（数值越大越靠前）',
  `post_count` int(11) DEFAULT 0 COMMENT '帖子数量',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`topic_id`),
  UNIQUE KEY `uk_topic_name` (`topic_name`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛话题分类表';

-- 初始化默认话题
INSERT IGNORE INTO `forum_topic` (`topic_name`, `topic_desc`, `icon`, `color`, `sort_order`, `status`) VALUES
('General', '综合讨论区', '💬', '#1890ff', 100, 1),
('Mods', '模组讨论', '🔧', '#52c41a', 90, 1),
('Servers', '服务器专区', '🖥️', '#fa8c16', 80, 1),
('Help', '求助问答', '❓', '#faad14', 70, 1),
('Showcase', '作品展示', '🎨', '#eb2f96', 60, 1),
('News', '新闻公告', '📢', '#f5222d', 50, 1);

-- ----------------------------
-- Table structure for forum_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_post` (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '发帖用户ID',
  `topic_id` bigint(20) DEFAULT NULL COMMENT '所属话题ID（保存草稿时可以为空）',
  `title` varchar(200) NOT NULL COMMENT '帖子标题',
  `excerpt` varchar(500) DEFAULT NULL COMMENT '帖子简介/摘要',
  `content` text NOT NULL COMMENT 'Markdown格式内容',
  `content_html` text DEFAULT NULL COMMENT '缓存的HTML内容（提升渲染性能）',
  `status` tinyint(2) DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-审核中 3-已拒绝 4-已删除',
  `is_pinned` tinyint(1) DEFAULT 0 COMMENT '是否置顶（0否 1是）',
  `is_hot` tinyint(1) DEFAULT 0 COMMENT '是否热门（0否 1是）',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `comment_count` int(11) DEFAULT 0 COMMENT '评论数',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览数',
  `bookmark_count` int(11) DEFAULT 0 COMMENT '收藏数',
  `audit_user_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注（拒绝原因等）',
  `last_comment_time` datetime DEFAULT NULL COMMENT '最后评论时间（用于排序）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC),
  KEY `idx_hot` (`is_hot`, `like_count` DESC),
  KEY `idx_pinned` (`is_pinned`, `create_time` DESC),
  KEY `idx_last_comment` (`last_comment_time` DESC),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_post_topic` FOREIGN KEY (`topic_id`) REFERENCES `forum_topic` (`topic_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- ----------------------------
-- Table structure for forum_comment
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_comment` (
  `comment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint(20) NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID（NULL表示顶级评论，否则为回复）',
  `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复的目标用户ID',
  `content` text NOT NULL COMMENT 'Markdown格式评论内容',
  `content_html` text DEFAULT NULL COMMENT '缓存的HTML内容',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-正常 2-已删除 3-已隐藏',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `forum_post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `forum_comment` (`comment_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛评论表（支持嵌套回复）';

-- ----------------------------
-- Table structure for forum_tag
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_tag` (
  `tag_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `use_count` int(11) DEFAULT 0 COMMENT '使用次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`),
  KEY `idx_use_count` (`use_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛标签表';

-- ----------------------------
-- Table structure for forum_post_tag
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_post_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_post_tag_post` FOREIGN KEY (`post_id`) REFERENCES `forum_post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_post_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `forum_tag` (`tag_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子-标签关联表';

-- ----------------------------
-- Table structure for forum_post_like
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_post_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_post_like_post` FOREIGN KEY (`post_id`) REFERENCES `forum_post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_post_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞记录表';

-- ----------------------------
-- Table structure for forum_comment_like
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_comment_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `forum_comment` (`comment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞记录表';

-- ----------------------------
-- Table structure for forum_bookmark_category
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_bookmark_category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序权重',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认分类（0否 1是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sort_order` (`user_id`, `sort_order` DESC),
  CONSTRAINT `fk_bookmark_category_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏分类表';

-- ----------------------------
-- Table structure for forum_post_bookmark
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_post_bookmark` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `user_id` bigint(20) NOT NULL COMMENT '收藏用户ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT '所属分类ID（NULL表示未分类）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_bookmark_post` FOREIGN KEY (`post_id`) REFERENCES `forum_post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bookmark_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bookmark_category` FOREIGN KEY (`category_id`) REFERENCES `forum_bookmark_category` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- ----------------------------
-- Table structure for forum_post_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_post_history` (
  `history_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `post_id` bigint(20) NOT NULL COMMENT '帖子ID',
  `editor_id` bigint(20) NOT NULL COMMENT '编辑者ID',
  `title` varchar(200) NOT NULL COMMENT '历史标题',
  `content` text NOT NULL COMMENT '历史Markdown内容',
  `content_html` text DEFAULT NULL COMMENT '历史HTML内容',
  `change_summary` varchar(500) DEFAULT NULL COMMENT '修改摘要说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_post_id` (`post_id`, `create_time` DESC),
  KEY `idx_editor_id` (`editor_id`),
  CONSTRAINT `fk_history_post` FOREIGN KEY (`post_id`) REFERENCES `forum_post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_history_editor` FOREIGN KEY (`editor_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子编辑历史表';

-- ----------------------------
-- Table structure for forum_report
-- ----------------------------
CREATE TABLE IF NOT EXISTS `forum_report` (
  `report_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `target_type` varchar(20) NOT NULL COMMENT '举报对象类型：POST-帖子 COMMENT-评论',
  `target_id` bigint(20) NOT NULL COMMENT '举报对象ID',
  `reporter_id` bigint(20) NOT NULL COMMENT '举报人ID',
  `reason` varchar(500) NOT NULL COMMENT '举报原因',
  `status` tinyint(1) DEFAULT 0 COMMENT '处理状态：0-待处理 1-已处理 2-已驳回',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  PRIMARY KEY (`report_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容举报表';

-- ----------------------------
-- Table structure for sys_notification
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_notification` (
  `notification_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收通知的用户ID',
  `type` varchar(50) NOT NULL COMMENT '通知类型：POST_REPLY-帖子回复 COMMENT_REPLY-评论回复 POST_LIKE-帖子点赞 COMMENT_LIKE-评论点赞 MENTION-@提及 SYSTEM-系统通知',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` varchar(500) DEFAULT NULL COMMENT '通知内容',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联对象ID（帖子ID/评论ID等）',
  `related_type` varchar(20) DEFAULT NULL COMMENT '关联对象类型：POST/COMMENT',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '触发通知的用户ID（点赞者/回复者等）',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读（0未读 1已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`notification_id`),
  KEY `idx_user_id` (`user_id`, `is_read`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_notification_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 创建服务器Token表
CREATE TABLE IF NOT EXISTS `sys_server_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(64) NOT NULL COMMENT 'Token名称（便于识别）',
    `token_value` VARCHAR(64) NOT NULL COMMENT 'Token值（UUID）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_value` (`token_value`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器Token表';

-- ----------------------------
-- Table structure for user_quick_action
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_quick_action` (
  `action_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '快捷入口ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `action_key` varchar(50) NOT NULL COMMENT '快捷入口标识（如：post_list, audit_pending）',
  `action_name` varchar(100) NOT NULL COMMENT '快捷入口名称',
  `action_url` varchar(255) NOT NULL COMMENT '快捷入口URL路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标（Font Awesome或自定义）',
  `sort_order` tinyint(4) DEFAULT 1 COMMENT '显示排序（1-4）',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`action_id`),
  KEY `idx_user_id_sort` (`user_id`, `sort_order`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_quick_action_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户快捷入口表';
