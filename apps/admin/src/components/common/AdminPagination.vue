<script setup lang="ts">
import { computed } from 'vue'

import SharedPagination from '@trailquest/ui/components/Pagination.vue'

const props = withDefaults(defineProps<{
  current: number
  totalPages: number
  totalItems: number
  itemLabel?: string
}>(), {
  itemLabel: '条',
})

const emit = defineEmits<{
  (e: 'update:current', page: number): void
}>()

const summary = computed(() => `第 ${props.current} 页 / 共 ${props.totalPages} 页，共 ${props.totalItems} ${props.itemLabel}`)
</script>

<template>
  <div class="pagination-container">
    <span class="pagination-summary">{{ summary }}</span>
    <SharedPagination
      :current="props.current"
      :total="props.totalPages"
      :max-visible="5"
      class="pagination-inner"
      @update:current="emit('update:current', $event)"
    />
  </div>
</template>

<style scoped>
.pagination-container {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.pagination-summary {
  color: var(--text-muted);
  font-size: 0.8125rem;
  font-weight: 600;
}

.pagination-container :deep(.shared-pagination) {
  --pagination-active-bg: var(--primary);
  --pagination-active-text: #fff;
}

@media (max-width: 720px) {
  .pagination-container {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
