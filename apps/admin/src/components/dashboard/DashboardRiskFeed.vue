<script setup lang="ts">
import { computed } from 'vue'

import { formatDateTime } from '../../utils/format'
import type { AdminDashboardRiskItem } from '../../types/admin'

const props = defineProps<{
  items: AdminDashboardRiskItem[]
  loading: boolean
}>()

const normalizedItems = computed(() =>
  props.items.map((item) => ({
    ...item,
    to:
      item.targetType === 'trail'
        ? { path: `/trails/review/${item.targetId}` }
        : item.targetType === 'review'
          ? { path: '/reviews', query: { status: 'hidden' } }
          : item.targetType === 'user'
            ? { path: '/users' }
            : item.targetType === 'report'
              ? { path: '/reports' }
              : { path: '/dashboard' },
  })),
)

function priorityLabel(priority: string) {
  if (priority === 'high') {
    return '高优先级'
  }
  if (priority === 'medium') {
    return '中优先级'
  }
  return '低优先级'
}
</script>

<template>
  <section class="admin-card admin-section">
    <div class="dashboard-section__heading">
      <div>
        <h2 class="admin-title">最近风险动态</h2>
        <p class="admin-subtitle">最近发生的治理动作和风险变化。</p>
      </div>
    </div>

    <div v-if="loading" class="dashboard-risk__empty admin-muted">正在加载风险动态...</div>

    <div v-else-if="normalizedItems.length" class="dashboard-risk">
      <article v-for="item in normalizedItems" :key="`${item.type}-${item.createdAt}-${item.targetId}`" class="dashboard-risk__item">
        <div class="dashboard-risk__top">
          <span class="dashboard-risk__badge" :data-priority="item.priority">{{ priorityLabel(item.priority) }}</span>
          <span class="dashboard-risk__time">{{ formatDateTime(item.createdAt) }}</span>
        </div>
        <strong class="dashboard-risk__title">{{ item.title }}</strong>
        <p class="dashboard-risk__desc">{{ item.description }}</p>
        <RouterLink class="dashboard-risk__action" :to="item.to">查看详情</RouterLink>
      </article>
    </div>

    <div v-else class="dashboard-risk__empty admin-muted">当前没有新的风险动态。</div>
  </section>
</template>

<style scoped>
.dashboard-section__heading,
.dashboard-risk__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.dashboard-risk,
.dashboard-risk__item {
  display: grid;
  gap: 0.8rem;
}

.dashboard-risk {
  margin-top: 1rem;
}

.dashboard-risk__item {
  padding: 1rem 1.05rem;
  border-radius: 20px;
  border: 1px solid var(--border);
  background: linear-gradient(180deg, color-mix(in srgb, var(--bg-soft) 84%, transparent), var(--bg-surface));
}

.dashboard-risk__badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 0.35rem 0.7rem;
  font-size: 0.82rem;
  font-weight: 700;
}

.dashboard-risk__badge[data-priority='high'] {
  color: var(--danger);
  background: rgba(181, 68, 68, 0.12);
}

.dashboard-risk__badge[data-priority='medium'] {
  color: var(--warning);
  background: rgba(181, 122, 33, 0.14);
}

.dashboard-risk__badge[data-priority='low'] {
  color: var(--text-muted);
  background: var(--bg-soft);
}

.dashboard-risk__time,
.dashboard-risk__desc,
.dashboard-risk__empty {
  color: var(--text-muted);
}

.dashboard-risk__title {
  color: var(--text-strong);
}

.dashboard-risk__desc {
  margin: 0;
  line-height: 1.7;
}

.dashboard-risk__action {
  color: var(--primary);
  font-weight: 600;
}

.dashboard-risk__empty {
  padding: 1rem 0 0.2rem;
}
</style>
