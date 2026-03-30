<script setup lang="ts">
import SharedImagePreviewModal from '@trailquest/ui/components/ImagePreviewModal.vue'

defineProps<{
  images: string[]
  initialIndex?: number
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <SharedImagePreviewModal
    :images="images"
    :initial-index="initialIndex"
    @close="emit('close')"
  />
</template>
