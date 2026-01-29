export default {
  bootstrap: async () => {
    const SDK = window.WknieticSDK;
    
    // 1. 获取 Vue 核心能力 (这是必须的，因为浏览器没有 'vue' 模块)
    // h = hyperscript (创建虚拟DOM的函数)
    const { h, ref, computed } = SDK.vue;

    console.log('[wk-pure-js] 启动中...');

    // 2. 定义组件对象 (这是标准的 Vue 3 组件结构)
    const PureJsComponent = {
      // 接收 Props
      props: ['context'],
      
      setup(props) {
        // 响应式数据
        const count = ref(0);
        
        // 计算属性
        const btnText = computed(() => `⚡ 点击加速: ${count.value}`);

        // 方法
        const handleClick = () => {
          count.value++;
          if (count.value % 5 === 0) {
            alert(`恭喜！你点击了 ${count.value} 次 (用户: ${props.context?.user?.name})`);
          }
        };

        // 3. 【核心】渲染函数
        // 相当于 <template>
        return () => {
          // 创建一个 div 容器
          return h('div', { 
            style: { 
              display: 'flex', 
              gap: '10px', 
              alignItems: 'center',
              border: '2px dashed #1890ff',
              padding: '5px 10px',
              borderRadius: '8px'
            } 
          }, [
            // 子元素 1: 文本标签
            h('strong', { style: { color: '#1890ff' } }, '纯JS插件:'),
            
            // 子元素 2: 按钮
            h('button', {
              onClick: handleClick,
              style: {
                background: '#1890ff',
                color: 'white',
                border: 'none',
                padding: '4px 12px',
                borderRadius: '4px',
                cursor: 'pointer'
              }
            }, btnText.value) // 按钮文字绑定
          ]);
        };
      }
    };

    // 4. 直接注册对象
    // 注意：这里没有 await loadVue，直接把对象塞进去
    SDK.registerComponent('user-profile-header', PureJsComponent, 'wk-pure-js');
  }
};