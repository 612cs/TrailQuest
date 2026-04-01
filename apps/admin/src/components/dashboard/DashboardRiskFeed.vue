<script setup lang="ts">
import { computed } from 'vue'
import { formatDateTime } from '../../utils/format'
import type { AdminDashboardRiskItem } from '../../types/admin'

const props = defineProps<{
  items: AdminDashboardRiskItem[]
  loading: boolean
}>()

const normalizedItems = computed(() =>
  props.items.slice(0, 4).map((item) => ({
    ...item,
    to:
      item.targetType === 'trail'
        ? { path: `/trails/review/${item.targetId}` }
        : item.targetType === 'review'
          ? { path: '/reviews', query: { status: 'hidden' } }
          : { path: '/dashboard' },
  })),
)

function priorityLabel(priority: string) {
  if (priority === 'high') return '高风险'
  if (priority === 'medium') return '中风险'
  return '低风险'
}
</script>

<template>
  <div class="dashboard-risk">
    <div class="section-header">
      <h2 class="admin-title">最近风险动态</h2>
      <RouterLink to="/operation-logs" class="view-all">查看全部</RouterLink>
    </div>

    <div v-if="loading" class="loading-placeholder">正在加载...</div>

    <div v-else class="risk-list">
      <div v-for="item in normalizedItems" :key="item.createdAt" class="risk-card">
        <div class="risk-card__header">
          <span class="risk-card__time">{{ formatDateTime(item.createdAt) }}</span>
          <span class="risk-badge" :data-priority="item.priority">{{ priorityLabel(item.priority) }}</span>
        </div>
        <h3 class="risk-card__title">{{ item.title }}</h3>
        <p class="risk-card__desc">{{ item.description }}</p>
        <RouterLink :to="item.to" class="risk-card__link">查看详情</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-risk {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.view-all {
  font-size: 0.8125rem;
  color: var(--primary);
  font-weight: 600;
}

.risk-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.risk-card {
  padding: 1.25rem;
  background: var(--bg-surface);
  border-radius: 20px;
  border: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  transition: border-color 0.2s ease;
}

.risk-card:hover {
  border-color: var(--primary-soft);
}

.risk-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.risk-card__time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.risk-badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
}

.risk-badge[data-priority='high'] { color: #b54444; background: rgba(181, 68, 68, 0.08); }
.risk-badge[data-priority='medium'] { color: #b57a21; background: rgba(181, 122, 33, 0.08); }
.risk-badge[data-priority='low'] { color: #6b7668; background: rgba(107, 118, 104, 0.08); }

.risk-card__title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0;
}

.risk-card__desc {
  font-size: 0.875rem;
  color: var(--text-muted);
  line-height: 1.5;
  margin: 0;
}

.risk-card__link {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--primary);
  margin-top: 0.25rem;
}

.loading-placeholder {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
}
</style>
