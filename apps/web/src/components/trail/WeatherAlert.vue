<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  title: string
  message: string
}>()
</script>

<template>
  <div
    class="animate-fade-in-up stagger-2 flex items-start gap-3 rounded-xl border p-4"
    style="background-color: var(--color-warning-bg); border-color: rgba(245, 158, 11, 0.3)"
  >
    <BaseIcon name="AlertTriangle" :size="20" class="mt-0.5 shrink-0 text-yellow-600" />
    <div>
      <p class="text-sm font-semibold text-yellow-800 dark:text-yellow-200">{{ title }}</p>
      <p class="mt-0.5 text-xs text-yellow-700 dark:text-yellow-300">{{ message }}</p>
    </div>
  </div>
</template>
