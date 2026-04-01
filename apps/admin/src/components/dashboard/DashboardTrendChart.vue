<script setup lang="ts">
import { computed } from 'vue'
import { formatDate } from '../../utils/format'
import type { AdminDashboardTrendItem } from '../../types/admin'

const props = defineProps<{
  items: AdminDashboardTrendItem[]
  loading: boolean
}>()

const series = [
  { key: 'newTrailCount', label: '路线', color: '#2f6a3a' },
  { key: 'newUserCount', label: '用户', color: '#00838f' },
] as const

const maxValue = computed(() => {
  const values = props.items.flatMap((item) => series.map((serie) => (item as any)[serie.key]))
  return Math.max(1, ...values)
})

function barHeight(value: number) {
  return `${Math.max(4, (value / maxValue.value) * 100)}%`
}
</script>

<template>
  <div class="dashboard-trend">
    <div class="section-header">
      <h2 class="admin-title">近 7 天增长趋势</h2>
      <div class="trend-legend">
        <div v-for="s in series" :key="s.key" class="legend-item">
          <span class="dot" :style="{ backgroundColor: s.color }"></span>
          {{ s.label }}
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-placeholder">正在加载趋势图...</div>

    <div v-else class="chart-container">
      <div class="chart-area">
        <div v-for="item in items" :key="item.date" class="chart-column">
          <div class="bars-wrapper">
            <div 
              v-for="s in series" 
              :key="s.key" 
              class="bar" 
              :style="{ height: barHeight((item as any)[s.key]), backgroundColor: s.color }"
              :title="`${s.label}: ${(item as any)[s.key]}`"
            ></div>
          </div>
          <span class="date-label">{{ formatDate(item.date) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-trend {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.trend-legend {
  display: flex;
  gap: 1rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.chart-container {
  height: 240px;
  position: relative;
  display: flex;
  flex-direction: column;
}

.chart-area {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 2rem;
  border-bottom: 1px solid var(--border);
}

.chart-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  position: relative;
}

.bars-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 4px;
}

.bar {
  width: 8px;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.bar:hover {
  filter: brightness(1.1);
  transform: scaleX(1.5);
}

.date-label {
  position: absolute;
  bottom: -1.75rem;
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
}

.loading-placeholder {
  height: 240px;
  display: grid;
  place-items: center;
  color: var(--text-muted);
}
</style>
