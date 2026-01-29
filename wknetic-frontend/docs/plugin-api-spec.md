# 插件管理后端 API 规范

## 数据库表结构

### user_plugins（用户插件表）

```sql
CREATE TABLE user_plugins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    plugin_id VARCHAR(100) NOT NULL COMMENT '插件ID',
    plugin_name VARCHAR(200) NOT NULL COMMENT '插件名称',
    plugin_version VARCHAR(50) NOT NULL COMMENT '插件版本',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    granted_permissions TEXT COMMENT '已授予的权限（JSON数组）',
    installed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '安装时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_plugin (user_id, plugin_id),
    INDEX idx_user_enabled (user_id, enabled)
) COMMENT='用户插件表';
```

---

## API 接口

### 1. 获取用户已安装的插件列表

**接口**：`GET /api/v1/plugins/installed`

**权限**：需要登录

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "pluginId": "wk-checkin",
      "pluginName": "每日签到",
      "pluginVersion": "1.0.0",
      "enabled": true,
      "grantedPermissions": ["http:api", "storage:local", "ui:notification"],
      "installedAt": "2026-01-29T10:00:00Z",
      "updatedAt": "2026-01-29T10:00:00Z"
    }
  ]
}
```

---

### 2. 获取已启用的插件ID列表

**接口**：`GET /api/v1/plugins/enabled`

**权限**：需要登录

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": ["wk-checkin", "wk-pure-js"]
}
```

---

### 3. 安装插件

**接口**：`POST /api/v1/plugins/install`

**权限**：需要登录

**请求体**：
```json
{
  "pluginId": "wk-checkin",
  "pluginName": "每日签到",
  "pluginVersion": "1.0.0",
  "grantedPermissions": ["http:api", "storage:local", "ui:notification"]
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "安装成功",
  "data": {
    "id": 1,
    "pluginId": "wk-checkin",
    "pluginName": "每日签到",
    "pluginVersion": "1.0.0",
    "enabled": true,
    "grantedPermissions": ["http:api", "storage:local", "ui:notification"],
    "installedAt": "2026-01-29T10:00:00Z",
    "updatedAt": "2026-01-29T10:00:00Z"
  }
}
```

**业务逻辑**：
- 检查插件是否已安装（根据 user_id + plugin_id）
- 如果已安装，返回错误或更新记录
- 插入新记录，默认 `enabled = true`

---

### 4. 卸载插件

**接口**：`DELETE /api/v1/plugins/{pluginId}`

**权限**：需要登录

**响应示例**：
```json
{
  "code": 200,
  "message": "卸载成功"
}
```

**业务逻辑**：
- 根据 user_id + plugin_id 删除记录
- 如果记录不存在，返回 404

---

### 5. 更新插件状态（启用/禁用）

**接口**：`PUT /api/v1/plugins/{pluginId}/status`

**权限**：需要登录

**请求体**：
```json
{
  "enabled": false
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "状态更新成功"
}
```

**业务逻辑**：
- 更新 `enabled` 字段
- 如果插件未安装，返回 404

---

### 6. 获取插件权限

**接口**：`GET /api/v1/plugins/{pluginId}/permissions`

**权限**：需要登录

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": ["http:api", "storage:local", "ui:notification"]
}
```

---

### 7. 更新插件权限

**接口**：`PUT /api/v1/plugins/{pluginId}/permissions`

**权限**：需要登录

**请求体**：
```json
{
  "permissions": ["http:api", "storage:local", "ui:notification", "user:profile"]
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "权限更新成功"
}
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 参数错误 |
| 401 | 未登录 |
| 404 | 插件未安装 |
| 409 | 插件已安装 |
| 500 | 服务器错误 |

---

## 安全建议

1. **验证插件ID**：确保 pluginId 不包含特殊字符，防止SQL注入
2. **权限校验**：验证 grantedPermissions 中的权限是否在允许列表中
3. **版本检查**：可选地验证插件版本号格式
4. **用户隔离**：确保用户只能操作自己的插件记录
5. **限流**：对安装接口进行限流，防止恶意刷接口
