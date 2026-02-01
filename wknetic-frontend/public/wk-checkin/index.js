export default {
  /**
   * @param {object} context - 由 Loader 注入的上下文
   * @param {string} context.baseUrl - 当前插件的根目录 URL (如 /plugins/wk-checkin/)
   */
  bootstrap: async ({ baseUrl }) => {
    const SDK = window.WknieticSDK;
    
    console.log('[wk-checkin] 正在启动...');

    // 1. 【修改点】拼接 Vue 文件的完整路径
    // 以前你可能写死了路径，现在要基于 baseUrl 动态计算
    const vueUrl = SDK.resolveUrl(baseUrl, './WkCheckIn.vue');

    // 2. 加载 Vue 组件 (源码模式专属)
    const CheckInComponent = await SDK.loadVue(vueUrl);

    // 3. 注册到插槽
    SDK.registerComponent('user-profile-header', CheckInComponent, 'wk-checkin');
  }
};