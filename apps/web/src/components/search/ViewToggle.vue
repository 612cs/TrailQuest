<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  modelValue: 'list' | 'map'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: 'list' | 'map']
}>()

const options = [
  { value: 'list' as const, label: '列表', icon: 'List' },
  { value: 'map' as const, label: '地图', icon: 'Map' },
]
</script>

<template>
  <div class="flex items-center rounded-lg p-0.5" style="background-color: var(--bg-tag);">
    <button
      v-for="opt in options"
      :key="opt.value"
      @click="emit('update:modelValue', opt.value)"
      class="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all duration-200"
      :class="modelValue === opt.value ? 'shadow-sm' : ''"
      :style="modelValue === opt.value
        ? 'background-color: var(--bg-card); color: var(--text-primary);'
        : 'color: var(--text-tertiary);'"
    >
      <BaseIcon :name="opt.icon" :size="14" />
      {{ opt.label }}
    </button>
  </div>
</template>
