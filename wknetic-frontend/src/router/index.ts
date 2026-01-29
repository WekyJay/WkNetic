import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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
                },
                {
                    path: 'users',
                    name: 'admin-users',
                    component: () => import('@/pages/admin/UsersPage.vue'),
                }
            ]
        },
        // 统一登录页面
        {
            path: '/login',
            name: 'login',
            component: () => import('@/pages/LoginPage.vue'),
        },
        // 兼容旧的管理员登录路径，重定向到统一登录页
        {
            path: '/admin/login',
            redirect: (to) => {
                return {
                    path: '/login',
                    query: { redirect: to.query.redirect || '/admin' }
                }
            }
        },
        // 注册页面
        {
            path: '/register',
            name: 'register',
            component: () => import('@/pages/auth/RegisterPage.vue'),
        },
        // 404 页面
        {
            path: '/404',
            name: 'not-found',
            component: () => import('../pages/NotFoundPage.vue'),
        },
        // 404 页面捕获（所有未匹配的路由）
        {
            path: '/:pathMatch(.*)*',
            redirect: '/404'
        }
    ]



const router = createRouter({
    // 使用 HTML5 History 模式 (URL 不带 #)
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(_to, _from, _savedPosition) {
        // 切换页面时滚动到顶部
        return { top: 0 }
    }
})

// 全局前置守卫：权限验证
router.beforeEach((to, _from, next) => {
    // 修改网页标题
    document.title = `${to.meta.title || to.name} | WkNetic` || 'WkNetic'
    
    // 检查是否需要登录（管理后台）
    if (to.path.startsWith('/admin')) {
        const authStore = useAuthStore()
        
        // 尝试从存储恢复登录状态
        const isAuthenticated = authStore.checkAuth()
        
        if (!isAuthenticated) {
            // 未登录，跳转到 404 页面（根据需求：强行访问后台显示 404）
            console.warn('未授权访问管理后台，跳转到404页面')
            next({
                path: '/404',
                replace: true
            })
            return
        }
        
        // 已登录，检查是否有管理员权限
        if (!authStore.isAdmin) {
            console.warn('无管理员权限，跳转到404页面')
            next({
                path: '/404',
                replace: true
            })
            return
        }
    }
    
    // 如果已登录，访问登录页则跳转到首页
    if (to.path === '/login') {
        const authStore = useAuthStore()
        if (authStore.checkAuth()) {
            const redirect = to.query.redirect as string
            next(redirect || '/')
            return
        }
    }
    
    next()
})

export default router