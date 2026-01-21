import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
// 定义路由配置
const routes: RouteRecordRaw[] = [
        {
            path: '/',
            name: 'home',
            component: () => import('../pages/HomePage.vue'),
        },
        {
            path: '/mods',
            name: 'mods',
            component: () => import('../pages/SearchPage.vue'),
        },
        {
            path: '/project/:id',
            name: 'project',
            component: () => import('../pages/ProjectPage.vue'),
        },
        // 404 页面捕获
        {
            path: '/:pathMatch(.*)*',
            name: 'not-found',
            component: () => import('../pages/NotFoundPage.vue'),
        }
    ]

const router = createRouter({
    // 使用 HTML5 History 模式 (URL 不带 #)
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(to, from, savedPosition) {
        // 切换页面时滚动到顶部
        return { top: 0 }
    }
})

// 全局前置守卫 (未来可以在这里做权限拦截)
router.beforeEach((to, from, next) => {
    // 简单的修改网页标题逻辑
    document.title = `${to.meta.title} | WekyJay System` || 'WekyJay System'
    next()
})

export default router