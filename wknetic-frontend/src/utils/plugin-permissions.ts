/**
 * 插件权限系统
 * 管理和验证插件权限
 */

export type Permission = 
  | 'storage:local'        // 本地存储访问
  | 'storage:session'      // 会话存储访问
  | 'http:api'             // API 请求权限
  | 'http:external'        // 外部域名请求权限
  | 'router:navigate'      // 路由跳转权限
  | 'router:guard'         // 路由守卫注册权限
  | 'ui:modal'             // 弹窗权限
  | 'ui:notification'      // 通知权限
  | 'user:profile'         // 用户资料读取权限
  | 'user:modify'          // 用户资料修改权限
  | 'file:upload'          // 文件上传权限
  | 'file:download'        // 文件下载权限
  | 'websocket:connect';   // WebSocket 连接权限

export interface PermissionInfo {
  name: Permission;
  label: string;
  description: string;
  risk: 'low' | 'medium' | 'high';
  category: 'storage' | 'network' | 'navigation' | 'ui' | 'user' | 'file' | 'realtime';
}

/**
 * 权限定义表
 */
export const PERMISSION_DEFINITIONS: Record<Permission, Omit<PermissionInfo, 'name'>> = {
  'storage:local': {
    label: '本地存储',
    description: '允许插件读写浏览器本地存储',
    risk: 'low',
    category: 'storage'
  },
  'storage:session': {
    label: '会话存储',
    description: '允许插件读写会话存储',
    risk: 'low',
    category: 'storage'
  },
  'http:api': {
    label: 'API 请求',
    description: '允许插件调用 WkNetic API',
    risk: 'medium',
    category: 'network'
  },
  'http:external': {
    label: '外部请求',
    description: '允许插件请求外部域名',
    risk: 'high',
    category: 'network'
  },
  'router:navigate': {
    label: '页面跳转',
    description: '允许插件跳转到其他页面',
    risk: 'low',
    category: 'navigation'
  },
  'router:guard': {
    label: '路由守卫',
    description: '允许插件注册路由守卫',
    risk: 'medium',
    category: 'navigation'
  },
  'ui:modal': {
    label: '弹窗',
    description: '允许插件显示弹窗',
    risk: 'low',
    category: 'ui'
  },
  'ui:notification': {
    label: '通知',
    description: '允许插件发送通知',
    risk: 'low',
    category: 'ui'
  },
  'user:profile': {
    label: '读取用户信息',
    description: '允许插件读取用户资料',
    risk: 'medium',
    category: 'user'
  },
  'user:modify': {
    label: '修改用户信息',
    description: '允许插件修改用户资料',
    risk: 'high',
    category: 'user'
  },
  'file:upload': {
    label: '文件上传',
    description: '允许插件上传文件',
    risk: 'medium',
    category: 'file'
  },
  'file:download': {
    label: '文件下载',
    description: '允许插件下载文件',
    risk: 'low',
    category: 'file'
  },
  'websocket:connect': {
    label: 'WebSocket 连接',
    description: '允许插件建立 WebSocket 连接',
    risk: 'medium',
    category: 'realtime'
  }
};

/**
 * 用户授权的插件权限记录
 * Key: pluginId, Value: 已授权的权限列表
 */
const grantedPermissions = new Map<string, Set<Permission>>();

/**
 * 解析权限字符串（支持通配符）
 */
const parsePermission = (permString: string): Permission[] => {
  // 支持通配符，如 "storage:*" 匹配所有 storage 权限
  if (permString.includes('*')) {
    const [category] = permString.split(':');
    return Object.keys(PERMISSION_DEFINITIONS).filter(
      key => key.startsWith(category + ':')
    ) as Permission[];
  }
  
  return [permString as Permission];
};

/**
 * 请求权限（返回需要用户确认的权限）
 */
