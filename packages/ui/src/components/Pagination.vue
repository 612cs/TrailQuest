<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  current: number
  total: number
  maxVisible?: number
}>(), {
  maxVisible: 5,
})

const emit = defineEmits<{
  (e: 'update:current', page: number): void
}>()

const totalPages = computed(() => Math.max(1, props.total || 1))
const visibleCount = computed(() => Math.max(3, props.maxVisible))

const visiblePages = computed(() => {
  const pages: number[] = []
  const half = Math.floor(visibleCount.value / 2)
  let start = Math.max(1, props.current - half)
  let end = Math.min(totalPages.value, start + visibleCount.value - 1)
  start = Math.max(1, end - visibleCount.value + 1)

  for (let page = start; page <= end; page += 1) {
    pages.push(page)
  }

  return pages
})

const lastVisiblePage = computed<number>(() => {
  const lastPage = visiblePages.value[visiblePages.value.length - 1]
  return typeof lastPage === 'number' ? lastPage : totalPages.value
})

const showFirstPage = computed(() => (visiblePages.value[0] ?? 1) > 1)
const showLeadingEllipsis = computed(() => (visiblePages.value[0] ?? 1) > 2)
const showLastPage = computed(() => lastVisiblePage.value < totalPages.value)
const showTrailingEllipsis = computed(() => lastVisiblePage.value < totalPages.value - 1)

function setPage(page: number) {
  if (page < 1 || page > totalPages.value || page === props.current) {
    return
  }
  emit('update:current', page)
}
</script>

<template>
  <nav class="shared-pagination" aria-label="分页导航">
    <button class="shared-pagination__nav" type="button" :disabled="props.current <= 1" @click="setPage(props.current - 1)">
      <ChevronLeft :size="16" :stroke-width="2" />
    </button>

    <button v-if="showFirstPage" class="shared-pagination__page" type="button" @click="setPage(1)">1</button>
    <span v-if="showLeadingEllipsis" class="shared-pagination__ellipsis">...</span>

    <button
      v-for="page in visiblePages"
      :key="page"
      class="shared-pagination__page"
      :class="{ 'shared-pagination__page--active': page === props.current }"
      type="button"
      @click="setPage(page)"
    >
      {{ page }}
    </button>

    <span v-if="showTrailingEllipsis" class="shared-pagination__ellipsis">...</span>
    <button v-if="showLastPage" class="shared-pagination__page" type="button" @click="setPage(totalPages)">
      {{ totalPages }}
    </button>

    <button class="shared-pagination__nav" type="button" :disabled="props.current >= totalPages" @click="setPage(props.current + 1)">
      <ChevronRight :size="16" :stroke-width="2" />
    </button>
  </nav>
</template>

<style scoped>
.shared-pagination {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.shared-pagination__nav,
.shared-pagination__page {
  min-width: 2.25rem;
  height: 2.25rem;
  padding: 0 0.7rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  border: 1px solid var(--pagination-border, var(--border-default, var(--border, rgba(0, 0, 0, 0.08))));
  background: var(--pagination-bg, var(--bg-card, var(--bg-surface, #fff)));
  color: var(--pagination-text, var(--text-secondary, var(--text-muted, #667085)));
  font-size: 0.92rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.18s ease, background-color 0.18s ease, color 0.18s ease, border-color 0.18s ease;
}

.shared-pagination__nav:hover,
.shared-pagination__page:hover {
  transform: translateY(-1px);
}

.shared-pagination__page--active {
  border-color: var(--pagination-active-bg, var(--color-primary-500, var(--primary, #2d5927)));
  background: var(--pagination-active-bg, var(--color-primary-500, var(--primary, #2d5927)));
  color: var(--pagination-active-text, #fff);
}

.shared-pagination__nav:disabled,
.shared-pagination__page:disabled {
  opacity: 0.42;
  cursor: not-allowed;
  transform: none;
}

.shared-pagination__ellipsis {
  color: var(--pagination-muted, var(--text-tertiary, var(--text-muted, #98a2b3)));
  font-size: 0.9rem;
}
</style>
