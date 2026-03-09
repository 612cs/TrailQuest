<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  filters: string[]
}>()
</script>

<template>
  <div class="flex items-center gap-2 overflow-x-auto scrollbar-hide pb-1">
    <!-- Filter Buttons (default slot or built-in) -->
    <button
      v-for="filter in filters"
      :key="filter"
      class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium shrink-0 transition-colors hover:bg-primary-500/5"
      style="background-color: var(--bg-tag); color: var(--text-secondary);"
    >
      {{ filter }}
      <BaseIcon name="ChevronDown" :size="14" />
    </button>

    <!-- Extra slot (for "更多筛选" or other custom buttons) -->
    <template v-if="$slots.extra">
      <div class="w-px h-5 mx-1 shrink-0" style="background-color: var(--border-default);"></div>
      <slot name="extra" />
    </template>
  </div>
</template>
