/**
 * 插件管理器
 * 负责插件的扫描、验证、权限检查和加载
 */

import { loadPlugin } from './plugin-loader';
import { scanPlugins } from './plugin-scanner';
import { 
  loadPermissionsFromStorage, 
  requestPermissions, 
  grantPermissions,
  hasAllPermissions 
} from './plugin-permissions';

export interface PluginManagerOptions {
  /** 是否自动授予权限（开发模式） */
  autoGrantPermissions?: boolean;
  /** 权限确认回调（生产环境使用） */
  onPermissionRequest?: (pluginId: string, permissions: any[]) => Promise<boolean>;
}

/**
 * 初始化插件系统
 * @param pluginIds 要加载的插件 ID 列表
 * @param options 配置选项
 */
export const initializePlugins = async (
  pluginIds: string[],
  options: PluginManagerOptions = {}
) => {
  const { 
    autoGrantPermissions = true,
    onPermissionRequest 
  } = options;

  console.log('🚀 初始化插件系统...');
  console.log(`📋 数据库返回 ${pluginIds.length} 个已启用的插件`);
  
  // 注意：不再需要 loadPermissionsFromStorage，因为权限信息在数据库中
  
  // 2. 扫描并验证插件
  const scanResult = await scanPlugins(pluginIds);
  
  // 3. 输出扫描结果
  logScanResult(scanResult);
  
  // 4. 过滤有效插件
  const validPlugins = scanResult.plugins.filter(p => p.valid);
  
  if (scanResult.invalid > 0) {
    console.warn(`⚠️ 检测到 ${scanResult.invalid} 个无效插件，已跳过加载`);
    scanResult.plugins.filter(p => !p.valid).forEach(p => {
      console.error(`❌ ${p.id}:`, p.errors);
    });
  }
  
  // 5. 准备加载列表（数据库已确认启用，直接加载）
  const pluginsToLoad: string[] = validPlugins.map(p => p.id);
  
  console.log(`✅ ${pluginsToLoad.length} 个插件通过验证，准备加载...`);
  
  // 6. HMR 支持：卸载已删除的插件
  cleanupRemovedPlugins(pluginsToLoad);
  
  // 7. 加载所有插件
  await loadAllPlugins(pluginsToLoad);
  
  console.log('\n✅ 插件系统初始化完成！');
  console.log(`已加载 ${pluginsToLoad.length} 个插件: ${pluginsToLoad.join(', ')}`);
  
  return {
    total: scanResult.total,
    loaded: pluginsToLoad.length,
    failed: scanResult.invalid,
    plugins: pluginsToLoad
  };
};

/**
 * 输出扫描结果
 */
const logScanResult = (scanResult: any) => {
  console.log('\n=== 插件扫描结果 ===');
  console.table(scanResult.plugins.map((p: any) => ({
    ID: p.id,
    名称: p.name,
    版本: p.version,
    状态: p.valid ? '✅ 有效' : '❌ 无效',
    权限数: p.permissions.length
  })));
};

/**
 * 处理插件权限
 */
const handlePluginPermissions = async (
  plugin: any,
  autoGrant: boolean,
  onPermissionRequest?: (pluginId: string, permissions: any[]) => Promise<boolean>
): Promise<boolean> => {
  // 检查是否所有权限都已授予
  if (hasAllPermissions(plugin.id, plugin.permissions)) {
    return true;
  }
  
  const needGrant = requestPermissions(plugin.id, plugin.permissions);
  
  if (needGrant.length === 0) {
    return true;
  }
  
  // 输出权限信息
  console.warn(`\n⚠️ 插件 ${plugin.name} 需要以下权限:`);
  needGrant.forEach((perm: any) => {
    const risk = perm.risk === 'high' ? '🔴' : perm.risk === 'medium' ? '🟡' : '🟢';
    console.log(`  ${risk} ${perm.label}: ${perm.description}`);
  });
  
  // 开发模式：自动授权
  if (autoGrant) {
    console.log(`✅ 已自动授予插件 ${plugin.name} 所需权限（开发模式）\n`);
    grantPermissions(plugin.id, needGrant.map((p: any) => p.name));
    return true;
  }
  
  // 生产模式：用户确认
  if (onPermissionRequest) {
    const granted = await onPermissionRequest(plugin.id, needGrant);
    if (granted) {
      grantPermissions(plugin.id, needGrant.map((p: any) => p.name));
    }
    return granted;
  }
  
  // 默认拒绝
  return false;
};

/**
 * 清理已删除的插件
 */
const cleanupRemovedPlugins = (currentPlugins: string[]) => {
  const SDK = (window as any).WknieticSDK;
  if (!SDK) return;
  
  const loadedPlugins = SDK.getLoadedPlugins() || [];
  const pluginsToUnload = loadedPlugins.filter(
    (pluginId: string) => !currentPlugins.includes(pluginId)
  );
  
  if (pluginsToUnload.length > 0) {
    console.log(`\n🔥 HMR: 检测到 ${pluginsToUnload.length} 个插件被删除`);
    pluginsToUnload.forEach((pluginId: string) => {
      console.log(`  卸载插件: ${pluginId}`);
      SDK.uninstall(pluginId);
    });
  }
};

/**
 * 加载所有插件
 */
const loadAllPlugins = async (pluginIds: string[]) => {
  if (pluginIds.length === 0) {
    console.log('\n没有可加载的插件');
    return;
  }
  
  console.log(`\n开始加载 ${pluginIds.length} 个插件...\n`);
  
  for (const pluginId of pluginIds) {
    try {
      await loadPlugin(pluginId);
    } catch (error) {
      console.error(`❌ 加载插件 ${pluginId} 失败:`, error);
    }
  }
};

/**
 * 从数据库获取已启用的插件列表
 */
export const fetchInstalledPlugins = async (): Promise<string[]> => {
  try {
    const { pluginApi } = await import('@/api/plugin');
    const response = await pluginApi.getEnabledPlugins();
    
    // axios拦截器已经解包了数据，response.data就是后端返回的数据
    const enabledPlugins = response.data;
    
    // 类型检查：确保返回的是数组
    if (!Array.isArray(enabledPlugins)) {
      console.error('[Manager] API返回的数据不是数组:', enabledPlugins);
      console.warn('[Manager] 使用默认插件列表');
      return ['wk-checkin', 'wk-pure-js'];
    }
    
    console.log(`[Manager] 从数据库获取到 ${enabledPlugins.length} 个已启用的插件`);
    return enabledPlugins;
  } catch (error) {
    console.error('[Manager] 从数据库获取插件列表失败，使用默认列表:', error);
    // 降级方案：使用开发模式的默认列表
    return ['wk-checkin', 'wk-pure-js'];
  }
};
