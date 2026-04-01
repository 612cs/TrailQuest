<script setup lang="ts">
import { ChevronRight, FileCheck, ShieldAlert, MessageSquare, AlertCircle } from 'lucide-vue-next'
import type { AdminDashboardTodoItem } from '../../types/admin'

defineProps<{
  items: AdminDashboardTodoItem[]
  loading: boolean
}>()

const iconMap: Record<string, any> = {
  'pending-trails': FileCheck,
  'pending-reports': ShieldAlert,
  'hidden-reviews': MessageSquare,
  'offline-trails': AlertCircle
}
</script>

<template>
  <div class="dashboard-todo">
    <div class="section-header">
      <h2 class="admin-title">今日待办</h2>
      <span class="badge badge--success">23 待处理</span>
    </div>

    <div class="todo-list">
      <RouterLink 
        v-for="item in items" 
        :key="item.key" 
        class="todo-item"
        :to="item.to"
      >
        <div class="todo-item__icon" :class="`todo-item__icon--${item.key}`">
          <component :is="iconMap[item.key] || FileCheck" :size="20" />
        </div>
        <div class="todo-item__content">
          <p class="todo-item__title">{{ item.title }}</p>
          <p class="todo-item__desc">{{ item.description }}</p>
        </div>
        <div class="todo-item__right">
          <span class="todo-item__count">{{ loading ? '...' : item.count }}</span>
          <ChevronRight :size="18" class="todo-item__chevron" />
        </div>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.dashboard-todo {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.badge {
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge--success {
  background: rgba(47, 106, 58, 0.12);
  color: #2f6a3a;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: var(--bg-surface);
  border-radius: 16px;
  border: 1px solid var(--border);
  transition: all 0.2s ease;
}

.todo-item:hover {
  transform: translateX(4px);
  border-color: var(--primary-soft);
  background: var(--bg-soft);
}

.todo-item__icon {
  width: 2.75rem;
  height: 2.75rem;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: var(--bg-soft);
  color: var(--primary);
  flex-shrink: 0;
}

.todo-item__icon--pending-trails { color: #2f6a3a; background: rgba(47, 106, 58, 0.08); }
.todo-item__icon--pending-reports { color: #b54444; background: rgba(181, 68, 68, 0.08); }
.todo-item__icon--hidden-reviews { color: #6b7668; background: rgba(107, 118, 104, 0.08); }
.todo-item__icon--offline-trails { color: #b57a21; background: rgba(181, 122, 33, 0.08); }

.todo-item__content {
  flex: 1;
  min-width: 0;
}

.todo-item__title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-strong);
  margin: 0;
}

.todo-item__desc {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin: 0.25rem 0 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-item__right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.todo-item__count {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-strong);
}

.todo-item__chevron {
  color: color-mix(in srgb, var(--text-muted) 40%, transparent);
}
</style>
