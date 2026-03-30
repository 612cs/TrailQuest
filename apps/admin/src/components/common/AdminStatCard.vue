<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  title: string
  value: string | number
  description?: string
  icon?: Component
}>()
</script>

<template>
  <article class="admin-card admin-stat-card">
    <div v-if="icon" class="admin-stat-card__icon">
      <component :is="icon" :size="18" :stroke-width="2" />
    </div>
    <div class="admin-stat-card__body">
      <p class="admin-stat-card__title">{{ title }}</p>
      <p class="admin-stat-card__value">{{ value }}</p>
      <p v-if="description" class="admin-stat-card__desc">{{ description }}</p>
    </div>
  </article>
</template>

<style scoped>
.admin-stat-card {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  padding: 1.2rem 1.25rem;
}

.admin-stat-card__icon {
  display: grid;
  place-items: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 16px;
  color: var(--primary);
  background: var(--primary-soft);
  flex: none;
}

.admin-stat-card__body {
  min-width: 0;
}

.admin-stat-card__title {
  margin: 0 0 0.4rem;
  color: var(--text-muted);
  font-size: 0.92rem;
}

.admin-stat-card__value {
  margin: 0;
  color: var(--text-strong);
  font-size: 1.9rem;
  font-weight: 800;
  line-height: 1.1;
}

.admin-stat-card__desc {
  margin: 0.45rem 0 0;
  color: var(--text-muted);
  font-size: 0.92rem;
}
</style>
