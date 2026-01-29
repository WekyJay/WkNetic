/**
 * 插件管理 API
 */
import api from './axios';

export interface PluginStatus {
  id: number;
  pluginId: string;
  pluginName: string;
  pluginVersion: string;
  enabled: boolean;
  grantedPermissions: string[];
  installedAt: string;
  updatedAt: string;
}

export interface InstallPluginParams {
  pluginId: string;
  pluginName: string;
  pluginVersion: string;
  grantedPermissions: string[];
}

export interface UpdatePluginStatusParams {
  pluginId: string;
  enabled: boolean;
}

/**
 * 插件 API
 */
export const pluginApi = {
  /**
   * 获取用户已安装的插件列表
   */
  getInstalledPlugins() {
    return api.get<PluginStatus[]>('/api/v1/plugins/installed');
  },

  /**
   * 获取已启用的插件列表
   */
  getEnabledPlugins() {
    return api.get<string[]>('/api/v1/plugins/enabled');
  },

  /**
   * 安装插件（授权并启用）
   */
  installPlugin(params: InstallPluginParams) {
    return api.post<PluginStatus>('/api/v1/plugins/install', params);
  },

  /**
   * 卸载插件
   */
  uninstallPlugin(pluginId: string) {
    return api.delete(`/api/v1/plugins/${pluginId}`);
  },

  /**
   * 更新插件状态（启用/禁用）
   */
  updatePluginStatus(params: UpdatePluginStatusParams) {
    return api.put(`/api/v1/plugins/${params.pluginId}/status`, {
      enabled: params.enabled
    });
  },

  /**
   * 获取插件权限
   */
  getPluginPermissions(pluginId: string) {
    return api.get<string[]>(`/api/v1/plugins/${pluginId}/permissions`);
  },

  /**
   * 更新插件权限
   */
  updatePluginPermissions(pluginId: string, permissions: string[]) {
    return api.put(`/api/v1/plugins/${pluginId}/permissions`, { permissions });
  }
};
