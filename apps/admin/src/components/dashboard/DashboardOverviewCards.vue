<script setup lang="ts">
import { FlagTriangleRight, MessageSquareMore, Mountain, Route, UsersRound } from 'lucide-vue-next'
import type { AdminDashboardSummary } from '../../types/admin'

defineProps<{
  summary: AdminDashboardSummary | null
  loading: boolean
}>()

const primaryCards = [
  {
    key: 'pendingTrailCount',
    title: '路线审核',
    description: '待处理 42',
    icon: Mountain,
    to: { path: '/trails/review', query: { reviewStatus: 'pending' } },
    color: 'green'
  },
  {
    key: 'pendingReportCount',
    title: '待处理举报',
    description: '18 条紧急处理',
    icon: FlagTriangleRight,
    to: { path: '/reports' },
    color: 'red'
  },
  {
    key: 'hiddenReviewCount',
    title: '已屏蔽评论',
    description: '盘点已违规标记',
    icon: MessageSquareMore,
    to: { path: '/reviews', query: { status: 'hidden' } },
    color: 'neutral'
  },
] as const

const secondaryCards = [
  {
    key: 'todayNewUserCount',
    title: '今日新增粉丝',
    icon: UsersRound,
    color: 'green'
  },
  {
    key: 'todayNewTrailCount',
    title: '今日新增路线',
    icon: Route,
    color: 'cyan'
  },
  {
    key: 'todayNewReviewCount',
    title: '今日新增评价',
    icon: MessageSquareMore,
    color: 'blue'
  },
] as const

function valueOf(summary: AdminDashboardSummary | null, key: string) {
  if (!summary) return 0
  return (summary as any)[key] ?? 0
}
</script>

<template>
  <div class="dashboard-overview">
    <!-- Top Row: Primary Cards -->
    <div class="overview-grid overview-grid--primary">
      <RouterLink
        v-for="item in primaryCards"
        :key="item.key"
        class="primary-card"
        :data-color="item.color"
        :to="item.to"
      >
        <div class="primary-card__icon">
          <component :is="item.icon" :size="20" />
        </div>
        <div class="primary-card__content">
          <p class="primary-card__label">{{ item.title }}</p>
          <p class="primary-card__value">{{ loading ? '...' : valueOf(summary, item.key) }}</p>
          <p class="primary-card__desc">{{ item.description }}</p>
        </div>
      </RouterLink>
    </div>

    <!-- Middle Row: Secondary Stats -->
    <div class="overview-grid overview-grid--secondary">
      <div
        v-for="item in secondaryCards"
        :key="item.key"
        class="secondary-card"
        :data-color="item.color"
      >
        <div class="secondary-card__icon">
          <component :is="item.icon" :size="18" />
        </div>
        <div class="secondary-card__content">
          <p class="secondary-card__label">{{ item.title }}</p>
          <p class="secondary-card__value">{{ loading ? '...' : valueOf(summary, item.key).toLocaleString() }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-overview {
  display: grid;
  gap: 1rem;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.25rem;
}

/* Primary Cards Styling */
.primary-card {
  position: relative;
  padding: 1.5rem;
  border-radius: 20px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.primary-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
  border-color: var(--primary-soft);
}

.primary-card__icon {
  width: 3rem;
  height: 3rem;
  display: grid;
  place-items: center;
  border-radius: 12px;
}

.primary-card[data-color="green"] .primary-card__icon {
  background: rgba(47, 106, 58, 0.08);
  color: #2f6a3a;
}
.primary-card[data-color="red"] .primary-card__icon {
  background: rgba(181, 68, 68, 0.08);
  color: #b54444;
}
.primary-card[data-color="neutral"] .primary-card__icon {
  background: rgba(107, 118, 104, 0.08);
  color: #6b7668;
}

.primary-card__label {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin: 0;
}

.primary-card__value {
  font-size: 2.25rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0.25rem 0;
  line-height: 1;
}

.primary-card__desc {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0;
}

.primary-card[data-color="red"] .primary-card__desc {
  color: #b54444;
  font-weight: 600;
}

/* Secondary Cards Styling */
.secondary-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  background: var(--bg-surface);
  border-radius: 16px;
  border: 1px solid var(--border);
  position: relative;
  overflow: hidden;
}

.secondary-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.secondary-card[data-color="green"]::before { background: #2f6a3a; }
.secondary-card[data-color="cyan"]::before { background: #00838f; }
.secondary-card[data-color="blue"]::before { background: #1565c0; }

.secondary-card__icon {
  width: 2.5rem;
  height: 2.5rem;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: var(--bg-soft);
  color: var(--primary);
}

.secondary-card__label {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0;
}

.secondary-card__value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0;
  line-height: 1.2;
}

@media (max-width: 1024px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>

