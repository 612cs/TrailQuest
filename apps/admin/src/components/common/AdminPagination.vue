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
  <div class="admin-pagination">
    <span class="admin-muted">{{ summary }}</span>
    <SharedPagination
      :current="props.current"
      :total="props.totalPages"
      :max-visible="5"
      class="admin-pagination__inner"
      @update:current="emit('update:current', $event)"
    />
  </div>
</template>

<style scoped>
.admin-pagination {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.admin-pagination :deep(.shared-pagination) {
  --pagination-active-bg: var(--primary);
  --pagination-active-text: #fff;
}

@media (max-width: 720px) {
  .admin-pagination {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
