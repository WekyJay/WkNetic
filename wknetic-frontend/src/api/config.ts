import api from './axios'

/**
 * 系统配置接口
 */

export interface SysConfig {
  configId: number
  configKey: string
  configValue: string
  configType: 'string' | 'number' | 'boolean' | 'json' | 'image' | 'textarea'
  configGroup: string
  configLabel: string
  configDesc: string
  isSystem: number
  isPublic: number
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
}

export interface ConfigGroup {
  groupKey: string
  groupLabel: string
  configs: SysConfig[]
}

export const configApi = {
  /**
   * 获取公开配置（无需登录）
   */
  getPublicConfigs() {
    return api.get<Record<string, string>>('/api/v1/config/public')
  },

  /**
   * 获取所有配置（管理员）
   */
  getAllConfigs() {
    return api.get<SysConfig[]>('/api/v1/admin/config/list')
  },

  /**
   * 根据配置组获取配置（管理员）
   */
  getConfigsByGroup(group: string) {
    return api.get<SysConfig[]>(`/api/v1/admin/config/group/${group}`)
  },

  /**
   * 更新单个配置
   */
  updateConfig(config: SysConfig) {
    return api.put<string>('/api/v1/admin/config/update', config)
  },

  /**
   * 批量更新配置
   */
  batchUpdateConfigs(configMap: Record<string, string>) {
    return api.put<string>('/api/v1/admin/config/batch-update', configMap)
  }
}

// 配置分组标签映射
export const CONFIG_GROUP_LABELS: Record<string, string> = {
  site: '站点设置',
  system: '系统设置',
  appearance: '外观设置',
  upload: '上传设置',
  security: '安全设置',
  email: '邮件设置',
  storage: '存储设置'
}
