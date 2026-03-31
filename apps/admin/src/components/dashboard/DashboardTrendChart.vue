<script setup lang="ts">
import { computed } from 'vue'

import { formatDate } from '../../utils/format'
import type { AdminDashboardTrendItem } from '../../types/admin'

const props = defineProps<{
  items: AdminDashboardTrendItem[]
  loading: boolean
}>()

const series = [
  { key: 'newTrailCount', label: '路线', color: 'var(--primary)' },
  { key: 'newReviewCount', label: '评论', color: 'var(--warning)' },
  { key: 'newReportCount', label: '举报', color: 'var(--danger)' },
  { key: 'newUserCount', label: '用户', color: 'var(--success)' },
] as const

const maxValue = computed(() => {
  const values = props.items.flatMap((item) => series.map((serie) => item[serie.key]))
  return Math.max(1, ...values)
})

function barHeight(value: number) {
  return `${Math.max(10, (value / maxValue.value) * 100)}%`
}
</script>

<template>
  <section class="admin-card admin-section">
    <div class="dashboard-section__heading">
      <div>
        <h2 class="admin-title">近 7 天趋势</h2>
        <p class="admin-subtitle">观察内容增长和治理压力的变化曲线。</p>
      </div>
    </div>

    <div v-if="loading" class="dashboard-trend__empty admin-muted">正在生成趋势数据...</div>

    <div v-else-if="items.length" class="dashboard-trend">
      <div class="dashboard-trend__legend">
        <span v-for="serie in series" :key="serie.key" class="dashboard-trend__legend-item">
          <i :style="{ backgroundColor: serie.color }"></i>
          {{ serie.label }}
        </span>
      </div>

      <div class="dashboard-trend__chart">
        <div v-for="item in items" :key="item.date" class="dashboard-trend__column">
          <div class="dashboard-trend__bars">
            <div
              v-for="serie in series"
              :key="serie.key"
              class="dashboard-trend__bar"
              :style="{ height: barHeight(item[serie.key]), backgroundColor: serie.color }"
              :title="`${serie.label}: ${item[serie.key]}`"
            />
          </div>
          <p class="dashboard-trend__date">{{ formatDate(item.date) }}</p>
        </div>
      </div>
    </div>

    <div v-else class="dashboard-trend__empty admin-muted">最近 7 天暂无趋势数据。</div>
  </section>
</template>

<style scoped>
.dashboard-section__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.dashboard-trend {
  margin-top: 1rem;
}

.dashboard-trend__legend {
  display: flex;
  flex-wrap: wrap;
  gap: 0.85rem;
  margin-bottom: 1rem;
}

.dashboard-trend__legend-item {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.dashboard-trend__legend-item i {
  display: inline-block;
  width: 0.8rem;
  height: 0.8rem;
  border-radius: 999px;
}

.dashboard-trend__chart {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 0.8rem;
  min-height: 250px;
}

.dashboard-trend__column {
  display: grid;
  gap: 0.75rem;
}

.dashboard-trend__bars {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-items: end;
  gap: 0.35rem;
  min-height: 210px;
  padding: 0.75rem;
  border-radius: 18px;
  background: linear-gradient(180deg, color-mix(in srgb, var(--bg-soft) 88%, transparent), transparent);
}

.dashboard-trend__bar {
  border-radius: 999px 999px 10px 10px;
  min-height: 0.65rem;
}

.dashboard-trend__date,
.dashboard-trend__empty {
  color: var(--text-muted);
}

.dashboard-trend__date {
  margin: 0;
  text-align: center;
  font-size: 0.84rem;
}

.dashboard-trend__empty {
  padding-top: 1rem;
}

@media (max-width: 960px) {
  .dashboard-trend__chart {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .dashboard-trend__chart {
    grid-template-columns: 1fr;
  }
}
</style>
