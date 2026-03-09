<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from './BaseIcon.vue'

const props = defineProps<{
  current: number
  total: number
  maxVisible?: number
}>()

const emit = defineEmits<{
  'update:current': [page: number]
}>()

const visibleCount = computed(() => props.maxVisible || 3)

const visiblePages = computed(() => {
  const pages: number[] = []
  const half = Math.floor(visibleCount.value / 2)
  let start = Math.max(1, props.current - half)
  const end = Math.min(props.total, start + visibleCount.value - 1)
  start = Math.max(1, end - visibleCount.value + 1)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const showEndEllipsis = computed(() => {
  const lastVisible = visiblePages.value[visiblePages.value.length - 1]
  return lastVisible < props.total - 1
})

const showLastPage = computed(() => {
  const lastVisible = visiblePages.value[visiblePages.value.length - 1]
  return lastVisible < props.total
})

function setPage(page: number) {
  if (page >= 1 && page <= props.total) {
    emit('update:current', page)
  }
}
</script>

<template>
  <div class="flex items-center justify-center gap-1.5">
    <!-- Prev -->
    <button
      @click="setPage(current - 1)"
      :disabled="current === 1"
      class="w-8 h-8 rounded flex items-center justify-center border transition-colors disabled:opacity-40"
      style="background-color: var(--bg-card); border-color: var(--border-default);"
    >
      <BaseIcon name="ChevronLeft" :size="16" style="color: var(--text-secondary)" />
    </button>

    <!-- Page Numbers -->
    <button
      v-for="page in visiblePages"
      :key="page"
      @click="setPage(page)"
      class="w-8 h-8 rounded text-sm font-medium transition-all duration-200"
      :class="current === page ? 'bg-primary-500 text-white shadow-sm' : 'border'"
      :style="current !== page ? 'background-color: var(--bg-card); border-color: var(--border-default); color: var(--text-secondary);' : ''"
    >
      {{ page }}
    </button>

    <!-- Ellipsis -->
    <span v-if="showEndEllipsis" class="px-1 text-sm" style="color: var(--text-tertiary);">...</span>

    <!-- Last Page -->
    <button
      v-if="showLastPage"
      @click="setPage(total)"
      class="w-8 h-8 rounded text-sm font-medium border transition-colors"
      :class="current === total ? 'bg-primary-500 text-white' : ''"
      :style="current !== total ? 'background-color: var(--bg-card); border-color: var(--border-default); color: var(--text-secondary);' : ''"
    >
      {{ total }}
    </button>

    <!-- Next -->
    <button
      @click="setPage(current + 1)"
      :disabled="current === total"
      class="w-8 h-8 rounded flex items-center justify-center border transition-colors disabled:opacity-40"
      style="background-color: var(--bg-card); border-color: var(--border-default);"
    >
      <BaseIcon name="ChevronRight" :size="16" style="color: var(--text-secondary)" />
    </button>
  </div>
</template>
