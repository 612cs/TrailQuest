<script setup lang="ts">
import { onMounted } from 'vue'
import { RefreshCcw } from 'lucide-vue-next'

import DashboardOverviewCards from '../components/dashboard/DashboardOverviewCards.vue'
import DashboardQuickLinks from '../components/dashboard/DashboardQuickLinks.vue'
import DashboardRiskFeed from '../components/dashboard/DashboardRiskFeed.vue'
import DashboardTodoPanel from '../components/dashboard/DashboardTodoPanel.vue'
import DashboardTrendChart from '../components/dashboard/DashboardTrendChart.vue'
import { useDashboardSummary } from '../composables/useDashboardSummary'

const { summary, loading, errorMessage, refreshedAt, todoItems, loadSummary } = useDashboardSummary()

onMounted(() => {
  void loadSummary()
})
</script>

<template>
  <div class="admin-dashboard">
    <div class="admin-dashboard__toolbar">
      <div>
        <h1 class="admin-title">后台工作台</h1>
        <p class="admin-subtitle">把待办、风险和趋势放到同一个首页里，减少在各模块之间来回切换。</p>
      </div>
      <div class="admin-dashboard__toolbar-actions">
        <div>
          <span class="admin-muted">最近刷新</span>
          <strong class="admin-dashboard__refreshed-at">{{ refreshedAt || '--' }}</strong>
        </div>
        <button class="admin-button admin-button-secondary" type="button" @click="loadSummary">
          <RefreshCcw :size="16" :stroke-width="2" />
          刷新
        </button>
      </div>
    </div>

    <div v-if="errorMessage" class="admin-dashboard__error">{{ errorMessage }}</div>

    <DashboardOverviewCards :summary="summary" :loading="loading" />

    <div class="admin-grid-2">
      <DashboardTodoPanel :items="todoItems" :loading="loading" />
      <DashboardRiskFeed :items="summary?.recentRisks ?? []" :loading="loading" />
    </div>

    <div class="admin-grid-2">
      <DashboardTrendChart :items="summary?.trends ?? []" :loading="loading" />
      <DashboardQuickLinks />
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard {
  display: grid;
  gap: 1rem;
}

.admin-dashboard__toolbar,
.admin-dashboard__toolbar-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-dashboard__error {
  border-radius: 16px;
  padding: 0.85rem 1rem;
  color: var(--danger);
  background: rgba(181, 68, 68, 0.08);
  border: 1px solid rgba(181, 68, 68, 0.16);
}

.admin-dashboard__refreshed-at {
  display: block;
  margin-top: 0.2rem;
  color: var(--text-strong);
}

@media (max-width: 768px) {
  .admin-dashboard__toolbar,
  .admin-dashboard__toolbar-actions {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
