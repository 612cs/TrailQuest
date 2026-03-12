<script setup lang="ts">
import BaseIcon from '../common/BaseIcon.vue'

defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <div class="card flex items-center gap-3 px-4 py-3">
    <BaseIcon name="Search" class="shrink-0" style="color: var(--text-tertiary)" :size="20" />
    <input
      :value="modelValue"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      type="text"
      :placeholder="placeholder || '搜索...'"
      class="w-full bg-transparent text-sm focus:outline-none"
      style="color: var(--text-primary);"
    />
    <button
      v-show="modelValue"
      @click="emit('update:modelValue', '')"
      class="p-1.5 rounded-md hover:bg-primary-500/10 transition-colors"
      style="color: var(--text-tertiary)"
    >
      <BaseIcon name="X" :size="16" />
    </button>
  </div>
</template>
