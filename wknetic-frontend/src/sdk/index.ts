import { reactive, markRaw, type Component } from 'vue';
// 如果你决定彻底放弃源码版（只支持 vite 打包后的插件），可以注释掉下面这行
import { loadVueComponent } from '@/sdk/sfc-loader'; 

// --- 1. 内部状态 (Registry & Resources) ---

// 组件注册表：Key=插槽名, Value=组件数组
const componentRegistry = reactive<Record<string, Component[]>>({});

// 动作注册表：Key=插槽名, Value=配置数组
const actionRegistry = reactive<Record<string, any[]>>({});

// 资源追踪表：Key=插件ID, Value=清理函数数组 (用于卸载)
const pluginResources = new Map<string, Function[]>();

// 已加载插件集合：防止重复加载
const loadedPlugins = new Set<string>();

// --- 2. 辅助函数 ---

/**
 * 记录资源以便卸载
 */
const trackResource = (pluginId: string | undefined, cleanupFn: Function) => {
  if (!pluginId) return;
  if (!pluginResources.has(pluginId)) {
    pluginResources.set(pluginId, []);
  }
  pluginResources.get(pluginId)?.push(cleanupFn);
};

// --- 3. SDK 定义 ---

export const WknieticSDK = {
  // ============================================
  // A. 宿主能力注入 (Injected Capabilities)
  // ============================================
  
  /** Vue 核心库 (由 main.ts 注入) */
  vue: null as any,

  /** 路由实例 (由 main.ts 注入，用于插件跳转) */
  router: null as any,

  /** 用户上下文 (包含 Token、用户信息等) */
  context: reactive({
    user: null as any,
    config: {} as any
  }),

  /** * HTTP 请求封装 (建议替换为真实的 axios)
   * 插件里调用: await SDK.http.post('/api/xxx')
   */
  http: {
    get: async (url: string) => { 
      console.log('HTTP GET', url); 
      return fetch(url).then(res => res.json()); 
    },
    post: async (url: string, data: any) => { 
      console.log('HTTP POST', url, data);
      return fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      }).then(res => res.json());
    }
  },

  // ============================================
  // B. 插件工具 (Plugin Utils)
  // ============================================

  /**
   * [核心] 加载 .vue 文件
   * 即使是静态文件，浏览器也不认识 .vue，必须用 loader 转换
   */
  loadVue: loadVueComponent, 

  // 【新增】清空某个插槽，防止重复堆叠
  clearSlot(slotName: string) {
    componentRegistry[slotName] = [];
    console.log(`[SDK] Cleared slot: ${slotName}`);
  },

  /**
   * [核心] 路径解析
   * 帮插件找到自己的静态资源 (图片/CSS)
   * 例子: resolveUrl('http://site.com/plugins/demo/', './icon.png')
   */
//   resolveUrl: (baseUrl: string, relativePath: string) => {
//     try {
//       return new URL(relativePath, baseUrl).href;
//     } catch (e) {
//       return relativePath;
//     }
//   },

  resolveUrl: (baseUrl: string, relativePath: string) => {
    try {
      // 1. 确保 base 是绝对路径 (http://localhost:5173/plugins/...)
      const baseAbsolute = baseUrl.startsWith('http') 
        ? baseUrl 
        : new URL(baseUrl, window.location.origin).href;

      // 2. 基于绝对路径解析资源
      return new URL(relativePath, baseAbsolute).href;
    } catch (e) {
      console.error('[SDK] Path resolve error:', e);
      // 保底返回，但这通常会导致 404，所以最好看控制台报错
      return relativePath;
    }
  },

  // ============================================
  // C. 注册 API (Registration API)
  // ============================================

  /**
   * 注册组件 (支持 源码版 和 编译版)
   * @param slotName 插槽位置，如 'user-profile-header'
   * @param component Vue组件对象
   * @param pluginId 插件ID (必填，否则无法卸载)
   */
  registerComponent(slotName: string, component: Component, pluginId: string) {
    if (!componentRegistry[slotName]) {
      componentRegistry[slotName] = [];
    }

    // 1. 获取当前插槽列表
    const listComponent = componentRegistry[slotName];
    
    // markRaw: 告诉 Vue 不要把组件实例变成响应式，提升性能
    const rawComponent = markRaw(component);

    (rawComponent as any).__pluginId = pluginId; // 记录来源插件

    // 2. 注册到插槽
    listComponent.push(rawComponent);
    console.log(`[SDK] ${pluginId} registered component to ${slotName}`);

    // 记录清理逻辑
    trackResource(pluginId, () => {
      const list = componentRegistry[slotName];
      if (!list) return;
      const idx = list.indexOf(rawComponent);
      if (idx !== -1) list.splice(idx, 1);
    });
  },

  /**
   * 注册简单动作按钮
   */
  registerAction(slotName: string, actionConfig: any, pluginId: string) {
    if (!actionRegistry[slotName]) {
      actionRegistry[slotName] = [];
    }
    actionRegistry[slotName].push(actionConfig);

    trackResource(pluginId, () => {
      const list = actionRegistry[slotName];
        if (!list) return; // 防御性编程
      const idx = list.indexOf(actionConfig);
      if (idx !== -1) list.splice(idx, 1);
    });
  },

  // ============================================
  // D. 宿主 API (Host API)
  // ============================================

  /** 获取组件列表 (给 ExtensionSlot 用) */
  getComponents(slotName: string) {
    return componentRegistry[slotName] || [];
  },

  /** 获取动作列表 (给 ExtensionSlot 用) */
  getActions(slotName: string) {
    return actionRegistry[slotName] || [];
  },

  /** 卸载插件 */
  uninstall(pluginId: string) {
    const cleanups = pluginResources.get(pluginId);
    if (cleanups) {
      cleanups.forEach(fn => fn());
      pluginResources.delete(pluginId);
      loadedPlugins.delete(pluginId);
      console.log(`[SDK] Plugin ${pluginId} uninstalled.`);
      return true;
    }
    return false;
  },

  /** 检查插件是否已加载 */
  isPluginLoaded(pluginId: string) {
    return loadedPlugins.has(pluginId);
  },

  /** 标记插件为已加载 */
  markPluginLoaded(pluginId: string) {
    loadedPlugins.add(pluginId);
  },

  /** 获取所有已加载的插件 ID 列表 */
  getLoadedPlugins() {
    return Array.from(loadedPlugins);
  }
};

// 挂载全局
(window as any).WknieticSDK = WknieticSDK;