/**
 * 插件安装/卸载管理（数据库版本）
 * 
 * 核心逻辑：
 * 1. 安装时：弹窗显示权限 → 用户确认 → 调用后端 API 保存到数据库
 * 2. 使用时：从数据库读取已启用的插件列表
 * 3. 卸载时：调用后端 API 删除数据库记录
 */

import { pluginApi, type InstallPluginParams } from '@/api/plugin';
import { getPluginInfo, type PluginInfo } from './plugin-scanner';
import { loadPlugin } from './plugin-loader';
import { 
  groupPermissionsByRisk,
  type PermissionInfo,
  type Permission,
  PERMISSION_DEFINITIONS
} from './plugin-permissions';

/**
 * 安装插件（显示权限确认对话框并保存到数据库）
 * @param pluginId 插件 ID
 * @param onPermissionRequest 权限确认回调（返回 Promise<boolean>）
 * @returns 是否安装成功
 */
export const installPlugin = async (
  pluginId: string,
  onPermissionRequest?: (plugin: PluginInfo, permissions: PermissionInfo[]) => Promise<boolean>
): Promise<{ success: boolean; message: string }> => {
  try {
    // 1. 获取插件信息
    const pluginInfo = await getPluginInfo(pluginId);
    
    if (!pluginInfo.valid) {
      return {
        success: false,
        message: `插件无效: ${pluginInfo.errors.join(', ')}`
      };
    }
    
    // 2. 检查是否需要权限
    const needPermissions = pluginInfo.permissions.length > 0;
    
    // 3. 准备权限信息（用于显示）
    const permissionInfos: PermissionInfo[] = pluginInfo.permissions.map(permStr => {
      const perm = permStr as Permission;
      return {
        name: perm,
        ...PERMISSION_DEFINITIONS[perm]
      };
    });
    
    // 4. 显示权限确认对话框（如果需要）
    let userConfirmed = true;
    
    if (needPermissions) {
      if (onPermissionRequest) {
        // 使用自定义对话框
        userConfirmed = await onPermissionRequest(pluginInfo, permissionInfos);
      } else {
        // 使用浏览器原生 confirm（开发模式）
        const grouped = groupPermissionsByRisk(permissionInfos);
        const riskText = [
          ...grouped.high.map(p => `🔴 ${p.label}: ${p.description}`),
          ...grouped.medium.map(p => `🟡 ${p.label}: ${p.description}`),
          ...grouped.low.map(p => `🟢 ${p.label}: ${p.description}`)
        ].join('\n');
        
        userConfirmed = confirm(
          `安装插件 "${pluginInfo.name}" 需要以下权限：\n\n${riskText}\n\n是否继续安装？`
        );
      }
    }
    
    if (!userConfirmed) {
      console.log(`[Install] 用户拒绝了插件 ${pluginInfo.name} 的权限请求`);
      return { success: false, message: '用户取消安装' };
    }
    
    // 5. 调用后端 API 保存到数据库
    const installParams: InstallPluginParams = {
      pluginId: pluginInfo.id,
      pluginName: pluginInfo.name,
      pluginVersion: pluginInfo.version,
      grantedPermissions: pluginInfo.permissions
    };
    
    await pluginApi.installPlugin(installParams);
    
    // 6. 加载插件
    await loadPlugin(pluginId);
    
    console.log(`✅ [Install] 插件 ${pluginInfo.name} 安装成功，已保存到数据库`);
    
    return {
      success: true,
      message: '安装成功'
    };
    
  } catch (error) {
    console.error(`[Install] 安装插件 ${pluginId} 失败:`, error);
    return {
      success: false,
      message: error instanceof Error ? error.message : '未知错误'
    };
  }
};

/**
 * 卸载插件（从数据库删除记录）
 * @param pluginId 插件 ID
 * @param onUninstallConfirm 卸载确认回调（可选）
 * @returns 是否卸载成功
 */
export const uninstallPlugin = async (
  pluginId: string,
  onUninstallConfirm?: (pluginId: string) => Promise<boolean>
): Promise<{ success: boolean; message: string }> => {
  try {
    // 1. 确认卸载
    let userConfirmed = true;
    
    if (onUninstallConfirm) {
      userConfirmed = await onUninstallConfirm(pluginId);
    }
    
    if (!userConfirmed) {
      return { success: false, message: '用户取消卸载' };
    }
    
    // 2. 卸载插件（通过 SDK）
    const SDK = (window as any).WknieticSDK;
    if (SDK?.uninstall) {
      SDK.uninstall(pluginId);
    }
    
    // 3. 调用后端 API 删除数据库记录
    await pluginApi.uninstallPlugin(pluginId);
    
    console.log(`✅ [Uninstall] 插件 ${pluginId} 已卸载，数据库记录已删除`);
    
    return {
      success: true,
      message: '卸载成功'
    };
    
  } catch (error) {
    console.error(`[Uninstall] 卸载插件 ${pluginId} 失败:`, error);
    return {
      success: false,
      message: error instanceof Error ? error.message : '未知错误'
    };
  }
};

/**
 * 启用/禁用插件
 */
export const togglePluginStatus = async (
  pluginId: string,
  enabled: boolean
): Promise<{ success: boolean; message: string }> => {
  try {
    await pluginApi.updatePluginStatus({ pluginId, enabled });
    
    const SDK = (window as any).WknieticSDK;
    
    if (enabled) {
      // 启用：加载插件
      await loadPlugin(pluginId);
      console.log(`✅ 插件 ${pluginId} 已启用`);
    } else {
      // 禁用：卸载插件
      if (SDK?.uninstall) {
        SDK.uninstall(pluginId);
      }
      console.log(`✅ 插件 ${pluginId} 已禁用`);
    }
    
    return {
      success: true,
      message: enabled ? '启用成功' : '禁用成功'
    };
    
  } catch (error) {
    console.error(`[Toggle] 切换插件 ${pluginId} 状态失败:`, error);
    return {
      success: false,
      message: error instanceof Error ? error.message : '未知错误'
    };
  }
};

/**
 * 批量安装插件
 */
export const batchInstallPlugins = async (
  pluginIds: string[],
  onPermissionRequest?: (plugin: PluginInfo, permissions: PermissionInfo[]) => Promise<boolean>
): Promise<{
  success: string[];
  failed: Array<{ id: string; reason: string }>;
}> => {
  const success: string[] = [];
  const failed: Array<{ id: string; reason: string }> = [];
  
  for (const pluginId of pluginIds) {
    const result = await installPlugin(pluginId, onPermissionRequest);
    
    if (result.success) {
      success.push(pluginId);
    } else {
      failed.push({ id: pluginId, reason: result.message });
    }
  }
  
  return { success, failed };
};

/**
 * 检查插件是否已安装（从数据库查询）
 */
export const isPluginInstalled = async (pluginId: string): Promise<boolean> => {
  try {
    const plugins = await pluginApi.getInstalledPlugins();
    return plugins.some(p => p.pluginId === pluginId);
  } catch (error) {
    console.error('检查插件安装状态失败:', error);
    return false;
  }
};

/**
 * 从数据库获取已启用的插件列表
 */
export const getEnabledPluginsFromDB = async (): Promise<string[]> => {
  try {
    const plugins = await pluginApi.getEnabledPlugins();
    console.log(`[Database] 获取到 ${plugins.length} 个已启用的插件:`, plugins);
    return plugins;
  } catch (error) {
    console.error('[Database] 获取已启用插件列表失败:', error);
    return [];
  }
};
