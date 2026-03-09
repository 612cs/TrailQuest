<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <div class="flex items-end justify-between mb-6 sm:mb-8">
    <div>
      <h2 class="text-xl sm:text-2xl font-bold" style="color: var(--text-primary);">{{ title }}</h2>
      <p v-if="subtitle" class="text-sm mt-1" style="color: var(--text-secondary);">{{ subtitle }}</p>
    </div>
    <slot name="action" />
  </div>
</template>
