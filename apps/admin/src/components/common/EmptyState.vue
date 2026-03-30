<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  title: string
  description: string
  icon?: Component
}>()
</script>

<template>
  <div class="admin-empty">
    <div>
      <div v-if="icon" class="admin-empty__icon">
        <component :is="icon" :size="24" :stroke-width="2" />
      </div>
      <h3 class="admin-empty__title">{{ title }}</h3>
      <p class="admin-empty__desc">{{ description }}</p>
      <div v-if="$slots.default" class="admin-empty__actions">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-empty {
  min-height: 320px;
}

.admin-empty__icon {
  display: grid;
  place-items: center;
  width: 3.5rem;
  height: 3.5rem;
  margin: 0 auto 1rem;
  border-radius: 18px;
  color: var(--primary);
  background: var(--primary-soft);
}

.admin-empty__title {
  margin: 0 0 0.5rem;
  color: var(--text-strong);
  font-size: 1.15rem;
}

.admin-empty__desc {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.7;
}

.admin-empty__actions {
  margin-top: 1.2rem;
}
</style>
