import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import i18n from '@/i18n'

// 获取 i18n 国际化实例
const { t } = i18n.global


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
                    meta: {
                        title: 'routes.home'
                    }
                },
                {
                    path: '/mods',
                    name: 'mods',
                    component: () => import('@/pages/SearchPage.vue'),
                    meta: {
                        title: 'routes.mods'
                    }
                },
                {
                    path: '/search',
                    name: 'search',
                    component: () => import('@/pages/SearchPage.vue'),
                    meta: {
                        title: 'routes.search'
                    }
                },
                {
                    path: '/project/:id',
                    name: 'project',
                    component: () => import('@/pages/ProjectPage.vue'),
                    meta: {
                        title: 'routes.project'
                    }
                },
                {
                    path: '/forum',
                    name: 'forum',
                    component: () => import('@/pages/forum/ForumPage.vue'),
                    meta: {
                        title: 'routes.forum'
                    }
                },
                {
                    path: '/forum/post/create',
                    name: 'post-create',
                    component: () => import('@/pages/PostCreatePage.vue'),
                    meta: {
                        title: 'routes.postCreate',
                        requiresAuth: true
                    }
                },
                {
                    path: '/forum/post/edit/:id',
                    name: 'post-edit',
                    component: () => import('@/pages/PostCreatePage.vue'),
                    meta: {
                        title: 'routes.postEdit',
                        requiresAuth: true
                    }
                },
                {
                    path: '/forum/post/:id',
                    name: 'forum-post',
                    component: () => import('@/pages/forum/PostDetailPage.vue'),
                    meta: {
                        title: 'routes.forumPost'
                    }
                },
                {
                    path: '/forum/topic/:id',
                    name: 'forum-topic',
                    component: () => import('@/pages/forum/TopicPage.vue'),
                    meta: {
                        title: 'routes.forumTopic'
                    }
                },
                {
                    path: '/chat',
                    name: 'game-chat',
                    component: () => import('@/pages/chat/GameChatPage.vue'),
                    meta: {
                        title: 'routes.gameChat',
                        requiresAuth: true,
                        requiresMinecraftAccount: true
                    }
                },
                {
                    path: '/user/:id',
                    name: 'user-profile',
                    component: () => import('@/pages/user/UserProfilePage.vue'),
                    meta: {
                        title: 'routes.userProfile'
                    }
                },
                {
                    path: '/settings',
                    name: 'user-settings',
                    component: () => import('@/pages/user/UserSettingsPage.vue'),
                    meta: {
                        title: 'routes.userSettings'
                    }
                },
                {
                    path: '/profile',
                    redirect: (to) => {
                        const auth = useAuthStore()
                        if (auth.user?.userId) {
                            return `/user/${auth.user.userId}`
                        }
                        return '/login'
                    }
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
                    meta: {
                        title: 'routes.adminHome'
                    }
                },
                {
                    path: 'users',
                    name: 'admin-users',
                    component: () => import('@/pages/admin/UsersPage.vue'),
                    meta: {
                        title: 'routes.adminUsers'
                    }
                },
                {
                    path: 'server-tokens',
                    name: 'admin-server-tokens',
                    component: () => import('@/pages/admin/ServerTokenPage.vue'),
                    meta: {
                        title: 'routes.adminServerTokens'
                    }
                },
                {
                    path: 'test-dialog',
                    name: 'admin-test-dialog',
                    component: () => import('@/pages/admin/TestDialog.vue'),
                    meta: {
                        title: 'Test Dialog'
                    }
                },
                {
                    path: 'server-tokens-simplified',
                    name: 'admin-server-tokens-simplified',
                    component: () => import('@/pages/admin/ServerTokenPageSimplified.vue'),
                    meta: {
                        title: 'Server Tokens Simplified'
                    }
                },
                {
                    path: 'plugins',
                    name: 'admin-plugins',
                    component: () => import('@/pages/admin/PluginsPage.vue'),
                    meta: {
                        title: 'routes.adminPlugins'
                    }
                },
                {
                    path: 'roles',
                    name: 'admin-roles',
                    component: () => import('@/pages/admin/RolesPage.vue'),
                    meta: {
                        title: 'routes.adminRoles'
                    }
                },
                {
                    path: 'audit',
                    name: 'admin-audit',
                    component: () => import('@/pages/admin/AuditPage.vue'),
                    meta: {
                        title: 'routes.adminAudit',
                        requiresRole: ['MODERATOR', 'ADMIN']
                    }
                },
                {
                    path: 'topics',
                    name: 'admin-topics',
                    component: () => import('@/pages/admin/TopicManagementPage.vue'),
                    meta: {
                        title: 'routes.adminTopics',
                        requiresRole: ['MODERATOR', 'ADMIN']
                    }
                },
                {
                    path: 'server-monitor',
                    name: 'admin-server-monitor',
                    component: () => import('@/pages/admin/ServerMonitorPage.vue'),
                    meta: {
                        title: 'routes.adminServerMonitor',
                        requiresRole: ['MODERATOR', 'ADMIN']
                    }
                },
                {
                    path: 'server-monitor/:sessionId',
                    name: 'admin-server-detail',
                    component: () => import('@/pages/admin/ServerDetailPage.vue'),
                    meta: {
                        title: 'routes.adminServerDetail',
                        requiresRole: ['MODERATOR', 'ADMIN']
                    }
                },
                {
                    path:'settings',
                    name: 'admin-settings',
                    component: () => import('@/pages/admin/SettingsPage.vue'),
                    meta: {
                        title: 'routes.adminSettings'
                    }
                }
            ]
        },
        // 统一登录页面
        {
            path: '/login',
            name: 'login',
            component: () => import('@/pages/LoginPage.vue'),
            meta: {
                title: 'routes.login'
            }
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
            meta: {
                title: 'routes.register'
            }
        },
        // 404 页面
        {
            path: '/404',
            name: 'not-found',
            component: () => import('../pages/NotFoundPage.vue'),
            meta: {
                title: 'routes.notFound'
            }
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
    // 动态网页标题处理（支持 i18n）
    let pageTitle: string = ''
    if (typeof to.meta.title === 'function') {
        pageTitle = to.meta.title(to)
    } else if (to.meta && typeof to.meta.title === 'string') {
        // 如果 meta.title 是 i18n key（包含 '.'），则翻译它
        const titleKey = to.meta.title
        if (titleKey.includes('.')) {
            pageTitle = t(titleKey)
        } else {
            pageTitle = titleKey
        }
    } else if (to.name) {
        pageTitle = to.name.toString()
    } else {
        pageTitle = 'WkNetic'
    }
    document.title = `${pageTitle} | WkNetic`

    const authStore = useAuthStore()
    const userStore = useUserStore()

    // 尝试从存储恢复登录状态
    const isAuthenticated = authStore.checkAuth()

    // 检查是否需要登录（管理后台）
    if (to.path.startsWith('/admin')) {
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

    // 检查普通页面是否需要认证
    if (to.meta.requiresAuth && !isAuthenticated) {
        console.warn('需要登录才能访问此页面')
        next({
            path: '/login',
            query: { redirect: to.fullPath }
        })
        return
    }

    // 检查是否需要Minecraft账户绑定
    if (to.meta.requiresMinecraftAccount && isAuthenticated) {
        const hasMinecraftAccount = userStore.user?.minecraftAccount != null
        if (!hasMinecraftAccount) {
            console.warn('需要绑定Minecraft账户才能访问此页面')
            next({
                path: '/settings',
                query: { tab: 'account' }
            })
            return
        }
    }

    // 如果已登录，访问登录页则跳转到首页
    if (to.path === '/login') {
        if (isAuthenticated) {
            const redirect = to.query.redirect as string
            next(redirect || '/')
            return
        }
    }

    next()
})

export default router
