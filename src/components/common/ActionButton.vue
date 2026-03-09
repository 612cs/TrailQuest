<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary'
}>()
</script>

<template>
  <button
    class="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all active:scale-95"
    :class="{
      'bg-primary-500 text-white hover:bg-primary-600 shadow-sm hover:shadow-md': variant === 'primary' || !variant,
      'border hover:border-primary-500/30': variant === 'secondary',
    }"
    :style="variant === 'secondary' ? 'background-color: var(--bg-tag); border-color: var(--border-default); color: var(--text-secondary);' : ''"
  >
    <slot />
  </button>
</template>
