<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  title: string
  message: string
}>()
</script>

<template>
  <div class="flex items-start gap-3 p-4 rounded-xl border animate-fade-in-up stagger-2" style="background-color: var(--color-warning-bg); border-color: rgba(245, 158, 11, 0.3);">
    <BaseIcon name="AlertTriangle" :size="20" class="text-yellow-600 shrink-0 mt-0.5" />
    <div>
      <p class="text-sm font-semibold text-yellow-800 dark:text-yellow-200">{{ title }}</p>
      <p class="text-xs text-yellow-700 dark:text-yellow-300 mt-0.5">{{ message }}</p>
    </div>
  </div>
</template>
