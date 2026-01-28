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
  `status` tinyint(1) DEFAULT 1 COMMENT '状态（0禁用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
  
-- 初始化管理员账号（密码：123456）
INSERT IGNORE INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin@wknetic.com', 1);

-- 你可以在这里继续添加其他表的 CREATE 语句