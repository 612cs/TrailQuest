<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { FlagTriangleRight, LayoutDashboard, MessageSquareMore, Mountain, Settings2, ShieldCheck, UsersRound } from 'lucide-vue-next'

const props = defineProps<{
  collapsed: boolean
}>()

const route = useRoute()
const router = useRouter()

const menuItems = [
  { path: '/dashboard', title: '后台首页', icon: LayoutDashboard },
  { path: '/trails/review', title: '路线审核', icon: Mountain },
  { path: '/users', title: '用户管理', icon: UsersRound },
  { path: '/reviews', title: '评论管理', icon: MessageSquareMore },
  { path: '/reports', title: '举报处理', icon: FlagTriangleRight },
  { path: '/settings', title: '设置', icon: Settings2 },
]

function isActive(path: string) {
  return computed(() => route.path === path || route.path.startsWith(`${path}/`)).value
}

function navigate(path: string) {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>

<template>
  <aside class="admin-sidebar" :class="{ 'is-collapsed': props.collapsed }">
    <div class="admin-sidebar__brand">
      <div class="admin-sidebar__logo">
        <ShieldCheck :size="18" :stroke-width="2" />
      </div>
      <div v-if="!props.collapsed" class="admin-sidebar__brand-text">
        <strong>TrailQuest</strong>
        <span>管理后台</span>
      </div>
    </div>

    <nav class="admin-sidebar__nav" aria-label="后台导航">
      <button
        v-for="item in menuItems"
        :key="item.path"
        type="button"
        class="admin-sidebar__nav-item"
        :class="{ active: isActive(item.path) }"
        @click="navigate(item.path)"
      >
        <component :is="item.icon" :size="18" :stroke-width="2" />
        <span v-if="!props.collapsed">{{ item.title }}</span>
      </button>
    </nav>

    <div class="admin-sidebar__footer" v-if="!props.collapsed">
      <p>内容治理最小集</p>
      <small>Pure Admin 风格 · TrailQuest Green</small>
    </div>
  </aside>
</template>

<style scoped>
.admin-sidebar {
  display: flex;
  flex-direction: column;
  width: 18rem;
  min-height: 100vh;
  padding: 1.1rem;
  border-right: 1px solid var(--border);
  background: color-mix(in srgb, var(--bg-surface) 88%, transparent);
}

.admin-sidebar.is-collapsed {
  width: 5.5rem;
}

.admin-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  margin-bottom: 1.2rem;
  padding: 0.9rem 0.85rem;
  border-radius: 20px;
  background: var(--bg-soft);
}

.admin-sidebar__logo {
  display: grid;
  place-items: center;
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, var(--primary), color-mix(in srgb, var(--primary) 72%, black));
  box-shadow: 0 16px 36px rgba(47, 94, 37, 0.24);
}

.admin-sidebar__brand-text {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  min-width: 0;
}

.admin-sidebar__brand-text strong {
  color: var(--text-strong);
}

.admin-sidebar__brand-text span,
.admin-sidebar__footer p,
.admin-sidebar__footer small {
  color: var(--text-muted);
}

.admin-sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
  margin-top: 0.25rem;
  flex: 1;
}

.admin-sidebar__nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  border: 1px solid transparent;
  border-radius: 18px;
  padding: 0.9rem 1rem;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  text-align: left;
  transition:
    background-color 0.18s ease,
    color 0.18s ease,
    border-color 0.18s ease,
    transform 0.18s ease;
}

.admin-sidebar__nav-item:hover {
  transform: translateX(2px);
  background: var(--bg-soft);
  color: var(--text-strong);
}

.admin-sidebar__nav-item.active {
  border-color: color-mix(in srgb, var(--primary) 28%, transparent);
  background: color-mix(in srgb, var(--primary) 12%, transparent);
  color: var(--primary);
}

.admin-sidebar__footer {
  margin-top: 1rem;
  padding: 0.95rem 1rem;
  border-radius: 18px;
  background: var(--bg-soft);
}

.admin-sidebar__footer p {
  margin: 0 0 0.3rem;
  font-weight: 600;
}

.admin-sidebar__footer small {
  display: block;
}

@media (max-width: 1200px) {
  .admin-sidebar {
    width: 5.5rem;
  }
}
</style>
