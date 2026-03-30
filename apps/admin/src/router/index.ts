import { createRouter, createWebHistory } from 'vue-router'

import { pinia } from '../stores/pinia'
import { useAdminAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录', hideShell: true },
  },
  {
    path: '/',
    component: () => import('../components/layout/AdminShell.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { title: '后台首页', requiresAuth: true },
      },
      {
        path: 'trails/review',
        name: 'trail-review-list',
        component: () => import('../views/TrailReviewListView.vue'),
        meta: { title: '路线审核', requiresAuth: true },
      },
      {
        path: 'trails/review/:id',
        name: 'trail-review-detail',
        component: () => import('../views/TrailReviewDetailView.vue'),
        meta: { title: '路线审核详情', requiresAuth: true },
      },
      {
        path: 'reviews',
        name: 'review-manage',
        component: () => import('../views/ReviewManageView.vue'),
        meta: { title: '评论管理', requiresAuth: true },
      },
      {
        path: 'reports',
        name: 'report-manage',
        component: () => import('../views/ReportManageView.vue'),
        meta: { title: '举报处理', requiresAuth: true },
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('../views/SettingView.vue'),
        meta: { title: '设置', requiresAuth: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const authStore = useAdminAuthStore(pinia)
  if (!authStore.ready) {
    await authStore.bootstrap()
  }

  if (to.meta.hideShell) {
    document.title = `${to.meta.title || 'TrailQuest Admin'} - TrailQuest`
    return true
  }

  if (!authStore.isLoggedIn) {
    document.title = '登录 - TrailQuest Admin'
    return { name: 'login' }
  }

  if (!authStore.isAdmin) {
    authStore.clearAuth()
    document.title = '无权限 - TrailQuest Admin'
    return { name: 'login', query: { reason: 'no-permission' } }
  }

  document.title = `${to.meta.title || 'TrailQuest Admin'} - TrailQuest`
  return true
})

export default router
