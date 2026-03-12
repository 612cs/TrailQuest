<script setup lang="ts">
import { computed, type Component } from 'vue'
import * as icons from 'lucide-vue-next'

const props = defineProps<{
  name: string
  size?: number | string
  color?: string
  strokeWidth?: number | string
}>()

const iconComponent = computed(() => {
  // @ts-ignore
  return icons[props.name] || icons.HelpCircle
})
</script>

<template>
  <component
    :is="iconComponent as Component"
    :size="size || 20"
    :color="color || 'currentColor'"
    :stroke-width="strokeWidth || 2"
    class="lucide-icon"
  />
</template>

<style scoped>
.lucide-icon {
  display: inline-block;
  vertical-align: middle;
}
</style>
