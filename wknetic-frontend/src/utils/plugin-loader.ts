import { WknieticSDK } from '@/sdk';

export const loadPlugin = async (pluginId: string) => {  // ⭐ HMR 支持：如果插件已加载，先卸载旧版本
  if (WknieticSDK.isPluginLoaded(pluginId)) {
    console.log(`[Loader] Plugin ${pluginId} already loaded, reloading...`);
    WknieticSDK.uninstall(pluginId);
  }
  const baseUrl = `/plugins/${pluginId}/`; 
  const manifestUrl = `${baseUrl}manifest.json`;

  try {
    // 1. 【第一步】先获取“身份证”
    const response = await fetch(manifestUrl);
    if (!response.ok) throw new Error('Manifest not found');
    
    const manifest = await response.json();
    console.log(`[Loader] Found plugin: ${manifest.name} (v${manifest.version})`);

    // 2. 【第二步】处理依赖 (可选)
    // if (manifest.dependencies) { ... check dependencies ... }

    // 3. 【第三步】加载样式 (如果有)
    if (manifest.style) {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = WknieticSDK.resolveUrl(baseUrl, manifest.style);
      document.head.appendChild(link);
    }

    // 4. 【第四步】根据 manifest 找到真正的入口
    // 无论是 source 还是 compiled，入口都是 JS 模块
    const entryUrl = WknieticSDK.resolveUrl(baseUrl, manifest.entry || 'index.js');
    
    // 动态加载入口
    const module = await import(/* @vite-ignore */ entryUrl);
    const pluginEntry = module.default;

    // 5. 【第五步】启动插件
    if (pluginEntry && pluginEntry.bootstrap) {
      await pluginEntry.bootstrap({ 
        baseUrl, 
        context: WknieticSDK.context,
        manifest // 把 manifest 也传给插件，插件可能需要知道自己的版本号
      });
    }

    // 6. 【第六步】标记为已加载
    WknieticSDK.markPluginLoaded(pluginId);
    console.log(`[Loader] Plugin ${pluginId} loaded successfully.`);

    // 7. 【第七步】记录到已加载列表 (用于 UI 显示)
    // installedPlugins.value.push(manifest);

  } catch (e) {
    console.error(`[Loader] Failed to load ${pluginId}:`, e);
  }
};