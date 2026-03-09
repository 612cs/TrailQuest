import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/SearchView.vue'),
    meta: { title: '搜索' },
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('../views/CommunityView.vue'),
    meta: { title: '社区' },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { title: '个人中心' },
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('../views/PublishView.vue'),
    meta: { title: '发布路线' },
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/ChatView.vue'),
    meta: { title: 'AI 助手' },
  },
  {
    path: '/trail/:id',
    name: 'TrailDetail',
    component: () => import('../views/TrailDetailView.vue'),
    meta: { title: '路线详情' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || 'TrailQuest'} - TrailQuest`
})

export default router
