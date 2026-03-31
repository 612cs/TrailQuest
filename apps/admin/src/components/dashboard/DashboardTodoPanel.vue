<script setup lang="ts">
import type { AdminDashboardTodoItem } from '../../types/admin'

defineProps<{
  items: AdminDashboardTodoItem[]
  loading: boolean
}>()
</script>

<template>
  <section class="admin-card admin-section">
    <div class="dashboard-section__heading">
      <div>
        <h2 class="admin-title">今日待办</h2>
        <p class="admin-subtitle">进入后台后最应该优先处理的工作队列。</p>
      </div>
    </div>

    <div class="dashboard-todo">
      <article v-for="item in items" :key="item.key" class="dashboard-todo__item">
        <div class="dashboard-todo__meta">
          <p class="dashboard-todo__title">{{ item.title }}</p>
          <strong class="dashboard-todo__value">{{ loading ? '...' : item.count }}</strong>
        </div>
        <p class="dashboard-todo__desc">{{ item.description }}</p>
        <RouterLink class="dashboard-todo__action" :to="item.to">{{ item.actionLabel }}</RouterLink>
      </article>
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

.dashboard-todo {
  display: grid;
  gap: 0.9rem;
  margin-top: 1rem;
}

.dashboard-todo__item {
  padding: 1rem 1.05rem;
  border-radius: 20px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
}

.dashboard-todo__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
}

.dashboard-todo__title,
.dashboard-todo__desc {
  margin: 0;
}

.dashboard-todo__title {
  color: var(--text-strong);
  font-weight: 700;
}

.dashboard-todo__value {
  color: var(--primary);
  font-size: 1.45rem;
}

.dashboard-todo__desc {
  margin-top: 0.55rem;
  color: var(--text-muted);
  line-height: 1.65;
}

.dashboard-todo__action {
  display: inline-flex;
  margin-top: 0.85rem;
  color: var(--primary);
  font-weight: 600;
}
</style>
