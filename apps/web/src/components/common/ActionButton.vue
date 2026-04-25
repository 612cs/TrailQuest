<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary'
}>()
</script>

<template>
  <button
    class="flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-all active:scale-95"
    :class="{
      'bg-primary-500 hover:bg-primary-600 text-white shadow-sm hover:shadow-md':
        variant === 'primary' || !variant,
      'hover:border-primary-500/30 border': variant === 'secondary',
    }"
    :style="
      variant === 'secondary'
        ? 'background-color: var(--bg-tag); border-color: var(--border-default); color: var(--text-secondary);'
        : ''
    "
  >
    <slot />
  </button>
</template>
