<script setup lang="ts">
import SharedPagination from '@trailquest/ui/components/Pagination.vue'

defineProps<{
  current: number
  total: number
  maxVisible?: number
}>()

const emit = defineEmits<{
  'update:current': [page: number]
}>()
</script>

<template>
  <SharedPagination
    :current="current"
    :total="total"
    :max-visible="maxVisible"
    @update:current="emit('update:current', $event)"
  />
</template>
