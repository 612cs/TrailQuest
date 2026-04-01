<script setup lang="ts">
import { onMounted } from 'vue'
import { RefreshCcw } from 'lucide-vue-next'

import DashboardOverviewCards from '../components/dashboard/DashboardOverviewCards.vue'
import DashboardQuickLinks from '../components/dashboard/DashboardQuickLinks.vue'
import DashboardRiskFeed from '../components/dashboard/DashboardRiskFeed.vue'
import DashboardTodoPanel from '../components/dashboard/DashboardTodoPanel.vue'
import DashboardTrendChart from '../components/dashboard/DashboardTrendChart.vue'
import { useDashboardSummary } from '../composables/useDashboardSummary'

const { summary, loading, refreshedAt, todoItems, loadSummary } = useDashboardSummary()

onMounted(() => {
  void loadSummary()
})
</script>

<template>
  <div class="admin-dashboard">
    <!-- Toolbar -->
    <header class="dashboard-header">
      <div>
        <h1 class="page-title">Admin Dashboard</h1>
        <p class="page-subtitle">Welcome back, managing your trails and community.</p>
      </div>
      <div class="header-actions">
        <span class="refresh-time" v-if="refreshedAt">Last updated: {{ refreshedAt }}</span>
        <button class="icon-button" @click="loadSummary" :disabled="loading">
          <RefreshCcw :size="18" :class="{ 'animate-spin': loading }" />
        </button>
      </div>
    </header>

    <!-- Content Sections -->
    <main class="dashboard-content">
      <!-- 1. Overview Statistics -->
      <section class="content-section">
        <DashboardOverviewCards :summary="summary" :loading="loading" />
      </section>

      <!-- 2. Two-Column Grid: Tasks & Risks -->
      <div class="dashboard-grid">
        <section class="content-section card-wrapper">
          <DashboardTodoPanel :items="todoItems" :loading="loading" />
        </section>
        <section class="content-section card-wrapper">
          <DashboardRiskFeed :items="summary?.recentRisks ?? []" :loading="loading" />
        </section>
      </div>

      <!-- 3. Bottom Grid: Trends & Quick Actions -->
      <div class="dashboard-grid">
        <section class="content-section card-wrapper">
          <DashboardTrendChart :items="summary?.trends ?? []" :loading="loading" />
        </section>
        <section class="content-section card-wrapper">
          <DashboardQuickLinks />
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-dashboard {
  max-width: 1440px;
  margin: 0 auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-strong);
  margin: 0;
  letter-spacing: -0.025em;
}

.page-subtitle {
  font-size: 0.9375rem;
  color: var(--text-muted);
  margin: 0.5rem 0 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.refresh-time {
  font-size: 0.8125rem;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}

.icon-button {
  width: 2.75rem;
  height: 2.75rem;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--bg-surface);
  color: var(--text-strong);
  transition: all 0.2s ease;
  cursor: pointer;
}

.icon-button:hover {
  background: var(--bg-soft);
  border-color: var(--primary-soft);
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2rem;
}

.card-wrapper {
  background: white;
  padding: 1.75rem;
  border-radius: 28px;
  border: 1px solid var(--border);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.02);
}

@media (max-width: 1024px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-dashboard {
    padding: 1rem;
  }
}
</style>
