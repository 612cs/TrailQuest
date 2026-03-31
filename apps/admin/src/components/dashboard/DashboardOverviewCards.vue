<script setup lang="ts">
import { FlagTriangleRight, MessageSquareMore, Mountain, Route, UsersRound } from 'lucide-vue-next'

import type { AdminDashboardSummary } from '../../types/admin'

defineProps<{
  summary: AdminDashboardSummary | null
  loading: boolean
}>()

const cards = [
  {
    key: 'pendingTrailCount',
    title: '待审核路线',
    description: '需要运营尽快完成首轮审核。',
    icon: Mountain,
    to: { path: '/trails/review', query: { reviewStatus: 'pending' } },
  },
  {
    key: 'pendingReportCount',
    title: '待处理举报',
    description: '举报模块预留治理入口，后续可直接承接真实工单。',
    icon: FlagTriangleRight,
    to: { path: '/reports' },
  },
  {
    key: 'hiddenReviewCount',
    title: '已隐藏评论',
    description: '隐藏评论总量可快速反映社区风险热度。',
    icon: MessageSquareMore,
    to: { path: '/reviews', query: { status: 'hidden' } },
  },
  {
    key: 'todayNewUserCount',
    title: '今日新增用户',
    description: '跟踪今日拉新情况和后台访问压力。',
    icon: UsersRound,
    to: { path: '/users' },
  },
  {
    key: 'todayNewTrailCount',
    title: '今日新增路线',
    description: '判断内容供给是否在持续增长。',
    icon: Route,
    to: { path: '/trails/review' },
  },
  {
    key: 'todayNewReviewCount',
    title: '今日新增评论',
    description: '观察社区互动强度与治理工作量。',
    icon: MessageSquareMore,
    to: { path: '/reviews' },
  },
] as const

function valueOf(summary: AdminDashboardSummary | null, key: (typeof cards)[number]['key']) {
  if (!summary) {
    return 0
  }
  return summary[key]
}
</script>

<template>
  <section class="admin-card admin-section">
    <div class="dashboard-section__heading">
      <div>
        <h2 class="admin-title">治理概览</h2>
        <p class="admin-subtitle">优先展示今天最关键的待办和新增信号。</p>
      </div>
    </div>

    <div class="dashboard-overview">
      <RouterLink
        v-for="item in cards"
        :key="item.key"
        class="dashboard-overview__card"
        :to="item.to"
      >
        <div class="dashboard-overview__icon">
          <component :is="item.icon" :size="18" :stroke-width="2" />
        </div>
        <div class="dashboard-overview__body">
          <p class="dashboard-overview__title">{{ item.title }}</p>
          <p class="dashboard-overview__value">{{ loading ? '...' : valueOf(summary, item.key) }}</p>
          <p class="dashboard-overview__desc">{{ item.description }}</p>
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

.dashboard-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.dashboard-overview__card {
  display: flex;
  gap: 1rem;
  padding: 1.15rem;
  border-radius: 22px;
  border: 1px solid var(--border);
  background:
    linear-gradient(145deg, color-mix(in srgb, var(--bg-surface) 78%, transparent), color-mix(in srgb, var(--primary-soft) 36%, transparent)),
    var(--bg-surface);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.dashboard-overview__card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary) 28%, var(--border));
  box-shadow: var(--shadow);
}

.dashboard-overview__icon {
  display: grid;
  place-items: center;
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 18px;
  color: var(--primary);
  background: var(--primary-soft);
  flex: none;
}

.dashboard-overview__body {
  min-width: 0;
}

.dashboard-overview__title,
.dashboard-overview__desc {
  margin: 0;
}

.dashboard-overview__title {
  color: var(--text-muted);
  font-size: 0.92rem;
}

.dashboard-overview__value {
  margin: 0.45rem 0 0;
  color: var(--text-strong);
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.05;
}

.dashboard-overview__desc {
  margin-top: 0.55rem;
  color: var(--text-muted);
  line-height: 1.6;
  font-size: 0.9rem;
}

@media (max-width: 1280px) {
  .dashboard-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-overview {
    grid-template-columns: 1fr;
  }
}
</style>
