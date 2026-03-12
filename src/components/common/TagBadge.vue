<script setup lang="ts">
import BaseIcon from './BaseIcon.vue'

const props = withDefaults(
  defineProps<{
    label: string
    icon?: string
  }>(),
  {
    icon: undefined,
  },
)
</script>

<template>
  <span
    class="inline-flex items-center gap-1 rounded-lg px-2.5 py-1 text-xs font-medium backdrop-blur-md"
    style="background-color: rgba(45, 89, 39, 0.7); color: white;"
  >
    <BaseIcon v-if="props.icon" :name="props.icon" :size="10" />
    {{ props.label }}
  </span>
</template>
