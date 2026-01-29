/**
 * 插件扫描器
 * 用于扫描、验证 public/plugins/ 目录下的所有插件
 */

export interface PluginManifest {
  id: string;
  name: string;
  version: string;
  description?: string;
  author?: string;
  type?: 'source' | 'compiled';
  entry: string;
  style?: string;
  icon?: string;
  permissions?: string[];
  dependencies?: Record<string, string>;
}

export interface PluginInfo {
  id: string;
  name: string;
  version: string;
  description?: string;
  author?: string;
  manifest: PluginManifest;
  valid: boolean;
  errors: string[];
  permissions: string[];
}

export interface ScanResult {
  total: number;
  valid: number;
  invalid: number;
  plugins: PluginInfo[];
}

/**
 * 验证 manifest 必填字段
 */
const validateManifest = (manifest: any, pluginId: string): string[] => {
  const errors: string[] = [];
  
  // 必填字段检查
  if (!manifest.id) {
    errors.push('缺少必填字段: id');
  } else if (manifest.id !== pluginId) {
    errors.push(`manifest.id (${manifest.id}) 与目录名 (${pluginId}) 不匹配`);
  }
  
  if (!manifest.name) {
    errors.push('缺少必填字段: name');
  }
  
  if (!manifest.version) {
    errors.push('缺少必填字段: version');
  } else if (!/^\d+\.\d+\.\d+/.test(manifest.version)) {
    errors.push(`版本号格式错误: ${manifest.version} (应为 x.y.z)`);
  }
  
  if (!manifest.entry) {
    errors.push('缺少必填字段: entry');
  }
  
  // 类型检查
  if (manifest.type && !['source', 'compiled'].includes(manifest.type)) {
    errors.push(`type 字段值无效: ${manifest.type} (应为 source 或 compiled)`);
  }
  
  // 权限格式检查
  if (manifest.permissions && !Array.isArray(manifest.permissions)) {
    errors.push('permissions 字段应为数组');
  }
  
  return errors;
};

/**
 * 验证入口文件是否存在
 */
const validateEntryFile = async (pluginId: string, entry: string): Promise<boolean> => {
  try {
    const entryUrl = `/plugins/${pluginId}/${entry}`;
    const response = await fetch(entryUrl, { method: 'HEAD' });
    return response.ok;
  } catch {
    return false;
  }
};

/**
 * 扫描单个插件
 */
const scanPlugin = async (pluginId: string): Promise<PluginInfo> => {
  const manifestUrl = `/plugins/${pluginId}/manifest.json`;
  const errors: string[] = [];
  let manifest: PluginManifest | null = null;
  let valid = false;
  
  try {
    // 1. 尝试加载 manifest.json
    const response = await fetch(manifestUrl);
    
    if (!response.ok) {
      errors.push('manifest.json 文件不存在或无法访问');
      return {
        id: pluginId,
        name: pluginId,
        version: '未知',
        manifest: manifest as any,
        valid: false,
        errors,
        permissions: []
      };
    }
    
    manifest = await response.json();
    
    // 2. 验证 manifest 格式
    const manifestErrors = validateManifest(manifest, pluginId);
    errors.push(...manifestErrors);

    
    // 3. 验证入口文件是否存在
    if (manifest && manifest.entry) {
      const entryExists = await validateEntryFile(pluginId, manifest.entry);
      if (!entryExists) {
        errors.push(`入口文件不存在: ${manifest.entry}`);
      }
    }
    
    // 4. 验证样式文件（可选）
    if (manifest && manifest.style) {
      const styleExists = await validateEntryFile(pluginId, manifest.style);
      if (!styleExists) {
        errors.push(`样式文件不存在: ${manifest.style} (警告)`);
      }
    }
    
    valid = errors.length === 0;
    
    return {
      id: manifest.id || pluginId,
      name: manifest.name || pluginId,
      version: manifest.version || '未知',
      description: manifest.description,
      author: manifest.author,
      manifest,
      valid,
      errors,
      permissions: manifest.permissions || []
    };
    
  } catch (error) {
    errors.push(`解析失败: ${error instanceof Error ? error.message : '未知错误'}`);
    
    return {
      id: pluginId,
      name: pluginId,
      version: '未知',
      manifest: manifest as any,
      valid: false,
      errors,
      permissions: []
    };
  }
};

/**
 * 扫描所有插件
 * @param pluginIds 插件 ID 列表（目录名）
 * @returns 扫描结果
 */
export const scanPlugins = async (pluginIds: string[]): Promise<ScanResult> => {
  console.log(`[Scanner] 开始扫描 ${pluginIds.length} 个插件...`);
  
  const plugins = await Promise.all(
    pluginIds.map(id => scanPlugin(id))
  );
  
  const valid = plugins.filter(p => p.valid).length;
  const invalid = plugins.length - valid;
  
  console.log(`[Scanner] 扫描完成: 总计 ${plugins.length}, 有效 ${valid}, 无效 ${invalid}`);
  
  return {
    total: plugins.length,
    valid,
    invalid,
    plugins
  };
};

/**
 * 获取单个插件信息
 */
export const getPluginInfo = async (pluginId: string): Promise<PluginInfo> => {
  return await scanPlugin(pluginId);
};

/**
 * 验证插件是否可用
 */
export const isPluginValid = async (pluginId: string): Promise<boolean> => {
  const info = await scanPlugin(pluginId);
  return info.valid;
};
