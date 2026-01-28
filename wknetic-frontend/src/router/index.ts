import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
// 定义路由配置
const routes: RouteRecordRaw[] = [
        // 首页布局
        {
            path: '/',
            name: 'home-layout',
            component: () => import('@/components/layout/HomeLayout.vue'),
            children: [
                {
                    path: '',
                    name: 'home-home',
                    component: () => import('@/pages/HomePage.vue'),
                },
                {
                    path: '/mods',
                    name: 'mods',
                    component: () => import('@/pages/SearchPage.vue'),
                },
                {
                    path: '/project/:id',
                    name: 'project',
                    component: () => import('@/pages/ProjectPage.vue'),
                },
                {
                    path: '/forum',
                    name: 'forum',
                    component: () => import('@/pages/forum/ForumPage.vue'),
                },
                {
                    path: '/forum/post/:id',
                    name: 'forum-post',
                    component: () => import('@/pages/forum/PostDetailPage.vue'),
                },
            ]
        },
        // 管理后台路由
        {
            path: '/admin',
            name: 'admin-layout',
            component: () => import('@/components/layout/admin/AdminLayout.vue'),
            children: [
                {
                    path: '',
                    name: 'admin-home',
                    component: () => import('@/pages/admin/DashboardPage.vue'),
                }
            ]
        },
        // 管理员登录页面
        {
            path: '/admin/login',
            name: 'admin-login',
            component: () => import('@/pages/LoginPage.vue'),
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