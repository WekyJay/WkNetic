// uno.config.ts
import {
    defineConfig,
    transformerVariantGroup,
    transformerDirectives,
    presetAttributify,
    presetIcons,
    presetTypography
} from 'unocss'
// 引入新预设
import presetWind3 from '@unocss/preset-wind3'

export default defineConfig({
    // 1. 预设：包含 Tailwind/Windi 兼容的原子类、属性化模式、图标支持
    presets: [
        presetWind3(), // 默认预设，包含响应式断点 (sm, md, lg, xl)
        presetAttributify(), // 属性化模式，可以直接写 <div text="red">
        presetIcons({
            scale: 1.2,
            warn: true, // 找不到图标时在控制台警告
            extraProperties: {
                'display': 'inline-block',
                'vertical-align': 'middle',
            },
            // 启用自动安装（当检测到缺失图标时自动 npm install）
            // 注意：这在某些受限网络环境下可能会失败
            autoInstall: true,
        }), // 图标支持
        presetTypography(), // 排版预设
    ],

    transformers:[transformerVariantGroup(),transformerDirectives()], // 使用UnoCss语法糖

    // 2. 暗黑模式：使用 class 策略（通过在 html 标签添加 class="dark" 来触发）
    darkMode: 'class',

    // 3. 主题系统：这里我们不写死颜色值，而是引用 CSS 变量
    // 这样后续只需要在运行时修改 CSS 变量，就能实现“一键换肤”
    theme: {
        colors: {
            bg: {
                // 使用 var() 引用我们在 CSS 里定义好的变量
                DEFAULT: 'var(--bg-default)',
                raised: 'var(--bg-raised)',
                surface: 'var(--bg-surface)',
                hover: 'var(--bg-hover)',
                input: 'var(--bg-input)',
            },
            brand: {
                DEFAULT: 'var(--brand-default)',
                primary: 'var(--bg-primary)',
                hover: 'var(--brand-hover)',
                light: 'var(--brand-light)',
                dark: 'var(--brand-dark)',
            },
            text: {
                DEFAULT: 'var(--text-default)',
                secondary: 'var(--text-secondary)',
                muted: 'var(--text-muted)',
                disabled: 'var(--text-disabled)',
            },
            border: {
                DEFAULT: 'var(--border-default)',
                light: 'var(--border-light)',
                dark: 'var(--border-dark)',
            }
        },
        fontFamily: {
            sans: ['Inter', 'system-ui', 'sans-serif'],
            mono: ['JetBrains Mono', 'monospace'],
        },
        borderRadius: {
            DEFAULT: 'var(--radius-md)',
            sm: 'var(--radius-sm)',
            md: 'var(--radius-md)',
            lg: 'var(--radius-lg)',
            xl: '1rem',
            '2xl': '1.5rem',
            full: '9999px',
        },
    },
    // 4. 快捷方式：将常用的长类名组合成短别名
    shortcuts: {
        // 按钮样式
        // 1. 加上 border-transparent 防止边框挤压布局
        // 2. 加上 active:scale-95 增加点击反馈感
        'btn': 'px-4 py-2 rounded-lg font-medium transition-all duration-200 cursor-pointer inline-flex items-center justify-center gap-2 border-transparent active:scale-95',

        // 调整：强制 text-white，确保暗黑模式下文字也是白色的
        'btn-primary': 'btn bg-brand text-white hover:bg-brand-hover hover:shadow-md',

        // 调整：次级按钮在暗黑模式下边框可能太暗，加深一点 hover 效果
        'btn-secondary': 'btn bg-bg-raised text-text border-border hover:bg-bg-hover hover:border-border-light',

        // 幽灵按钮
        'btn-ghost': 'btn bg-transparent text-text-secondary hover:bg-bg-hover hover:text-text',

        // 卡片样式
        // 调整：移除 transition-all，改为 transition-shadow/border 性能更好
        // 加上 overflow-hidden 防止子元素圆角溢出
        'card': 'bg-bg-surface rounded-xl border border-border p-4 transition duration-200 overflow-hidden',
        // 调整：暗黑模式下阴影不明显，主要靠 border 高亮
        'card-hover': 'card hover:border-brand/50 hover:shadow-lg',

        // 输入框样式
        // 调整：outline-none 是必须的，否则 focus 时会有双重边框
        'input-base': 'w-full bg-bg-input border border-border rounded-lg px-4 py-2 text-text placeholder-text-muted focus:border-brand focus:ring-1 focus:ring-brand focus:outline-none transition-colors',

        // 导航链接
        'nav-link': 'text-text-secondary hover:text-text px-3 py-2 rounded-lg hover:bg-bg-hover transition-colors duration-200 cursor-pointer',
        'nav-link-active': 'text-brand bg-brand-light/10 font-semibold', // 这里的 /10 需要配合下面的进阶技巧

        // 标签/徽章
        'badge': 'px-2.5 py-0.5 rounded-full text-xs font-medium inline-flex items-center',
        // 调整：由于 css 变量限制，暂时改用 brand-light (或者看下面的"进阶技巧"来启用透明度)
        'badge-brand': 'badge bg-brand-light text-brand-dark',
        // 注意：你需要确保 theme.colors 里定义了 success/warning，否则下面这两行会失效
        // 'badge-green': 'badge bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300',

        // 布局
        'container-main': 'max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full',
        'flex-center': 'flex items-center justify-center',
        'flex-between': 'flex items-center justify-between',
        'flex-col-center': 'flex flex-col items-center justify-center',
    }
})