<script setup lang="ts">
import {
  FlagTriangleRight,
  LayoutDashboard,
  MessageSquareMore,
  Mountain,
  ScrollText,
  Settings2,
  UsersRound,
} from 'lucide-vue-next'

const links = [
  { title: '路线审核', description: '处理待审核路线。', to: '/trails/review', icon: Mountain },
  { title: '路线管理', description: '下架、恢复和查看路线状态。', to: '/trails/manage', icon: LayoutDashboard },
  { title: '用户管理', description: '封禁、解封和查看用户。', to: '/users', icon: UsersRound },
  { title: '评论治理', description: '隐藏、恢复和删除评论。', to: '/reviews', icon: MessageSquareMore },
  { title: '举报处理', description: '承接后续举报治理闭环。', to: '/reports', icon: FlagTriangleRight },
  { title: '配置中心', description: '维护后台业务选项。', to: '/config-center', icon: Settings2 },
  { title: '操作日志', description: '追踪关键治理操作。', to: '/operation-logs', icon: ScrollText },
] as const
</script>

<template>
  <section class="admin-card admin-section">
    <div class="dashboard-section__heading">
      <div>
        <h2 class="admin-title">快捷操作</h2>
        <p class="admin-subtitle">从首页直接跳转到高频治理模块。</p>
      </div>
    </div>

    <div class="dashboard-links">
      <RouterLink v-for="item in links" :key="item.to" class="dashboard-links__item" :to="item.to">
        <div class="dashboard-links__icon">
          <component :is="item.icon" :size="18" :stroke-width="2" />
        </div>
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
        </div>
      </RouterLink>
    </div>
  </section>
</template>

<style scoped>
.dashboard-section__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.dashboard-links {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.9rem;
  margin-top: 1rem;
}

.dashboard-links__item {
  display: flex;
  gap: 0.85rem;
  padding: 1rem 1.05rem;
  border-radius: 20px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease;
}

.dashboard-links__item:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary) 28%, var(--border));
}

.dashboard-links__icon {
  display: grid;
  place-items: center;
  width: 2.5rem;
  height: 2.5rem;
  flex: none;
  border-radius: 16px;
  color: var(--primary);
  background: var(--primary-soft);
}

.dashboard-links__item strong {
  display: block;
  color: var(--text-strong);
}

.dashboard-links__item p {
  margin: 0.35rem 0 0;
  color: var(--text-muted);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .dashboard-links {
    grid-template-columns: 1fr;
  }
}
</style>