export const requestPermissions = (
  pluginId: string, 
  permissions: string[]
): PermissionInfo[] => {
  const expandedPerms = permissions.flatMap(parsePermission);
  const grantedSet = grantedPermissions.get(pluginId) || new Set();
  
  // 过滤出尚未授权的权限
  const needGrant = expandedPerms.filter(perm => !grantedSet.has(perm));
  
  return needGrant.map(perm => ({
    name: perm,
    ...PERMISSION_DEFINITIONS[perm]
  }));
};

/**
 * 授予权限
 */
export const grantPermissions = (
  pluginId: string, 
  permissions: Permission[]
): void => {
  if (!grantedPermissions.has(pluginId)) {
    grantedPermissions.set(pluginId, new Set());
  }
  
  const grantedSet = grantedPermissions.get(pluginId)!;
  permissions.forEach(perm => grantedSet.add(perm));
  
  console.log(`[Permissions] 已授予插件 ${pluginId} 权限:`, permissions);
  
  // 持久化到 localStorage
  savePermissionsToStorage();
};

/**
 * 撤销权限
 */
export const revokePermissions = (
  pluginId: string, 
  permissions?: Permission[]
): void => {
  const grantedSet = grantedPermissions.get(pluginId);
  if (!grantedSet) return;
  
  if (permissions) {
    permissions.forEach(perm => grantedSet.delete(perm));
  } else {
    grantedPermissions.delete(pluginId);
  }
  
  console.log(`[Permissions] 已撤销插件 ${pluginId} 权限:`, permissions || '全部');
  savePermissionsToStorage();
};

/**
 * 检查权限
 */
export const hasPermission = (
  pluginId: string, 
  permission: Permission
): boolean => {
  const grantedSet = grantedPermissions.get(pluginId);
  return grantedSet ? grantedSet.has(permission) : false;
};

/**
 * 获取已授权权限列表
 */
export const getGrantedPermissions = (pluginId: string): Permission[] => {
  const grantedSet = grantedPermissions.get(pluginId);
  return grantedSet ? Array.from(grantedSet) : [];
};

/**
 * 检查所有权限是否已授权
 */
export const hasAllPermissions = (
  pluginId: string, 
  permissions: string[]
): boolean => {
  const expandedPerms = permissions.flatMap(parsePermission);
  return expandedPerms.every(perm => hasPermission(pluginId, perm));
};

/**
 * 从 localStorage 加载权限
 */
export const loadPermissionsFromStorage = (): void => {
  try {
    const stored = localStorage.getItem('wknetic_plugin_permissions');
    if (!stored) return;
    
    const data = JSON.parse(stored);
    Object.entries(data).forEach(([pluginId, perms]) => {
      grantedPermissions.set(pluginId, new Set(perms as Permission[]));
    });
    
    console.log('[Permissions] 已从本地存储加载权限配置');
  } catch (error) {
    console.error('[Permissions] 加载权限配置失败:', error);
  }
};

/**
 * 保存权限到 localStorage
 */
const savePermissionsToStorage = (): void => {
  try {
    const data: Record<string, Permission[]> = {};
    grantedPermissions.forEach((perms, pluginId) => {
      data[pluginId] = Array.from(perms);
    });
    
    localStorage.setItem('wknetic_plugin_permissions', JSON.stringify(data));
  } catch (error) {
    console.error('[Permissions] 保存权限配置失败:', error);
  }
};

/**
 * 重置所有权限（用于调试）
 */
export const resetAllPermissions = (): void => {
  grantedPermissions.clear();
  localStorage.removeItem('wknetic_plugin_permissions');
  console.log('[Permissions] 已重置所有权限');
};

/**
 * 按风险等级分组权限
 */
export const groupPermissionsByRisk = (permissions: PermissionInfo[]) => {
  return {
    low: permissions.filter(p => p.risk === 'low'),
    medium: permissions.filter(p => p.risk === 'medium'),
    high: permissions.filter(p => p.risk === 'high')
  };
};
