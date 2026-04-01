<script setup lang="ts">
import {
  LayoutDashboard,
  MessageSquareMore,
  Mountain,
  ScrollText,
  Settings2,
  UsersRound,
} from 'lucide-vue-next'

const links = [
  { title: '路线审核', to: '/trails/review', icon: Mountain },
  { title: '路线管理', to: '/trails/manage', icon: LayoutDashboard },
  { title: '用户管理', to: '/users', icon: UsersRound },
  { title: '评论治理', to: '/reviews', icon: MessageSquareMore },
  { title: '配置中心', to: '/config-center', icon: Settings2 },
  { title: '操作日志', to: '/operation-logs', icon: ScrollText },
] as const
</script>

<template>
  <div class="quick-links">
    <h2 class="admin-title">快捷操作</h2>
    <div class="links-grid">
      <RouterLink v-for="item in links" :key="item.to" class="link-card" :to="item.to">
        <div class="link-card__icon">
          <component :is="item.icon" :size="18" />
        </div>
        <span class="link-card__title">{{ item.title }}</span>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.quick-links {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.link-card {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  padding: 1rem;
  background: var(--bg-surface);
  border-radius: 16px;
  border: 1px solid var(--border);
  transition: all 0.2s ease;
}

.link-card:hover {
  transform: translateY(-2px);
  border-color: var(--primary-soft);
  background: var(--bg-soft);
}

.link-card__icon {
  width: 2.25rem;
  height: 2.25rem;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: var(--bg-soft);
  color: var(--primary);
  flex-shrink: 0;
}

.link-card__title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-strong);
}

@media (max-width: 640px) {
  .links-grid {
    grid-template-columns: 1fr;
  }
}
</style>

